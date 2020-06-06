package ru.gostmaster.controller;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.gostmaster.updater.DataUpdater;

import java.util.concurrent.Semaphore;

/**
 * Контроллер для управления обновлением данных.
 * 
 * @author maksimgurin 
 */
@RestController
@RequestMapping("/update")
public class UpdateDataController {
    
    @Setter(onMethod_ = {@Autowired})
    private DataUpdater dataUpdater;
    
    private Semaphore semaphore = new Semaphore(1); 

    /**
     * Запускаем обновление.
     * @return HTTP OK
     */
    @RequestMapping(
        path = "/do-scheduled-update",
        method = RequestMethod.POST
    )
    public Mono<Void> doSheduledUpdate() {
        if (semaphore.tryAcquire()) {
            dataUpdater.doUpdate()
                .doFinally(a -> semaphore.release())
                .subscribe();
            return Mono.empty();
        } else {
            return Mono.empty();
        }
        
    }
}
