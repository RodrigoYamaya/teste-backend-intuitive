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
    private final CadopImportService cadopService;

    public ExecutarDados(BaixarArquivosService baixarService,
                         CargaDadosService cargaService,
                         CadopImportService cadopService) {
        this.baixarService = baixarService;
        this.cargaService = cargaService;
        this.cadopService = cadopService;
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("INICIANDO A IMPORTAÇÃO ANS");

        // log.info("[ETAPA 1] Buscando arquivos...");
        // List<String> arquivos = baixarService.baixarEExtrairUltimosTrimestres();

        // log.info("[ETAPA 2] Carga Financeira...");
        // if (arquivos != null) {
        //     for (String arquivo : arquivos) {
        //         cargaService.carregarArquivoCsv(arquivo);
        //     }
        // }

         log.info("[ETAPA 3] Atualização de UFs...");
         String caminhoCadop = "dados_ans/cadop.csv";
         if (new File(caminhoCadop).exists()) {
             cadopService.importarCadop(caminhoCadop);
         }

        log.info("Banco de dados pronto e preservado. Pode rodar as queries!");
    }

}