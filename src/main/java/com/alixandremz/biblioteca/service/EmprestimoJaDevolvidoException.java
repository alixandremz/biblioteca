package com.alixandremz.biblioteca.service;

public class EmprestimoJaDevolvidoException extends RuntimeException {

    public EmprestimoJaDevolvidoException(Long id) {
        super("este empréstimo já foi devolvido: id " + id);
    }
}
