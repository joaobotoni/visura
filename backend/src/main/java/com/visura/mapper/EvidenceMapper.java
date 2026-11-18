package com.visura.mapper;

import com.visura.database.entity.Evidence;
import com.visura.database.entity.Item;
import com.visura.database.entity.Room;
import com.visura.dto.response.EvidenceResponse;
import com.visura.vo.Media;
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
    @Mapping(target = "contentType", source = "media.contentType")
    @Mapping(target = "size", expression = "java(media.size())")
    @Mapping(target = "hash", expression = "java(media.hash())")
    @Mapping(target = "receivedAt", source = "receivedAt")
    @Mapping(target = "description", source = "description")
    Evidence entity(Room room, Item item, String path, Media media, Instant receivedAt, String description);

    @Mapping(target = "id", source = "uuid")
    @Mapping(target = "item", source = "item.uuid")
    EvidenceResponse response(Evidence evidence);
}
