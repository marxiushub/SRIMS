package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByEndDateBeforeAndReservationStatusAndOverdueReminderSentFalse(
        LocalDate date,
        ReservationStatus status
    );

    List<Reservation> findByStartDateBetweenAndReservationStatusAndPickUpReminderSentFalse(
        LocalDate startDateFrom,
        LocalDate startDateTo,
        ReservationStatus status
    );
}
