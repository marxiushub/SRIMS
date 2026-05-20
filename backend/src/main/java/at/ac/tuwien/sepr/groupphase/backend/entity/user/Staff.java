package at.ac.tuwien.sepr.groupphase.backend.entity.user;

import jakarta.persistence.Entity;

@Entity
public class Staff extends ApplicationUser {

    protected Staff() {}

    public  Staff(String userName, String hashedPassword, String email) {
        super(email, hashedPassword, true, userName);
    }

}
