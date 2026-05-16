package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.HelmetDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EquipmentMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EquipmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EquipmentServiceTest {

    @Mock
    private HelmetRepository helmetRepository;
    @Mock
    private EquipmentMapper mapper;

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    private Helmet testEquipment;
    private HelmetDetailDto testEquipmentDto;

    @BeforeEach
    public void setup() {
        testEquipment = new Helmet("Test Helmet Model", 10.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER);

        testEquipmentDto = new HelmetDetailDto();
        testEquipmentDto.setModel("Test Helmet Model");
    }

    @Test
    void getEquipmentByTypeValidTypeReturnsMappedList() {
        List<Helmet> helmetList = Collections.singletonList(testEquipment);
        List<EquipmentDetailDto> dtoList = Collections.singletonList(testEquipmentDto);

        when(helmetRepository.findAll()).thenReturn(helmetList);
        when(mapper.entityToDto(anyList())).thenReturn(dtoList);

        List<EquipmentDetailDto> result = equipmentService.equipmentByType("helmet");

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Test Helmet Model", result.getFirst().getModel())

        );

        verify(helmetRepository, times(1)).findAll();
        verify(mapper, times(1)).entityToDto(anyList());
    }


    @Test
    void getEquipmentByTypeUnknownTypeThrowsNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            equipmentService.equipmentByType("invalid_type"));

        assertTrue(exception.getMessage().contains("Unknown equipment type"));
        verifyNoInteractions(mapper);
    }
}
