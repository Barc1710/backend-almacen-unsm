package pe.edu.unsm.almacen.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.unsm.almacen.dto.response.JwtResponse;
import pe.edu.unsm.almacen.exception.GlobalExceptionHandler;

class AuthControllerTest {

    private final AtomicInteger loginCalls = new AtomicInteger();
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var controller = new AuthController(request -> {
            loginCalls.incrementAndGet();
            if ("incorrecta".equals(request.clave())) {
                throw new BadCredentialsException("Credenciales incorrectas");
            }
            return new JwtResponse("test-token", request.usuario(), "Ana Perez", "ADMIN", true);
        });
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "", "null", "[]"})
    void invalidBodyReturns400WithoutCallingLogin(String body) throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("message").value("El cuerpo de la solicitud debe ser un JSON válido"));
        assertEquals(0, loginCalls.get());
    }

    @Test
    void blankCredentialsStillReturn400WithoutCallingLogin() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"\",\"clave\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("success").value(false));
        assertEquals(0, loginCalls.get());
    }

    @Test
    void badCredentialsStillUseAuthenticationErrorHandler() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"ana\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value("Usuario o clave incorrectos"));
    }

    @Test
    void validBodyStillReturnsJwtResponse() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"ana\",\"clave\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data.token").value("test-token"))
                .andExpect(jsonPath("data.usuario").value("ana"))
                .andExpect(jsonPath("data.debeCambiarClave").value(true));
        assertEquals(1, loginCalls.get());
    }
}
