package br.com.pedroneves.controle_gastos.dto;

import java.math.BigDecimal;

public record ResumoTransacoesResponse(BigDecimal receitas, BigDecimal despesas, BigDecimal saldo) {
}
