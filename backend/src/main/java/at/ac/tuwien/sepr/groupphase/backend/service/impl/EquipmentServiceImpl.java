package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EquipmentMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SkiBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SkiRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SnowboardBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SnowboardRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link EquipmentService} for handling equipment-related operations.
 */
@Service
public class EquipmentServiceImpl implements EquipmentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EquipmentRepository equipmentRepository;
    private final HelmetRepository helmetRepository;
    private final PoleRepository poleRepository;
    private final SkiRepository skiRepository;
    private final SkiBootRepository skiBootRepository;
    private final SnowboardRepository snowboardRepository;
    private final SnowboardBootRepository snowboardBootRepository;
    private final EquipmentMapper mapper;

    /**
     * Constructor for EquipmentService. Initializes the service with the necessary repositories and mapper.
     *
     * @param equipmentRepository     the repository for managing equipment entities
     * @param helmetRepository        the repository for managing helmet entities
     * @param poleRepository          the repository for managing pole entities
     * @param skiRepository           the repository for managing ski entities
     * @param skiBootRepository       the repository for managing ski boot entities
     * @param snowboardRepository     the repository for managing snowboard entities
     * @param snowboardBootRepository the repository for managing snowboard boot entities
     * @param mapper                  the mapper for converting between entities and DTOs
     */
    @Autowired
    public EquipmentServiceImpl(
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

    public List<EquipmentDetailDto> equipmentByType(String type) {
        LOGGER.trace("Get equipment by type: {}", type);

        EquipmentType equipmentType;
        try {
            equipmentType = EquipmentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Unknown equipment type: " + type);
        }

        List<? extends Equipment> equipmentList = switch (equipmentType) {
            case HELMET -> helmetRepository.findAll();
            case POLE -> poleRepository.findAll();
            case SKI -> skiRepository.findAll();
            case SKIBOOT -> skiBootRepository.findAll();
            case SNOWBOARD -> snowboardRepository.findAll();
            case SNOWBOARDBOOT -> snowboardBootRepository.findAll();
        };
        return mapper.entityToDto(new ArrayList<>(equipmentList));
    }

}
