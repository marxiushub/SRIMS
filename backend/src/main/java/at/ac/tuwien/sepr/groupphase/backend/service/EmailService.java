package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service interface for interacting with the email API (e.g. sending emails to customers).
 */
public interface EmailService {

    /**
     * Sends an email to the specified email address with the given subject and body using the Brevo API.Audit done.
     *
     * @param emailAddressTo receiver address
     * @param subject subject of the email
     * @param body body of the email
     */
    void sendEmail(String emailAddressTo, String subject, String body);

    /**
     * Sends an account creation success email to the specified email address.
     *
     * @param emailAddressTo receiver address
     * @param customerName name of the customer to personalize the email
     */
    void sendAccountCreationSuccessEmail(String emailAddressTo, String customerName);

    /**
     * Sends a reservation confirmation email to the specified email address.
     *
     * @param emailAddressTo receiver address
     * @param equipmentList list of equipment reserved
     * @param startDate start date of the reservation
     * @param endDate end date of the reservation
     * @param pickUpTime pick-up time of the reservation
     * @param firstName first name of the customer to personalize the email
     * @param lastName last name of the customer to personalize the email
     * @param totalPrice total price of the reservation (in the application's currency)
     */
    void sendReservationConfirmation(String emailAddressTo, List<Equipment> equipmentList, LocalDate startDate, LocalDate endDate, LocalTime pickUpTime, String firstName, String lastName, double totalPrice);

}
