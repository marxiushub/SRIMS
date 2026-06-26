package at.ac.tuwien.sepr.groupphase.backend.repository.equipment;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.id IN :ids")
    List<Equipment> findAllByIdsLocked(@org.springframework.data.repository.query.Param("ids") Iterable<Long> ids);

}
