package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.HelmetDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.PoleDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiBootDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SnowboardBootDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SnowboardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Pole;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SkiBoot;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Snowboard;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SnowboardBoot;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

import java.util.List;

/**
 * Mapper class responsible for converting between {@link Equipment} entities and {@link EquipmentDetailDto} data transfer objects.
 */
@Mapper(subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface EquipmentMapper {

    /**
     * Converts an {@link Equipment} entity to an {@link EquipmentDetailDto}.
     *
     * @param equipment the {@link Equipment} entity to be converted
     * @return the corresponding {@link EquipmentDetailDto} containing the data from the provided entity
     */
    @SubclassMapping(source = Ski.class, target = SkiDetailDto.class)
    @SubclassMapping(source = SkiBoot.class, target = SkiBootDetailDto.class)
    @SubclassMapping(source = Snowboard.class, target = SnowboardDetailDto.class)
    @SubclassMapping(source = SnowboardBoot.class, target = SnowboardBootDetailDto.class)
    @SubclassMapping(source = Helmet.class, target = HelmetDetailDto.class)
    @SubclassMapping(source = Pole.class, target = PoleDetailDto.class)
    EquipmentDetailDto entityToDto(Equipment equipment);

    /**
     * Converts a list of {@link Equipment} entities to a list of {@link EquipmentDetailDto} objects.
     *
     * @param equipment the list of {@link Equipment} entities to be converted
     * @return a list of {@link EquipmentDetailDto} objects corresponding to the provided entities
     */
    List<EquipmentDetailDto> entityToDto(List<Equipment> equipment);
}
