package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AsignarPermisosRequest(
        @NotNull(message = "La lista de módulos no puede ser nula")
        List<@NotNull(message = "El identificador de módulo no puede ser nulo") Integer> idsModulos
) {
}
