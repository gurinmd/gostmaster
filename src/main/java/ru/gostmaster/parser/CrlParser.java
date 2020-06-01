package ru.gostmaster.parser;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Component;
import ru.gostmaster.data.crl.Crl;
import ru.gostmaster.model.MongoCrlData;
import ru.gostmaster.parser.exception.CrlParserException;
import ru.gostmaster.util.BouncyCastleUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Парсер списка отозванных сертификатов (CRL).
 * 
 * @author maksimgurin 
 */
@Component
@Slf4j
public class CrlParser {
    private CertificateFactory certificateFactory;

    /**
     * Конструктор.
     * @throws CertificateException если что то с загрузкой BouncyCastle.
     * @throws NoSuchProviderException если что то с загрузкой BouncyCastle.
     */
    public CrlParser() throws CertificateException, NoSuchProviderException {
        this.certificateFactory = CertificateFactory.getInstance("X509", BouncyCastleProvider.PROVIDER_NAME);
    }

    /**
     * Парсинг Crl на основании массива байтов.
     * @param bytes байты
     * @param url ссылка, откуда был скачан CRL
     * @return Crl
     * @throws RuntimeException при ошибке
     */
    public Crl parseRawDataCrl(byte[] bytes, String url) {
        return parseRawDataCrl(new ByteArrayInputStream(bytes), url);
    }

    /**
     * Парсинг Crl на основании стрима байтов.
     * @param inputStream байты
     * @param url ссылка, откуда был скачан CRL
     * @return Crl
     * @throws CrlParserException при ошибке
     */
    public Crl parseRawDataCrl(InputStream inputStream, String url) {
        try {
            X509CRL parsed = (X509CRL) certificateFactory.generateCRL(inputStream);
            X509CRLHolder crlHolder = new X509CRLHolder(parsed.getEncoded());
            MongoCrlData crl = new MongoCrlData();
            crl.setIssuerKey(BouncyCastleUtils.getAuthorityKeyIdentifier(parsed));
            crl.setPemData(toPem(crlHolder.getEncoded()));
            crl.setNextUpdate(crlHolder.getNextUpdate());
            crl.setThisUpdate(crlHolder.getThisUpdate());
            crl.setIssuer(crlHolder.getIssuer().toString());
            crl.setDownloadedFrom(url);
            return crl;
        } catch (Exception ex) {
            log.debug("Ошибка разбора CRL по ссылке " + url, ex.getMessage());
            throw new CrlParserException(ex);
        }
    }

    /**
     * Запись CRL в строку формата PEM.
     * @param crl байт
     * @return PEM представление crl
     * @throws IOException при ошибке
     */
    public String toPem(byte[] crl) throws IOException {
        StringWriter sw = new StringWriter();
        PemWriter pemWriter = new JcaPEMWriter(sw);
        PemObject pemObject = new PemObject("X509 CRL", crl);
        pemWriter.writeObject(pemObject);
        pemWriter.flush();
        return sw.toString();
    }
    
    private X509CRLHolder toHolder(Crl crl) {
        try  {
            X509CRL parsed = (X509CRL) certificateFactory
                .generateCRL(new ByteArrayInputStream(crl.getPemData().getBytes()));
            X509CRLHolder crlHolder = new X509CRLHolder(parsed.getEncoded());
            return crlHolder;
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }

    /**
     * Конвертация представление в БД в представлнеие в  виде  BouncyCastle.
     * @param crlList список CRL в терминах модели данных приложения
     * @return список CRL в терминах модели данных BouncyCastle
     */
    public List<X509CRLHolder> toHolderList(List<Crl> crlList) {
        return crlList.stream().map(this::toHolder).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
