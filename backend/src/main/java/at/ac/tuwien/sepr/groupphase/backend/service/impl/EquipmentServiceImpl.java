package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation.EquipmentCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.search.EquipmentSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EquipmentMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Pole;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SkiBoot;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Snowboard;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SnowboardBoot;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.PoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.SkiBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.SkiRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.SnowboardBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.SnowboardRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.EquipmentUpdateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType.HELMET;
import static at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType.SKI;

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

    private final Map<EquipmentType, JpaRepository<? extends Equipment, Long>> repositoryMap;

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
        this.repositoryMap = Map.of(
            HELMET, helmetRepository,
            EquipmentType.POLE, poleRepository,
            SKI, skiRepository,
            EquipmentType.SKIBOOT, skiBootRepository,
            EquipmentType.SNOWBOARD, snowboardRepository,
            EquipmentType.SNOWBOARDBOOT, snowboardBootRepository
        );
    }

    @Override
    public List<EquipmentDetailDto> createEquipment(EquipmentCreationDto dto) {
        LOGGER.trace("Creation of an {}", dto.getType());


        List<EquipmentDetailDto> created = new ArrayList<>();
        JpaRepository<Equipment, Long> repo =
            (JpaRepository<Equipment, Long>) repositoryMap.get(dto.getType());

        if (repo == null) {
            throw new IllegalArgumentException("Unknown equipment type: " + dto.getType());
        }
        for (int i = 0; i < dto.getCreationNumber(); i++) {
            Equipment equipment = dto.toEntity();
            created.add(mapper.entityToDto(repo.save(equipment)));
        }
        return created;
    }

    @Override
    public void deleteEquipment(Long id) {
        LOGGER.trace("Deleting equipment with id {}", id);

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        if (id < 0) {
            throw new IllegalArgumentException("id is negative");
        }

        if (!equipmentRepository.existsById(id)) {
            throw new NotFoundException("Equipment with ID " + id + " was not found.");
        }

        equipmentRepository.deleteById(id);
    }

    @Override
    public List<EquipmentDetailDto> equipmentByType(String type) {
        LOGGER.trace("Get equipment by type: {}", type);

        EquipmentType equipmentType;
        try {
            equipmentType = EquipmentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Unknown equipment type: " + type);
        }

        JpaRepository<Equipment, Long> repo =
            (JpaRepository<Equipment, Long>) repositoryMap.get(equipmentType);

        if (repo == null) {
            throw new IllegalArgumentException("No repository found for equipment type: " + equipmentType);
        }

        List<Equipment> equipmentList = repo.findAll();
        return mapper.entityToDto(equipmentList);
    }

    @Override
    public EquipmentDetailDto equipmentById(Long id) {
        LOGGER.trace("Get equipment by id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        if (id < 0) {
            throw new IllegalArgumentException("id is negative");
        }

        Equipment equipment = equipmentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Equipment with ID " + id + " was not found."));

        return mapper.entityToDto(equipment);
    }

    @Transactional
    @Override
    public EquipmentDetailDto updateEquipment(Long id, EquipmentUpdateDto updateDto) {
        LOGGER.info("Updating equipment with id {}", id);

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        if (id < 0) {
            throw new IllegalArgumentException("id is negative");
        }

        Equipment existingEquipment = equipmentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Equipment with ID " + id + " was not found."));

        if (existingEquipment.getEquipmentType() != updateDto.getType()) {
            throw new IllegalArgumentException(
                String.format("Type mismatch: Cannot update a %s with a %s DTO.",
                    existingEquipment.getEquipmentType(), updateDto.getType())
            );
        }
        mapper.updateEntityFromDto(updateDto, existingEquipment);

        Equipment savedEquipment = equipmentRepository.save(existingEquipment);
        return mapper.entityToDto(savedEquipment);
    }

    @Override
    public List<EquipmentDetailDto> searchEquipment(EquipmentSearchDto searchDto) {
        LOGGER.info("Searching equipment with parameters: {}", searchDto);

        if (searchDto == null) {
            searchDto = new EquipmentSearchDto();
        }

        final EquipmentSearchDto equipmentSearchDto = searchDto;

        Specification<Equipment> spec = (root, query, cb) -> cb.conjunction();

        if (equipmentSearchDto.getModel() != null && !equipmentSearchDto.getModel().isBlank()) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("model")), "%" + equipmentSearchDto.getModel().toLowerCase() + "%"));
        }

        if (equipmentSearchDto.getType() != null) {
            spec = spec.and((root, query, cb) -> {
                Class<? extends Equipment> targetClass = switch (equipmentSearchDto.getType()) {
                    case SKI -> Ski.class;
                    case HELMET -> Helmet.class;
                    case POLE -> Pole.class;
                    case SKIBOOT -> SkiBoot.class;
                    case SNOWBOARD -> Snowboard.class;
                    case SNOWBOARDBOOT -> SnowboardBoot.class;
                };
                return cb.equal(root.type(), targetClass);
            });
        }

        if (equipmentSearchDto.getStatus() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("status"), equipmentSearchDto.getStatus()));
        }

        if (equipmentSearchDto.getTargetSkillLevel() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("targetSkillLevel"), equipmentSearchDto.getTargetSkillLevel()));
        }

        List<Equipment> foundEquipment = equipmentRepository.findAll(spec);

        return mapper.entityToDto(foundEquipment);
    }

}
