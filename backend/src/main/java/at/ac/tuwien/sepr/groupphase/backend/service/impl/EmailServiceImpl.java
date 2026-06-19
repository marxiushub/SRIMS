package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Service
@Profile("!test")
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendEmail(String emailAddressTo, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(emailAddressTo);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("srims@widmer.wien");

        mailSender.send(message);


    }

    @Override
    public void sendAccountCreationSuccessEmail(String emailAddressTo, String customerName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            Context context = new Context();
            context.setVariable("name", customerName);

            String htmlContent = templateEngine.process("welcome-email", context);


            helper.setFrom("srims@widmer.wien");
            helper.setTo(emailAddressTo);
            helper.setSubject(String.format("Welcome to SRIMS %s 🎉", customerName));
            helper.setText(htmlContent, true);


            mailSender.send(mimeMessage);
            LOGGER.warn("Sent account creation success email to {} for customer {}", emailAddressTo, customerName);

        } catch (Exception e) {
            throw new RuntimeException("Error sending the HTML email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendReservationConfirmation(String emailAddressTo, List<Equipment> equipmentList, LocalDate startDate, LocalDate endDate, LocalTime pickUpTime, String firstName, String lastName, double totalPrice) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            Context context = new Context();
            context.setVariable("equipmentList", equipmentList);
            context.setVariable("startDate", startDate);
            context.setVariable("endDate", endDate);
            context.setVariable("pickUpTime", pickUpTime);
            context.setVariable("firstName", firstName);
            context.setVariable("lastName", lastName);
            context.setVariable("totalPrice", String.format(Locale.US, "%.2f €", totalPrice));

            String htmlContent = templateEngine.process("reservation-created-email", context);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom("srims@widmer.wien");
            helper.setTo(emailAddressTo);
            helper.setSubject(String.format("Reservation Confirmation for %s", firstName + " " + lastName));
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            LOGGER.warn("Sent reservation confirmation email to {} for customer {}", emailAddressTo, firstName + " " + lastName);

        } catch (Exception e) {
            throw new RuntimeException("Error sending the HTML email: " + e.getMessage(), e);
        }
    }
}
