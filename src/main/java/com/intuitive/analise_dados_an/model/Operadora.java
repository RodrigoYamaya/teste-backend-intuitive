package com.intuitive.analise_dados_an.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "tb_operadoras")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Operadora {
    
    @Id
    @Column(length = 20)
    @EqualsAndHashCode.Include
    private String cnpj;

    @Column(name = "registro_ans", unique = true)
    private String registroAns;

    @Column(name = "razao_social", columnDefinition = "TEXT")
    private String razaoSocial;

    private String modalidade;

    @Column(length = 2)
    private String uf;
}
