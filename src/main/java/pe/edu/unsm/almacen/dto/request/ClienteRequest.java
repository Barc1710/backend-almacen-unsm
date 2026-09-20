package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
        String nombre,

        @Pattern(regexp = "\\d{8}", message = "El DNI debe contener exactamente 8 dígitos")
        String dni,

        @Size(max = 50, message = "El teléfono no debe exceder 50 caracteres")
        String telefono,

        @Size(max = 50, message = "El celular no debe exceder 50 caracteres")
        String celular,

        @Email(message = "El formato de correo no es válido")
        @Size(max = 100, message = "El correo no debe exceder 100 caracteres")
        String correo,

        @Size(max = 255, message = "La dirección no debe exceder 255 caracteres")
        String direccion
) {
}
