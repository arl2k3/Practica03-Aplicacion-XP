package madstodolist.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="Usuario no autorizado para acceder a este recurso")
public class UsuarioNoAutorizadoException extends RuntimeException {
}