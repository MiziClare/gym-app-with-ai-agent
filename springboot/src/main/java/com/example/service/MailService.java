package com.example.service;

import com.example.entity.Course;
import com.example.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleMail() {
        // Create a simple email message object
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("xiaoyi0127@yahoo.com"); // Sender email
        message.setTo("mizi99207@gmail.com");  // Receiver email
        message.setSubject("Test Mail");         // Email subject
        message.setText("This is a test email from Spring Boot!"); // Email body

        // Send email
        mailSender.send(message);
    }
    
    /**
     * Send course purchase success email       
     * @param user User information
     * @param course Course information
     * @param price Purchase price
     */
    @Async
    public void sendCoursePurchaseEmail(User user, Course course, Double price) {
        if (user == null || course == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            return; // If user or course is null, or user has no email, do not send email
        }
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("xiaoyi0127@yahoo.com"); // Sender email
        message.setTo(user.getEmail());  // Receiver email
        message.setSubject("Course Purchase Success Notification"); // Email subject
        
        // Email content
        String content = String.format(
            "Dear %s,\n\n" +
            "Thank you for purchasing a single class pass on our platform. Your order has been successfully processed.\n\n" +
            "Order details:\n" +
            "- Course Name: %s\n" +
            "- Coach: %s\n" +
            "- Price: £%.2f\n\n" +
            "You can view your course and appointment schedule in your 'Orders' page.\n\n" +
            "If you have any questions, please contact our customer service team at any time.\n\n" +
            "I wish you a happy fitness journey!\n" +
            "Gym Panel Team",
            user.getName(), course.getName(), course.getCoachName(), price
        );
        
        message.setText(content);
        
        // Send email
        mailSender.send(message);
    }
}
