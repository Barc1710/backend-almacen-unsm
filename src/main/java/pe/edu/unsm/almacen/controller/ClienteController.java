package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ClienteRequest;
import pe.edu.unsm.almacen.dto.response.ClienteResponse;
import pe.edu.unsm.almacen.service.IClienteService;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión del catálogo de clientes y dependencias solicitantes")
public class ClienteController {

    private final IClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar clientes paginados", description = "Retorna un listado paginado con filtro de búsqueda opcional por nombre, DNI o correo")
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<ClienteResponse> response = clienteService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Clientes recuperados exitosamente", response));
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar clientes activos", description = "Retorna todos los clientes activos para combos y selección")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listarActivos() {
        List<ClienteResponse> response = clienteService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Clientes activos recuperados exitosamente", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Retorna el detalle de un cliente específico")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        ClienteResponse response = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cliente encontrado exitosamente", response));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo cliente", description = "Registra un nuevo cliente con validaciones de formato")
    public ResponseEntity<ApiResponse<ClienteResponse>> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Cliente creado exitosamente", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cliente actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Borrado lógico de cliente", description = "Desactiva un cliente cambiando su estado a '0'")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        clienteService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Cliente desactivado exitosamente", null));
    }
}
