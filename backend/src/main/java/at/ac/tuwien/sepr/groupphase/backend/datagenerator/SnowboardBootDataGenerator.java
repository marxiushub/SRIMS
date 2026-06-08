package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SnowboardBoot;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.SnowboardBootRepository;
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
public class SnowboardBootDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SnowboardBootRepository snowboardBootRepository;
    private final List<SnowboardBoot> snowboardBootList = new ArrayList<>(Arrays.asList(
        new SnowboardBoot("Burton Moto BOA", 18.0, "BOA", RentalStatus.FREE, SkillLevel.BEGINNER),
        new SnowboardBoot("Nitro Sentinel TLS", 16.0, "TLS", RentalStatus.FREE, SkillLevel.BEGINNER),
        new SnowboardBoot("Salomon Launch Lace", 17.0, "Laces", RentalStatus.FREE, SkillLevel.BEGINNER),
        new SnowboardBoot("Ride Anthem BOA", 21.0, "BOA", RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SnowboardBoot("K2 Raider BOA", 20.0, "BOA", RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SnowboardBoot("ThirtyTwo STW Double BOA", 23.0, "Double BOA", RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SnowboardBoot("Vans Hi-Standard OG", 22.0, "Laces", RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SnowboardBoot("DC Control BOA", 25.0, "BOA", RentalStatus.FREE, SkillLevel.ADVANCED),
        new SnowboardBoot("Burton Ion BOA", 35.0, "BOA", RentalStatus.FREE, SkillLevel.ADVANCED),
        new SnowboardBoot("Nitro Team TLS", 26.0, "TLS", RentalStatus.FREE, SkillLevel.ADVANCED),
        new SnowboardBoot("Salomon Dialogue Dual BOA", 28.0, "Double BOA", RentalStatus.FREE, SkillLevel.ADVANCED),
        new SnowboardBoot("Ride Lasso Pro", 30.0, "BOA", RentalStatus.FREE, SkillLevel.ADVANCED),
        new SnowboardBoot("K2 Maysis", 31.0, "BOA", RentalStatus.FREE, SkillLevel.ADVANCED),
        new SnowboardBoot("ThirtyTwo TM-2", 27.0, "Laces", RentalStatus.FREE, SkillLevel.INTERMEDIATE),
        new SnowboardBoot("Vans Aura Pro", 24.0, "BOA", RentalStatus.FREE, SkillLevel.INTERMEDIATE)
    ));

    public SnowboardBootDataGenerator(SnowboardBootRepository snowboardBootRepository) {
        this.snowboardBootRepository = snowboardBootRepository;
    }

    @PostConstruct
    public void generateSnowboards() {
        if (snowboardBootRepository.findAll().size() > 0) {
            LOGGER.debug("Snowboardboots already generated");
            return;
        } else {
            LOGGER.debug("Creating " + snowboardBootList.size() + " snowboardboots");
            for (SnowboardBoot snowboardBoot : snowboardBootList) {
                snowboardBootRepository.save(snowboardBoot);
            }
        }
    }
}
