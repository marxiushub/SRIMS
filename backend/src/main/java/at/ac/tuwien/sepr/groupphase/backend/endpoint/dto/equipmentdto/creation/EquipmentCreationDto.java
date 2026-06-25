package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * A general dto for Equipment.
 * */


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)

@JsonSubTypes({
    @JsonSubTypes.Type(value = HelmetCreationDto.class, name = "HELMET"),
    @JsonSubTypes.Type(value = SkiCreationDto.class, name = "SKI"),
    @JsonSubTypes.Type(value = PoleCreationDto.class, name = "POLE"),
    @JsonSubTypes.Type(value = SkiBootCreationDto.class, name = "SKIBOOT"),
    @JsonSubTypes.Type(value = SnowboardCreationDto.class, name = "SNOWBOARD"),
    @JsonSubTypes.Type(value = SnowboardBootCreationDto.class, name = "SNOWBOARDBOOT")
})

public abstract class EquipmentCreationDto {

    @Positive(message = "Price is negative")
    private double price;

    @NotBlank(message = "Model is empty")
    @Size(max = 100, message = "Model must be at most 100 characters")
    private String model;

    @NotNull(message = "RentalStatus is empty")
    private RentalStatus status;

    @NotNull(message = "Skillevel is empty")
    private SkillLevel targetSkillLevel;

    @Min(value = 1, message = "CreationNumber must be at least 1")
    @Max(value = 100, message = "CreationNumber must be at most 100")
    private int creationNumber = 1;

    /**
     * Getter and Setter.
     * */

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
    }

    public SkillLevel getTargetSkillLevel() {
        return targetSkillLevel;
    }

    public void setTargetSkillLevel(SkillLevel targetSkillLevel) {
        this.targetSkillLevel = targetSkillLevel;
    }

    public int getCreationNumber() {
        return creationNumber;
    }

    public void setCreationNumber(int creationNumber) {
        this.creationNumber = creationNumber;
    }

    /**
     * management methods.
     * */

    public abstract EquipmentType getType();

    public abstract Equipment toEntity();


}
