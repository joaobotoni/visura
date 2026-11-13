package com.visura.mapper;

import com.visura.database.entity.Item;
import com.visura.database.entity.ItemType;
import com.visura.database.entity.Room;
import com.visura.dto.request.ItemEditRequest;
import com.visura.dto.request.ItemRequest;
import com.visura.dto.response.ItemResponse;
import com.visura.lib.Texts;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfiguration.class)
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "room", source = "room")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "condition", source = "request.condition")
    @Mapping(target = "material", source = "request.material")
    @Mapping(target = "color", source = "request.color")
    @Mapping(target = "notes", source = "request.notes")
    Item entity(Room room, ItemType type, ItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "condition", ignore = true)
    @Mapping(target = "material", ignore = true)
    @Mapping(target = "color", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "room", source = "room")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "name", source = "type.name")
    Item standard(Room room, ItemType type);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    void amend(ItemEditRequest request, @MappingTarget Item item);

    /** Na edição, campo opcional enviado vazio é apagado; ausente ({@code null}) fica como estava. */
    @AfterMapping
    default void erase(ItemEditRequest request, @MappingTarget Item item) {
        item.setDescription(Texts.stored(item.getDescription()));
        item.setMaterial(Texts.stored(item.getMaterial()));
        item.setColor(Texts.stored(item.getColor()));
        item.setNotes(Texts.stored(item.getNotes()));
    }

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "type", source = "type.uuid")
    @Mapping(target = "origin", source = "origin.uuid")
    ItemResponse response(Item item);
}
