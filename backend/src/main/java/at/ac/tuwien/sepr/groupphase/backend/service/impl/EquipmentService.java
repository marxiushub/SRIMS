package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SkiRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SnowboardBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SkiBootRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SnowboardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Map;

@Service
public class EquipmentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Map<EquipmentType, JpaRepository<? extends Equipment, Long>> repositoryMap;

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


        this.repositoryMap = Map.of(
            EquipmentType.HELMET, helmetRepository,
            EquipmentType.POLE, poleRepository,
            EquipmentType.SKI, skiRepository,
            EquipmentType.SKIBOOT, skiBootRepository,
            EquipmentType.SNOWBOARD, snowboardRepository,
            EquipmentType.SNOWBOARDBOOT, snowboardBootRepository
        );
    }


    public Equipment createEquipment(EquipmentCreationDto dto) {
        Equipment equipment = dto.toEntity();
        JpaRepository<Equipment, Long> repo =
            (JpaRepository<Equipment, Long>) repositoryMap.get(dto.getType());

        if (repo == null) {
            throw new IllegalArgumentException("Unknown equipment type: " + dto.getType());
        }

        return repo.save(equipment);
    }

    public void deleteEquipment(EquipmentType type, Long id) {
        LOGGER.info("Deleting equipment of type {} with id {}", type, id);

        @SuppressWarnings("unchecked")
        JpaRepository<Equipment, Long> repo =
            (JpaRepository<Equipment, Long>) repositoryMap.get(type);

        if (repo == null) {
            throw new IllegalArgumentException("Unknown equipment type: " + type);
        }


        if (!repo.existsById(id)) {
            throw new NotFoundException("Equipment type: " + type + " with ID " + id + " was not found.");
        }

        repo.deleteById(id);
    }







}
