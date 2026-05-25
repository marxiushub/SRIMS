package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * DTO for creating a CustomerProfile.
 */
public class CustomerProfileCreationDto {

    @NotBlank(message = "Profile name is empty")
    private String profileName;

    @Positive(message = "Height must be positive")
    @Max(value = 250, message = "Height too large")
    @Min(value = 50, message = "Height too small")
    private double height;

    @Positive(message = "Weight must be positive")
    @Max(value = 150, message = "Weight too large")
    @Min(value = 20, message = "Weight too small")
    private double weight;

    @Positive(message = "Shoe size must be positive")
    @Max(value = 50, message = "Shoe size too large")
    @Min(value = 25, message = "Shoe size too small")
    private double shoeSize;

    @NotNull(message = "Skill level is required")
    private SkillLevel skillLevel;

    //TODO: nur positive CustomerIds?
    @NotNull(message = "Customer id is required")
    private Long customerId;

    public CustomerProfileCreationDto() {
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

    public double getShoeSize() {
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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
