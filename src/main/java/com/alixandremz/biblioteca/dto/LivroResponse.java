package com.alixandremz.biblioteca.dto;

import java.util.List;

import com.alixandremz.biblioteca.model.Livro;

public record LivroResponse(
        Long id,
        String titulo,
        String isbn,
        Integer anoPublicacao,
        boolean disponivel,
        List<AutorResponse> autores
) {
    public static LivroResponse de(Livro livro) {
        return new LivroResponse(
                livro.getId(),
                livro.getTitulo(),
                livro.getIsbn(),
                livro.getAnoPublicacao(),
                livro.isDisponivel(),
                livro.getAutores().stream().map(AutorResponse::de).toList()
        );
    }
}
