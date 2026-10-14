package com.alixandremz.biblioteca.dto;

import java.time.Instant;

import com.alixandremz.biblioteca.model.Emprestimo;

public record EmprestimoResponse(
        Long id,
        Long livroId,
        String livroTitulo,
        String nomeLeitor,
        Instant emprestadoEm,
        Instant devolvidoEm
) {
    public static EmprestimoResponse de(Emprestimo emprestimo) {
        return new EmprestimoResponse(
                emprestimo.getId(),
                emprestimo.getLivro().getId(),
                emprestimo.getLivro().getTitulo(),
                emprestimo.getNomeLeitor(),
                emprestimo.getEmprestadoEm(),
                emprestimo.getDevolvidoEm()
        );
    }
}
