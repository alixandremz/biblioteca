package com.alixandremz.biblioteca.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alixandremz.biblioteca.dto.EmprestimoResponse;
import com.alixandremz.biblioteca.dto.LivroRequest;
import com.alixandremz.biblioteca.dto.LivroResponse;
import com.alixandremz.biblioteca.dto.PaginaResponse;
import com.alixandremz.biblioteca.model.Autor;
import com.alixandremz.biblioteca.model.Livro;
import com.alixandremz.biblioteca.repository.AutorRepository;
import com.alixandremz.biblioteca.repository.EmprestimoRepository;
import com.alixandremz.biblioteca.repository.LivroRepository;

@Service
public class LivroService {

    private final LivroRepository repositorio;
    private final AutorRepository autorRepositorio;
    private final EmprestimoRepository emprestimoRepositorio;

    public LivroService(LivroRepository repositorio, AutorRepository autorRepositorio,
            EmprestimoRepository emprestimoRepositorio) {
        this.repositorio = repositorio;
        this.autorRepositorio = autorRepositorio;
        this.emprestimoRepositorio = emprestimoRepositorio;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<LivroResponse> listar(String titulo, Pageable pageable) {
        Page<Livro> pagina = (titulo == null || titulo.isBlank())
                ? repositorio.findAll(pageable)
                : repositorio.findByTituloContainingIgnoreCase(titulo.trim(), pageable);
        return PaginaResponse.de(pagina.map(LivroResponse::de));
    }

    @Transactional(readOnly = true)
    public LivroResponse buscar(Long id) {
        return LivroResponse.de(obter(id));
    }

    @Transactional
    public LivroResponse criar(LivroRequest pedido) {
        List<Autor> autores = autorRepositorio.findAllById(pedido.autores());
        if (autores.size() != pedido.autores().size()) {
            throw new AutorNaoEncontradoException("um ou mais autores informados não existem");
        }

        Livro livro = new Livro();
        livro.setTitulo(pedido.titulo().trim());
        livro.setIsbn(pedido.isbn().trim());
        livro.setAnoPublicacao(pedido.anoPublicacao());
        livro.setAutores(autores);
        return LivroResponse.de(repositorio.save(livro));
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponse> historicoDeEmprestimos(Long livroId) {
        if (!repositorio.existsById(livroId)) {
            throw new LivroNaoEncontradoException(livroId);
        }
        return emprestimoRepositorio.findByLivroIdOrderByEmprestadoEmDesc(livroId)
                .stream().map(EmprestimoResponse::de).toList();
    }

    private Livro obter(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new LivroNaoEncontradoException(id));
    }
}
