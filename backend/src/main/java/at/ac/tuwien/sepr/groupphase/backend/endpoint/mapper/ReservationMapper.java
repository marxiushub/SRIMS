package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.ReservationRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EquipmentMapper.class})
public interface ReservationMapper {
    @Mapping(source = "customerProfile.name", target = "customerName")
    ReservationDetailDto entityToDetailDto(Reservation reservation);

    @Mapping(source = ".", target = "equipment")
    EquipmentDetailDto relationToEquipmentDto(ReservationRelation relation);
}
