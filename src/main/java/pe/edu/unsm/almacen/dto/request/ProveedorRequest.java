package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
        @Pattern(regexp = "\\d{11}", message = "El RUC debe contener exactamente 11 dígitos")
        String ruc,

        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 150, message = "La razón social no debe exceder 150 caracteres")
        String razonSocial,

        @Size(max = 50, message = "El teléfono no debe exceder 50 caracteres")
        String telefono,

        @Size(max = 50, message = "El celular no debe exceder 50 caracteres")
        String celular,

        @Email(message = "El formato de correo no es válido")
        @Size(max = 100, message = "El correo no debe exceder 100 caracteres")
        String correo,

        @Size(max = 255, message = "La dirección no debe exceder 255 caracteres")
        String direccion,

        @Size(max = 100, message = "El contacto no debe exceder 100 caracteres")
        String contacto,

        @Size(max = 50, message = "El banco no debe exceder 50 caracteres")
        String banco,

        @Size(max = 50, message = "La cuenta corriente no debe exceder 50 caracteres")
        String cuentaCorriente
) {
}
