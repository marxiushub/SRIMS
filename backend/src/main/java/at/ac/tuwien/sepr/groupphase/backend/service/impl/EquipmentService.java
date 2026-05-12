package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.repository.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SkiRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SnowboardBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SkiBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SnowboardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class EquipmentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final HelmetRepository helmetRepository;
    private final PoleRepository poleRepository;
    private final SkiRepository skiRepository;
    private final SkiBootRepository skiBootRepository;
    private final SnowboardRepository snowboardRepository;
    private final SnowboardBootRepository snowboardBootRepository;

    @Autowired
    public EquipmentService(
        HelmetRepository helmetRepository,
        PoleRepository poleRepository,
        SkiRepository skiRepository,
        SkiBootRepository skiBootRepository,
        SnowboardRepository snowboardRepository,
        SnowboardBootRepository snowboardBootRepository
    ) {
        this.helmetRepository = helmetRepository;
        this.poleRepository = poleRepository;
        this.skiRepository = skiRepository;
        this.skiBootRepository = skiBootRepository;
        this.snowboardRepository = snowboardRepository;
        this.snowboardBootRepository = snowboardBootRepository;
    }







}
