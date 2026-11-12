package com.visura.mapper;

import com.visura.database.entity.Evidence;
import com.visura.database.entity.Item;
import com.visura.database.entity.Room;
import com.visura.dto.response.EvidenceResponse;
import com.visura.vo.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(config = MapperConfiguration.class)
public interface EvidenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "room", source = "room")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "path", source = "path")
    @Mapping(target = "contentType", source = "photo.contentType")
    @Mapping(target = "size", expression = "java(photo.size())")
    @Mapping(target = "hash", expression = "java(photo.hash())")
    @Mapping(target = "receivedAt", source = "receivedAt")
    @Mapping(target = "description", source = "description")
    Evidence entity(Room room, Item item, String path, Photo photo, Instant receivedAt, String description);

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "item", source = "item.uuid")
    EvidenceResponse response(Evidence evidence);
}
