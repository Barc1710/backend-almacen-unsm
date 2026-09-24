package pe.edu.unsm.almacen.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.unsm.almacen.dto.request.LoginRequest;
import pe.edu.unsm.almacen.dto.response.JwtResponse;
import pe.edu.unsm.almacen.dto.response.ModuloResponse;
import pe.edu.unsm.almacen.exception.GlobalExceptionHandler;
import pe.edu.unsm.almacen.service.IAuthService;

class AuthControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var controller = new AuthController(new IAuthService() {
            @Override
            public JwtResponse login(LoginRequest request) {
                if ("clave123".equals(request.clave())) {
                    return new JwtResponse("token-jwt-prueba", request.usuario(), "Usuario Demo", "ADMINISTRADOR", false);
                }
                throw new BadCredentialsException("Usuario o clave incorrectos");
            }

            @Override
            public List<ModuloResponse> obtenerMisModulos(String username) {
                return List.of(new ModuloResponse(1, "ARTICULOS", "Artículos", "/articulos", "box", 1));
            }
        });

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginExitoso() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"clave\":\"clave123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token-jwt-prueba"));
    }

    @Test
    void loginCredencialesInvalidas() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"clave\":\"clave_erronea\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void misModulosDevuelveDatosDelServicio() throws Exception {
        mockMvc.perform(get("/auth/mis-modulos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].nombre").value("Artículos"));
    }
}
