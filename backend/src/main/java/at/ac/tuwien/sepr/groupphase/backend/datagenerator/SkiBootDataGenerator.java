package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SkiBoot;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.SkiBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Profile("generateData")
@Component
public class SkiBootDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SkiBootRepository skiBootRepository;
    private final List<SkiBoot> skiBootList = new ArrayList<>(Arrays.asList(
        new SkiBoot("Salomon X Access 70", 18.0, 260, RentalStatus.FREE, SkillLevel.BEGINNER),
        new SkiBoot("Atomic Hawx Prime 80", 22.0, 265, RentalStatus.FREE, SkillLevel.BEGINNER),
        new SkiBoot("Head Edge LYT 90", 26.0, 270, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SkiBoot("Rossignol Evo 70", 17.0, 255, RentalStatus.FREE, SkillLevel.BEGINNER),
        new SkiBoot("Nordica Cruise 90", 24.0, 275, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SkiBoot("Tecnica Mach Sport 100", 30.0, 280, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SkiBoot("Fischer RC One 100", 28.0, 285, RentalStatus.FREE, SkillLevel.ADVANCED),
        new SkiBoot("Lange RX 110", 34.0, 290, RentalStatus.FREE, SkillLevel.ADVANCED),
        new SkiBoot("Dalbello Panterra 90", 27.0, 275, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SkiBoot("K2 BFC 80", 23.0, 270, RentalStatus.FREE, SkillLevel.BEGINNER),
        new SkiBoot("Salomon S/Pro 120", 38.0, 295, RentalStatus.FREE, SkillLevel.ADVANCED),
        new SkiBoot("Atomic Hawx Ultra 130", 42.0, 300, RentalStatus.FREE, SkillLevel.ADVANCED),
        new SkiBoot("Head Vector Evo 110", 32.0, 285, RentalStatus.FREE, SkillLevel.ADVANCED),
        new SkiBoot("Rossignol Alltrack 90", 26.0, 280, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SkiBoot("Nordica Speedmachine 110", 35.0, 290, RentalStatus.FREE, SkillLevel.ADVANCED),
        new SkiBoot("Tecnica Cochise 100", 30.0, 285, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SkiBoot("Fischer Ranger One 80", 24.0, 275, RentalStatus.FREE, SkillLevel.BEGINNER),
        new SkiBoot("Dalbello DS MX 75", 20.0, 265, RentalStatus.FREE, SkillLevel.BEGINNER),
        new SkiBoot("Lange LX 100", 31.0, 285, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SkiBoot("K2 Recon 120", 36.0, 295, RentalStatus.FREE, SkillLevel.ADVANCED)
    ));

    public SkiBootDataGenerator(SkiBootRepository skiBootRepository) {
        this.skiBootRepository = skiBootRepository;
    }

    @PostConstruct
    public void generateSkis() {
        if (skiBootRepository.findAll().size() > 0) {
            LOGGER.debug("Ski boots already generated");
            return;
        } else {
            LOGGER.debug("Creating " + skiBootList.size() + " ski boots");
            for (SkiBoot skiBott : skiBootList) {
                skiBootRepository.save(skiBott);
            }
        }
    }
}
