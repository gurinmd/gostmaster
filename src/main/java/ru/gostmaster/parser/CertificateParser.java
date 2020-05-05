package ru.gostmaster.parser;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Component;
import ru.gostmaster.model.MongoCertificateData;
import ru.gostmaster.util.BouncyCastleUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Component
@Slf4j
public class CertificateParser {
    private CertificateFactory certificateFactory;
    
    public CertificateParser() throws CertificateException, NoSuchProviderException {
        certificateFactory = CertificateFactory.getInstance("X509", BouncyCastleProvider.PROVIDER_NAME);
    }
    
    public MongoCertificateData parseRawDataCertificate(byte[] certBytes) throws CertificateException, IOException {
        return parseRawDataCertificate(new ByteArrayInputStream(certBytes));
    }

    public MongoCertificateData parseRawDataCertificate(String cert) throws CertificateException, IOException {
        return parseRawDataCertificate(cert.getBytes());
    }

    public MongoCertificateData parseRawDataCertificate(InputStream stream) throws CertificateException, IOException {
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(stream);
        MongoCertificateData certificateData = new MongoCertificateData();
        certificateData.setCrlUrls(BouncyCastleUtils.getCrlUrlsFromCertificate(certificate));
        certificateData.setIssuer(certificate.getIssuerDN().getName());
        certificateData.setSubject(certificate.getSubjectDN().getName());
        certificateData.setSn(certificate.getSerialNumber());
        certificateData.setStartDate(certificate.getNotBefore());
        certificateData.setEndDate(certificate.getNotAfter());
        certificateData.setCertificateEncodedData(certificateAsString(certificate.getEncoded()));
        return certificateData;
    }
    
    public String certificateAsString(byte[] cert) throws IOException {
        StringWriter sw = new StringWriter();
        PemWriter pemWriter = new JcaPEMWriter(sw);
        PemObject pemObject = new PemObject("CERTIFICATE", cert);
        pemWriter.writeObject(pemObject);
        pemWriter.flush();
        return sw.toString();
    }
    
}
