package com.example.service;

import com.example.entity.Course;
import com.example.entity.CourseSchedule;
import com.example.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
        
        try {
            // Get course schedule
            List<CourseSchedule> scheduleList = courseScheduleService.selectByCourseId(course.getId());
            
            // Create HTML email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom("xiaoyi0127@yahoo.com"); // Sender email
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
            htmlContent.append("Gym Panel Team</p>");
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
        message.setFrom("xiaoyi0127@yahoo.com");
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
            "Gym Panel Team",
            user.getName(), course.getName(), course.getCoachName(), price
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
}
