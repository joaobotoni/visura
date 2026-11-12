package com.visura.dto.request;

import com.visura.database.enums.DevicePlatform;
import com.visura.support.Controle;
import com.visura.vo.Cpf;
import com.visura.vo.Email;
import com.visura.vo.Name;
import com.visura.vo.NewPassword;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Dados do cadastro")
class RegisterRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @Controle
    @DisplayName("cadastro com todos os dados é válido")
    void completeRequestIsValid() {
        assertThat(validator.validate(request(Name.of("Ana Maria")))).isEmpty();
    }

    @Test
    @Controle
    @DisplayName("nome ausente é recusado na validação")
    void missingNameIsRejected() {
        assertThat(validator.validate(request(null)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("name");
    }

    private static RegisterRequest request(Name name) {
        DeviceRequest device = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14");
        return new RegisterRequest(name, Cpf.of("52998224725"), Email.of("ana@vsr.com"), NewPassword.of("Senha@Segura123"), device);
    }
}
