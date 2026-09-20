package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarcaRequest(
        @NotBlank(message = "El nombre de la marca es obligatorio")
        @Size(max = 100, message = "El nombre de la marca no debe exceder 100 caracteres")
        String nombre
) {
}
