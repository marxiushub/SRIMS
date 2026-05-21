package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.SkiRepository;
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
public class SkiDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SkiRepository skiRepository;
    private final List<Ski> skiList = new ArrayList<>(Arrays.asList(
        new Ski("Salomon QST 85", 25.0, 160.0, RentalStatus.FREE, SkillLevel.BEGINNER),
        new Ski("Atomic Redster X5", 35.0, 165.0, RentalStatus.RENTED, SkillLevel.INTERMEDIATE),
        new Ski("Head Supershape e-Magnum", 45.0, 170.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Ski("Rossignol Experience 76", 28.0, 158.0, RentalStatus.MAINTENANCE, SkillLevel.BEGINNER),
        new Ski("Völkl Deacon 7.2", 38.0, 168.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Ski("Fischer RC One 74", 32.0, 162.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Ski("K2 Disruption 78C", 36.0, 166.0, RentalStatus.RENTED, SkillLevel.ADVANCED),
        new Ski("Blizzard Thunderbird R15", 48.0, 172.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Ski("Dynastar Speed 263", 29.0, 159.0, RentalStatus.FREE, SkillLevel.BEGINNER),
        new Ski("Nordica Steadfast 80", 37.0, 167.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Ski("Salomon S/Force 11", 42.0, 174.0, RentalStatus.RENTED, SkillLevel.ADVANCED),
        new Ski("Atomic Vantage 75 C", 26.0, 161.0, RentalStatus.FREE, SkillLevel.BEGINNER),
        new Ski("Head Kore 87", 45.0, 173.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Ski("Rossignol React 6 Compact", 30.0, 163.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Ski("Völkl Flair 76", 33.0, 160.0, RentalStatus.MAINTENANCE, SkillLevel.BEGINNER),
        new Ski("Fischer The Curv GT 80", 44.0, 171.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Ski("K2 Press", 25.0, 165.0, RentalStatus.RENTED, SkillLevel.BEGINNER),
        new Ski("Blizzard Firebird HRC", 50.0, 175.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Ski("Nordica Navigator 75", 30.0, 164.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Ski("Atomic Bent 85", 40.0, 169.0, RentalStatus.FREE, SkillLevel.ADVANCED)
    ));

    public SkiDataGenerator(SkiRepository skiRepository) {
        this.skiRepository = skiRepository;
    }

    @PostConstruct
    public void generateSkis() {
        if (skiRepository.findAll().size() > 0) {
            LOGGER.debug("Skis already generated");
            return;
        } else {
            LOGGER.debug("Creating " + skiList.size() + " skis");
            for (Ski ski : skiList) {
                skiRepository.save(ski);
            }
        }
    }
}
