package com.intuitive.analise_dados_an.runner;

import com.intuitive.analise_dados_an.service.BaixarArquivosService;
import com.intuitive.analise_dados_an.service.CargaDadosService;
import com.intuitive.analise_dados_an.service.CadopImportService; // <--- 1. NOVO: Importe a classe
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File; // Para checar se o arquivo existe
import java.util.List;

@Component
public class ExecutarDados implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ExecutarDados.class);

    private final BaixarArquivosService baixarService;
    private final CargaDadosService cargaService;
    private final CadopImportService cadopService; // <--- 2. NOVO: Declare a variável

    public ExecutarDados(BaixarArquivosService baixarService,
                         CargaDadosService cargaService,
                         CadopImportService cadopService) {
        this.baixarService = baixarService;
        this.cargaService = cargaService;
        this.cadopService = cadopService;
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("🚀 INICIANDO A IMPORTAÇÃO ANS");

        try {
            // --- ETAPA 1: Download ---
            log.info("[ETAPA 1] Buscando arquivos no site da ANS...");
            List<String> arquivos = baixarService.baixarEExtrairUltimosTrimestres();

            if (arquivos.isEmpty()) {
                log.warn("ALERTA: Nenhum arquivo CSV foi encontrado nos últimos trimestres!");
                return;
            }

            log.info("Arquivos prontos para processamento: {}", arquivos);

            // --- ETAPA 2: Carga Financeira ---
            log.info("[ETAPA 2] Iniciando carga para o Banco de Dados...");
            for (String arquivo : arquivos) {
                log.info("Processando arquivo financeiro: {}", arquivo);
                cargaService.carregarArquivoCsv(arquivo);
            }

            // --- ETAPA 3: Atualização de Cadastros (UF) ---
            // <--- 4. NOVO: O bloco inteiro abaixo
            log.info("[ETAPA 3] Iniciando atualização de UFs (Cadastro de Operadoras)...");
            String caminhoCadop = "dados_ans/cadop.csv"; // Caminho onde você salvou o arquivo baixado

            // Uma verificação simples pra não quebrar se você esqueceu de baixar
            if (new File(caminhoCadop).exists()) {
                cadopService.importarCadop(caminhoCadop);
            } else {
                log.warn("⚠ AVISO: Arquivo 'cadop.csv' não encontrado em 'dados_ans/'.");
                log.warn("As operadoras ficarão com UF = NULL. Baixe o arquivo no site da ANS para corrigir.");
            }

            log.info(" PROCESSO FINALIZADO COM SUCESSO!");

        } catch (Exception e) {
            log.error(" ERRO CRÍTICO na execução do teste", e); // Use log.error para ver o stacktrace
        }
    }
}