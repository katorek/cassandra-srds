package com.wjaronski.cassandrademo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {
    
    @GetMapping(value = "/", produces = "text/html")
    public String redirectToDoc() {
        return new StringBuilder(""
                + "<html>"
                + " <head>"
                + "  <meta http-equiv=\"refresh\" content=\"0; url=swagger-ui.html\" />"
                + " </head>"
                + " <body/></html>").toString();
    }

}
