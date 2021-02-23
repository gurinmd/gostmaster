package ru.gostmaster.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.RedirectView;

/**
 * Контроллер для перенаправления на страницу документации.
 * 
 * @author Maksim Gurin 
 */
@Controller
public class HomeController {

    /**
     * Перенаправление на документацию при обращении к корню.
     * @return редирект
     */
    @GetMapping(value = "/")
    public RedirectView index() {
        return new RedirectView("/swagger-ui/index.html");
    }
}
