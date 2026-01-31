package com.intuitive.analise_dados_an.repository;

import com.intuitive.analise_dados_an.model.Operadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperadoraRepository extends JpaRepository<Operadora, String> {
}