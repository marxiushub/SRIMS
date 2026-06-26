package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    @Query("SELECT DISTINCT r FROM Reservation r "
        + "LEFT JOIN FETCH r.items "
        + "WHERE r.reservationStatus IN :statuses")
    List<Reservation> findByReservationStatusIn(@Param("statuses") List<ReservationStatus> status);

    List<Reservation> findByEndDateBeforeAndReservationStatusAndOverdueReminderSentFalse(
        LocalDate date,
        ReservationStatus status
    );

    List<Reservation> findByStartDateBetweenAndReservationStatusAndPickUpReminderSentFalse(
        LocalDate startDateFrom,
        LocalDate startDateTo,
        ReservationStatus status
    );

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    java.util.Optional<Reservation> findByIdLocked(@org.springframework.data.repository.query.Param("id") Long id);

    boolean existsByCustomerProfileId(Long customerProfileId);
}
