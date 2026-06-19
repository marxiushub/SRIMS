package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Profile("test")
public class FakeEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeEmailService.class);

    @Override
    public void sendEmail(String emailAddressTo, String subject, String body) {
        LOGGER.info("Fake email sent to {} (Subject: {})", emailAddressTo, subject);
    }

    @Override
    public void sendAccountCreationSuccessEmail(String emailAddressTo, String customerName) {
        LOGGER.info("Fake email sent to {} (Account creation successful)", emailAddressTo);
    }


    @Override
    public void sendReservationConfirmation(String emailAddressTo, List<Equipment> equipmentList, LocalDate startDate,
                                            LocalDate endDate, LocalTime pickUpTime, String firstName, String lastName,
                                            double totalPrice) {
        LOGGER.info("Fake email sent to {} (Reservation)", emailAddressTo);
    }
}