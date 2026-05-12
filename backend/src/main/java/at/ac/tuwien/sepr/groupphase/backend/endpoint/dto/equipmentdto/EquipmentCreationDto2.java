package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class EquipmentCreationDto2 {

    /*
     * General Variables
     * */
    @Positive(message = "price is negative")
    private double price;

    @NotBlank(message = "model name is empty")
    private String model;

    @NotNull(message = "RentalStatus is empty")
    private RentalStatus status;

    @NotNull(message = "skillevel is empty")
    private SkillLevel targetSkillLevel;

    /**
     *  specific variables: Helmet,Pole,Ski,Snowboard.
     * */
    private double size;

    /**
     *SnowboardBoots.
     * */
    private String lancingSystem;


    /**
     *Skiboots.
     * */
    private int soleLengthMm;

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

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getLancingSystem() {
        return lancingSystem;
    }

    public void setLancingSystem(String lancingSystem) {
        this.lancingSystem = lancingSystem;
    }

    public int getSoleLengthMm() {
        return soleLengthMm;
    }

    public void setSoleLengthMm(int soleLengthMm) {
        this.soleLengthMm = soleLengthMm;
    }


}
