package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
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
        } catch (Exception e) {
            throw new RuntimeException("Error sending the HTML email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendReservationConfirmation(List<Equipment> equipmentList, Reservation reservation) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            Context context = new Context();
            context.setVariable("equipmentList", equipmentList);
            context.setVariable("startDate", reservation.getStartDate());
            context.setVariable("endDate", reservation.getEndDate());
            context.setVariable("pickUpTime", reservation.getPickUpTime());
            context.setVariable("firstName", reservation.getCustomerProfile().getCustomer().getFirstName());
            context.setVariable("lastName", reservation.getCustomerProfile().getCustomer().getLastName());
            context.setVariable("totalPrice", String.format(Locale.US,
                "%.2f €", reservation.getTotalPrice()));

            String htmlContent = templateEngine.process("reservation-created-email", context);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom("srims@widmer.wien");
            helper.setTo(reservation.getCustomerProfile().getCustomer().getEmail());
            helper.setSubject(String.format("Reservation Confirmation for %s",
                context.getVariable("firstName") + " " + context.getVariable("lastName")));
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Error sending the HTML email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendOverdueReminder(List<Equipment> equipmentList, Reservation reservation) {
        try {


            MimeMessage message = mailSender.createMimeMessage();
            Context context = new Context();
            context.setVariable("equipmentList", equipmentList);
            context.setVariable("endDate", reservation.getEndDate());
            context.setVariable("firstName", reservation.getCustomerProfile().getCustomer().getFirstName());
            context.setVariable("lastName", reservation.getCustomerProfile().getCustomer().getLastName());

            String htmlContent = templateEngine.process("overdue-email", context);

            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setFrom("srims@widmer.wien");
            helper.setTo(reservation.getCustomerProfile().getCustomer().getEmail());
            helper.setSubject(String.format("Overdue reminder for %s %s — items due %s",
                context.getVariable("firstName"), context.getVariable("lastName"),
                context.getVariable("endDate")));
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error sending the HTML email: " + e.getMessage(), e);

        }

    }

    @Override
    public void sendPickUpReminderEmail(List<Equipment> equipmentList, Reservation reservation) {

        String to = reservation.getCustomerProfile().getCustomer().getEmail();

        LOGGER.info("Preparing pick-up reminder email for {}", to);

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom("srims@widmer.wien");
            helper.setSubject("Get Ready: Your Rental Starts Soon!");

            Context context = new Context();
            context.setVariable("firstName", reservation.getCustomerProfile().getCustomer().getFirstName());
            context.setVariable("lastName", reservation.getCustomerProfile().getCustomer().getLastName());
            context.setVariable("startDate", reservation.getStartDate());
            context.setVariable("pickUpTime", reservation.getPickUpTime());
            context.setVariable("equipmentList", equipmentList);

            String htmlContent = templateEngine.process("pick-up-reminder-email", context);
            helper.setText(htmlContent, true);


            mailSender.send(message);
            LOGGER.info("Pick-up reminder email successfully sent to {}", to);

        } catch (Exception e) {
            LOGGER.error("Failed to send pick-up reminder email to {}", to, e);
            throw new RuntimeException("Error sending the pick-up reminder email", e);
        }
    }
}
