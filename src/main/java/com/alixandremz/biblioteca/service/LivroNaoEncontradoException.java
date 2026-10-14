package com.alixandremz.biblioteca.service;

public class LivroNaoEncontradoException extends RuntimeException {

    public LivroNaoEncontradoException(Long id) {
        super("livro não encontrado: id " + id);
    }
}
