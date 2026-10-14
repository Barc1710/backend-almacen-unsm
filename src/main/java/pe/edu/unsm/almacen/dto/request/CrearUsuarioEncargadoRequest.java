package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearUsuarioEncargadoRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
        String usuario,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La clave debe tener entre 6 y 100 caracteres")
        String clave,

        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
        String correo,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String telefono,

        @NotNull(message = "El perfil es obligatorio")
        Integer idPerfil
) {
}
