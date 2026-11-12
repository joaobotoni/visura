package com.visura.support;

import com.visura.controller.AuthenticationController;
import com.visura.controller.UserController;
import com.visura.mapper.TokenMapper;
import com.visura.service.ChangePasswordService;
import com.visura.service.LoginService;
import com.visura.service.RefreshService;
import com.visura.service.RegisterService;
import com.visura.service.SessionService;
import com.visura.service.LogoutService;
import com.visura.service.ProfileService;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sobe a cadeia de segurança real (SecurityConfig, filtros, JWT e rate limit) com os serviços mockados.
 * Os mocks são obtidos no teste com {@code @Autowired}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(controllers = {AuthenticationController.class, UserController.class})
@TestPropertySource("classpath:security-test.properties")
@Import(WebSecurityTestConfiguration.class)
@MockitoBean(types = {
        RegisterService.class,
        LoginService.class,
        SessionService.class,
        RefreshService.class,
        ProfileService.class,
        LogoutService.class,
        ChangePasswordService.class,
        UserDetailsService.class,
        TokenMapper.class
})
public @interface WebSecurityTest {
}
