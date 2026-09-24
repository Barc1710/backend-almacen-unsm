package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EncargadoAlmacenRequest(
        @NotBlank(message = "El nombre del encargado de almacén es obligatorio")
        @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
        String nombre,

        Boolean esTitular,

        @Size(max = 100, message = "El cargo no debe exceder 100 caracteres")
        String cargo
) {
}
