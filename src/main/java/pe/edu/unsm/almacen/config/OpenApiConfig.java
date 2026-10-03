package pe.edu.unsm.almacen.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.unsm.almacen.controller.AuthController;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().components(new Components().addSecuritySchemes(
                BEARER_AUTH,
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
        ));
    }

    @Bean
    public OperationCustomizer jwtSecurityCustomizer() {
        return (operation, handlerMethod) -> {
            if (!AuthController.class.isAssignableFrom(handlerMethod.getBeanType())) {
                operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
            }
            return operation;
        };
    }
}
