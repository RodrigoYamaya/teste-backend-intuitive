package com.intuitive.analise_dados_an.service;

import com.intuitive.analise_dados_an.model.Operadora; // <--- Usando sua entidade certa
import com.intuitive.analise_dados_an.repository.OperadoraRepository; // <--- Seu repositório
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CadopImportService {

    private static final Logger log = LoggerFactory.getLogger(CadopImportService.class);

    private final OperadoraRepository operadoraRepository;

    public CadopImportService(OperadoraRepository operadoraRepository) {
        this.operadoraRepository = operadoraRepository;
    }

    @Transactional
    public void importarCadop(String caminhoArquivo) {
        log.info("🔄 Iniciando leitura do CADOP para atualizar UFs: {}", caminhoArquivo);

        Map<String, String> mapaUf = new HashMap<>();
        Path path = Paths.get(caminhoArquivo);

        // Leitura do CSV (usando ISO-8859-1 para acentos corretos)
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {

            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(new CSVParserBuilder().withSeparator(';').build()) // Separador ;
                    .withSkipLines(1) // Pula cabeçalho
                    .build();

            List<String[]> linhas = csvReader.readAll();

            // Índices confirmados pelo cabeçalho que você mandou:
            int indexRegAns = 0; // Coluna 0: REGISTRO_OPERADORA
            int indexUf = 10;    // Coluna 10: UF

            for (String[] colunas : linhas) {
                if (colunas.length > indexUf) {
                    // Limpeza dos dados (tira aspas e espaços)
                    String regAns = colunas[indexRegAns].replace("\"", "").trim();
                    String uf = colunas[indexUf].replace("\"", "").trim();

                    if (!regAns.isEmpty() && !uf.isEmpty()) {
                        mapaUf.put(regAns, uf);
                    }
                }
            }

            log.info("✅ Mapa carregado. Total de operadoras encontradas no CSV: {}", mapaUf.size());

            // --- ATUALIZAÇÃO NO BANCO ---
            List<Operadora> todasOperadoras = operadoraRepository.findAll();
            int atualizadas = 0;

            log.info("Processando {} operadoras do banco...", todasOperadoras.size());

            for (Operadora op : todasOperadoras) {
                // Ajuste aqui se o seu get for diferente (ex: getRegistroAns)
                String regAnsBanco = String.valueOf(op.getRegistroAns());

                if (mapaUf.containsKey(regAnsBanco)) {
                    op.setUf(mapaUf.get(regAnsBanco));
                    atualizadas++;
                }
            }

            operadoraRepository.saveAll(todasOperadoras);
            log.info("🚀 SUCESSO! UFs atualizadas para {} operadoras.", atualizadas);

        } catch (Exception e) {
            log.error("❌ Erro ao processar arquivo CADOP", e);
        }
    }
}