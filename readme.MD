#  Teste Técnico - Intuitive Care (Módulo ETL em Java)

Este módulo é responsável pela etapa de **ETL (Extract, Transform, Load)** do teste. Ele automatiza o download de arquivos públicos da ANS, processa planilhas de demonstrações contábeis e persiste os dados de forma estruturada em um banco relacional.

## ️ Tecnologias Utilizadas
* **Java 21:** Linguagem base.
* **Spring Boot 3:** Framework para injeção de dependência e gerenciamento de contexto.
* **Spring Data JPA:** Camada de persistência.
* **OpenCSV:** Biblioteca robusta para leitura e parser de arquivos CSV.
* **MySQL 8:** Banco de dados relacional.
* **Jsoup:** Para varredura (web scraping) da página da ANS.

---

##  Decisões Técnicas e Trade-offs

Abaixo justifico as escolhas técnicas adotadas para resolver o desafio de processamento de dados massivos.

### 1. Escolha da Linguagem: Java vs Python
**Decisão:** **Java**.

**Justificativa:**
* **Proficiência e Produtividade:** A escolha foi baseada na minha experiência prévia e afinidade com o ecossistema Java para manipulação de arquivos de texto e lógica backend. Utilizar uma ferramenta que domino permitiu focar na regra de negócio e na qualidade da solução, garantindo uma entrega mais rápida e com menos erros.
* **Segurança e Tipagem:** O Java oferece tipagem estática forte, o que é crucial para evitar erros silenciosos ao lidar com dados financeiros. O uso da classe `BigDecimal` foi determinante para garantir a precisão monetária (centavos), evitando problemas de ponto flutuante comuns em linguagens dinâmicas.

### 2. Processamento de Arquivos: OpenCSV vs Leitura Manual (`BufferedReader`)
**Decisão:** **Biblioteca OpenCSV**.

**Justificativa:**
* **Robustez vs Reinvenção da Roda:** Embora fosse possível ler o arquivo linha a linha com `BufferedReader` e separar as colunas usando `split(";")`, essa abordagem manual é frágil. Arquivos CSV reais frequentemente contêm "sujeira", como caracteres especiais, aspas dentro de campos de texto ou quebras de linha inesperadas.
* **Confiabilidade:** O **OpenCSV** lida automaticamente com esses casos de borda (edge cases), garantindo que os dados sejam interpretados corretamente conforme o padrão RFC 4180. Isso tornou o código mais limpo e menos propenso a falhas de parser.

### 3. Persistência de Dados: Batch Processing vs Transação Única
**Decisão:** **Inserção em Lotes (Batch) sem transação global**.

**Justificativa:**
* **Performance e Memória:** Com arquivos ultrapassando 200.000 registros, tentar salvar tudo em uma única transação (`@Transactional`) sobrecarregaria a memória da aplicação e do banco.
* **Estratégia:** Implementei um controle manual onde os dados são salvos a cada 1.000 registros (`saveAll`). Isso libera a memória periodicamente e permite acompanhar o progresso da importação em tempo real via logs, sem travar o banco de dados.

### 4. Integridade Relacional: Cache de Memória vs Consultas Repetitivas
**Decisão:** **Cache Local (`Map<String, Operadora>`)**.

**Justificativa:**
* O arquivo de despesas repete o CNPJ da operadora milhares de vezes. Fazer uma consulta ao banco (`findById`) para cada linha tornaria o processo extremamente lento.
* Criei um cache em memória (HashMap) para armazenar as operadoras já processadas na execução atual. Isso eliminou a redundância de queries e resolveu conflitos de identidade do Hibernate (`TransientPropertyValueException`), garantindo que cada operadora seja salva apenas uma vez.

---

## Como Executar o Projeto

1. **Banco de Dados:** Certifique-se de que o MySQL está rodando na porta `3306` e o schema `intuitive_care_ans` foi criado.
2. **Configuração:** Ajuste usuário e senha no `application.properties`.
3. **Execução:**
   ```bash
   mvn spring-boot:run