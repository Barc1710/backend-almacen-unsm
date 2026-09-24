package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String apellido,

        @Size(max = 8, message = "El DNI no puede superar los 8 caracteres")
        String dni,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String telefono,

        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
        String correo,

        @NotNull(message = "El perfil es obligatorio")
        Integer idPerfil,

        @Pattern(regexp = "^[01]$", message = "El estado debe ser '0' o '1'")
        String estado
) {
}
