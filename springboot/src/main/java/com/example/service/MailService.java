package com.example.service;

import com.example.entity.Course;
import com.example.entity.CourseSchedule;
import com.example.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import java.util.Base64;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Resource
    private CourseScheduleService courseScheduleService;
    
    // Read sender email from configuration file
    @Value("${spring.mail.sender.email}")
    private String senderEmail;
    
    // Read sender name from configuration file
    @Value("${spring.mail.sender.name}")
    private String senderName;

    public void sendSimpleMail() {
        // Create a simple email message object
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail); // Use the configured sender email
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
        
        try {
            // Get course schedule
            List<CourseSchedule> scheduleList = courseScheduleService.selectByCourseId(course.getId());
            
            // Create HTML email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(senderEmail); // Use the configured sender email
            helper.setTo(user.getEmail());  // Receiver email
            helper.setSubject("Course Purchase Success Notification"); // Email subject
            
            // Build HTML content with schedule table
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<html><body>");
            htmlContent.append("<h2>Dear ").append(user.getName()).append(",</h2>");
            htmlContent.append("<p>Thank you for purchasing a single class pass on our platform. Your order has been successfully processed.</p>");
            htmlContent.append("<h3>Order details:</h3>");
            htmlContent.append("<ul>");
            htmlContent.append("<li><strong>Course Name:</strong> ").append(course.getName()).append("</li>");
            htmlContent.append("<li><strong>Coach:</strong> ").append(course.getCoachName()).append("</li>");
            htmlContent.append("<li><strong>Price:</strong> £").append(String.format("%.2f", price)).append("</li>");
            htmlContent.append("</ul>");
            
            // Add schedule table
            if (scheduleList != null && !scheduleList.isEmpty()) {
                htmlContent.append("<h3>Course Schedule:</h3>");
                htmlContent.append("<p>With your Single Class Pass, you can attend any of the following sessions:</p>");
                htmlContent.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
                htmlContent.append("<tr style='background-color: #eef5fe;'>");
                htmlContent.append("<th>Weekday</th>");
                htmlContent.append("<th>Start Time</th>");
                htmlContent.append("<th>Room</th>");
                htmlContent.append("</tr>");
                
                // Add rows for each schedule
                for (int i = 0; i < scheduleList.size(); i++) {
                    CourseSchedule schedule = scheduleList.get(i);
                    String rowStyle = i % 2 == 0 ? "background-color: #f9f9f9;" : "background-color: #ffffff;";
                    
                    htmlContent.append("<tr style='").append(rowStyle).append("'>");
                    htmlContent.append("<td>").append(schedule.getWeekday()).append("</td>");
                    htmlContent.append("<td>").append(formatTime(schedule.getStartTime())).append("</td>");
                    htmlContent.append("<td>").append(schedule.getRoom()).append("</td>");
                    htmlContent.append("</tr>");
                }
                
                htmlContent.append("</table>");
            } else {
                htmlContent.append("<p>No schedule is currently available for this course. Please check the website for updates.</p>");
            }
            
            htmlContent.append("<p>You can view your course and appointment schedule in your 'Orders' page.</p>");
            htmlContent.append("<p>If you have any questions, please contact our customer service team at any time.</p>");
            htmlContent.append("<p>I wish you a happy fitness journey!<br>");
            htmlContent.append(senderName).append("</p>"); // Use the configured sender name
            htmlContent.append("</body></html>");
            
            // Set HTML content
            helper.setText(htmlContent.toString(), true);
            
            // Send email
            mailSender.send(mimeMessage);
            
        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simple text email if HTML email fails
            sendSimpleTextPurchaseEmail(user, course, price);
        }
    }
    
    /**
     * Fallback method to send simple text email if HTML email fails
     */
    private void sendSimpleTextPurchaseEmail(User user, Course course, Double price) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail); // Use the configured sender email
        message.setTo(user.getEmail());
        message.setSubject("Course Purchase Success Notification");
        
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
            "%s",
            user.getName(), course.getName(), course.getCoachName(), price, senderName
        );
        
        message.setText(content);
        
        // Send email
        mailSender.send(message);
    }
    
    /**
     * Format time string to display only hours and minutes
     */
    private String formatTime(Object timeObj) {
        if (timeObj == null) {
            return "";
        }
        
        if (timeObj instanceof String) {
            String timeString = (String) timeObj;
            if (timeString.isEmpty()) {
                return "";
            }
            // Handle LocalTime format, usually "HH:MM:SS" format
            return timeString.substring(0, Math.min(timeString.length(), 5)); // Only display hours and minutes
        } else if (timeObj instanceof java.time.LocalTime) {
            java.time.LocalTime localTime = (java.time.LocalTime) timeObj;
            return localTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        }
        
        return timeObj.toString();
    }

    /**
     * Send membership card by email
     * @param user User information
     * @param toEmail Email address to send to
     * @param cardImageBase64 Base64 encoded image of the membership card
     * @return true if email sent successfully, false otherwise
     */
    @Async
    public boolean sendMembershipCardEmail(User user, String toEmail, String cardImageBase64) {
        try {
            // Create HTML email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(senderEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject("Your Gym Membership Card");
            
            // Build HTML content
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<html><body>");
            htmlContent.append("<h2>Dear ").append(user.getName()).append(",</h2>");
            htmlContent.append("<p>Here is your gym membership card as requested.</p>");
            
            // Process the base64 image data
            String base64Image = cardImageBase64;
            if (base64Image.startsWith("data:image")) {
                base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
            }
            
            // Add the image as an attachment with a content ID
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            helper.addInline("membershipCard", new ByteArrayResource(imageBytes), "image/png");
            
            // Reference the image in the HTML using the content ID
            htmlContent.append("<p><img src='cid:membershipCard' alt='Membership Card' style='max-width:100%;'></p>");
            
            // Also add the image as a regular attachment so it can be saved
            helper.addAttachment("membership_card.png", new ByteArrayResource(imageBytes), "image/png");
            
            htmlContent.append("<p>You can save this image for future reference or print it out.</p>");
            htmlContent.append("<p>Thank you for being a valued member of our gym!</p>");
            htmlContent.append("<p>Best regards,<br>");
            htmlContent.append(senderName).append("</p>");
            htmlContent.append("</body></html>");
            
            // Set HTML content
            helper.setText(htmlContent.toString(), true);
            
            // Send email
            mailSender.send(mimeMessage);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to send membership card email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
