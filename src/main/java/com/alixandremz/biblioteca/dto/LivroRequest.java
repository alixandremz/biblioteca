package com.alixandremz.biblioteca.dto;

import java.util.Set;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LivroRequest(
        @NotBlank(message = "o título é obrigatório")
        @Size(max = 180, message = "o título deve ter no máximo 180 caracteres")
        String titulo,

        @NotBlank(message = "o isbn é obrigatório")
        @Size(max = 20, message = "o isbn deve ter no máximo 20 caracteres")
        String isbn,

        @NotNull(message = "o ano de publicação é obrigatório")
        @Min(value = 1, message = "o ano de publicação deve ser positivo")
        Integer anoPublicacao,

        // ids dos autores já cadastrados
        @NotEmpty(message = "informe pelo menos um autor")
        Set<Long> autores
) {
}
