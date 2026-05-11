package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelmetRepository extends JpaRepository<Helmet, Long> {

    //Add here all method signatures that extend the default CRUD feature and should therefore be implemented in the service layer

}
