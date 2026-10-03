package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnidadMedidaRequest(
        @NotBlank(message = "El código SUNAT es obligatorio")
        @Size(max = 10, message = "El código SUNAT no debe superar los 10 caracteres")
        String codigoSunat,

        @NotBlank(message = "El nombre de la unidad de medida es obligatorio")
        @Size(max = 100, message = "El nombre no debe superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El símbolo es obligatorio")
        @Size(max = 10, message = "El símbolo no debe superar los 10 caracteres")
        String simbolo,

        @NotNull(message = "Debe indicar si la unidad permite cantidades decimales")
        Boolean permiteDecimales
) {
}
