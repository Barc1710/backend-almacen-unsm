package pe.edu.unsm.almacen.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class CorsConfigTest {

    @Test
    void corsConfigurationPermiteOrigenesConfiguradosYPatrones() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(
                corsConfig,
                "allowedOrigins",
                "http://localhost:4201,http://127.0.0.1:4201,http://100.123.100.46:4201"
        );

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        assertNotNull(source);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config);
        assertTrue(Boolean.TRUE.equals(config.getAllowCredentials()));
        assertNotNull(config.getAllowedOriginPatterns());
        assertTrue(config.getAllowedOriginPatterns().contains("http://100.123.100.46:4201"));

        // Comprobación de resolución de origen
        String allowed = config.checkOrigin("http://100.123.100.46:4201");
        assertNotNull(allowed);
    }

    @Test
    void corsConfigurationPermiteComodinYCredenciales() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "*");

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("POST", "/api/v1/auth/login"));

        assertNotNull(config);
        assertTrue(Boolean.TRUE.equals(config.getAllowCredentials()));

        // Debe permitir cualquier IP (Tailscale, LAN, localhost, etc.)
        assertNotNull(config.checkOrigin("http://100.123.100.46:4201"));
        assertNotNull(config.checkOrigin("http://192.168.18.254:4201"));
        assertNotNull(config.checkOrigin("http://localhost:4200"));
    }
}
