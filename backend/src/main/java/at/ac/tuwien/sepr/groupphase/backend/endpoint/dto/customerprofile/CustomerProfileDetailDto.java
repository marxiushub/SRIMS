package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;

/**
 * DTO for displaying details of a CustomerProfile.
 */
public class CustomerProfileDetailDto {

    private Long id;
    private Long customerId;
    private String profileName;
    private double height;
    private double weight;
    private double shoeSize;
    private SkillLevel skillLevel;

    public CustomerProfileDetailDto() {
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Long getCustomerId() {return customerId;}

    public void setCustomerId(Long customerId) {this.customerId = customerId;}

    public String getProfileName() {return profileName;}

    public void setProfileName(String profileName) {this.profileName = profileName;}

    public double getHeight() {return height;}

    public void setHeight(double height) {this.height = height;}

    public double getWeight() {return weight;}

    public void setWeight(double weight) {this.weight = weight;}

    public double getShoeSize() {return shoeSize;}

    public void setShoeSize(double shoeSize) {this.shoeSize = shoeSize;}

    public SkillLevel getSkillLevel() {return skillLevel;}

    public void setSkillLevel(SkillLevel skillLevel) {this.skillLevel = skillLevel;}
}
