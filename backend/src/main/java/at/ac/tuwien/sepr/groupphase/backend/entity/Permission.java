package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Permission {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    /**
     * empty constructor,for jpa.
     *
     */
    protected Permission() {
    }

    public Permission(String name) {
        this.name = name;
    }

    /**
     * Getter and setter.
     *
     */
    public String getName() {
        return name;
    }
}
