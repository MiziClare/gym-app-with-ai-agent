package com.example.controller;

import com.example.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailController {
    @Autowired
    private MailService mailService;

    @GetMapping("/sendMail")
    public String sendMail() {
        try {
            mailService.sendSimpleMail();
            return "Mail sent successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Mail sending failed: " + e.getMessage();
        }
    }
}
