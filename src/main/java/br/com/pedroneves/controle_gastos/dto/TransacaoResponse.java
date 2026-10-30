package br.com.pedroneves.controle_gastos.dto;

import br.com.pedroneves.controle_gastos.model.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        TipoTransacao tipo,
        Long categoriaId,
        String categoriaNome) {
}
