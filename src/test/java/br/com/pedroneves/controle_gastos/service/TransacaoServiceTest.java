package br.com.pedroneves.controle_gastos.service;

import br.com.pedroneves.controle_gastos.dto.ResumoTransacoesResponse;
import br.com.pedroneves.controle_gastos.model.TipoTransacao;
import br.com.pedroneves.controle_gastos.model.Transacao;
import br.com.pedroneves.controle_gastos.repository.CategoriaRepository;
import br.com.pedroneves.controle_gastos.repository.TransacaoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransacaoServiceTest {

    private final TransacaoRepository transacaoRepository = mock(TransacaoRepository.class);
    private final TransacaoService transacaoService =
            new TransacaoService(transacaoRepository, mock(CategoriaRepository.class));

    @Test
    void calculaResumoDoMesComValoresMonetariosExatos() {
        when(transacaoRepository.findByDataBetweenOrderByDataDescIdDesc(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(
                        transacao(TipoTransacao.RECEITA, "1250.40"),
                        transacao(TipoTransacao.RECEITA, "99.60"),
                        transacao(TipoTransacao.DESPESA, "350.75")));

        ResumoTransacoesResponse resumo = transacaoService.resumo(2026, 10);

        assertEquals(new BigDecimal("1350.00"), resumo.receitas());
        assertEquals(new BigDecimal("350.75"), resumo.despesas());
        assertEquals(new BigDecimal("999.25"), resumo.saldo());
        verify(transacaoRepository).findByDataBetweenOrderByDataDescIdDesc(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31));
    }

    private Transacao transacao(TipoTransacao tipo, String valor) {
        Transacao transacao = new Transacao();
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        return transacao;
    }
}
