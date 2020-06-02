package ru.gostmaster.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.gostmaster.messages.Messages;
import ru.gostmaster.parser.CMSDataParser;
import ru.gostmaster.parser.exception.CMSSignedDataParserException;
import ru.gostmaster.util.FileUtils;
import ru.gostmaster.verification.VerificationService;
import ru.gostmaster.verification.data.VerificationResult;
import ru.gostmaster.verification.exception.SignatureUploadException;

import java.util.Date;

/**
 * Контроллер для обращения с фронта.
 *
 * @author maksimgurin
 */
@RestController
public class SignatureVerificationController {

    @Setter(onMethod_ = {@Autowired})
    private VerificationService verificationService;

    /**
     * Проверить файлы.
     * @param data файл для проверки
     * @param signature его подпись            
     * @return результат проверки
     */
    @RequestMapping(
        path = "/verify",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, response = VerificationResult.class, message = "Результат проверки"),
        @ApiResponse(code = 400, response = VerificationResult.class, message = "Ошибка проверки")
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "data", dataType = "__file", paramType = "form", required = true),
        @ApiImplicitParam(name = "sig", dataType = "__file", paramType = "form", required = true)
    })
    public Mono<ResponseEntity<VerificationResult>> verify(@RequestPart("data") Mono<FilePart> data,
                                                           @RequestPart("sig") Mono<FilePart> signature) {
        Mono<byte[]> dataBytesMono = data.flatMap(filePart -> FileUtils.readToBytes(filePart.content()));
        Mono<byte[]> sigBytesMono = signature.flatMap(filePart -> FileUtils.readToBytes(filePart.content()));

        Mono<ResponseEntity<VerificationResult>> res = Mono.zip(dataBytesMono, sigBytesMono)
            .map(pair -> CMSDataParser.parse(pair.getT1(), pair.getT2()))
            .flatMap(cmsSignedData -> verificationService.verify(cmsSignedData))
            .map(verificationResult -> ResponseEntity.ok(verificationResult))
            .onErrorResume(SignatureUploadException.class, e -> {
                VerificationResult verificationResult = new VerificationResult();
                verificationResult.setUploadingErrorDate(new Date());
                verificationResult.setUploadingErrorDescription(Messages.getMessage(Messages.ERROR_UPLOADING_SIGNATURE));
                verificationResult.setUploadingError(true);
                return Mono.just(ResponseEntity.badRequest().body(verificationResult));
            })
            .onErrorResume(CMSSignedDataParserException.class, e -> {
                VerificationResult verificationResult = new VerificationResult();
                verificationResult.setUploadingErrorDate(new Date());
                verificationResult.setUploadingErrorDescription(Messages.getMessage(Messages.SIGNATURE_INVALID_FORMAT));
                verificationResult.setUploadingError(true);
                return Mono.just(ResponseEntity.badRequest().body(verificationResult));
            });

        return res;
    }
}
