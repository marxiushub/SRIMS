package at.ac.tuwien.sepr.groupphase.backend.basetest;


import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TimePeriodsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class IntegrationTestBase {

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected TimePeriodsRepository timePeriodsRepository;

    @Autowired
    protected CustomerProfileRepository customerProfileRepository;

    @Autowired
    protected EquipmentRepository equipmentRepository;

    @Autowired
    protected CustomerRepository customerRepository;

    @Autowired
    protected StaffRepository staffRepository;

    @Autowired
    protected UserRepository userRepository;

    @AfterEach
    public void cleanupDatabase() {
        reservationRepository.deleteAll();
        timePeriodsRepository.deleteAll();

        customerProfileRepository.deleteAll();

        equipmentRepository.deleteAll();

        customerRepository.deleteAll();
        staffRepository.deleteAll();
        userRepository.deleteAll();
    }
}
