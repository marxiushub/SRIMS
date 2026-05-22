package at.ac.tuwien.sepr.groupphase.backend.repository.equipment;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Snowboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnowboardRepository extends JpaRepository<Snowboard, Long> {

    //Add here all method signatures that extend the default CRUD feature and should therefore be implemented in the service layer

}
