package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.TimePeriods;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimePeriodsRepository extends JpaRepository<TimePeriods, Long> {

    List<TimePeriods> findByEquipment(Equipment equipment);


}
