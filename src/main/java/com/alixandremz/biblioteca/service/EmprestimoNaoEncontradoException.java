package com.alixandremz.biblioteca.service;

public class EmprestimoNaoEncontradoException extends RuntimeException {

    public EmprestimoNaoEncontradoException(Long id) {
        super("empréstimo não encontrado: id " + id);
    }
}
