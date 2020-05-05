package ru.gostmaster.common.core.verificator;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.gostmaster.common.data.cert.Certificate;
import ru.gostmaster.common.data.verification.SignatureInformationResult;
import ru.gostmaster.common.data.verification.VerificationResult;
import ru.gostmaster.common.messages.Messages;
import ru.gostmaster.common.spi.storage.CRLStorage;
import ru.gostmaster.common.spi.storage.CertificateStorage;
import ru.gostmaster.parser.CrlParser;
import ru.gostmaster.util.BouncyCastleUtils;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SignatureVerificator {

    private CRLStorage crlStorage;
    private CertificateStorage certificateStorage;
    private CrlParser crlParser;

    public Mono<VerificationResult> verify(byte[] data, byte[] signature) {
        Mono<Pair<VerificationResult, CMSSignedData>> initCheckMono = initVerification(data, signature);

        Flux<Pair<CMSSignedData, SignerInformation>> pairFlux = initCheckMono
            .flatMapIterable(verificationResultCMSSignedDataPair ->
                Optional.ofNullable(verificationResultCMSSignedDataPair.getSecond())
                    .map(o -> BouncyCastleUtils.createSignerInfoList(o))
                    .orElse(Collections.emptyList()));

        Flux<SignatureInformationResult> signatureInformationResultFlux = pairFlux
            .flatMap(cmsSignedDataSignerInformationPair ->
                createVerificationResult(cmsSignedDataSignerInformationPair.getFirst(),
                    cmsSignedDataSignerInformationPair.getSecond()));

        Mono<List<SignatureInformationResult>> listMono = signatureInformationResultFlux.collectList();
        Mono<VerificationResult> initialResultMono = initCheckMono.map(pair -> pair.getFirst());

        Mono<VerificationResult> finalResult = Mono.zip(initialResultMono, listMono).map(objects -> {
            VerificationResult result = objects.getT1();
            List<SignatureInformationResult> sigInfoResult = objects.getT2();
            return buildFinalResult(result, sigInfoResult);
        });
        return finalResult;
    }

    private Mono<Pair<VerificationResult, CMSSignedData>> initVerification(byte[] data, byte[] signature) {
        Mono<Pair<VerificationResult, CMSSignedData>> pairMono = Mono.fromCallable(() -> {
            VerificationResult result = new VerificationResult();
            result.setSignatureCheckResultSuccess(true);
            CMSSignedData signedData = null;
            try {
                //1. Проверка, что пришли нормальные данные
                signedData = SignatureVerificationHelper.createCMSSignedData(data, signature);
            } catch (Exception ex) {
                log.error("", ex);
                result.setSignatureCheckResultSuccess(false);
                result.setSignatureCheckResultDescription(Messages.getMessage(Messages.CORRUPTED_DATA));
            }
            return Pair.of(result, signedData);
        });
        return pairMono;
    }


    private Mono<SignatureInformationResult> createVerificationResult(CMSSignedData signedData,
                                                                      SignerInformation signerInformation) {
        SignatureInformationResult result = BouncyCastleUtils.fromSignatureInformationAndSignedData(signerInformation, signedData);
        
        try {
            boolean isSignatureValid = SignatureVerificationHelper.isSignerInfoValid(signerInformation, signedData);
            if (!isSignatureValid) {
                throw new RuntimeException();
            }
            result.setSignatureCheckResultSuccess(true);
            result.setSignatureCheckResultDescription(Messages.getMessage(Messages.VALID_SIGNED_CONTENT));
        } catch (Exception ex) {
            result.setSignatureCheckResultSuccess(false);
            result.setSignatureCheckResultDescription(Messages.getMessage(Messages.INVALID_SIGNED_CONTENT));
        }
        
        // если подпись не совпадает - проверять дальше смысла нет
        if (!result.isSignatureCheckResultSuccess()) {
            return Mono.just(result);
        }
        
        X509CertificateHolder certificateHolder = (X509CertificateHolder) signedData.getCertificates()
            .getMatches(signerInformation.getSID()).iterator().next();
        String issuer = certificateHolder.getIssuer().toString();
        Mono<List<Certificate>> certChain = getCertificateChain(issuer);

        Mono<List<X509CRLHolder>> crlsMono = getCrls(certChain);
        Mono<Set<TrustAnchor>> trustedAnchorsMono = certChain.flatMap(certificates -> createTrustAnchors(certificates));
        Mono<List<X509CertificateHolder>> intermCertificatesMono = certChain
            .flatMap(certificates -> createIntermediateCertificatesList(certificates));

        Mono<SignatureInformationResult> res = Mono.zip(crlsMono, trustedAnchorsMono, intermCertificatesMono).map(objects ->
            SignatureVerificationHelper.validateSignatureCertificate(objects.getT2(), objects.getT3(), objects.getT1(),
                signedData, signerInformation, result));

        return res;
    }

    private Mono<List<Certificate>> getCertificateChain(String subject) {
        return certificateStorage.getCertificateChainForLeafSubject(subject);
    }

    private Mono<List<X509CRLHolder>> getCrls(Mono<List<Certificate>> certificates) {
        Mono<List<String>> cas = certificates.map(certs -> certs.stream().map(Certificate::getSubject).collect(Collectors.toList()));

        Mono<List<String>> urls = certificates.map(certs -> certs.stream().flatMap(certificate ->
            Optional.ofNullable(certificate.getCrlUrls()).orElse(Collections.emptyList()).stream()).collect(Collectors.toList()));

        Mono<List<X509CRLHolder>> res = Mono.zip(cas, urls, (caList, urlList) -> crlStorage.getAllByDownloadedFromOrCa(urlList, caList).collectList())
            .flatMap(listMono -> listMono)
            .map(crls -> crlParser.toHolderList(crls));

        return res;
    }

    private Mono<Set<TrustAnchor>> createTrustAnchors(List<Certificate> certificateList) {
        Mono<Set<TrustAnchor>> res = Mono.fromCallable(() -> {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X509", BouncyCastleProvider.PROVIDER_NAME);
            Set<TrustAnchor> set = new HashSet<>();
            certificateList.forEach(certificate -> {
                if (certificate.isTrusted()) {
                    try {
                        X509Certificate cert = (X509Certificate) certificateFactory
                            .generateCertificate(new ByteArrayInputStream(certificate.getCertificateEncodedData().getBytes()));
                        set.add(new TrustAnchor(cert, null));
                    } catch (Exception ex) {
                        log.error("", ex.getMessage());
                    }
                }
            });
            return set;
        });
        return res;
    }

    private Mono<List<X509CertificateHolder>> createIntermediateCertificatesList(List<Certificate> certificateList) {
        Mono<List<X509CertificateHolder>> res = Mono.fromCallable(() -> {
            CertificateFactory factory = CertificateFactory.getInstance("X509", BouncyCastleProvider.PROVIDER_NAME);
            List<X509CertificateHolder> holders = new ArrayList<>();
            certificateList.forEach(certificate -> {
                if (!certificate.isTrusted()) {
                    try {
                        X509Certificate parsedCert = (X509Certificate) factory
                            .generateCertificate(new ByteArrayInputStream(certificate.getCertificateEncodedData().getBytes()));
                        X509CertificateHolder holder = new X509CertificateHolder(parsedCert.getEncoded());
                        holders.add(holder);
                    } catch (Exception ex) {
                        log.error("", ex);
                    }
                }
            });
            return holders;
        });
        return res;
    }
    
    private VerificationResult buildFinalResult(VerificationResult verificationResult, 
                                                List<SignatureInformationResult> results) {
        verificationResult.setResults(results);
        
        if (verificationResult.isSignatureCheckResultSuccess()) {
            verificationResult
                .setSignatureCheckResultSuccess(verificationResult.isSignatureCheckResultSuccess() &&
                    results.stream().allMatch(s -> s.isSignatureCheckResultSuccess()));
        
            verificationResult.setSignatureCheckResultDescription(results.stream()
                .map(SignatureInformationResult::getSignatureCheckResultDescription)
                .collect(Collectors.joining(", ")));
        }

        verificationResult
            .setCertificateCheckResultSuccess(results.stream().allMatch(s -> s.isCertificateCheckResultSuccess()));
        verificationResult.setCertificateCheckResultDescription(results.stream()
            .map(SignatureInformationResult::getCertificateCheckResultDescription).collect(Collectors.joining(", ")));
        
        verificationResult.setVerificationTime(new Date());
        verificationResult.setSignaturesCount(verificationResult.getResults().size());
        return verificationResult;
    }

    @Autowired
    public void setCrlStorage(CRLStorage crlStorage) {
        this.crlStorage = crlStorage;
    }

    @Autowired
    public void setCertificateStorage(CertificateStorage certificateStorage) {
        this.certificateStorage = certificateStorage;
    }

    @Autowired
    public void setCrlParser(CrlParser crlParser) {
        this.crlParser = crlParser;
    }
}
