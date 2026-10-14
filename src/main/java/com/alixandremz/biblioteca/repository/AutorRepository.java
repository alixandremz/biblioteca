package com.alixandremz.biblioteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alixandremz.biblioteca.model.Autor;

public interface AutorRepository extends JpaRepository<Autor, Long> {
}
