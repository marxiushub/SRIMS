package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.HelmetRepository;
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
public class HelmetDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final HelmetRepository helmetRepository;
    private final List<Helmet> helmets = new ArrayList<>(Arrays.asList(
        new Helmet("Salomon Icon LT", 8.0, 54.0, RentalStatus.FREE, SkillLevel.BEGINNER),
        new Helmet("Atomic Savor Visor", 12.0, 56.0, RentalStatus.RENTED, SkillLevel.BEGINNER),
        new Helmet("Head Rev MIPS", 10.0, 58.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Helmet("Poc Obex Pure", 11.0, 55.0, RentalStatus.MAINTENANCE, SkillLevel.INTERMEDIATE),
        new Helmet("Giro Jackson MIPS", 13.0, 57.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Helmet("Smith Mission", 9.0, 59.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Helmet("Uvex P1us 2.0", 8.0, 53.0, RentalStatus.RENTED, SkillLevel.BEGINNER),
        new Helmet("K2 Diversion", 11.0, 60.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Helmet("Anon Logan WaveCel", 14.0, 61.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Helmet("Bollé Instinct MIPS", 9.0, 52.0, RentalStatus.FREE, SkillLevel.BEGINNER)
    ));

    public HelmetDataGenerator(HelmetRepository helmetRepository) {
        this.helmetRepository = helmetRepository;
    }

    @PostConstruct
    public void generateHelmets() {
        if (helmetRepository.findAll().size() > 0) {
            LOGGER.debug("Helmets already generated");
            return;
        } else {
            LOGGER.debug("Generating "  + helmets.size() + " helmets");
            for (Helmet helmet : helmets) {
                helmetRepository.save(helmet);
            }
        }
    }
}
