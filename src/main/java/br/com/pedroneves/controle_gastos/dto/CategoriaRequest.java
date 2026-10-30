package br.com.pedroneves.controle_gastos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "O nome da categoria é obrigatório")
        @Size(max = 255, message = "O nome da categoria deve ter no máximo 255 caracteres")
        String nome) {
}
