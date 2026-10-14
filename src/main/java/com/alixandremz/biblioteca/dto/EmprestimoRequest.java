package com.alixandremz.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmprestimoRequest(
        @NotNull(message = "o id do livro é obrigatório")
        Long livroId,

        @NotBlank(message = "o nome do leitor é obrigatório")
        @Size(max = 120, message = "o nome do leitor deve ter no máximo 120 caracteres")
        String nomeLeitor
) {
}
