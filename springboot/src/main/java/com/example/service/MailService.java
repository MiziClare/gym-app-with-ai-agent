package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleMail() {
        // 创建简单邮件消息对象
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("xiaoyi0127@yahoo.com"); // 发送者邮箱
        message.setTo("mizi99207@gmail.com");  // 接收者邮箱
        message.setSubject("Test Mail");         // 邮件主题
        message.setText("This is a test email from Spring Boot!"); // 邮件正文

        // 发送邮件
        mailSender.send(message);
    }
}
