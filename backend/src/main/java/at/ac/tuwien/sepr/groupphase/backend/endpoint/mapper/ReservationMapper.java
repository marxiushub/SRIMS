package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.ReservationRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {EquipmentMapper.class})
public abstract class ReservationMapper {

    @Autowired
    protected EquipmentMapper equipmentMapper;

    @Mapping(source = "customerProfile.id", target = "customerProfileId")
    @Mapping(source = "customerProfile.profileName", target = "customerName")
    public abstract ReservationDetailDto entityToDetailDto(Reservation reservation);

    public EquipmentDetailDto relationToEquipmentDto(ReservationRelation relation) {
        if (relation == null || relation.getEquipment() == null) {
            return null;
        }

        return equipmentMapper.entityToDto(relation.getEquipment());
    }
}
