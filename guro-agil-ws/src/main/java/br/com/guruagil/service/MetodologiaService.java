package br.com.guruagil.service;

import br.com.guruagil.dto.ImportacaoResultadoDTO;
import br.com.guruagil.dto.RecomendacaoDTO;
import br.com.guruagil.entity.Arquivo;
import br.com.guruagil.entity.Pergunta;
import br.com.guruagil.entity.Resposta;
import br.com.guruagil.repository.ArquivoRepository;
import br.com.guruagil.repository.PerguntaRepository;
import br.com.guruagil.repository.RespostaRepository;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MetodologiaService {

    private final ArquivoRepository arquivoRepository;
    private final PerguntaRepository perguntaRepository;
    private final RespostaRepository respostaRepository;

    private static final Map<String, Map<Integer, Map<String, Integer>>> PONTUACOES = new HashMap<>();

    static {
        initializePontuacoes();
    }

    private static void initializePontuacoes() {
        // Inicializar estrutura
        PONTUACOES.put("Agile", new HashMap<>());
        PONTUACOES.put("Lean", new HashMap<>());
        PONTUACOES.put("ContinuousDeliveryAgile", new HashMap<>());
        PONTUACOES.put("ContinuousDeliveryLean", new HashMap<>());
        PONTUACOES.put("Exploratory", new HashMap<>());
        PONTUACOES.put("Program", new HashMap<>());

        // Configurar pontuações para cada questão
        for (int i = 1; i <= 10; i++) {
            for (String metodologia : PONTUACOES.keySet()) {
                PONTUACOES.get(metodologia).put(i, new HashMap<>());
            }
        }

        configurarPontuacoes();
    }

    private static void configurarPontuacoes() {
        // Questão 1
        configurarPontuacao(1, "A", 8, 7, 10, 9, 9, 10);
        configurarPontuacao(1, "B", 10, 9, 9, 8, 8, 7);
        configurarPontuacao(1, "C", 7, 8, 6, 7, 6, 9);
        configurarPontuacao(1, "D", 5, 7, 5, 6, 4, 10);

        // Questão 2
        configurarPontuacao(2, "A", 4, 5, 3, 4, 6, 7);
        configurarPontuacao(2, "B", 6, 6, 5, 5, 8, 8);
        configurarPontuacao(2, "C", 8, 8, 8, 7, 9, 9);
        configurarPontuacao(2, "D", 10, 9, 10, 8, 8, 8);

        // Questão 3
        configurarPontuacao(3, "A", 10, 7, 10, 8, 10, 6);
        configurarPontuacao(3, "B", 9, 8, 9, 9, 9, 7);
        configurarPontuacao(3, "C", 8, 9, 8, 9, 7, 8);
        configurarPontuacao(3, "D", 7, 10, 7, 10, 5, 10);

        // Questão 4
        configurarPontuacao(4, "A", 10, 7, 10, 8, 10, 5);
        configurarPontuacao(4, "B", 9, 8, 9, 9, 9, 7);
        configurarPontuacao(4, "C", 7, 9, 7, 10, 6, 8);
        configurarPontuacao(4, "D", 5, 10, 5, 10, 4, 10);

        // Questão 5
        configurarPontuacao(5, "A", 8, 9, 8, 8, 7, 9);
        configurarPontuacao(5, "B", 9, 8, 9, 8, 9, 8);
        configurarPontuacao(5, "C", 8, 7, 10, 9, 10, 7);
        configurarPontuacao(5, "D", 10, 8, 10, 9, 10, 8);

        // Questão 6
        configurarPontuacao(6, "A", 7, 9, 8, 9, 6, 9);
        configurarPontuacao(6, "B", 8, 9, 9, 9, 8, 8);
        configurarPontuacao(6, "C", 9, 8, 10, 8, 10, 7);
        configurarPontuacao(6, "D", 10, 8, 10, 8, 10, 8);

        // Questão 7
        configurarPontuacao(7, "A", 10, 8, 10, 8, 10, 7);
        configurarPontuacao(7, "B", 8, 9, 8, 9, 8, 8);
        configurarPontuacao(7, "C", 7, 9, 7, 9, 6, 9);
        configurarPontuacao(7, "D", 5, 10, 5, 10, 4, 10);

        // Questão 8
        configurarPontuacao(8, "A", 10, 9, 10, 8, 10, 6);
        configurarPontuacao(8, "B", 9, 9, 9, 8, 9, 8);
        configurarPontuacao(8, "C", 7, 8, 7, 9, 6, 9);
        configurarPontuacao(8, "D", 5, 7, 5, 8, 4, 10);

        // Questão 9
        configurarPontuacao(9, "A", 10, 9, 10, 8, 10, 6);
        configurarPontuacao(9, "B", 9, 9, 9, 9, 9, 8);
        configurarPontuacao(9, "C", 7, 8, 7, 9, 6, 9);
        configurarPontuacao(9, "D", 5, 7, 5, 8, 4, 10);

        // Questão 10
        configurarPontuacao(10, "A", 10, 9, 10, 8, 10, 6);
        configurarPontuacao(10, "B", 9, 9, 9, 9, 9, 8);
        configurarPontuacao(10, "C", 7, 8, 7, 9, 6, 9);
        configurarPontuacao(10, "D", 8, 9, 8, 9, 8, 8);
    }

    private static void configurarPontuacao(int questao, String alternativa,
                                            int agile, int lean, int cdAgile, int cdLean, int exploratory, int program) {
        PONTUACOES.get("Agile").get(questao).put(alternativa, agile);
        PONTUACOES.get("Lean").get(questao).put(alternativa, lean);
        PONTUACOES.get("ContinuousDeliveryAgile").get(questao).put(alternativa, cdAgile);
        PONTUACOES.get("ContinuousDeliveryLean").get(questao).put(alternativa, cdLean);
        PONTUACOES.get("Exploratory").get(questao).put(alternativa, exploratory);
        PONTUACOES.get("Program").get(questao).put(alternativa, program);
    }

    public MetodologiaService(ArquivoRepository arquivoRepository,
                              PerguntaRepository perguntaRepository,
                              RespostaRepository respostaRepository) {
        this.arquivoRepository = arquivoRepository;
        this.perguntaRepository = perguntaRepository;
        this.respostaRepository = respostaRepository;
    }

    public ImportacaoResultadoDTO processarArquivo(MultipartFile file, String usuario) {
        try {
            if (file.isEmpty()) {
                return new ImportacaoResultadoDTO(false, "Arquivo vazio", null);
            }

            Arquivo arquivo = new Arquivo();
            arquivo.setNome(file.getOriginalFilename());
            arquivo.setUsuario(usuario);
            arquivo.setStatusImportacao("N");
            arquivo.setDataInclusao(LocalDateTime.now());
            arquivo.setDataAlteracao(LocalDateTime.now());
            arquivo = arquivoRepository.save(arquivo);

            try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
                String[] header = reader.readNext();
                if (header == null || header.length < 2) {
                    throw new RuntimeException("Formato do arquivo inválido. É necessário ter pelo menos duas colunas: número da questão e resposta");
                }

                String[] linha;
                int numQuestoes = 0;
                while ((linha = reader.readNext()) != null) {
                    if (linha.length >= 2) {
                        numQuestoes++;
                        if (numQuestoes > 10) {
                            throw new RuntimeException("O arquivo não pode conter mais que 10 questões");
                        }

                        Pergunta pergunta = new Pergunta();
                        pergunta.setDescricao("Questão " + linha[0].trim());
                        pergunta.setArquivo(arquivo);
                        pergunta.setDataInclusao(LocalDateTime.now());
                        pergunta.setDataAlteracao(LocalDateTime.now());
                        pergunta = perguntaRepository.save(pergunta);

                        String respostaValor = linha[1].trim().toUpperCase();
                        if (!respostaValor.matches("[A-D]")) {
                            throw new RuntimeException("Resposta inválida na questão " + linha[0] + ". Deve ser A, B, C ou D");
                        }

                        Resposta resposta = new Resposta();
                        resposta.setPergunta(pergunta);
                        resposta.setArquivo(arquivo);
                        resposta.setResposta(respostaValor);
                        resposta.setDataInclusao(LocalDateTime.now());
                        resposta.setDataAlteracao(LocalDateTime.now());
                        respostaRepository.save(resposta);
                    }
                }

                if (numQuestoes != 10) {
                    throw new RuntimeException("O arquivo deve conter exatamente 10 questões. Encontradas: " + numQuestoes);
                }
            }

            arquivo.setStatusImportacao("S");
            arquivoRepository.save(arquivo);

            return new ImportacaoResultadoDTO(true, "Arquivo processado com sucesso", arquivo.getId());

        } catch (Exception e) {
            return new ImportacaoResultadoDTO(false, "Erro ao processar arquivo: " + e.getMessage(), null);
        }
    }

    public RecomendacaoDTO calcularRecomendacao(Long arquivoId) {
        try {
            Arquivo arquivo = arquivoRepository.findById(arquivoId)
                    .orElseThrow(() -> new RuntimeException("Arquivo não encontrado"));

            List<Resposta> respostas = respostaRepository.findByArquivo(arquivo);

            if (respostas.isEmpty()) {
                throw new RuntimeException("Nenhuma resposta encontrada para este arquivo");
            }

            if (respostas.size() != 10) {
                throw new RuntimeException("Número incorreto de respostas. Esperado: 10, Encontrado: " + respostas.size());
            }

            Map<String, Integer> pontuacoes = new HashMap<>();
            pontuacoes.put("Agile", 0);
            pontuacoes.put("Lean", 0);
            pontuacoes.put("Continuous Delivery Agile", 0);
            pontuacoes.put("Continuous Delivery Lean", 0);
            pontuacoes.put("Exploratory", 0);
            pontuacoes.put("Program", 0);

            for (Resposta resposta : respostas) {
                int numeroQuestao = extrairNumeroQuestao(resposta.getPergunta().getDescricao());
                String alternativa = resposta.getResposta().toUpperCase();

                // Agile
                int pontuacaoAgile = PONTUACOES.get("Agile").get(numeroQuestao).get(alternativa);
                pontuacoes.put("Agile", pontuacoes.get("Agile") + pontuacaoAgile);

                // Lean
                int pontuacaoLean = PONTUACOES.get("Lean").get(numeroQuestao).get(alternativa);
                pontuacoes.put("Lean", pontuacoes.get("Lean") + pontuacaoLean);

                // Continuous Delivery Agile
                int pontuacaoCDAgile = PONTUACOES.get("ContinuousDeliveryAgile").get(numeroQuestao).get(alternativa);
                pontuacoes.put("Continuous Delivery Agile",
                        pontuacoes.get("Continuous Delivery Agile") + pontuacaoCDAgile);

                // Continuous Delivery Lean
                int pontuacaoCDLean = PONTUACOES.get("ContinuousDeliveryLean").get(numeroQuestao).get(alternativa);
                pontuacoes.put("Continuous Delivery Lean",
                        pontuacoes.get("Continuous Delivery Lean") + pontuacaoCDLean);

                // Exploratory
                int pontuacaoExploratory = PONTUACOES.get("Exploratory").get(numeroQuestao).get(alternativa);
                pontuacoes.put("Exploratory", pontuacoes.get("Exploratory") + pontuacaoExploratory);

                // Program
                int pontuacaoProgram = PONTUACOES.get("Program").get(numeroQuestao).get(alternativa);
                pontuacoes.put("Program", pontuacoes.get("Program") + pontuacaoProgram);
            }

            String metodologiaRecomendada = Collections.max(pontuacoes.entrySet(), Map.Entry.comparingByValue()).getKey();

            return new RecomendacaoDTO(metodologiaRecomendada, pontuacoes, "Recomendação calculada com sucesso");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular recomendação: " + e.getMessage());
        }
    }

    private int extrairNumeroQuestao(String descricao) {
        try {
            return Integer.parseInt(descricao.replace("Questão ", "").trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Formato inválido do número da questão: " + descricao);
        }
    }
}