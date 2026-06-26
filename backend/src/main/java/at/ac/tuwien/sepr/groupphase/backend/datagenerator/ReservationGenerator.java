package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Pole;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.PoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Profile({"generateData", "datagenerator"})
@Component
@Order(3)
public class ReservationGenerator implements CommandLineRunner {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final PoleRepository poleRepository;

    public ReservationGenerator(
        ReservationRepository reservationRepository,
        CustomerRepository customerRepository,
        CustomerProfileRepository customerProfileRepository,
        PoleRepository poleRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.customerRepository = customerRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.poleRepository = poleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (reservationRepository.count() > 0) {
            LOGGER.debug("Reservations already generated");
            return;
        }
        LOGGER.debug("Generating reservations");
        List<CustomerProfile> profiles = customerProfileRepository.findAll();
        if (profiles.size() < 3) {
            LOGGER.warn("<3 customerProfiles found");
            return;
        }

        List<Pole> allPoles = poleRepository.findAll();
        if (allPoles.size() < 3) {
            LOGGER.warn("<3 poles found>");
            return;
        }

        CustomerProfile profile1 = profiles.get(0);
        Pole pole1 = allPoles.get(0);
        LocalTime pickUp1 = LocalTime.of(9, 0);
        LocalDate start1 = LocalDate.now().minusDays(60);
        LocalDate end1 = LocalDate.now().minusDays(55);

        Reservation res1 = new Reservation(profile1, pickUp1, start1, end1, ReservationStatus.RETURNED);
        res1.setOverdueReminderSent(false);
        res1.addItem(pole1);
        calculateAndSetTotalPrice(res1);


        CustomerProfile profile2 = profiles.get(1);
        Pole pole2 = allPoles.get(1);
        LocalTime pickUp2 = LocalTime.of(10, 30);
        LocalDate start2 = LocalDate.now().minusDays(30);
        LocalDate end2 = LocalDate.now().minusDays(28);

        Reservation res2 = new Reservation(profile2, pickUp2, start2, end2, ReservationStatus.RETURNED);
        res2.setOverdueReminderSent(false);
        res2.addItem(pole2);
        calculateAndSetTotalPrice(res2);

        CustomerProfile profile3 = profiles.get(2);
        Pole pole3 = allPoles.get(2);
        LocalTime pickUp3 = LocalTime.of(8, 15);
        LocalDate start3 = LocalDate.now().minusDays(14);
        LocalDate end3 = LocalDate.now().minusDays(10);

        Reservation res3 = new Reservation(profile3, pickUp3, start3, end3, ReservationStatus.RETURNED);
        res3.setOverdueReminderSent(false);
        res3.addItem(pole3);
        calculateAndSetTotalPrice(res3);

        reservationRepository.saveAll(List.of(res1, res2, res3));
        LOGGER.debug("Successfully generated 3 completed reservations.");


    }

    private void calculateAndSetTotalPrice(Reservation reservation) {
        if (reservation.getStartDate() == null || reservation.getEndDate() == null || reservation.getItems().isEmpty()) {
            reservation.setTotalPrice(0.0);
            return;
        }

        long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate()) + 1;

        double equipmentSum = reservation.getItems().stream()
            .filter(item -> item.getEquipment() != null)
            .mapToDouble(item -> item.getEquipment().getPrice())
            .sum();

        reservation.setTotalPrice(equipmentSum * days);
    }
}
