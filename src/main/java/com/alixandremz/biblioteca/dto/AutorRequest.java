package com.alixandremz.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AutorRequest(
        @NotBlank(message = "o nome do autor é obrigatório")
        @Size(max = 120, message = "o nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 60, message = "a nacionalidade deve ter no máximo 60 caracteres")
        String nacionalidade
) {
}
