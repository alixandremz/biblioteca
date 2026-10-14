package com.alixandremz.biblioteca.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alixandremz.biblioteca.dto.EmprestimoRequest;
import com.alixandremz.biblioteca.dto.EmprestimoResponse;
import com.alixandremz.biblioteca.model.Emprestimo;
import com.alixandremz.biblioteca.model.Livro;
import com.alixandremz.biblioteca.repository.EmprestimoRepository;
import com.alixandremz.biblioteca.repository.LivroRepository;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepositorio;
    private final LivroRepository livroRepositorio;

    public EmprestimoService(EmprestimoRepository emprestimoRepositorio, LivroRepository livroRepositorio) {
        this.emprestimoRepositorio = emprestimoRepositorio;
        this.livroRepositorio = livroRepositorio;
    }

    @Transactional
    public EmprestimoResponse emprestar(EmprestimoRequest pedido) {
        // carrega o livro com trava de escrita: dois empréstimos simultâneos
        // do mesmo livro não conseguem passar pela verificação ao mesmo tempo
        Livro livro = livroRepositorio.findComBloqueioPorId(pedido.livroId())
                .orElseThrow(() -> new LivroNaoEncontradoException(pedido.livroId()));

        if (!livro.isDisponivel()) {
            throw new LivroIndisponivelException(livro.getId());
        }
        livro.setDisponivel(false);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setNomeLeitor(pedido.nomeLeitor().trim());
        return EmprestimoResponse.de(emprestimoRepositorio.save(emprestimo));
    }

    @Transactional
    public EmprestimoResponse devolver(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepositorio.findById(emprestimoId)
                .orElseThrow(() -> new EmprestimoNaoEncontradoException(emprestimoId));

        if (emprestimo.getDevolvidoEm() != null) {
            throw new EmprestimoJaDevolvidoException(emprestimoId);
        }
        emprestimo.setDevolvidoEm(Instant.now());

        Livro livro = livroRepositorio.findComBloqueioPorId(emprestimo.getLivro().getId())
                .orElseThrow(() -> new LivroNaoEncontradoException(emprestimo.getLivro().getId()));
        livro.setDisponivel(true);

        return EmprestimoResponse.de(emprestimo);
    }
}
