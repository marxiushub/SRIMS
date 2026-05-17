package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.HelmetDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.HelmetUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.PoleDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.PoleUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiBootDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiBootUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SnowboardBootDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SnowboardBootUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SnowboardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SnowboardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Pole;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SkiBoot;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Snowboard;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SnowboardBoot;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

import java.util.List;

/**
 * Mapper class responsible for converting between {@link Equipment} entities and {@link EquipmentDetailDto} data transfer objects.
 */
@Mapper(subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION, componentModel = "spring")
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

    default void updateEntityFromDto(EquipmentUpdateDto dto, @MappingTarget Equipment entity) {
        if (dto == null || entity == null) {
            return;
        }

        switch (dto) {
            case HelmetUpdateDto helmetDto when entity instanceof Helmet helmetEntity ->
                updateHelmet(helmetDto, helmetEntity);
            case PoleUpdateDto poleDto when entity instanceof Pole poleEntity -> updatePole(poleDto, poleEntity);
            case SkiUpdateDto skiDto when entity instanceof Ski skiEntity -> updateSki(skiDto, skiEntity);
            case SnowboardBootUpdateDto sbBootDto when entity instanceof SnowboardBoot sbBootEntity ->
                updateSnowboardBoot(sbBootDto, sbBootEntity);
            case SkiBootUpdateDto skiBootDto when entity instanceof SkiBoot skiBootEntity ->
                updateSkiBoot(skiBootDto, skiBootEntity);
            case SnowboardUpdateDto snowboardDto when entity instanceof Snowboard snowboardEntity ->
                updateSnowboard(snowboardDto, snowboardEntity);
            default -> {
            }
        }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateHelmet(HelmetUpdateDto dto, @MappingTarget Helmet entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePole(PoleUpdateDto dto, @MappingTarget Pole entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSki(SkiUpdateDto dto, @MappingTarget Ski entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSnowboardBoot(SnowboardBootUpdateDto dto, @MappingTarget SnowboardBoot entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSkiBoot(SkiBootUpdateDto dto, @MappingTarget SkiBoot entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSnowboard(SnowboardUpdateDto dto, @MappingTarget Snowboard entity);
}
