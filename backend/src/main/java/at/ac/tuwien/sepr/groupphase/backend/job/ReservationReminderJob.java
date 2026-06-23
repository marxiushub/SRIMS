package at.ac.tuwien.sepr.groupphase.backend.job;

import at.ac.tuwien.sepr.groupphase.backend.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReservationReminderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationReminderJob.class);



    private final ReservationService reservationService;

    public ReservationReminderJob(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "0 0 22 * * *")
    //@Scheduled(fixedDelay = 10000)
    public void processPickupReminders() {
        LOGGER.info("Searching for overdue reservations...");

        LocalDate boundaryDate = LocalDate.now().minusDays(1);

        reservationService.processOverdueReservations(boundaryDate);
    }

    @Scheduled(cron = "0 0 8 * * *")
    //@Scheduled(fixedDelay = 10000)
    public void processPickUpReminders() {
        LOGGER.info("Searching for upcoming reservations (<= 2 days)...");
        reservationService.processPickUpReminders();
    }
}
