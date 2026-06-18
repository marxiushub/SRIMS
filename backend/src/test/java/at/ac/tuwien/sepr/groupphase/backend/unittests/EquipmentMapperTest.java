package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EquipmentMapperImpl;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EquipmentMapperTest {

    private final EquipmentMapperImpl mapper = new EquipmentMapperImpl();


    private <T> T createInstance(Class<T> clazz) {
        try {
            java.lang.reflect.Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /*
    @Test
    void allMethods_withNullInputs_returnNullOrDoNothing() {
        assertAll(
            () -> assertThat(mapper.entityToDto((Equipment) null)).isNull(),
            () -> assertThat(mapper.entityToDto((List<Equipment>) null)).isNull(),
            () -> assertDoesNotThrow(() -> {
                mapper.updateHelmet(null, createInstance(Helmet.class));
                mapper.updateSki(null, createInstance(Ski.class));
                mapper.updatePole(null, createInstance(Pole.class));
                mapper.updateSkiBoot(null, createInstance(SkiBoot.class));
                mapper.updateSnowboard(null, createInstance(Snowboard.class));
                mapper.updateSnowboardBoot(null, createInstance(SnowboardBoot.class));
            })
        );
    }

    @Test
    void entityToDto_withAllSubclasses_mapsToCorrectDetailDtoType() {
        assertAll(
            () -> assertThat(mapper.entityToDto(createInstance(Ski.class))).isInstanceOf(SkiDetailDto.class),
            () -> assertThat(mapper.entityToDto(createInstance(SkiBoot.class))).isInstanceOf(SkiBootDetailDto.class),
            () -> assertThat(mapper.entityToDto(createInstance(Snowboard.class))).isInstanceOf(SnowboardDetailDto.class),
            () -> assertThat(mapper.entityToDto(createInstance(SnowboardBoot.class))).isInstanceOf(SnowboardBootDetailDto.class),
            () -> assertThat(mapper.entityToDto(createInstance(Helmet.class))).isInstanceOf(HelmetDetailDto.class),
            () -> assertThat(mapper.entityToDto(createInstance(Pole.class))).isInstanceOf(PoleDetailDto.class)
        );
    }

    @Test
    void entityToDtoList_withValidList_mapsAllItems() {
        List<Equipment> equipmentList = List.of(createInstance(Ski.class), createInstance(Snowboard.class));
        List<EquipmentDetailDto> result = mapper.entityToDto(equipmentList);

        assertAll(
            () -> assertThat(result).hasSize(2),
            () -> assertThat(result.get(0)).isInstanceOf(SkiDetailDto.class),
            () -> assertThat(result.get(1)).isInstanceOf(SnowboardDetailDto.class)
        );
    }

    @Test
    void updatePole_withPartialData_updatesOnlyProvidedFields() {
        Pole pole = createInstance(Pole.class);
        pole.setPrice(50.0);
        pole.setModel("Old Model");
        pole.setLength(110.0);

        PoleUpdateDto dto = new PoleUpdateDto();
        dto.setPrice(60.0);
        dto.setLength(115.0);

        mapper.updatePole(dto, pole);

        assertAll(
            () -> assertThat(pole.getPrice()).isEqualTo(60.0),
            () -> assertThat(pole.getLength()).isEqualTo(115.0),
            () -> assertThat(pole.getModel()).isEqualTo("Old Model")
        );
    }

    @Test
    void updateSnowboardBoot_withPartialData_updatesOnlyProvidedFields() {
        SnowboardBoot boot = createInstance(SnowboardBoot.class);
        boot.setModel("Burton");
        boot.setPrice(200.0);

        SnowboardBootUpdateDto dto = new SnowboardBootUpdateDto();
        dto.setModel("Vans");

        mapper.updateSnowboardBoot(dto, boot);

        assertAll(
            () -> assertThat(boot.getModel()).isEqualTo("Vans"),
            () -> assertThat(boot.getPrice()).isEqualTo(200.0)
        );
    }

    @Test
    void entityToDto_withUnknownSubclass_throwsIllegalArgumentException() {
        Equipment unknownEquipment = org.mockito.Mockito.mock(Equipment.class);
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mapper.entityToDto(unknownEquipment)
        );

        assertThat(exception.getMessage()).contains("Not all subclasses are supported");
    }

    This test is not applicable for the current implementation of the mapper, in my opinion we should exclude the
    mapper from jacoco as it is generated code
    @Test
    void protectedMappingMethods_withNullInputs_returnNull() {
        assertAll(
            () -> assertThat(mapper.timePeriodsToTimePeriodDto(null)).isNull(),
            () -> assertThat(mapper.timePeriodsListToTimePeriodDtoList(null)).isNull(),
            () -> assertThat(mapper.skiToSkiDetailDto(null)).isNull(),
            () -> assertThat(mapper.skiBootToSkiBootDetailDto(null)).isNull(),
            () -> assertThat(mapper.snowboardToSnowboardDetailDto(null)).isNull(),
            () -> assertThat(mapper.snowboardBootToSnowboardBootDetailDto(null)).isNull(),
            () -> assertThat(mapper.helmetToHelmetDetailDto(null)).isNull(),
            () -> assertThat(mapper.poleToPoleDetailDto(null)).isNull()
        );
    }






    @Test
    void updateMethods_withAllFieldsSet_updatesEveryField() {
        Helmet helmet = createInstance(Helmet.class);
        HelmetUpdateDto helmetDto = new HelmetUpdateDto();
        helmetDto.setPrice(100.0);
        helmetDto.setModel("Model X");
        helmetDto.setStatus(at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus.FREE);
        helmetDto.setTargetSkillLevel(at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel.ADVANCED);
        helmetDto.setSize(55.0);
        mapper.updateHelmet(helmetDto, helmet);

        Ski ski = createInstance(Ski.class);
        SkiUpdateDto skiDto = new SkiUpdateDto();
        skiDto.setPrice(100.0);
        skiDto.setModel("Model X");
        skiDto.setStatus(at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus.FREE);
        skiDto.setTargetSkillLevel(at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel.ADVANCED);
        skiDto.setLength(160.0);
        mapper.updateSki(skiDto, ski);

        SkiBoot skiBoot = createInstance(SkiBoot.class);
        SkiBootUpdateDto skiBootDto = new SkiBootUpdateDto();
        skiBootDto.setPrice(100.0);
        skiBootDto.setModel("Model X");
        skiBootDto.setStatus(at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus.FREE);
        skiBootDto.setTargetSkillLevel(at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel.ADVANCED);
        skiBootDto.setSoleLengthMm(310.0);
        mapper.updateSkiBoot(skiBootDto, skiBoot);

        Snowboard snowboard = createInstance(Snowboard.class);
        SnowboardUpdateDto snowboardDto = new SnowboardUpdateDto();
        snowboardDto.setPrice(100.0);
        snowboardDto.setModel("Model X");
        snowboardDto.setStatus(at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus.FREE);
        snowboardDto.setTargetSkillLevel(at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel.ADVANCED);
        snowboardDto.setLength(155.0);
        mapper.updateSnowboard(snowboardDto, snowboard);

        SnowboardBoot snowboardBoot = createInstance(SnowboardBoot.class);
        SnowboardBootUpdateDto snowboardBootDto = new SnowboardBootUpdateDto();
        snowboardBootDto.setPrice(100.0);
        snowboardBootDto.setModel("Model X");
        snowboardBootDto.setStatus(at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus.FREE);
        snowboardBootDto.setTargetSkillLevel(at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel.ADVANCED);
        mapper.updateSnowboardBoot(snowboardBootDto, snowboardBoot);

        Pole pole = createInstance(Pole.class);
        PoleUpdateDto poleDto = new PoleUpdateDto();
        poleDto.setPrice(100.0);
        poleDto.setModel("Model X");
        poleDto.setStatus(at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus.FREE);
        poleDto.setTargetSkillLevel(at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel.ADVANCED);
        poleDto.setLength(120.0);
        mapper.updatePole(poleDto, pole);

        assertAll(
            () -> assertThat(helmet.getModel()).isEqualTo("Model X"),
            () -> assertThat(ski.getLength()).isEqualTo(160.0),
            () -> assertThat(skiBoot.getSoleLengthMm()).isEqualTo(310.0),
            () -> assertThat(snowboard.getLength()).isEqualTo(155.0),
            () -> assertThat(pole.getLength()).isEqualTo(120.0)
        );
    }

     */


    @Test
    void updateEntityFromDto_withEachMatchingType_dispatchesToCorrectUpdate() {
        // HELMET
        Helmet helmet = createInstance(Helmet.class);
        helmet.setPrice(10.0);
        HelmetUpdateDto helmetDto = new HelmetUpdateDto();
        helmetDto.setPrice(99.0);
        mapper.updateEntityFromDto(helmetDto, helmet);

        // POLE
        Pole pole = createInstance(Pole.class);
        pole.setPrice(10.0);
        PoleUpdateDto poleDto = new PoleUpdateDto();
        poleDto.setPrice(99.0);
        mapper.updateEntityFromDto(poleDto, pole);

        // SKI
        Ski ski = createInstance(Ski.class);
        ski.setPrice(10.0);
        SkiUpdateDto skiDto = new SkiUpdateDto();
        skiDto.setPrice(99.0);
        mapper.updateEntityFromDto(skiDto, ski);

        // SKIBOOT
        SkiBoot skiBoot = createInstance(SkiBoot.class);
        skiBoot.setPrice(10.0);
        SkiBootUpdateDto skiBootDto = new SkiBootUpdateDto();
        skiBootDto.setPrice(99.0);
        mapper.updateEntityFromDto(skiBootDto, skiBoot);

        // SNOWBOARD
        Snowboard snowboard = createInstance(Snowboard.class);
        snowboard.setPrice(10.0);
        SnowboardUpdateDto snowboardDto = new SnowboardUpdateDto();
        snowboardDto.setPrice(99.0);
        mapper.updateEntityFromDto(snowboardDto, snowboard);

        // SNOWBOARDBOOT
        SnowboardBoot snowboardBoot = createInstance(SnowboardBoot.class);
        snowboardBoot.setPrice(10.0);
        SnowboardBootUpdateDto sbBootDto = new SnowboardBootUpdateDto();
        sbBootDto.setPrice(99.0);
        mapper.updateEntityFromDto(sbBootDto, snowboardBoot);

        assertAll(
            () -> assertThat(helmet.getPrice()).isEqualTo(99.0),
            () -> assertThat(pole.getPrice()).isEqualTo(99.0),
            () -> assertThat(ski.getPrice()).isEqualTo(99.0),
            () -> assertThat(skiBoot.getPrice()).isEqualTo(99.0),
            () -> assertThat(snowboard.getPrice()).isEqualTo(99.0),
            () -> assertThat(snowboardBoot.getPrice()).isEqualTo(99.0)
        );
    }

    @Test
    void updateEntityFromDto_withNullInputs_doesNothing() {
        assertDoesNotThrow(() -> {
            mapper.updateEntityFromDto(null, createInstance(Helmet.class));
            mapper.updateEntityFromDto(new HelmetUpdateDto(), null);
        });
    }

    @Test
    void updateEntityFromDto_withTypeMismatch_fallsThroughToDefault() {
        Ski ski = createInstance(Ski.class);
        ski.setPrice(10.0);
        HelmetUpdateDto helmetDto = new HelmetUpdateDto();
        helmetDto.setPrice(99.0);

        assertDoesNotThrow(() -> mapper.updateEntityFromDto(helmetDto, ski));

        assertThat(ski.getPrice()).isEqualTo(10.0);
    }

    @Test
    void updateEntityFromDto_withMismatchedEntities_guardsEvaluateFalse() {
        // DTO-Type with wrong entity -> sets guard false
        Equipment wrongEntity = createInstance(Helmet.class);

        assertDoesNotThrow(() -> {
            mapper.updateEntityFromDto(new PoleUpdateDto(), wrongEntity);
            mapper.updateEntityFromDto(new SkiUpdateDto(), wrongEntity);
            mapper.updateEntityFromDto(new SnowboardBootUpdateDto(), wrongEntity);
            mapper.updateEntityFromDto(new SkiBootUpdateDto(), wrongEntity);
            mapper.updateEntityFromDto(new SnowboardUpdateDto(), wrongEntity);
        });

        // wrongEntity is helmet, no case -> default
        assertThat(((Helmet) wrongEntity).getSize()).isEqualTo(0.0);
    }
}