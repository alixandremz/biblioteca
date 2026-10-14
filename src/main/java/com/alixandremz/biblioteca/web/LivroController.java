package com.alixandremz.biblioteca.web;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alixandremz.biblioteca.dto.EmprestimoResponse;
import com.alixandremz.biblioteca.dto.LivroRequest;
import com.alixandremz.biblioteca.dto.LivroResponse;
import com.alixandremz.biblioteca.dto.PaginaResponse;
import com.alixandremz.biblioteca.service.LivroService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/livros")
@Validated
public class LivroController {

    private final LivroService servico;

    public LivroController(LivroService servico) {
        this.servico = servico;
    }

    @GetMapping
    public PaginaResponse<LivroResponse> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "a página não pode ser negativa") int pagina,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "o tamanho mínimo é 1")
            @Max(value = 100, message = "o tamanho máximo é 100") int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.ASC, "titulo"));
        return servico.listar(titulo, pageable);
    }

    @GetMapping("/{id}")
    public LivroResponse buscar(@PathVariable Long id) {
        return servico.buscar(id);
    }

    @PostMapping
    public ResponseEntity<LivroResponse> criar(@Valid @RequestBody LivroRequest pedido) {
        LivroResponse criado = servico.criar(pedido);
        return ResponseEntity.created(URI.create("/livros/" + criado.id())).body(criado);
    }

    @GetMapping("/{id}/emprestimos")
    public List<EmprestimoResponse> historico(@PathVariable Long id) {
        return servico.historicoDeEmprestimos(id);
    }
}
