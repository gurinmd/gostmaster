package ru.gostmaster.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.gostmaster.verification.data.VerificationResult;

/**
 * Контроллер для обращения с фронта.
 * 
 * @author maksimgurin 
 */
@RestController
public class SignatureVerificationController {

    /**
     * Проверить файлы.
     * @return результат проверки
     */
    @RequestMapping(
        path = "/verify",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = @ApiResponse(code = 200, response = VerificationResult.class, 
        message = "Результат проверки"))
    public Mono<VerificationResult> verify() {
        return Mono.just(new VerificationResult());
    }
}
