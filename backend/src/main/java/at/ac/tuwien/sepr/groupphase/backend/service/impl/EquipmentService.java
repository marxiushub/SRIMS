package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EquipmentMapper;
import at.ac.tuwien.sepr.groupphase.backend.repository.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SkiBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SkiRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SnowboardBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SnowboardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class EquipmentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EquipmentRepository equipmentRepository;
    private final HelmetRepository helmetRepository;
    private final PoleRepository poleRepository;
    private final SkiRepository skiRepository;
    private final SkiBootRepository skiBootRepository;
    private final SnowboardRepository snowboardRepository;
    private final SnowboardBootRepository snowboardBootRepository;
    private final EquipmentMapper mapper;

    @Autowired
    public EquipmentService(
        EquipmentRepository equipmentRepository,
        HelmetRepository helmetRepository,
        PoleRepository poleRepository,
        SkiRepository skiRepository,
        SkiBootRepository skiBootRepository,
        SnowboardRepository snowboardRepository,
        SnowboardBootRepository snowboardBootRepository,
        EquipmentMapper mapper
    ) {
        this.equipmentRepository = equipmentRepository;
        this.helmetRepository = helmetRepository;
        this.poleRepository = poleRepository;
        this.skiRepository = skiRepository;
        this.skiBootRepository = skiBootRepository;
        this.snowboardRepository = snowboardRepository;
        this.snowboardBootRepository = snowboardBootRepository;
        this.mapper = mapper;
    }

    public List<EquipmentDetailDto> allEquipment() {
        LOGGER.trace("Get all equipment");
        return mapper.entityToDto(equipmentRepository.findAll());
    }

}
