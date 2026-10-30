package br.com.pedroneves.controle_gastos.controller;

import br.com.pedroneves.controle_gastos.dto.ResumoTransacoesResponse;
import br.com.pedroneves.controle_gastos.dto.TransacaoRequest;
import br.com.pedroneves.controle_gastos.dto.TransacaoResponse;
import br.com.pedroneves.controle_gastos.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public List<TransacaoResponse> listar(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes) {
        return transacaoService.listar(ano, mes);
    }

    @GetMapping("/resumo")
    public ResumoTransacoesResponse resumo(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes) {
        return transacaoService.resumo(ano, mes);
    }

    @GetMapping("/{id}")
    public TransacaoResponse buscarPorId(@PathVariable Long id) {
        return transacaoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoResponse criar(@Valid @RequestBody TransacaoRequest request) {
        return transacaoService.criar(request);
    }

    @PutMapping("/{id}")
    public TransacaoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TransacaoRequest request) {
        return transacaoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        transacaoService.excluir(id);
    }
}
