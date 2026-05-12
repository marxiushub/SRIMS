package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Snowboard;
import at.ac.tuwien.sepr.groupphase.backend.repository.SnowboardRepository;
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
public class SnowboardDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SnowboardRepository snowboardRepository;
    private final List<Snowboard> snowboardList = new ArrayList<>(Arrays.asList(
        new Snowboard("Burton Ripcord", 20.0, 150.0, RentalStatus.FREE, SkillLevel.BEGINNER),
        new Snowboard("Nitro Prime Raw", 19.0, 152.0, RentalStatus.RENTED, SkillLevel.BEGINNER),
        new Snowboard("Salomon Pulse", 20.0, 155.0, RentalStatus.FREE, SkillLevel.BEGINNER),
        new Snowboard("Ride Agenda", 23.0, 154.0, RentalStatus.MAINTENANCE, SkillLevel.INTERMEDIATE),
        new Snowboard("K2 Raygun Pop", 26.0, 157.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Snowboard("Capita Outerspace Living", 28.0, 156.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Snowboard("Jones Mountain Twin", 33.0, 158.0, RentalStatus.RENTED, SkillLevel.ADVANCED),
        new Snowboard("Lib Tech Cold Brew", 35.0, 159.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Snowboard("Arbor Element Camber", 30.0, 157.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Snowboard("Rossignol Templar", 25.0, 155.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Snowboard("Burton Custom Flying V", 36.0, 160.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Snowboard("Nitro Team", 31.0, 158.0, RentalStatus.RENTED, SkillLevel.ADVANCED),
        new Snowboard("Salomon Assassin", 33.0, 159.0, RentalStatus.FREE, SkillLevel.ADVANCED),
        new Snowboard("Ride Warpig", 27.0, 154.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new Snowboard("Capita DOA", 34.0, 158.0, RentalStatus.FREE, SkillLevel.ADVANCED)
    ));

    public SnowboardDataGenerator(SnowboardRepository snowboardRepository) {
        this.snowboardRepository = snowboardRepository;
    }

    @PostConstruct
    public void generateSnowboards() {
        if (snowboardRepository.findAll().size() > 0) {
            LOGGER.debug("Snowboards already generated");
            return;
        } else {
            LOGGER.debug("Creating " + snowboardList.size() + " snowboards");
            for (Snowboard snowboard : snowboardList) {
                snowboardRepository.save(snowboard);
            }
        }
    }
}
