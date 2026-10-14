package com.alixandremz.biblioteca.service;

public class LivroIndisponivelException extends RuntimeException {

    public LivroIndisponivelException(Long id) {
        super("livro indisponível (já emprestado): id " + id);
    }
}
