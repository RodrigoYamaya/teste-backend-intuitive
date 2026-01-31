package com.intuitive.analise_dados_an.runner;

import com.intuitive.analise_dados_an.service.BaixarArquivosService;
import com.intuitive.analise_dados_an.service.CargaDadosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExecutarDados implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ExecutarDados.class);
    private final BaixarArquivosService baixarService;
    private final CargaDadosService cargaService;

    public ExecutarDados(BaixarArquivosService baixarService, CargaDadosService cargaService) {
        this.baixarService = baixarService;
        this.cargaService = cargaService;
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("INICIANDO A IMPORTAÇÃO ANS");

        try {
            // ETAPA 1: Download
            log.info("ETAPA 1:  Buscando arquivos no site da ANS...");
            List<String> arquivos = baixarService.baixarEExtrairUltimosTrimestres();

            if (arquivos.isEmpty()) {
                log.warn("ALERTA: Nenhum arquivo CSV foi encontrado nos últimos trimestres!");
                log.warn("Verifique se o site da ANS mudou o layout ou se há conexão.");
                return;
            }

            log.info("Arquivos prontos para processamento: {}");

            log.info("[ETAPA 2] Iniciando carga para o Banco de Dados...");
            for (String arquivo : arquivos) {
                log.info("Processando arquivo: {}");
                cargaService.carregarArquivoCsv(arquivo);
            }

            log.info("PROCESSO FINALIZADO COM SUCESSO!");

        } catch (Exception e) {
            log.warn("ERRO CRÍTICO na execução do teste");
        }

    }
}
