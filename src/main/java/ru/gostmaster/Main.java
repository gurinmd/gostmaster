package ru.gostmaster;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;
import ru.gostmaster.updater.DataUpdater;

import java.security.Security;

/**
 * Запускаем все.
 * 
 * @author maksimgurin 
 */
@SpringBootApplication
@EnableWebFlux
@EnableReactiveMongoRepositories
@EnableScheduling
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
        context.getBean(DataUpdater.class).doUpdate().block();
        System.out.println();
    }
}
