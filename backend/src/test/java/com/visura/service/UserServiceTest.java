package com.visura.service;

import com.visura.database.entity.Individual;
import com.visura.database.entity.User;
import com.visura.database.repository.UserRepository;
import com.visura.exception.custom.UserException;
import com.visura.exception.enums.problem.UserProblem;
import com.visura.mapper.UserMapper;
import com.visura.support.Controle;
import com.visura.support.Users;
import com.visura.vo.Cpf;
import com.visura.vo.Email;
import com.visura.vo.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Usuários")
class UserServiceTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final UserService userService = new UserService(repository, Mappers.getMapper(UserMapper.class));

    private final User user = User.builder().id(1).uuid(Users.UUID).email(Email.of(Users.EMAIL))
            .person(new Individual(null, Name.of("Ana"), Cpf.of("52998224725"))).build();

    @BeforeEach
    void storedUser() {
        when(repository.findWithPersonByUuid(any())).thenReturn(Optional.empty());
        when(repository.findWithPersonByUuid(Users.UUID)).thenReturn(Optional.of(user));
    }

    @Test
    @Controle
    @DisplayName("usuário é encontrado junto com a pessoa")
    void userIsFoundWithPerson() {
        assertThat(userService.find(Users.UUID).getPerson().getName()).isEqualTo("Ana");
    }

    @Test
    @Controle
    @DisplayName("busca com a pessoa por UUID desconhecido é recusada")
    void unknownUserWithPersonIsRejected() {
        assertThatThrownBy(() -> userService.find(UUID.randomUUID()))
                .isInstanceOf(UserException.class)
                .extracting("problem").isEqualTo(UserProblem.NOT_FOUND);
    }
}
