package com.intuitive.analise_dados_an.service;

import com.intuitive.analise_dados_an.model.Operadora;
import com.intuitive.analise_dados_an.repository.OperadoraRepository;
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
        log.info("Iniciando leitura do CADOP (UF + Modalidade): {}", caminhoArquivo);

        Map<String, DadosCadop> mapaDados = new HashMap<>();
        Path path = Paths.get(caminhoArquivo);

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {

            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                    .withSkipLines(1)
                    .build();

            List<String[]> linhas = csvReader.readAll();

            int indexRegAns = 0;
            int indexModalidade = 4; // <--- NOVO
            int indexUf = 10;

            for (String[] colunas : linhas) {
                if (colunas.length > indexUf) {
                    String regAns = colunas[indexRegAns].replace("\"", "").trim();
                    String modalidade = colunas[indexModalidade].replace("\"", "").trim();
                    String uf = colunas[indexUf].replace("\"", "").trim();

                    if (!regAns.isEmpty()) {
                        mapaDados.put(regAns, new DadosCadop(uf, modalidade));
                    }
                }
            }

            log.info("Mapa carregado. Processando atualização no banco...");

            List<Operadora> todasOperadoras = operadoraRepository.findAll();
            int atualizadas = 0;

            for (Operadora op : todasOperadoras) {
                String regAnsBanco = String.valueOf(op.getRegistroAns());

                if (mapaDados.containsKey(regAnsBanco)) {
                    DadosCadop dados = mapaDados.get(regAnsBanco);
                    op.setUf(dados.uf);
                    op.setModalidade(dados.modalidade);
                    atualizadas++;
                }
            }

            operadoraRepository.saveAll(todasOperadoras);
            log.info("SUCESSO! UF e Modalidade atualizadas para {} operadoras.", atualizadas);

        } catch (Exception e) {
            log.error("Erro ao processar arquivo CADOP", e);
        }
    }

    private static class DadosCadop {
        String uf;
        String modalidade;

        public DadosCadop(String uf, String modalidade) {
            this.uf = uf;
            this.modalidade = modalidade;
        }
    }
}