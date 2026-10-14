package com.alixandremz.biblioteca.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "emprestimos")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // muitos empréstimos para um livro
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Column(nullable = false, length = 120)
    private String nomeLeitor;

    @Column(nullable = false, updatable = false)
    private Instant emprestadoEm;

    // null enquanto o empréstimo está em aberto (livro fora)
    @Column
    private Instant devolvidoEm;

    @PrePersist
    void aoEmprestar() {
        emprestadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public String getNomeLeitor() {
        return nomeLeitor;
    }

    public void setNomeLeitor(String nomeLeitor) {
        this.nomeLeitor = nomeLeitor;
    }

    public Instant getEmprestadoEm() {
        return emprestadoEm;
    }

    public Instant getDevolvidoEm() {
        return devolvidoEm;
    }

    public void setDevolvidoEm(Instant devolvidoEm) {
        this.devolvidoEm = devolvidoEm;
    }
}
