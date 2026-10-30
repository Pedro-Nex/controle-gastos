package br.com.pedroneves.controle_gastos.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse tratarNaoEncontrado(RecursoNaoEncontradoException exception) {
        return new ErroResponse(exception.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse tratarConflito(ConflitoException exception) {
        return new ErroResponse(exception.getMessage());
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse tratarRequisicaoInvalida(RequisicaoInvalidaException exception) {
        return new ErroResponse(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse tratarValidacao(MethodArgumentNotValidException exception) {
        String mensagem = exception.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .findFirst()
                .orElse("Os dados enviados são inválidos.");
        return new ErroResponse(mensagem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse tratarFormatoInvalido(HttpMessageNotReadableException exception) {
        return new ErroResponse("O corpo da requisição está inválido ou contém valores em formato incorreto.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse tratarTipoDeParametroInvalido(MethodArgumentTypeMismatchException exception) {
        return new ErroResponse("O parâmetro '" + exception.getName() + "' possui um formato inválido.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse tratarIntegridade(DataIntegrityViolationException exception) {
        return new ErroResponse("A operação viola uma restrição de integridade dos dados.");
    }

    public record ErroResponse(String mensagem) {
    }
}
