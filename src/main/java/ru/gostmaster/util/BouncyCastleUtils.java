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
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;
import ru.gostmaster.common.data.verification.CertificateSubject;
import ru.gostmaster.common.data.verification.SignatureCertificateInfo;
import ru.gostmaster.common.data.verification.SignatureInformationResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс для работы с сущностями BouncyCastle. 
 * 
 * @author maksimgurin
 */
@Slf4j
public final class BouncyCastleUtils {

    private BouncyCastleUtils() { }
    
    /**
     * Получить дату подписания документа.
     * @param signedData информация о подписанте
     * @return дата подписания
     */
    public static Date getSignedDate(SignerInformation signedData) {
        Date res = null;
        Attribute attribute = signedData.getSignedAttributes().get(CMSAttributes.signingTime);
        if (attribute != null) {
            res = Time.getInstance(attribute.getAttributeValues()[0]).getDate();
        }
        return res;
    }

    /**
     * Получить текстовое значение атрибута по OID из имени.
     * @param name имя в формате X500Name
     * @param attributeOid OID получаемого атрибута
     * @return значение полученного атрибута
     */
    public static String getAttributeValue(X500Name name, String attributeOid) {
        try {
            ASN1ObjectIdentifier objectIdentifier = new ASN1ObjectIdentifier(attributeOid);
            return getAttributeValue(name, objectIdentifier);
        } catch (Exception ex) {
            log.error("", ex.getMessage());
            return null;
        }
    }

    /**
     * Получить текстовое значение атрибута по OID из имени.
     * @param name имя в формате X500Name
     * @param objectIdentifier OID получаемого атрибута
     * @return значение полученного атрибута
     */
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

    /**
     * Получить идентификатор субъекта данного сертификата.
     * @param certificate сертификат
     * @return идентификатор в виде строки
     * @throws NoSuchAlgorithmException если алгоритм сертификата не поддерживается
     * @throws IOException в случае ошибки доступа к данным сертификата
     */
    public static String getSubjectKeyIdentifier(X509Certificate certificate) throws NoSuchAlgorithmException, IOException {
        SubjectKeyIdentifier subjectKeyIdentifier = SubjectKeyIdentifier
            .getInstance(new JcaX509ExtensionUtils().parseExtensionValue(certificate
                .getExtensionValue(Extension.subjectKeyIdentifier.getId())));
        byte[] keyBytes = subjectKeyIdentifier.getKeyIdentifier();
        return Hex.toHexString(keyBytes);
    }

    /**
     * Получить идентификатор издателя данного сертификата.
     * @param certificate сертификат
     * @return идентификатор в виде строки
     * @throws NoSuchAlgorithmException если алгоритм сертификата не поддерживается
     * @throws IOException в случае ошибки доступа к данным сертификата
     */
    public static String getAuthorityKeyIdentifier(X509Certificate certificate) throws NoSuchAlgorithmException, IOException {
        byte[] extensionValue = certificate
            .getExtensionValue(Extension.authorityKeyIdentifier.getId());
        String res = null;
        if (extensionValue != null) {
            AuthorityKeyIdentifier authorityKeyIdentifier = AuthorityKeyIdentifier
                .getInstance(new JcaX509ExtensionUtils().parseExtensionValue(extensionValue));
            byte[] keyBytes = authorityKeyIdentifier.getKeyIdentifier();
            res = Hex.toHexString(keyBytes);
        }
        return res;
    }

    /**
     * Получить идентификатор издателя данного списка отозванных сертификатов (CRL).
     * @param crl СRL
     * @return идентификатор в виде строки
     * @throws NoSuchAlgorithmException если алгоритм CRL не поддерживается
     * @throws IOException в случае ошибки доступа к данным CRL
     */
    public static String getAuthorityKeyIdentifier(X509CRL crl) throws NoSuchAlgorithmException, IOException {
        AuthorityKeyIdentifier authorityKeyIdentifier = AuthorityKeyIdentifier
            .getInstance(new JcaX509ExtensionUtils().parseExtensionValue(crl
                .getExtensionValue(Extension.authorityKeyIdentifier.getId())));
        byte[] keyBytes = authorityKeyIdentifier.getKeyIdentifier();
        return Hex.toHexString(keyBytes);
    }

    /**
     * Получить список ссылок, по которым доступны CRL для данного сертификата.
     * @param certificate сертификат
     * @return список со ссылками на CRL
     */
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
            log.error("", ex);
        }
        return crlUrls;
    }

    /**
     * Получить список ссылок, по которым доступны CRL для данного сертификата.
     * @param certificateHolder сертификат
     * @return список со ссылками на CRL
     */
    public static List<String> getCrlUrlsFromCertificate(X509CertificateHolder certificateHolder) {
        List<String> crlUrls = new ArrayList<>();

        try {
            byte[] crlDistributionPointDerEncodedArray = certificateHolder.getExtensions()
                .getExtension(Extension.cRLDistributionPoints).getEncoded();
            
            ASN1InputStream oAsnInStream = 
                new ASN1InputStream(new ByteArrayInputStream(crlDistributionPointDerEncodedArray));
            
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

    /**
     * Построить объект SignatureInformationResult из подписанных данных и данных о подписанте.
     * @param signerInformation данные о подписанте
     * @param cmsSignedData подписанные данные
     * @return SignatureInformationResult
     */
    public static SignatureInformationResult fromSignatureInformationAndSignedData(SignerInformation signerInformation,
                                                                                   CMSSignedData cmsSignedData) {
        return fromSignatureInformationAndCertificates(signerInformation,
            cmsSignedData.getCertificates().getMatches(signerInformation.getSID()));
    }

    /**
     * Построить объект SignatureInformationResult из сертификатов и данных о подписанте.
     * @param signerInformation данные о подписанте
     * @param certificateHolders подписанные данные
     * @return SignatureInformationResult
     */
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

    /**
     * Получить информацию о сертификате в виде SignatureCertificateInfo.
     * @param certificateHolder сертификат.
     * @return SignatureCertificateInfo
     */
    public static SignatureCertificateInfo fromSignatureCertificateHolder(X509CertificateHolder certificateHolder) {
        SignatureCertificateInfo res = new SignatureCertificateInfo();
        res.setIssuer(buildSubject(certificateHolder.getIssuer()));
        res.setSubject(buildSubject(certificateHolder.getSubject()));
        res.setValidFrom(certificateHolder.getNotBefore());
        res.setValidTo(certificateHolder.getNotAfter());
        return res;
    }

    /**
     * Сконструировать CertificateSubject на основании данных о владельце сертификата.
     * @param name данные о владельце.
     * @return CertificateSubject
     */
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

    /**
     * Получить пары типа подписанные данные-подписант.
     * @param signedData подписанные данные
     * @return список пар
     */
    public static List<Pair<CMSSignedData, SignerInformation>> createSignerInfoList(CMSSignedData signedData) {
        return signedData.getSignerInfos().getSigners().stream()
            .map(signerInformation -> Pair.of(signedData, signerInformation))
            .collect(Collectors.toList());
    }

    /**
     * Создание представления подписанного содержимого в терминах BouncyCastle.
     * @param data данные
     * @param sig подпись (DER)
     * @return CMSSignedData
     * @throws CMSException при ошибке
     */
    public static CMSSignedData createCMSSignedData(byte[] data, byte[] sig) throws CMSException {
        return new CMSSignedData(new CMSProcessableByteArray(data), sig);
    }
}
