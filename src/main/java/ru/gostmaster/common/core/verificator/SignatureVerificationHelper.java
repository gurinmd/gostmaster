package ru.gostmaster.common.core.verificator;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import ru.gostmaster.common.core.exception.SignatureVerificationException;
import ru.gostmaster.common.data.verification.SignatureInformationResult;
import ru.gostmaster.common.messages.Messages;
import ru.gostmaster.util.GetterUtils;

import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
public class SignatureVerificationHelper {

    public static boolean isSignerInfoValid(SignerInformation signerInformation, CMSSignedData cmsSignedData) throws CertificateException,
        CMSException,
        OperatorCreationException {
        Collection<X509CertificateHolder> certs = cmsSignedData.getCertificates().getMatches(signerInformation.getSID());
        boolean res = true;
        for (X509CertificateHolder certificateHolder : certs) {
            res &= isSignerInfoValid(signerInformation, certificateHolder);
        }
        return res;
    }

    public static boolean isSignerInfoValid(SignerInformation signerInformation, X509CertificateHolder holder) throws CMSException,
        CertificateException, OperatorCreationException {
        return signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().build(holder));
    }

    public static SignatureInformationResult validateSignatureCertificate(Set<TrustAnchor> trusted,
                                                                          List<X509CertificateHolder> intermCerts,
                                                                          List<X509CRLHolder> crls,
                                                                          CMSSignedData cmsSignedData,
                                                                          SignerInformation signerInformation,
                                                                          SignatureInformationResult signatureInformationResult) {
        Collection<X509CertificateHolder> certificateHolders = cmsSignedData.getCertificates()
            .getMatches(signerInformation.getSID());

        if (certificateHolders.size() != 1) {
            // у подписанта несколько сертификатов. херь какая то.
            signatureInformationResult.setCertificateCheckResultSuccess(false);
            signatureInformationResult.setCertificateCheckResultDescription(Messages.getMessage(Messages.CORRUPTED_DATA));
        } else {
            try {
                validChainWithCrl(trusted, intermCerts, crls, certificateHolders.stream().findAny().get());
                signatureInformationResult.setCertificateCheckResultSuccess(true);
                signatureInformationResult.setCertificateCheckResultDescription(Messages.getMessage(Messages.CERTIFICATE_VALID));
            } catch (SignatureVerificationException sve) {
                String text = getReadableErrorMessage(sve);
                signatureInformationResult.setCertificateCheckResultSuccess(false);
                signatureInformationResult.setCertificateCheckResultDescription(text);
            }
        }
        return signatureInformationResult;
    }

    public static PKIXCertPathBuilderResult validChainWithCrl(Set<TrustAnchor> trusted,
                                                              List<X509CertificateHolder> intermCerts,
                                                              List<X509CRLHolder> crls,
                                                              X509CertificateHolder signatureCertificate) {
        try {

            CertPathBuilder pathValidator = CertPathBuilder.getInstance("PKIX", BouncyCastleProvider.PROVIDER_NAME);
            X509CertSelector targetConstraints = new X509CertSelector();
            targetConstraints.setSubject(signatureCertificate.getSubject().getEncoded());

            // создадим cert store
            JcaCertStoreBuilder builder = new JcaCertStoreBuilder();
            builder.setProvider(BouncyCastleProvider.PROVIDER_NAME);

            //  добавим туда все промежуточные
            intermCerts.forEach(holder -> builder.addCertificate(holder));

            // добавим сертификат подписи
            builder.addCertificate(signatureCertificate);

            // добавим crl
            crls.forEach(x509CRLHolder -> builder.addCRL(x509CRLHolder));

            CertStore certStore = builder.build();

            // параметры валидатора
            PKIXBuilderParameters params = new PKIXBuilderParameters(trusted, targetConstraints);
            params.addCertStore(certStore);
            params.setRevocationEnabled(true);

            return (PKIXCertPathBuilderResult) pathValidator.build(params);

        } catch (Exception ex) {
            throw new SignatureVerificationException(ex);
        }
    }

    public static CMSSignedData createCMSSignedData(byte[] data, byte[] sig) throws CMSException {
        return new CMSSignedData(new CMSProcessableByteArray(data), sig);
    }

    public static String getReadableErrorMessage(SignatureVerificationException ex) {
        String res;
        Throwable cause = GetterUtils.get(() -> ex.getCause().getCause().getCause(), new CertificateException());
        if (cause instanceof CertificateExpiredException) {
            res = Messages.getMessage(Messages.CERTIFICATE_EXPIRED);
        } else if (cause instanceof CertificateNotYetValidException) {
            res = Messages.getMessage(Messages.CERTIFICATE_NOT_YET_VALID);
        } else if (cause instanceof CertificateRevokedException) {
            res = Messages.getMessage(Messages.CERTIFICATE_REVOKED);
        } else {
            res = ex.getMessage();
        }
        return res;
    }
}
