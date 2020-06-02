package ru.gostmaster;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;
import ru.gostmaster.config.Config;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

import java.security.Security;
import java.util.Date;

/**
 * Запускаем все.
 * 
 * @author maksimgurin 
 */
@SpringBootApplication
@EnableWebFlux
@EnableSwagger2WebFlux
@EnableReactiveMongoRepositories
@EnableScheduling
@Import(Config.class)
public class Main {
    
    /**
     * Запуск Spring Context.
     * @param args параметры командной строки
     * @throws Exception если контейнер не смог стартовать.
     */
    @SuppressWarnings("uncommentedmain")
    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        ApplicationContext context = SpringApplication.run(Main.class, args);
        ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
        System.out.println(objectMapper.writeValueAsString(new Date()));
    }
}
