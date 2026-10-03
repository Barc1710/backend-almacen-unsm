package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EncargadoRequest(
        @Size(max = 20, message = "La sigla de profesión no debe exceder 20 caracteres")
        String siglaProfesion,

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no deben exceder 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no deben exceder 100 caracteres")
        String apellidos,

        @Pattern(regexp = "\\d{8}", message = "El DNI debe contener exactamente 8 dígitos")
        String dni,

        @Size(max = 100, message = "El ambiente no debe exceder 100 caracteres")
        String ambiente
) {
}
