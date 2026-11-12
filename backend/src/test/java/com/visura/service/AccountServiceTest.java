package com.visura.service;

import com.visura.database.entity.Individual;
import com.visura.database.entity.User;
import com.visura.database.enums.DevicePlatform;
import com.visura.dto.request.DeviceRequest;
import com.visura.dto.request.RegisterRequest;
import com.visura.support.Brecha;
import com.visura.support.Controle;
import com.visura.support.Users;
import com.visura.vo.Cpf;
import com.visura.vo.Email;
import com.visura.vo.Name;
import com.visura.vo.NewPassword;
import com.visura.vo.PasswordHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Conta")
class AccountServiceTest {

    private static final PasswordHash HASH = PasswordHash.of("$argon2id$v=19$m=19456,t=2,p=1$c2FsdA$aGFzaA");

    private final IndividualService individualService = mock(IndividualService.class);
    private final UserService userService = mock(UserService.class);
    private final LocalCredentialService localCredentialService = mock(LocalCredentialService.class);
    private final AccessService accessService = mock(AccessService.class);
    private final AccountService accountService = new AccountService(
            individualService, userService, localCredentialService, accessService);

    private final User user = Users.ana();

    @Test
    @Brecha
    @DisplayName("cadastro emite tokens na hora, sem confirmar a posse do e-mail ou do CPF")
    void openGrantsAccessWithoutVerification() {
        RegisterRequest request = request();
        InetAddress ip = InetAddress.getLoopbackAddress();
        Individual person = new Individual(null, request.name(), request.cpf());
        when(individualService.save(request.name(), request.cpf())).thenReturn(person);
        when(userService.save(person, request.email())).thenReturn(user);

        accountService.open(request, HASH, ip);

        InOrder order = inOrder(individualService, userService, localCredentialService, accessService);
        order.verify(individualService).save(request.name(), request.cpf());
        order.verify(userService).save(person, request.email());
        order.verify(localCredentialService).save(user, HASH);
        order.verify(accessService).grant(user, request.device(), ip);
    }

    @Controle
    @ParameterizedTest(name = "{0}.{1} transacional = {2}")
    @CsvSource({
            "AccountService,        open,     true",
            "PasswordService,       commit,   true",
            "RegisterService,       register, false",
            "ChangePasswordService, change,   false",
            "LocalCredentialService, verify,  false",
            "LocalCredentialService, replace, true"
    })
    @DisplayName("só a gravação é transacional; o cálculo de senha fica fora da transação")
    void onlyWritesAreTransactional(String service, String method, boolean transactional) throws Exception {
        Method found = Arrays.stream(Class.forName("com.visura.service." + service).getMethods())
                .filter(candidate -> candidate.getName().equals(method))
                .findFirst()
                .orElseThrow();

        assertThat(found.isAnnotationPresent(Transactional.class)).isEqualTo(transactional);
    }

    private static RegisterRequest request() {
        DeviceRequest device = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14");
        return new RegisterRequest(Name.of("Ana"), Cpf.of("52998224725"), Email.of("email.de.terceiro@vsr.com"), NewPassword.of("Senha@Segura123"), device);
    }
}
