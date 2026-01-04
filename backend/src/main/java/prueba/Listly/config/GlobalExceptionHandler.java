package prueba.Listly.config;

import com.mongodb.MongoException;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.MethodArgumentNotValidException;
 import org.springframework.web.servlet.NoHandlerFoundException;
 import org.springframework.web.servlet.resource.NoResourceFoundException;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RestControllerAdvice;
 import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst().map(e -> e.getDefaultMessage()).orElse("Solicitud inválida");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorPayload("validation_error", message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return ResponseEntity.status(status).body(new ErrorPayload("service_error", message));
    }

    @ExceptionHandler(MongoException.class)
    public ResponseEntity<Object> handleMongo(MongoException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorPayload("database_error", ex.getMessage()));
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<Object> handleNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorPayload("not_found", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorPayload("internal_error", ex.getMessage()));
    }

    public record ErrorPayload(String error, String message) { }
}
