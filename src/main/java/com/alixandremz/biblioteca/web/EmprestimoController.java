package com.alixandremz.biblioteca.web;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alixandremz.biblioteca.dto.EmprestimoRequest;
import com.alixandremz.biblioteca.dto.EmprestimoResponse;
import com.alixandremz.biblioteca.service.EmprestimoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService servico;

    public EmprestimoController(EmprestimoService servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<EmprestimoResponse> emprestar(@Valid @RequestBody EmprestimoRequest pedido) {
        EmprestimoResponse criado = servico.emprestar(pedido);
        return ResponseEntity.created(URI.create("/emprestimos/" + criado.id())).body(criado);
    }

    @PostMapping("/{id}/devolucao")
    public EmprestimoResponse devolver(@PathVariable Long id) {
        return servico.devolver(id);
    }
}
