package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Pole;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.PoleRepository;
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
public class PoleDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PoleRepository poleRepository;
    private final List<Pole> poles = new ArrayList<>(Arrays.asList(
        new Pole("Salomon Arctic Carbon", 7.0, 110.0, RentalStatus.FREE, SkillLevel.BEGINNER),
        new Pole("Atomic AMT Ultra SQS", 9.0, 115.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Pole("Leki Spitfire Vario", 13.0, 120.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Pole("Head Carbon Race", 10.0, 118.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Pole("K2 Power Composite", 6.0, 105.0, RentalStatus.FREE, SkillLevel.BEGINNER),
        new Pole("Black Diamond Razor Carbon", 12.0, 122.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Pole("Rossignol Tactic Pro", 8.0, 112.0, RentalStatus.FREE, SkillLevel.BEGINNER),
        new Pole("Fischer RC4 Carbon", 11.0, 117.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Pole("Dynastar Speed Zone", 8.0, 114.0, RentalStatus.FREE, SkillLevel.BEGINNER),
        new Pole("Völkl Phantastick Carbon", 14.0, 123.0, RentalStatus.FREE, SkillLevel.ADVANCED)
    ));

    public PoleDataGenerator(PoleRepository poleRepository) {
        this.poleRepository = poleRepository;
    }

    @PostConstruct
    public void generatePoles() {
        if (poleRepository.findAll().size() > 0) {
            LOGGER.debug("Poles already generated");
            return;
        } else {
            LOGGER.debug("Generating " + poles.size() + " Poles");
            for (Pole pole : poles) {
                poleRepository.save(pole);
            }
        }
    }
}
