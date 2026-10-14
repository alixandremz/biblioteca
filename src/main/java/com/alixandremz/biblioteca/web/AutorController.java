package com.alixandremz.biblioteca.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alixandremz.biblioteca.dto.AutorRequest;
import com.alixandremz.biblioteca.dto.AutorResponse;
import com.alixandremz.biblioteca.service.AutorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/autores")
public class AutorController {

    private final AutorService servico;

    public AutorController(AutorService servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<AutorResponse> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public AutorResponse buscar(@PathVariable Long id) {
        return servico.buscar(id);
    }

    @PostMapping
    public ResponseEntity<AutorResponse> criar(@Valid @RequestBody AutorRequest pedido) {
        AutorResponse criado = servico.criar(pedido);
        return ResponseEntity.created(URI.create("/autores/" + criado.id())).body(criado);
    }
}
