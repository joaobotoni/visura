package com.visura.mapper;

import com.visura.database.entity.Individual;
import com.visura.database.entity.User;
import com.visura.dto.response.UserResponse;
import com.visura.support.Controle;
import com.visura.support.Users;
import com.visura.vo.Cpf;
import com.visura.vo.Email;
import com.visura.vo.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Resposta de usuário")
class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    @Controle
    @DisplayName("a resposta expõe o UUID público, não o id sequencial do banco")
    void responseExposesPublicUuid() {
        UUID uuid = UUID.randomUUID();
        User user = User.builder().id(42).uuid(uuid).email(Email.of(Users.EMAIL)).person(new Individual(null, Name.of("Ana"), Cpf.of("52998224725"))).build();

        UserResponse response = userMapper.response(user);

        assertThat(response.id()).isEqualTo(uuid);
    }
}
