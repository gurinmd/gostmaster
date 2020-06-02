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
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.util.StringUtils;
import ru.gostmaster.dictionary.AlgorithmsNames;
import ru.gostmaster.dictionary.CertificatePoliciesName;
import ru.gostmaster.dictionary.ExtendedKeyUsageNames;
import ru.gostmaster.dictionary.SubjectAttributesOID;
import ru.gostmaster.verification.data.AlgorithmDescription;
import ru.gostmaster.verification.data.CertificateKeyUsage;
import ru.gostmaster.verification.data.CertificatePolicy;
import ru.gostmaster.verification.data.CertificateSubject;
import ru.gostmaster.verification.data.SignatureCertificateInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
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

    private BouncyCastleUtils() {
    }

    /**
     * Получить дату подписания документа.
     *
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
     *
     * @param name         имя в формате X500Name
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
     *
     * @param name             имя в формате X500Name
     * @param objectIdentifier OID получаемого атрибута
     * @return значение полученного атрибута
     */
    public static String getAttributeValue(X500Name name, ASN1ObjectIdentifier objectIdentifier) {
        try {
            RDN[] rdNs = name.getRDNs(objectIdentifier);
            String res = Arrays.stream(rdNs).map(rdn -> rdn.getFirst().getValue().toString())
                .collect(Collectors.joining(", "));
            if (!StringUtils.hasText(res)) {
                res = null;
            }
            return res;
        } catch (Exception ex) {
            log.error("", ex.getMessage());
            return null;
        }
    }

    /**
     * Получить идентификатор субъекта данного сертификата.
     *
     * @param certificate сертификат
     * @return идентификатор в виде строки
     * @throws NoSuchAlgorithmException если алгоритм сертификата не поддерживается
     * @throws IOException              в случае ошибки доступа к данным сертификата
     */
    public static String getSubjectKeyIdentifier(X509Certificate certificate) throws NoSuchAlgorithmException, IOException {
        SubjectKeyIdentifier subjectKeyIdentifier = SubjectKeyIdentifier
            .getInstance(new JcaX509ExtensionUtils().parseExtensionValue(certificate
                .getExtensionValue(Extension.subjectKeyIdentifier.getId())));
        byte[] keyBytes = subjectKeyIdentifier.getKeyIdentifier();
        return Hex.toHexString(keyBytes);
    }

    /**
     * Получить идентификатор субъекта данного сертификата.
     *
     * @param certificateHolder сертификат
     * @return идентификатор в виде строки
     * @throws NoSuchAlgorithmException если алгоритм сертификата не поддерживается
     * @throws IOException              в случае ошибки доступа к данным сертификата
     */
    public static String getSubjectKeyIdentifier(X509CertificateHolder certificateHolder) {
        SubjectKeyIdentifier subjectKeyIdentifier = SubjectKeyIdentifier
            .fromExtensions(certificateHolder.getExtensions());
        byte[] keyBytes = subjectKeyIdentifier.getKeyIdentifier();
        return Hex.toHexString(keyBytes);
    }

    /**
     * Получить идентификатор субъекта данного сертификата в виде массива байтов.
     *
     * @param certificateHolder сертификат
     * @return идентификатор в виде массива
     */
    public static byte[] getSubjectKeyIdentifierAsBytes(X509CertificateHolder certificateHolder) {
        SubjectKeyIdentifier subjectKeyIdentifier = SubjectKeyIdentifier
            .fromExtensions(certificateHolder.getExtensions());
        byte[] keyBytes = subjectKeyIdentifier.getKeyIdentifier();
        return keyBytes;
    }

    /**
     * Получить идентификатор издателя данного сертификата.
     *
     * @param certificate сертификат
     * @return идентификатор в виде строки
     * @throws NoSuchAlgorithmException если алгоритм сертификата не поддерживается
     * @throws IOException              в случае ошибки доступа к данным сертификата
     */
    public static String getAuthorityKeyIdentifier(X509Certificate certificate) throws NoSuchAlgorithmException, 
        IOException {
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
     * Получить идентификатор издателя данного сертификата.
     *
     * @param certificateHolder сертификат
     * @return идентификатор в виде строки
     * @throws NoSuchAlgorithmException если алгоритм сертификата не поддерживается
     * @throws IOException              в случае ошибки доступа к данным сертификата
     */
    public static String getAuthorityKeyIdentifier(X509CertificateHolder certificateHolder) {
        AuthorityKeyIdentifier authorityKeyIdentifier = AuthorityKeyIdentifier
            .fromExtensions(certificateHolder.getExtensions());
        String res = null;
        if (authorityKeyIdentifier != null) {
            byte[] keyBytes = authorityKeyIdentifier.getKeyIdentifier();
            res = Hex.toHexString(keyBytes);
        }
        return res;
    }

    /**
     * Получить идентификатор издателя данного списка отозванных сертификатов (CRL).
     *
     * @param crl СRL
     * @return идентификатор в виде строки
     * @throws NoSuchAlgorithmException если алгоритм CRL не поддерживается
     * @throws IOException              в случае ошибки доступа к данным CRL
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
     *
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
            log.debug("Не удалось получить ссылки на CRL из сертификата для " + certificate.getSubjectDN().toString(),
                ex.getMessage());
        }
        return crlUrls;
    }

    /**
     * Получить список ссылок, по которым доступны CRL для данного сертификата.
     *
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
     * Построить объект SignatureCertificateInfo из сертификатов и данных о подписанте.
     *
     * @param signerInformation  данные о подписанте
     * @param certificateHolder сертификат
     * @return SignatureInformationResult
     */
    public static SignatureCertificateInfo buildSignatureCertificateInfo(SignerInformation signerInformation,
                                                                         X509CertificateHolder certificateHolder) {
        SignatureCertificateInfo res = new SignatureCertificateInfo();
        res.setIssuer(buildSubject(certificateHolder.getIssuer()));
        res.setSubject(buildSubject(certificateHolder.getSubject()));

        res.setHashAlgorithm(buildHashAlgDescritpion(signerInformation));
        res.setSignatureAlgorithm(buildSignatureAlgDecription(certificateHolder));

        res.setKeyUsage(getKeyUsages(certificateHolder));
        
        res.setCertificatePolicies(getCertificatePolices(certificateHolder));

        res.setValidFrom(certificateHolder.getNotBefore());
        res.setValidTo(certificateHolder.getNotAfter());
        return res;
    }
    
    /**
     * Сконструировать CertificateSubject на основании данных о владельце сертификата.
     *
     * @param name данные о владельце.
     * @return CertificateSubject
     */
    private static CertificateSubject buildSubject(X500Name name) {
        CertificateSubject subject = new CertificateSubject();
        subject.setOrganization(BouncyCastleUtils.getAttributeValue(name, BCStyle.O));
        subject.setOrganizationUnit(BouncyCastleUtils.getAttributeValue(name, BCStyle.OU));
        subject.setEmail(BouncyCastleUtils.getAttributeValue(name, BCStyle.E));
        subject.setTitle(BouncyCastleUtils.getAttributeValue(name, BCStyle.T));
        subject.setCommonName(BouncyCastleUtils.getAttributeValue(name, BCStyle.CN));
        subject.setCountry(BouncyCastleUtils.getAttributeValue(name, BCStyle.C));
        subject.setState(BouncyCastleUtils.getAttributeValue(name, BCStyle.ST));
        subject.setStreet(BouncyCastleUtils.getAttributeValue(name, BCStyle.STREET));
        subject.setLocality(BouncyCastleUtils.getAttributeValue(name, BCStyle.L));
        subject.setSurname(BouncyCastleUtils.getAttributeValue(name, BCStyle.SURNAME));
        subject.setGivenName(BouncyCastleUtils.getAttributeValue(name, BCStyle.GIVENNAME));
        subject.setInn(BouncyCastleUtils.getAttributeValue(name, SubjectAttributesOID.INN));
        subject.setKpp(null);
        subject.setOgrn(BouncyCastleUtils.getAttributeValue(name, SubjectAttributesOID.OGRN));
        subject.setSnils(BouncyCastleUtils.getAttributeValue(name, SubjectAttributesOID.SNILS));
        subject.setOgrnip(BouncyCastleUtils.getAttributeValue(name, SubjectAttributesOID.OGRNIP));

        return subject;
    }

    private static AlgorithmDescription buildHashAlgDescritpion(SignerInformation signerInformation) {
        String hashAlgOid = signerInformation.getDigestAlgOID();
        String hashAlgName = AlgorithmsNames.getAlgorithmName(hashAlgOid);
        AlgorithmDescription res = new AlgorithmDescription();
        res.setOid(hashAlgOid);
        res.setName(hashAlgName);
        return res;
    }

    private static AlgorithmDescription buildSignatureAlgDecription(X509CertificateHolder certificateHolder) {
        String encAlgOid = certificateHolder.getSignatureAlgorithm().getAlgorithm().getId();
        String endAlgName = AlgorithmsNames.getAlgorithmName(encAlgOid);
        AlgorithmDescription res = new AlgorithmDescription();
        res.setOid(encAlgOid);
        res.setName(endAlgName);
        return res;
    }
    
    private static List<CertificatePolicy> getCertificatePolices(X509CertificateHolder certificateHolder) {
        CertificatePolicies certificatePolicies = CertificatePolicies.fromExtensions(certificateHolder.getExtensions());
        List<CertificatePolicy> res = Stream.of(certificatePolicies.getPolicyInformation())
            .map(PolicyInformation::getPolicyIdentifier)
            .map(objectIdentifier -> {
                CertificatePolicy policy = new CertificatePolicy();
                policy.setOid(objectIdentifier.getId());
                policy.setName(CertificatePoliciesName.getPolicyName(objectIdentifier.getId()));
                return policy;
            })
            .collect(Collectors.toList());
        return res;
    }

    private static List<CertificateKeyUsage> getKeyUsages(X509CertificateHolder certificateHolder) {
        ExtendedKeyUsage extendedKeyUsage = ExtendedKeyUsage.fromExtensions(certificateHolder.getExtensions());
        List<CertificateKeyUsage> res = Stream.of(extendedKeyUsage.getUsages())
            .map(KeyPurposeId::getId)
            .map(oid -> {
                CertificateKeyUsage usage = new CertificateKeyUsage();
                usage.setOid(oid);
                usage.setName(ExtendedKeyUsageNames.getUsageName(oid));
                return usage;
            })
            .collect(Collectors.toList());

        return res;
    }
    
}
