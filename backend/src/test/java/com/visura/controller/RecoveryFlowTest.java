package com.visura.controller;

import com.visura.support.ApplicationTest;
import com.visura.support.Controle;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Recuperação de senha pela API, com banco de verdade")
class RecoveryFlowTest extends ApplicationTest {

    private static final Pattern CODE = Pattern.compile("\\b(\\d{6})\\b");
    private static final String DEVICE = """
            {"identifier":"7f1c2a8e-3b4d-4c5e-8f90-112233445566","platform":"android",
             "manufacturer":"Samsung","model":"S23","osVersion":"14"}""";

    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void clean() {
        org.mockito.Mockito.reset(mailSender);
    }

    @Test
    @Controle
    @DisplayName("o código do e-mail troca a senha, encerra as sessões e não serve duas vezes")
    void codeResetsPasswordOnce() throws Exception {
        String token = register("Gina Paiva", "19834441703", "gina.recupera@vsr.com");

        mockMvc.perform(json(post("/api/1/auth/password/forgot"), "{\"email\":\"Gina.Recupera@vsr.com\"}"))
                .andExpect(status().isNoContent());
        String code = code();

        mockMvc.perform(json(post("/api/1/auth/password/verify"), body("gina.recupera@vsr.com", other(code))))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("CredentialProblem.INVALID_RECOVERY_CODE"));
        mockMvc.perform(json(post("/api/1/auth/password/verify"), body("gina.recupera@vsr.com", code)))
                .andExpect(status().isNoContent());
        mockMvc.perform(json(post("/api/1/auth/password/reset"), reset("gina.recupera@vsr.com", code)))
                .andExpect(status().isNoContent());

        mockMvc.perform(authorized(get("/api/1/users/me"), token))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(json(post("/api/1/auth/password/reset"), reset("gina.recupera@vsr.com", code)))
                .andExpect(status().isUnprocessableContent());
        mockMvc.perform(json(post("/api/1/auth/login"),
                        "{\"email\":\"gina.recupera@vsr.com\",\"password\":\"Nova@Senha456\",\"device\":" + DEVICE + "}"))
                .andExpect(status().isOk());
    }

    @Test
    @Controle
    @DisplayName("e-mail sem conta responde igual e não envia nada")
    void unknownEmailLooksTheSame() throws Exception {
        mockMvc.perform(json(post("/api/1/auth/password/forgot"), "{\"email\":\"ninguem@vsr.com\"}"))
                .andExpect(status().isNoContent());
        verify(mailSender, never()).send(any(MimeMessagePreparator.class));

        mockMvc.perform(json(post("/api/1/auth/password/verify"), body("ninguem@vsr.com", "123456")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("CredentialProblem.INVALID_RECOVERY_CODE"));
    }

    @Test
    @Controle
    @DisplayName("o código aceita no máximo 5 tentativas, e pedir outro invalida o anterior")
    void attemptsAreLimited() throws Exception {
        register("Hugo Pires", "47661435586", "hugo.recupera@vsr.com");

        mockMvc.perform(json(post("/api/1/auth/password/forgot"), "{\"email\":\"hugo.recupera@vsr.com\"}"))
                .andExpect(status().isNoContent());
        String first = code();
        clearInvocations(mailSender);
        mockMvc.perform(json(post("/api/1/auth/password/forgot"), "{\"email\":\"hugo.recupera@vsr.com\"}"))
                .andExpect(status().isNoContent());
        String second = code();

        if (!first.equals(second)) {
            mockMvc.perform(json(post("/api/1/auth/password/verify"), body("hugo.recupera@vsr.com", first)))
                    .andExpect(status().isUnprocessableContent());
        }
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(json(post("/api/1/auth/password/verify"), body("hugo.recupera@vsr.com", other(second))));
        }
        mockMvc.perform(json(post("/api/1/auth/password/verify"), body("hugo.recupera@vsr.com", second)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @Controle
    @DisplayName("falha no envio do e-mail não muda a resposta, e o perfil mostra o CPF da conta")
    void mailFailureLooksTheSame() throws Exception {
        String token = register("Iara Lins", "27726568100", "iara.recupera@vsr.com");
        mockMvc.perform(authorized(get("/api/1/users/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("27726568100"));

        doThrow(new MailSendException("SMTP fora do ar")).when(mailSender).send(any(MimeMessagePreparator.class));
        mockMvc.perform(json(post("/api/1/auth/password/forgot"), "{\"email\":\"iara.recupera@vsr.com\"}"))
                .andExpect(status().isNoContent());
    }

    private String code() throws Exception {
        ArgumentCaptor<MimeMessagePreparator> captor = ArgumentCaptor.forClass(MimeMessagePreparator.class);
        verify(mailSender).send(captor.capture());
        MimeMessage message = new MimeMessage((Session) null);
        captor.getValue().prepare(message);
        Matcher matcher = CODE.matcher(message.getContent().toString());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private static String other(String code) {
        return String.format("%06d", (Integer.parseInt(code) + 1) % 1_000_000);
    }

    private static String body(String email, String code) {
        return "{\"email\":\"" + email + "\",\"code\":\"" + code + "\"}";
    }

    private static String reset(String email, String code) {
        return "{\"email\":\"" + email + "\",\"code\":\"" + code + "\",\"newPassword\":\"Nova@Senha456\"}";
    }
}
