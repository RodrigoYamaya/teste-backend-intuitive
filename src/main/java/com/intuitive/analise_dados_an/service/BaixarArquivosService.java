package com.intuitive.analise_dados_an.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class BaixarArquivosService {

    private static final String URL_BASE = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";
    private static final String DIR_TEMP = "dados_ans";
    private static final Logger log = LoggerFactory.getLogger(BaixarArquivosService.class);

    public List<String> baixarEExtrairUltimosTrimestres() {
        List<String> arquivosParaProcessar = new ArrayList<>();

        try {
            Files.createDirectories(Paths.get(DIR_TEMP));

            log.info(" Conectando ao site da ANS...");
            Document doc = Jsoup.connect(URL_BASE).timeout(10000).get();

            List<String> anos = doc.select("a[href]").stream()
                    .map(link -> link.attr("href"))
                    .filter(href -> href.matches("\\d{4}/"))
                    .sorted(Comparator.reverseOrder())
                    .toList();

            int contagem = 0;

            for (String ano : anos) {
                if (contagem >= 3) break;

                String urlAno = URL_BASE + ano;
                log.info(" Verificando ano: {}", ano);

                Document docAno = Jsoup.connect(urlAno).timeout(10000).get();
                List<String> zips = docAno.select("a[href]").stream()
                        .map(link -> link.attr("href"))
                        .filter(href -> href.toLowerCase().endsWith(".zip"))
                        .sorted(Comparator.reverseOrder())
                        .toList();

                for (String zip : zips) {
                    if (contagem >= 3) break;

                    File arquivoZip = baixarArquivo(urlAno + zip, zip);

                    String csvPath = extrairCsvDoZip(arquivoZip);
                    if (csvPath != null) {
                        arquivosParaProcessar.add(csvPath);
                        contagem++;
                    }
                }
            }

        } catch (IOException e) {
            log.error("Erro ao baixar dados da ANS", e);
            throw new RuntimeException("Erro ao baixar dados: " + e.getMessage());
        }

        return arquivosParaProcessar;
    }

    private File baixarArquivo(String urlStr, String nomeArquivo) throws IOException {
        Path destino = Paths.get(DIR_TEMP, nomeArquivo);
        File arquivo = destino.toFile();

        if (arquivo.exists() && arquivo.length() > 0) {
            log.info(" Arquivo já existe (Cache): {}", nomeArquivo);
            return arquivo;
        }

        log.info("Baixando da internet: {}", nomeArquivo);
        URL url = new URL(urlStr);
        try (InputStream in = url.openStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }
        return arquivo;
    }

    private String extrairCsvDoZip(File zipFile) throws IOException {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = zis.getNextEntry();

            while (entry != null) {
                String nome = entry.getName().toLowerCase();

                log.info(" Conteúdo do ZIP {}: {}", zipFile.getName(), nome);

                if (nome.endsWith(".csv") && !nome.contains("meta") && !nome.contains("leiame")) {

                    log.info(" ALVO DETECTADO! Extraindo: {}", nome);

                    File novoArquivo = new File(DIR_TEMP, entry.getName());
                    try (FileOutputStream fos = new FileOutputStream(novoArquivo)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    return novoArquivo.getAbsolutePath();
                }
                entry = zis.getNextEntry();
            }
        }
        return null;
    }
}