package at.ac.tuwien.sepr.groupphase.backend.entity.user;

import jakarta.persistence.Entity;

@Entity
public class Staff extends User {

    protected Staff() {}

    protected  Staff(String userName, String hashedPassword, String email) {
        super(userName, hashedPassword, email);
    }

}
