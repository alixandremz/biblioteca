package com.alixandremz.biblioteca.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alixandremz.biblioteca.dto.AutorRequest;
import com.alixandremz.biblioteca.dto.AutorResponse;
import com.alixandremz.biblioteca.model.Autor;
import com.alixandremz.biblioteca.repository.AutorRepository;

@Service
public class AutorService {

    private final AutorRepository repositorio;

    public AutorService(AutorRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<AutorResponse> listar() {
        return repositorio.findAll().stream().map(AutorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public AutorResponse buscar(Long id) {
        return AutorResponse.de(obter(id));
    }

    @Transactional
    public AutorResponse criar(AutorRequest pedido) {
        Autor autor = new Autor();
        autor.setNome(pedido.nome().trim());
        autor.setNacionalidade(pedido.nacionalidade());
        return AutorResponse.de(repositorio.save(autor));
    }

    private Autor obter(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new AutorNaoEncontradoException(id));
    }
}
