package at.ac.tuwien.sepr.groupphase.backend.entity.user;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"customer_id", "profile_name"}
        )
    }
)
public class CustomerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String profileName;

    @Column
    private double height;

    @Column
    private double weight;

    @Column
    private double shoeSize;

    @Enumerated(EnumType.STRING)
    private SkillLevel skillLevel;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    protected CustomerProfile() {}

    public CustomerProfile(String profileName, double height, double weight, double shoeSize, SkillLevel skillLevel, Customer customer) {
        this.profileName = profileName;
        this.height = height;
        this.weight = weight;
        this.shoeSize = shoeSize;
        this.skillLevel = skillLevel;
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public  double getShoeSize() {
        return shoeSize;
    }

    public void setShoeSize(double shoeSize) {
        this.shoeSize = shoeSize;
    }

    public  SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
