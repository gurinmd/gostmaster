package ru.gostmaster.parser;

import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import ru.gostmaster.parser.exception.CMSSignedDataParserException;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

/**
 * Парсинг подписи для проверки.
 * 
 * @author maksimgurin 
 */
public final class CMSDataParser {

    private CMSDataParser() { }
    
    /**
     * Парсим файл и подпись.
     * @param file файл
     * @param signature подпись
     * @return CMSSignedData
     * @throws CMSSignedDataParserException если подпись битая
     */
    public static CMSSignedData parse(byte[] file, byte[] signature) throws CMSSignedDataParserException {
        CMSSignedData res;
        // пробуем как DER
        try {
            res = tryWithSigAsDer(file, signature);
        } catch (CMSSignedDataParserException ex) {
            res = tryWithSigAsPem(file, signature);
        }
        return res;
    }
    
    private static CMSSignedData tryWithSigAsDer(byte[] file, byte[] signature) throws CMSSignedDataParserException {
        try {
            return new CMSSignedData(new CMSProcessableByteArray(file), signature);
        } catch (Exception ex) {
            throw new CMSSignedDataParserException(ex);
        }
    }
    
    private static CMSSignedData tryWithSigAsPem(byte[] file, byte[] signature) throws CMSSignedDataParserException {
        try {
            PemReader reader = new PemReader(new InputStreamReader(new ByteArrayInputStream(signature)));
            PemObject pemObject = reader.readPemObject();
            CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(file), pemObject.getContent());
            return signedData;
        } catch (Exception ex) {
            throw new CMSSignedDataParserException(ex);
        }
        
    }
}
