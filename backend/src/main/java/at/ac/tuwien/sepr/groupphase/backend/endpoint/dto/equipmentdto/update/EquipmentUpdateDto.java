package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = HelmetUpdateDto.class, name = "HELMET"),
    @JsonSubTypes.Type(value = SkiUpdateDto.class, name = "SKI"),
    @JsonSubTypes.Type(value = PoleUpdateDto.class, name = "POLE"),
    @JsonSubTypes.Type(value = SkiBootUpdateDto.class, name = "SKIBOOT"),
    @JsonSubTypes.Type(value = SnowboardUpdateDto.class, name = "SNOWBOARD"),
    @JsonSubTypes.Type(value = SnowboardBootUpdateDto.class, name = "SNOWBOARDBOOT")
})
public abstract class EquipmentUpdateDto {

    @NotNull(message = "Type is not allowed to be missing")
    private EquipmentType type;


    @Min(0)
    private Double price;

    private String model;
    private RentalStatus status;
    private SkillLevel targetSkillLevel;

    /**
     * Getter and Setter.
     * */

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
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

    /**
     * management methods.
     * */

    public EquipmentType getType() {
        return type;
    }


}