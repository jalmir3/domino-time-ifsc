package sistema.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/favicon.ico")
public class FaviconController {

    @GetMapping
    public ResponseEntity<Void> favicon() {
        return ResponseEntity
                .noContent()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .build();
    }
}

