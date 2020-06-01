package ru.gostmaster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Конфиг для сваггера.
 * 
 * @author maksimgurin 
 */
@Configuration
public class SpringFoxConfig {

    private static final String CONTROLLER_BASE_PACKAGE = "ru.gostmaster.controller";
    
    /**
     * Бин конфигурации сваггера.
     * @return бин
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .apiInfo(apiInfo())
            .enable(true)
            .select()
            .apis(RequestHandlerSelectors.basePackage(CONTROLLER_BASE_PACKAGE))
            .paths(PathSelectors.any())
            .build()
            .pathMapping("/");
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("API проверки открепленной УКЭП")
            .description("API проверки открепленной УКЭП")
            .build();
    }
}
