package com.intuitive.analise_dados_an.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "tb_despesas", indexes = {
        @Index(name = "idx_ano_trimestre", columnList = "ano, trimestre"),
        @Index(name = "idx_uf_despesa", columnList = "uf")
})
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_evento")
    private LocalDate dataEvento;

    private Integer trimestre;
    private Integer ano;

    @Column(precision = 19, scale = 2)
    private BigDecimal valor;


    @ManyToOne
    @JoinColumn(name = "operadora_cnpj")
    private Operadora operadora;

    @Column(length = 2)
    private String uf;

}
