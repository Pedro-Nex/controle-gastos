package br.com.pedroneves.controle_gastos.dto;

import br.com.pedroneves.controle_gastos.model.TipoTransacao;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoRequest(
        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,
        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "O valor deve ter no máximo 10 dígitos inteiros e 2 casas decimais")
        BigDecimal valor,
        @NotNull(message = "A data é obrigatória") LocalDate data,
        @NotNull(message = "O tipo é obrigatório") TipoTransacao tipo,
        @NotNull(message = "A categoria é obrigatória")
        @Positive(message = "O id da categoria deve ser maior que zero")
        Long categoriaId) {
}
