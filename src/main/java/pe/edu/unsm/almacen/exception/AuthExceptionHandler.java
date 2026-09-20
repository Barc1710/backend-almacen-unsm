package pe.edu.unsm.almacen.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.unsm.almacen.controller.AuthController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;

@RestControllerAdvice(assignableTypes = AuthController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "El cuerpo de la solicitud debe ser un JSON válido", null));
    }
}
