package com.intuitive.analise_dados_an.service;

import com.intuitive.analise_dados_an.model.Despesa;
import com.intuitive.analise_dados_an.model.Operadora;
import com.intuitive.analise_dados_an.repository.DespesaRepository;
import com.intuitive.analise_dados_an.repository.OperadoraRepository;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CargaDadosService {

    private static final Logger log = LoggerFactory.getLogger(CargaDadosService.class);
    private static final int BATCH_SIZE = 1000;

    private final DespesaRepository despesaRepository;
    private final OperadoraRepository operadoraRepository;

    private final Set<String> operadorasVerificadas = new HashSet<>();

    public CargaDadosService(DespesaRepository despesaRepository, OperadoraRepository operadoraRepository) {
        this.despesaRepository = despesaRepository;
        this.operadoraRepository = operadoraRepository;
    }

    public void carregarArquivoCsv(String caminhoArquivo) {
        log.info(" Lendo arquivo: {}", caminhoArquivo);
        long inicio = System.currentTimeMillis();

        operadorasVerificadas.clear();

        List<Despesa> lote = new ArrayList<>();
        int linhasSalvas = 0;

        try (Reader reader = Files.newBufferedReader(Paths.get(caminhoArquivo));
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withSkipLines(1)
                     .withCSVParser(new CSVParserBuilder().withSeparator(';').withQuoteChar('"').build())
                     .build()) {

            String[] linha;
            while ((linha = csvReader.readNext()) != null) {
                try {
                    Despesa d = converterLinha(linha);
                    if (d != null) {
                        lote.add(d);
                        linhasSalvas++;
                    }

                    if (lote.size() >= BATCH_SIZE) {
                        salvarLote(lote);
                    }
                } catch (Exception e) {

                }
            }

            if (!lote.isEmpty()) {
                salvarLote(lote);
            }

            log.info("Finalizado! Total: {} registros em {} ms", linhasSalvas, (System.currentTimeMillis() - inicio));

        } catch (Exception e) {
            log.error(" Erro fatal", e);
        }
    }

    private void salvarLote(List<Despesa> lote) {
        despesaRepository.saveAll(lote);
        despesaRepository.flush();

        lote.clear();
    }

    private Despesa converterLinha(String[] dados) {
        if (dados.length < 6) return null;

        Despesa d = new Despesa();


        String dataStr = dados[0];
        try {
            d.setDataEvento(LocalDate.parse(dataStr));
        } catch (Exception e) {
            d.setDataEvento(LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        d.setAno(d.getDataEvento().getYear());
        d.setTrimestre((d.getDataEvento().getMonthValue() - 1) / 3 + 1);

        String cnpj = dados[1];

        if (!operadorasVerificadas.contains(cnpj)) {
            if (!operadoraRepository.existsById(cnpj)) {
                Operadora nova = new Operadora();
                nova.setCnpj(cnpj);
                nova.setRegistroAns(dados[1]);
                nova.setRazaoSocial("Operadora " + cnpj);
                operadoraRepository.saveAndFlush(nova);
            }
            operadorasVerificadas.add(cnpj);
        }

        Operadora ref = new Operadora();
        ref.setCnpj(cnpj);
        d.setOperadora(ref);

        String valorStr = dados[5].replace(".", "").replace(",", ".");
        d.setValor(new BigDecimal(valorStr));

        return d;
    }
}