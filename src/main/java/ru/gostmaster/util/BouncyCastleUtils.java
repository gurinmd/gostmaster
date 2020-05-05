package ru.gostmaster.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;
import ru.gostmaster.common.data.crl.Crl;
import ru.gostmaster.common.data.verification.CertificateSubject;
import ru.gostmaster.common.data.verification.SignatureCertificateInfo;
import ru.gostmaster.common.data.verification.SignatureInformationResult;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class BouncyCastleUtils {
    
    public static Date getSignedDate(SignerInformation signedData) {
        Date res = null;
        Attribute attribute = signedData.getSignedAttributes().get(CMSAttributes.signingTime);
        if (attribute != null) {
            res = Time.getInstance(attribute.getAttributeValues()[0]).getDate();
        }
        return res;
    }
    
    public static String getAttributeValue(X500Name name, String attributeOid) {
        try {
            ASN1ObjectIdentifier objectIdentifier = new ASN1ObjectIdentifier(attributeOid);
            return getAttributeValue(name, objectIdentifier);
        } catch (Exception ex) {
            log.error("", ex.getMessage());
            return null;
        }
    }
    
    public static String getAttributeValue(X500Name name, ASN1ObjectIdentifier objectIdentifier) {
        try {
            RDN[] rdNs = name.getRDNs(objectIdentifier);
            String res = Arrays.stream(rdNs).map(rdn -> rdn.getFirst().getValue().toString())
                .collect(Collectors.joining(", "));
            return res;
        } catch (Exception ex) {
            log.error("", ex.getMessage());
            return null;
        }
    }
    
    public static List<String> getCrlUrlsFromCertificate(X509Certificate certificate) {
        List<String> crlUrls = new ArrayList<>();

        try {
            byte[] crlDistributionPointDerEncodedArray = certificate.getExtensionValue(Extension.cRLDistributionPoints.getId());
            ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crlDistributionPointDerEncodedArray));
            ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
            DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;

            oAsnInStream.close();

            byte[] crldpExtOctets = dosCrlDP.getOctets();
            ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets));
            ASN1Primitive derObj2 = oAsnInStream2.readObject();
            CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);

            oAsnInStream2.close();

            for (DistributionPoint dp : distPoint.getDistributionPoints()) {
                DistributionPointName dpn = dp.getDistributionPoint();
                // Look for URIs in fullName
                if (dpn != null) {
                    if (dpn.getType() == DistributionPointName.FULL_NAME) {
                        GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
                        // Look for an URI
                        for (int j = 0; j < genNames.length; j++) {
                            if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
                                String url = DERIA5String.getInstance(genNames[j].getName()).getString();
                                crlUrls.add(url);
                            }
                        }
                    }
                }
            }


        } catch (Exception ex) {
            log.error("", ex.getMessage());
        }
        return crlUrls;
    }

    public static SignatureInformationResult fromSignatureInformationAndSignedData(SignerInformation signerInformation,
                                                                                   CMSSignedData cmsSignedData) {
        return fromSignatureInformationAndCertificates(signerInformation,
            cmsSignedData.getCertificates().getMatches(signerInformation.getSID()));
    }

    public static SignatureInformationResult fromSignatureInformationAndCertificates(SignerInformation signerInformation,
                                                                                     Collection<X509CertificateHolder> certificateHolders) {
        SignatureInformationResult information = new SignatureInformationResult();
        information.setSignedAt(BouncyCastleUtils.getSignedDate(signerInformation));
        List<SignatureCertificateInfo> certInfos = certificateHolders.stream()
            .map(BouncyCastleUtils::fromSignatureCertificateHolder)
            .collect(Collectors.toList());
        information.setSignatureCertificateInfo(certInfos);
        return information;
    }

    public static SignatureCertificateInfo fromSignatureCertificateHolder(X509CertificateHolder certificateHolder) {
        SignatureCertificateInfo res = new SignatureCertificateInfo();
        res.setIssuer(buildSubject(certificateHolder.getIssuer()));
        res.setSubject(buildSubject(certificateHolder.getSubject()));
        res.setValidFrom(certificateHolder.getNotBefore());
        res.setValidTo(certificateHolder.getNotAfter());
        return res;
    }

    private static CertificateSubject buildSubject(X500Name name) {
        CertificateSubject subject = new CertificateSubject();
        subject.setOrganization(BouncyCastleUtils.getAttributeValue(name, BCStyle.O));
        subject.setOrganizationUnit(BouncyCastleUtils.getAttributeValue(name, BCStyle.OU));
        subject.setName(BouncyCastleUtils.getAttributeValue(name, BCStyle.CN));
        subject.setInn(BouncyCastleUtils.getAttributeValue(name, "1.2.643.3.131.1.1"));
        subject.setOgrn(BouncyCastleUtils.getAttributeValue(name, "1.2.643.100.1"));
        String location = Stream.of(
            BouncyCastleUtils.getAttributeValue(name, BCStyle.C),
            BouncyCastleUtils.getAttributeValue(name, BCStyle.L),
            BouncyCastleUtils.getAttributeValue(name, BCStyle.ST),
            BouncyCastleUtils.getAttributeValue(name, BCStyle.STREET)
        ).filter(StringUtils::hasText).collect(Collectors.joining(", "));
        subject.setLocation(location);
        subject.setEmail(BouncyCastleUtils.getAttributeValue(name, BCStyle.E));
        return subject;
    }

    public static List<Pair<CMSSignedData, SignerInformation>> createSignerInfoList(CMSSignedData signedData) {
        return signedData.getSignerInfos().getSigners().stream()
            .map(signerInformation -> Pair.of(signedData, signerInformation))
            .collect(Collectors.toList());
    }
}
