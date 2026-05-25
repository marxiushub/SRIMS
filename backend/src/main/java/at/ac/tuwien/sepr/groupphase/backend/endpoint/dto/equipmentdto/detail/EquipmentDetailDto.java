package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.occupancy.TimePeriodDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * Represents a Data Transfer Object (DTO) for equipment information. This class
 * is used to transfer equipment data between different layers of the application.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "equipmentType",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SkiDetailDto.class, name = "SKI"),
    @JsonSubTypes.Type(value = SkiBootDetailDto.class, name = "SKIBOOT"),
    @JsonSubTypes.Type(value = SnowboardDetailDto.class, name = "SNOWBOARD"),
    @JsonSubTypes.Type(value = SnowboardBootDetailDto.class, name = "SNOWBOARDBOOT"),
    @JsonSubTypes.Type(value = HelmetDetailDto.class, name = "HELMET"),
    @JsonSubTypes.Type(value = PoleDetailDto.class, name = "POLE")
})
public abstract class EquipmentDetailDto {
    private Long id;
    private double price;
    private String model;
    private RentalStatus status;
    private String barcodeId;
    private SkillLevel targetSkillLevel;
    private EquipmentType equipmentType;
    private List<TimePeriodDto> occupancy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(String barcodeId) {
        this.barcodeId = barcodeId;
    }

    public SkillLevel getTargetSkillLevel() {
        return targetSkillLevel;
    }

    public void setTargetSkillLevel(SkillLevel targetSkillLevel) {
        this.targetSkillLevel = targetSkillLevel;
    }

    public EquipmentType getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(EquipmentType equipmentType) {
        this.equipmentType = equipmentType;
    }

    public List<TimePeriodDto> getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(List<TimePeriodDto> occupancy) {
        this.occupancy = occupancy;
    }
}


