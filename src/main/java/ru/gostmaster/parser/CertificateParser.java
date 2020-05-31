package ru.gostmaster.parser;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Component;
import ru.gostmaster.data.cert.Certificate;
import ru.gostmaster.model.MongoCertificateData;
import ru.gostmaster.parser.exception.CertificateParserException;
import ru.gostmaster.util.BouncyCastleUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Парсер сертификатов в модель данных приложения.
 * 
 * @author maksimgurin 
 */
@Component
@Slf4j
public class CertificateParser {
    private CertificateFactory certificateFactory;

    /**
     * Конструктор.
     * @throws CertificateException проблема с BouncyCastle
     * @throws NoSuchProviderException проблема с BouncyCastle
     */
    public CertificateParser() throws CertificateException, NoSuchProviderException {
        certificateFactory = CertificateFactory.getInstance("X509", BouncyCastleProvider.PROVIDER_NAME);
    }
    
    /**
     * Распарсить сертификат, который представлен в виде набора байт.
     * @param certBytes байты
     * @return сертификат
     */
    public Certificate parseRawDataCertificate(byte[] certBytes) {
        return parseRawDataCertificate(new ByteArrayInputStream(certBytes));
    }

    /**
     * Распарсить сертификат, который представлен в виде PEM.
     * @param cert PEM представление сертификата
     * @return сертификат
     */
    public Certificate parseRawDataCertificate(String cert) {
        return parseRawDataCertificate(cert.getBytes());
    }

    /**
     * Распарсить сертификат, который представлен в виде потока байтов.
     * @param stream поток байтов сертификата (DER)
     * @return сертификат
     */
    public Certificate parseRawDataCertificate(InputStream stream)  {
        try {
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(stream);
            MongoCertificateData certificateData = new MongoCertificateData();
            certificateData.setCrlUrls(BouncyCastleUtils.getCrlUrlsFromCertificate(certificate));
            certificateData.setIssuerKey(BouncyCastleUtils.getAuthorityKeyIdentifier(certificate));
            certificateData.setSubjectKey(BouncyCastleUtils.getAuthorityKeyIdentifier(certificate));
            certificateData.setSn(certificate.getSerialNumber());
            certificateData.setStartDate(certificate.getNotBefore());
            certificateData.setEndDate(certificate.getNotAfter());
            certificateData.setPemData(toPem(certificate.getEncoded()));
            return certificateData;
        } catch (Exception ex) {
            log.error("", ex);
            throw new CertificateParserException(ex);
        }
    }

    private String toPem(byte[] cert) throws IOException {
        StringWriter sw = new StringWriter();
        PemWriter pemWriter = new JcaPEMWriter(sw);
        PemObject pemObject = new PemObject("CERTIFICATE", cert);
        pemWriter.writeObject(pemObject);
        pemWriter.flush();
        return sw.toString();
    }

}
