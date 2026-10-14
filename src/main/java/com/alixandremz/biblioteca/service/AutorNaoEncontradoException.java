package com.alixandremz.biblioteca.service;

public class AutorNaoEncontradoException extends RuntimeException {

    public AutorNaoEncontradoException(Long id) {
        super("autor não encontrado: id " + id);
    }

    public AutorNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
