package com.visura.mapper;

import com.visura.database.entity.ItemType;
import com.visura.database.entity.RoomType;
import com.visura.dto.response.ItemTypeResponse;
import com.visura.dto.response.RoomTypeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapperConfiguration.class)
public interface CatalogMapper {

    @Mapping(target = "id", source = "type.uuid")
    @Mapping(target = "name", source = "type.name")
    @Mapping(target = "standard", source = "type.standard")
    @Mapping(target = "items", source = "items")
    RoomTypeResponse room(RoomType type, List<ItemType> items);

    @Mapping(target = "id", source = "uuid")
    ItemTypeResponse item(ItemType type);

    List<ItemTypeResponse> items(List<ItemType> types);
}
