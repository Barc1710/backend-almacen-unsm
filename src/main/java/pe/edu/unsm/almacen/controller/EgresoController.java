package pe.edu.unsm.almacen.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.EgresoRequest;
import pe.edu.unsm.almacen.dto.response.EgresoResponse;

@RestController
@RequestMapping("/egresos")
public class EgresoController {

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EgresoResponse>>> listar(Pageable pageable) {
        // TODO: delegar al servicio cuando se implemente el caso de uso.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EgresoResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        // TODO: delegar al servicio cuando se implemente el caso de uso.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EgresoResponse>> registrar(@RequestBody EgresoRequest request) {
        // TODO: delegar al servicio cuando se implemente el caso de uso.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
