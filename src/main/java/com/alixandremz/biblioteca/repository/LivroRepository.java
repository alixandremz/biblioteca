package com.alixandremz.biblioteca.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.alixandremz.biblioteca.model.Livro;

import jakarta.persistence.LockModeType;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    Page<Livro> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    // SELECT ... FOR UPDATE: trava a linha do livro até o fim da transação.
    // É o que impede dois empréstimos simultâneos do mesmo livro.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Livro l where l.id = :id")
    Optional<Livro> findComBloqueioPorId(@Param("id") Long id);
}
