package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

import pe.edu.unsm.almacen.entity.TipoEgreso;

public record EgresoCreateRequest(
        @NotNull(message = "El cliente/destinatario es obligatorio")
        Integer idCliente,

        Integer idEncargado,

        @Size(max = 150, message = "El nombre del encargado libre no puede exceder los 150 caracteres")
        String nombreEncargadoLibre,

        TipoEgreso tipoEgreso,

        @NotNull(message = "El área de destino es obligatoria")
        Integer idArea,

        @NotNull(message = "El encargado de almacén es obligatorio")
        Integer idEncargadoAlmacen,

        @Size(max = 100, message = "El ambiente no puede exceder los 100 caracteres")
        String ambiente,

        @Size(max = 10, message = "El prefijo no puede exceder los 10 caracteres")
        String prefijo,

        @NotEmpty(message = "El documento debe incluir al menos una línea de detalle")
        @Size(max = 100, message = "No se pueden procesar más de 100 líneas por transacción")
        List<@NotNull(message = "La línea de detalle no puede ser nula") @Valid DetalleEgresoRequest> detalles
) {
}
