package br.com.pedroneves.controle_gastos.service;

import br.com.pedroneves.controle_gastos.dto.ResumoTransacoesResponse;
import br.com.pedroneves.controle_gastos.dto.TransacaoRequest;
import br.com.pedroneves.controle_gastos.dto.TransacaoResponse;
import br.com.pedroneves.controle_gastos.exception.RecursoNaoEncontradoException;
import br.com.pedroneves.controle_gastos.exception.RequisicaoInvalidaException;
import br.com.pedroneves.controle_gastos.model.Categoria;
import br.com.pedroneves.controle_gastos.model.TipoTransacao;
import br.com.pedroneves.controle_gastos.model.Transacao;
import br.com.pedroneves.controle_gastos.repository.CategoriaRepository;
import br.com.pedroneves.controle_gastos.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoService(
            TransacaoRepository transacaoRepository,
            CategoriaRepository categoriaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listar(Integer ano, Integer mes) {
        List<Transacao> transacoes = buscarPorPeriodo(ano, mes);
        return transacoes.stream().map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public ResumoTransacoesResponse resumo(Integer ano, Integer mes) {
        BigDecimal receitas = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO;

        for (Transacao transacao : buscarPorPeriodo(ano, mes)) {
            if (transacao.getTipo() == TipoTransacao.RECEITA) {
                receitas = receitas.add(transacao.getValor());
            } else {
                despesas = despesas.add(transacao.getValor());
            }
        }

        return new ResumoTransacoesResponse(receitas, despesas, receitas.subtract(despesas));
    }

    @Transactional(readOnly = true)
    public TransacaoResponse buscarPorId(Long id) {
        return paraResponse(encontrarTransacao(id));
    }

    @Transactional
    public TransacaoResponse criar(TransacaoRequest request) {
        Transacao transacao = new Transacao();
        aplicarRequest(transacao, request);
        return paraResponse(transacaoRepository.save(transacao));
    }

    @Transactional
    public TransacaoResponse atualizar(Long id, TransacaoRequest request) {
        Transacao transacao = encontrarTransacao(id);
        aplicarRequest(transacao, request);
        return paraResponse(transacao);
    }

    @Transactional
    public void excluir(Long id) {
        Transacao transacao = encontrarTransacao(id);
        transacaoRepository.delete(transacao);
    }

    private List<Transacao> buscarPorPeriodo(Integer ano, Integer mes) {
        if (ano == null && mes == null) {
            return transacaoRepository.findAllByOrderByDataDescIdDesc();
        }
        if (ano == null || mes == null) {
            throw new RequisicaoInvalidaException("Informe ano e mês juntos para filtrar as transações.");
        }

        try {
            YearMonth periodo = YearMonth.of(ano, mes);
            LocalDate inicio = periodo.atDay(1);
            LocalDate fim = periodo.atEndOfMonth();
            return transacaoRepository.findByDataBetweenOrderByDataDescIdDesc(inicio, fim);
        } catch (DateTimeException exception) {
            throw new RequisicaoInvalidaException("O ano ou mês informado é inválido.");
        }
    }

    private Transacao encontrarTransacao(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Transação de id " + id + " não foi encontrada."));
    }

    private void aplicarRequest(Transacao transacao, TransacaoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria de id " + request.categoriaId() + " não foi encontrada."));

        transacao.setDescricao(request.descricao().trim());
        transacao.setValor(request.valor());
        transacao.setData(request.data());
        transacao.setTipo(request.tipo());
        transacao.setCategoria(categoria);
    }

    private TransacaoResponse paraResponse(Transacao transacao) {
        Categoria categoria = transacao.getCategoria();
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getData(),
                transacao.getTipo(),
                categoria == null ? null : categoria.getId(),
                categoria == null ? null : categoria.getNome());
    }
}
