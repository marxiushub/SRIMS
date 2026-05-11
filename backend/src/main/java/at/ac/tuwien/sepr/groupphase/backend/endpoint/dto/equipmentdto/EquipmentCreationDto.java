package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * A general dto for Equipment.
 * */

//PS: Dominik: die Annoationen werfen MethodArgumentNotValidException, muss im exception hanler behandelt werden
public abstract class EquipmentCreationDto {

    @Positive(message = "price is negative")
    private double price;

    @NotBlank(message = "model name is empty")
    private String model;

    @NotNull(message = "RentalStatus is empty")
    private RentalStatus status;

    @NotNull(message = "skillevel is empty")
    private SkillLevel targetSkillLevel;


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



}
