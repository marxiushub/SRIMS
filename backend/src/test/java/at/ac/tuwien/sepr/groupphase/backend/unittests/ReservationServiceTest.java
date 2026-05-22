package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles({"test", "datagenerator"})
@SpringBootTest
public class ReservationServiceTest {
    @Autowired
    ReservationService reservationService;
    @Autowired
    EquipmentService equipmentService;
    @Autowired
    EquipmentRepository equipmentRepository;
    @Autowired
    CustomerProfileRepository customerProfileRepository;
    @Autowired
    CustomerRepository customerRepository;

    private CustomerProfile testCustomerProfile;
    private Equipment testEquipment;
    private Customer testCustomer;


    @BeforeEach
    public void setup() {
        testCustomer = new Customer("Adrian","asd","Adrian","67","69",LocalDate.of(1990,1,1));
        testCustomer = customerRepository.save(testCustomer);
        testCustomerProfile = new CustomerProfile("Max Mustermann",180,67,33, SkillLevel.BEGINNER,testCustomer);
        testCustomerProfile = customerProfileRepository.save(testCustomerProfile);
        testEquipment = new Helmet("Test Helmet Model", 10.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER);
        testEquipment = equipmentRepository.save(testEquipment);
    }

    @Test
    @Transactional
    @Rollback
    public void createReservation_withValidData_returnsSavedReservationDto() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setCustomerProfileId(testCustomerProfile.getId());
        dto.setEquipmentIds(List.of(testEquipment.getId()));
        dto.setPickUpDate(LocalDate.now().plusDays(2));
        dto.setPickUpTime(LocalTime.of(10, 0));
        dto.setRentDurationDays(3);

        ReservationDetailDto result = reservationService.createReservation(dto);

        assertAll(
            "Verify that the reservation is saved correctly and mapped to DTO",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isNotNull(),
            () -> assertThat(result.getCustomerName()).isEqualTo("Max Mustermann"),
            () -> assertThat(result.getRentDurationDays()).isEqualTo(3),
            () -> assertThat(result.getReturnDate()).isEqualTo(LocalDate.now().plusDays(5))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void createReservation_withUnknownCustomerId_throwsNotFoundException() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setCustomerProfileId(99999L);
        dto.setEquipmentIds(List.of(testEquipment.getId()));
        dto.setPickUpDate(LocalDate.now().plusDays(2));
        dto.setPickUpTime(LocalTime.of(10, 0));
        dto.setRentDurationDays(3);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            reservationService.createReservation(dto)
        );

        assertAll(
            "Verify that the correct exception with the correct message is thrown",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage()).containsIgnoringCase("not found")
        );
    }



}
