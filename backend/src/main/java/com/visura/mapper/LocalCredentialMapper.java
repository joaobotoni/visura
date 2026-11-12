package com.visura.mapper;

import com.visura.database.entity.LocalCredential;
import com.visura.database.entity.User;
import com.visura.vo.PasswordHash;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(config = MapperConfiguration.class)
public interface LocalCredentialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "passwordUpdatedAt", source = "updatedAt")
    LocalCredential entity(User user, PasswordHash passwordHash, Instant updatedAt);
}
