package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;

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
     * @param equipmentList list of equipment reserved
     * @param reservation the reservation being confirmed
     */
    void sendReservationConfirmation(List<Equipment> equipmentList, Reservation reservation);

    /**
     * Sends an overdue reminder email to the specified email address informing the customer
     * that their reservation ended on the given {@code endDate} and listing the equipment that
     * is overdue.
     *
     * @param reservation the reservation that is overdue
     * @param equipmentList list of equipment that is overdue
     */
    void sendOverdueReminder(List<Equipment> equipmentList, Reservation reservation);

    /**
     * Sends a pick-up reminder email to the customer informing them about their upcoming reservation
     * and the equipment that needs to be picked up.
     *
     * @param equipmentList list of equipment to be picked up
     * @param reservation reservation details including customer information and dates
     */
    void sendPickUpReminderEmail(List<Equipment> equipmentList, Reservation reservation);

    /**
     * Sends a password reset email to the specified user with a temporary password.
     *
     * @param tempPassword temporary password for the user to reset their account
     * @param user the user receiving the password reset email
     */
    void sendPasswordResetEmail(String tempPassword, ApplicationUser user);
}
