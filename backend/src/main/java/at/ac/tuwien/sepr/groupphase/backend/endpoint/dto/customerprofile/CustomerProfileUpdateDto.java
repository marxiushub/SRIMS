package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating a customer profile.
 */
public class CustomerProfileUpdateDto {

    @Size(max = 100, message = "Profile name must not exceed 100 characters")
    private String profileName;

    @Positive(message = "Height must be positive")
    @Max(value = 250, message = "Height too large")
    @Min(value = 50, message = "Height too small")
    private Double height;

    @Positive(message = "Weight must be positive")
    @Max(value = 150, message = "Weight too large")
    @Min(value = 20, message = "Weight too small")
    private Double weight;

    @Positive(message = "Shoe size must be positive")
    @Max(value = 50, message = "Shoe size too large")
    @Min(value = 25, message = "Shoe size too small")
    private Double shoeSize;

    private SkillLevel skillLevel;

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Double getShoeSize() {
        return shoeSize;
    }

    public void setShoeSize(double shoeSize) {
        this.shoeSize = shoeSize;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

}
