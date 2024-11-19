package dev.anta.secure_jdbc.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public String home() {
        return "Hello World!";
    }

    @GetMapping(value = "/user")
    public String user() {
        return "Welcome Dear User";
    }

    @GetMapping(value = "/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "Welcome Respected Admin";
    }

}
