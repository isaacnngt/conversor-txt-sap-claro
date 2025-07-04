package br.com.guruagil.service;

import br.com.guruagil.dto.ImportacaoResultadoDTO;
import br.com.guruagil.dto.QuestionAnswerDTO;
import br.com.guruagil.dto.QuestionRequestDTO;
import br.com.guruagil.dto.RecomendacaoDTO;
import br.com.guruagil.entity.*;
import br.com.guruagil.repository.*;
import com.opencsv.CSVReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetodologiaService {

    // Injeção de dependências dos repositórios e JdbcTemplate
    private final ArquivoRepository arquivoRepository;
    private final PerguntaRepository perguntaRepository;
    private final RespostaRepository respostaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final MetodologiaDescriptionService descriptionService;
    private final CicloRepository cicloRepository;
    private final RecomendaCicloRepository recomendaCicloRepository;


    // Códigos de processo para log de erros
    private static final int PROCESSO_ERRO_ARQUIVO = 1001;
    private static final int PROCESSO_ERRO_PERGUNTA = 1002;
    private static final int PROCESSO_ERRO_RESPOSTA = 1003;
    private static final int PROCESSO_ERRO_JAVA = 1004;
    private static final int PROCESSO_ERRO_VALIDACAO = 1005;

    // Mapa estático para armazenar as pontuações das metodologias por questão e alternativa
    private static final Map<String, Map<Integer, Map<String, Integer>>> PONTUACOES = new HashMap<>();

    // Bloco estático para inicializar as pontuações
    static {
        initializePontuacoes();
    }

    // Metodo para inicializar o mapa de pontuações com as metodologias e números de questões
    private static void initializePontuacoes() {
        PONTUACOES.put("Agile", new HashMap<>());
        PONTUACOES.put("Lean", new HashMap<>());
        PONTUACOES.put("ContinuousDeliveryAgile", new HashMap<>());
        PONTUACOES.put("ContinuousDeliveryLean", new HashMap<>());
        PONTUACOES.put("Exploratory", new HashMap<>());
        PONTUACOES.put("Program", new HashMap<>());

        for (int i = 1; i <= 10; i++) {
            for (String metodologia : PONTUACOES.keySet()) {
                PONTUACOES.get(metodologia).put(i, new HashMap<>());
            }
        }

        configurarPontuacoes();
    }

    /* Metodo para configurar as pontuações específicas para cada questão e alternativa*/
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

    // Metodo auxiliar para configurar a pontuação de uma questão/alternativa para todas as metodologias
    private static void configurarPontuacao(int questao, String alternativa,
                                            int agile, int lean, int cdAgile, int cdLean, int exploratory, int program) {
        PONTUACOES.get("Agile").get(questao).put(alternativa, agile);
        PONTUACOES.get("Lean").get(questao).put(alternativa, lean);
        PONTUACOES.get("ContinuousDeliveryAgile").get(questao).put(alternativa, cdAgile);
        PONTUACOES.get("ContinuousDeliveryLean").get(questao).put(alternativa, cdLean);
        PONTUACOES.get("Exploratory").get(questao).put(alternativa, exploratory);
        PONTUACOES.get("Program").get(questao).put(alternativa, program);
    }

    // Construtor da classe, responsável por injetar as dependências
    public MetodologiaService(ArquivoRepository arquivoRepository,
                              PerguntaRepository perguntaRepository,
                              RespostaRepository respostaRepository,
                              JdbcTemplate jdbcTemplate,
                              MetodologiaDescriptionService descriptionService,
                              CicloRepository cicloRepository,
                              RecomendaCicloRepository recomendaCicloRepository) {
        this.arquivoRepository = arquivoRepository;
        this.perguntaRepository = perguntaRepository;
        this.respostaRepository = respostaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.descriptionService = descriptionService;
        this.cicloRepository = cicloRepository;
        this.recomendaCicloRepository = recomendaCicloRepository;
    }

    // Metodo principal para processar o arquivo CSV contendo as respostas do usuário
    public ImportacaoResultadoDTO processarArquivo(MultipartFile file, String usuario) {
        Arquivo arquivo = null;
        try {
            if (file.isEmpty()) {
                registrarErro("Arquivo vazio", usuario, PROCESSO_ERRO_VALIDACAO);
                return new ImportacaoResultadoDTO(false, "Arquivo vazio", null);
            }

            arquivo = new Arquivo();
            arquivo.setNome(file.getOriginalFilename());
            arquivo.setUsuario(usuario);
            arquivo.setStatusImportacao("N");
            arquivo.setDataInclusao(LocalDateTime.now());
            arquivo.setDataAlteracao(LocalDateTime.now());

            try {
                arquivo = arquivoRepository.save(arquivo);
            } catch (Exception e) {
                registrarErro("Erro ao salvar arquivo: " + e.getMessage(), usuario, PROCESSO_ERRO_ARQUIVO);
                throw e;
            }

            try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
                String[] header = reader.readNext();
                if (header == null || header.length < 2) {
                    String erro = "Formato do arquivo inválido. É necessário ter pelo menos duas colunas";
                    registrarErro(erro, usuario, PROCESSO_ERRO_ARQUIVO);
                    throw new RuntimeException(erro);
                }

                String[] linha;
                int numQuestoes = 0;

                while ((linha = reader.readNext()) != null) {
                    if (linha.length >= 2) {
                        numQuestoes++;
                        if (numQuestoes > 10) {
                            String erro = "O arquivo não pode conter mais que 10 questões";
                            registrarErro(erro, usuario, PROCESSO_ERRO_VALIDACAO);
                            throw new RuntimeException(erro);
                        }

                        Pergunta pergunta = null;
                        try {
                            pergunta = new Pergunta();
                            pergunta.setDescricao("Questão " + linha[0].trim());
                            pergunta.setArquivo(arquivo);
                            pergunta.setDataInclusao(LocalDateTime.now());
                            pergunta.setDataAlteracao(LocalDateTime.now());
                            pergunta = perguntaRepository.save(pergunta);
                        } catch (Exception e) {
                            registrarErro("Erro ao processar pergunta: " + e.getMessage(), usuario, PROCESSO_ERRO_PERGUNTA);
                            throw e;
                        }

                        String respostaValor = linha[1].trim().toUpperCase();
                        if (!respostaValor.matches("[A-D]")) {
                            String erro = "Resposta inválida na questão " + linha[0] + ". Deve ser A, B, C ou D";
                            registrarErro(erro, usuario, PROCESSO_ERRO_RESPOSTA);
                            throw new RuntimeException(erro);
                        }

                        try {
                            Resposta resposta = new Resposta();
                            resposta.setPergunta(pergunta);
                            resposta.setArquivo(arquivo);
                            resposta.setResposta(respostaValor);
                            resposta.setDataInclusao(LocalDateTime.now());
                            resposta.setDataAlteracao(LocalDateTime.now());
                            respostaRepository.save(resposta);
                        } catch (Exception e) {
                            registrarErro("Erro ao processar resposta: " + e.getMessage(), usuario, PROCESSO_ERRO_RESPOSTA);
                            throw e;
                        }
                    }
                }

                //Valida se o arquivo tem 10 questoes
                if (numQuestoes != 10) {
                    String erro = "O arquivo deve conter exatamente 10 questões. Encontradas: " + numQuestoes;
                    registrarErro(erro, usuario, PROCESSO_ERRO_VALIDACAO);
                    throw new RuntimeException(erro);
                }
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw e;
                }
                String erro = "Erro ao ler arquivo: " + e.getMessage();
                registrarErro(erro, usuario, PROCESSO_ERRO_ARQUIVO);
                throw new RuntimeException(erro);
            }

            arquivo.setStatusImportacao("S");
            arquivoRepository.save(arquivo);

            return new ImportacaoResultadoDTO(true, "Arquivo processado com sucesso", arquivo.getId());

        } catch (Exception e) {
            String mensagemErro = "Erro ao processar arquivo: " + e.getMessage();

            if (arquivo != null && arquivo.getId() != null) {
                arquivo.setStatusImportacao("N");
                arquivoRepository.save(arquivo);

                if (!(e instanceof RuntimeException)) {
                    registrarErro(mensagemErro, usuario, PROCESSO_ERRO_JAVA);
                }
            }

            return new ImportacaoResultadoDTO(false, mensagemErro, arquivo != null ? arquivo.getId() : null);
        }
    }

    public ImportacaoResultadoDTO processarFormulario(QuestionRequestDTO request) {
        try {
            if (request.getRespostas().size() != 10) {
                String erro = "O formulário deve conter exatamente 10 respostas";
                registrarErro(erro, request.getUsuario(), PROCESSO_ERRO_VALIDACAO);
                throw new RuntimeException(erro);
            }

            // Criar ciclo
            Ciclo ciclo = new Ciclo();
            ciclo.setUsuario(request.getUsuario());
            ciclo = cicloRepository.save(ciclo);

            // Processar perguntas e respostas
            for (QuestionAnswerDTO questionAnswer : request.getRespostas()) {
                Pergunta pergunta = new Pergunta();
                pergunta.setDescricao("Questão " + questionAnswer.getId());
                pergunta.setCiclo(ciclo);
                pergunta.setDataInclusao(LocalDateTime.now());
                pergunta.setDataAlteracao(LocalDateTime.now());
                pergunta = perguntaRepository.save(pergunta);

                Resposta resposta = new Resposta();
                resposta.setPergunta(pergunta);
                resposta.setCiclo(ciclo);
                resposta.setResposta(questionAnswer.getResposta());
                resposta.setDataInclusao(LocalDateTime.now());
                resposta.setDataAlteracao(LocalDateTime.now());
                respostaRepository.save(resposta);
            }

            // Calcula recomendação imediatamente após salvar
            try {
                calcularRecomendacaoPorCiclo(ciclo.getId());
            } catch (Exception e) {
                registrarErro("Erro ao calcular recomendação inicial: " + e.getMessage(),
                        request.getUsuario(), PROCESSO_ERRO_JAVA);
                // Não propaga o erro para não impedir o salvamento do formulário
            }

            return new ImportacaoResultadoDTO(true, "Formulário processado com sucesso", ciclo.getId());

        } catch (Exception e) {
            String mensagemErro = "Erro ao processar formulário: " + e.getMessage();
            registrarErro(mensagemErro, request.getUsuario(), PROCESSO_ERRO_JAVA);
            return new ImportacaoResultadoDTO(false, mensagemErro, null);
        }
    }

    public RecomendacaoDTO calcularRecomendacaoPorCiclo(Long cicloId) {
        try {
            Ciclo ciclo = cicloRepository.findById(cicloId)
                    .orElseThrow(() -> {
                        String erro = "Ciclo não encontrado";
                        registrarErro(erro, "SISTEMA", PROCESSO_ERRO_VALIDACAO);
                        return new RuntimeException(erro);
                    });

            List<Resposta> respostas = respostaRepository.findByCiclo(ciclo);

            if (respostas.isEmpty()) {
                String erro = "Nenhuma resposta encontrada para este ciclo";
                registrarErro(erro, ciclo.getUsuario(), PROCESSO_ERRO_RESPOSTA);
                RecomendaCiclo recomendaCiclo = new RecomendaCiclo();
                recomendaCiclo.setCiclo(ciclo);
                recomendaCiclo.setCalculado("N");
                recomendaCiclo.setDataInclusao(LocalDateTime.now());
                recomendaCiclo.setDataAlteracao(LocalDateTime.now());
                recomendaCicloRepository.save(recomendaCiclo);
                throw new RuntimeException(erro);
            }

            if (respostas.size() != 10) {
                String erro = "Número incorreto de respostas. Esperado: 10, Encontrado: " + respostas.size();
                registrarErro(erro, ciclo.getUsuario(), PROCESSO_ERRO_VALIDACAO);
                RecomendaCiclo recomendaCiclo = new RecomendaCiclo();
                recomendaCiclo.setCiclo(ciclo);
                recomendaCiclo.setCalculado("N");
                recomendaCiclo.setDataInclusao(LocalDateTime.now());
                recomendaCiclo.setDataAlteracao(LocalDateTime.now());
                recomendaCicloRepository.save(recomendaCiclo);
                throw new RuntimeException(erro);
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

                pontuacoes.put("Agile",
                        pontuacoes.get("Agile") + PONTUACOES.get("Agile").get(numeroQuestao).get(alternativa));
                pontuacoes.put("Lean",
                        pontuacoes.get("Lean") + PONTUACOES.get("Lean").get(numeroQuestao).get(alternativa));
                pontuacoes.put("Continuous Delivery Agile",
                        pontuacoes.get("Continuous Delivery Agile") + PONTUACOES.get("ContinuousDeliveryAgile").get(numeroQuestao).get(alternativa));
                pontuacoes.put("Continuous Delivery Lean",
                        pontuacoes.get("Continuous Delivery Lean") + PONTUACOES.get("ContinuousDeliveryLean").get(numeroQuestao).get(alternativa));
                pontuacoes.put("Exploratory",
                        pontuacoes.get("Exploratory") + PONTUACOES.get("Exploratory").get(numeroQuestao).get(alternativa));
                pontuacoes.put("Program",
                        pontuacoes.get("Program") + PONTUACOES.get("Program").get(numeroQuestao).get(alternativa));
            }

            int maxPontuacao = Collections.max(pontuacoes.values());
            List<String> metodologiasRecomendadas = pontuacoes.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxPontuacao)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // Salvar a recomendação
            RecomendaCiclo recomendaCiclo = new RecomendaCiclo();
            recomendaCiclo.setCiclo(ciclo);
            recomendaCiclo.setCalculado("S");
            recomendaCiclo.setEmpate(metodologiasRecomendadas.size() > 1 ? "S" : "N");
            recomendaCiclo.setCicloRecomendado(metodologiasRecomendadas.size() == 1 ?
                    metodologiasRecomendadas.get(0) :
                    String.join(", ", metodologiasRecomendadas));
            recomendaCiclo.setPontuacaoAgile(pontuacoes.get("Agile"));
            recomendaCiclo.setPontuacaoLean(pontuacoes.get("Lean"));
            recomendaCiclo.setPontuacaoCdAgile(pontuacoes.get("Continuous Delivery Agile"));
            recomendaCiclo.setPontuacaoCdLean(pontuacoes.get("Continuous Delivery Lean"));
            recomendaCiclo.setPontuacaoExploratory(pontuacoes.get("Exploratory"));
            recomendaCiclo.setPontuacaoProgram(pontuacoes.get("Program"));
            recomendaCiclo.setDataInclusao(LocalDateTime.now());
            recomendaCiclo.setDataAlteracao(LocalDateTime.now());
            recomendaCicloRepository.save(recomendaCiclo);

            Map<String, String> descricoes = descriptionService.getDescricoes(metodologiasRecomendadas);
            String mensagem = metodologiasRecomendadas.size() > 1
                    ? "Empate encontrado entre ciclo de vidas"
                    : "Recomendação calculada com sucesso";

            return new RecomendacaoDTO(metodologiasRecomendadas, pontuacoes, descricoes, mensagem);

        } catch (Exception e) {
            String erro = "Erro ao calcular recomendação: " + e.getMessage();
            if (!(e instanceof RuntimeException)) {
                registrarErro(erro, "SISTEMA", PROCESSO_ERRO_JAVA);
            }
            throw new RuntimeException(erro);
        }
    }

    // Metodo para calcular a recomendação de metodologia com base nas respostas do usuário
    public RecomendacaoDTO calcularRecomendacao(Long arquivoId) {
        try {
            Arquivo arquivo = arquivoRepository.findById(arquivoId)
                    .orElseThrow(() -> {
                        String erro = "Arquivo não encontrado";
                        registrarErro(erro, "SISTEMA", PROCESSO_ERRO_ARQUIVO);
                        return new RuntimeException(erro);
                    });

            List<Resposta> respostas = respostaRepository.findByArquivo(arquivo);

            if (respostas.isEmpty()) {
                String erro = "Nenhuma resposta encontrada para este arquivo";
                registrarErro(erro, arquivo.getUsuario(), PROCESSO_ERRO_RESPOSTA);
                RecomendaCiclo recomendaCiclo = new RecomendaCiclo();
                recomendaCiclo.setArquivo(arquivo);
                recomendaCiclo.setCalculado("N");
                recomendaCiclo.setDataInclusao(LocalDateTime.now());
                recomendaCiclo.setDataAlteracao(LocalDateTime.now());
                recomendaCicloRepository.save(recomendaCiclo);
                throw new RuntimeException(erro);
            }

            if (respostas.size() != 10) {
                String erro = "Número incorreto de respostas. Esperado: 10, Encontrado: " + respostas.size();
                registrarErro(erro, arquivo.getUsuario(), PROCESSO_ERRO_VALIDACAO);
                RecomendaCiclo recomendaCiclo = new RecomendaCiclo();
                recomendaCiclo.setArquivo(arquivo);
                recomendaCiclo.setCalculado("N");
                recomendaCiclo.setDataInclusao(LocalDateTime.now());
                recomendaCiclo.setDataAlteracao(LocalDateTime.now());
                recomendaCicloRepository.save(recomendaCiclo);
                throw new RuntimeException(erro);
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

                pontuacoes.put("Agile",
                        pontuacoes.get("Agile") + PONTUACOES.get("Agile").get(numeroQuestao).get(alternativa));
                pontuacoes.put("Lean",
                        pontuacoes.get("Lean") + PONTUACOES.get("Lean").get(numeroQuestao).get(alternativa));
                pontuacoes.put("Continuous Delivery Agile",
                        pontuacoes.get("Continuous Delivery Agile") + PONTUACOES.get("ContinuousDeliveryAgile").get(numeroQuestao).get(alternativa));
                pontuacoes.put("Continuous Delivery Lean",
                        pontuacoes.get("Continuous Delivery Lean") + PONTUACOES.get("ContinuousDeliveryLean").get(numeroQuestao).get(alternativa));
                pontuacoes.put("Exploratory",
                        pontuacoes.get("Exploratory") + PONTUACOES.get("Exploratory").get(numeroQuestao).get(alternativa));
                pontuacoes.put("Program",
                        pontuacoes.get("Program") + PONTUACOES.get("Program").get(numeroQuestao).get(alternativa));
            }

            int maxPontuacao = Collections.max(pontuacoes.values());
            List<String> metodologiasRecomendadas = pontuacoes.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxPontuacao)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // Salvar a recomendação
            RecomendaCiclo recomendaCiclo = new RecomendaCiclo();
            recomendaCiclo.setArquivo(arquivo);
            recomendaCiclo.setCalculado("S");
            recomendaCiclo.setEmpate(metodologiasRecomendadas.size() > 1 ? "S" : "N");
            recomendaCiclo.setCicloRecomendado(metodologiasRecomendadas.size() == 1 ?
                    metodologiasRecomendadas.get(0) :
                    String.join(", ", metodologiasRecomendadas));
            recomendaCiclo.setPontuacaoAgile(pontuacoes.get("Agile"));
            recomendaCiclo.setPontuacaoLean(pontuacoes.get("Lean"));
            recomendaCiclo.setPontuacaoCdAgile(pontuacoes.get("Continuous Delivery Agile"));
            recomendaCiclo.setPontuacaoCdLean(pontuacoes.get("Continuous Delivery Lean"));
            recomendaCiclo.setPontuacaoExploratory(pontuacoes.get("Exploratory"));
            recomendaCiclo.setPontuacaoProgram(pontuacoes.get("Program"));
            recomendaCiclo.setDataInclusao(LocalDateTime.now());
            recomendaCiclo.setDataAlteracao(LocalDateTime.now());
            recomendaCicloRepository.save(recomendaCiclo);

            Map<String, String> descricoes = descriptionService.getDescricoes(metodologiasRecomendadas);
            String mensagem = metodologiasRecomendadas.size() > 1
                    ? "Empate encontrado entre metodologias"
                    : "Recomendação calculada com sucesso";

            return new RecomendacaoDTO(metodologiasRecomendadas, pontuacoes, descricoes, mensagem);

        } catch (Exception e) {
            String erro = "Erro ao calcular recomendação: " + e.getMessage();
            if (!(e instanceof RuntimeException)) {
                registrarErro(erro, "SISTEMA", PROCESSO_ERRO_JAVA);
            }
            throw new RuntimeException(erro);
        }
    }

    // Metodo auxiliar para registrar logs de erro no banco de dados
    private void registrarErro(String mensagem, String usuario, int idProcesso) {
        try {
            jdbcTemplate.update(
                    "CALL guru_agile.log_erro_spi(?, ?, ?)",
                    mensagem, usuario, idProcesso
            );
        } catch (Exception e) {
            System.err.println("Erro ao registrar log: " + e.getMessage());
        }
    }

    // Metodo auxiliar para extrair o número da questão da descrição da pergunta
    private int extrairNumeroQuestao(String descricao) {
        try {
            return Integer.parseInt(descricao.replace("Questão ", "").trim());
        } catch (NumberFormatException e) {
            String erro = "Formato inválido do número da questão: " + descricao;
            registrarErro(erro, "SISTEMA", PROCESSO_ERRO_VALIDACAO);
            throw new RuntimeException(erro);
        }
    }

    private void salvarRecomendacao(Map<String, Integer> pontuacoes, List<String> metodologiasRecomendadas,
                                    Arquivo arquivo, Ciclo ciclo, boolean sucesso) {
        try {
            RecomendaCiclo recomendaCiclo = new RecomendaCiclo();

            // Define arquivo ou ciclo
            recomendaCiclo.setArquivo(arquivo);
            recomendaCiclo.setCiclo(ciclo);

            // Define status do cálculo
            recomendaCiclo.setCalculado(sucesso ? "S" : "N");

            if (sucesso) {
                // Define se houve empate
                boolean temEmpate = metodologiasRecomendadas.size() > 1;
                recomendaCiclo.setEmpate(temEmpate ? "S" : "N");

                // Define ciclo recomendado (agora sempre preenchido)
                if (temEmpate) {
                    // Em caso de empate, concatena todas as metodologias com vírgula
                    recomendaCiclo.setCicloRecomendado(String.join(", ", metodologiasRecomendadas));
                } else {
                    recomendaCiclo.setCicloRecomendado(metodologiasRecomendadas.get(0));
                }

                // Define pontuações
                recomendaCiclo.setPontuacaoAgile(pontuacoes.get("Agile"));
                recomendaCiclo.setPontuacaoLean(pontuacoes.get("Lean"));
                recomendaCiclo.setPontuacaoCdAgile(pontuacoes.get("Continuous Delivery Agile"));
                recomendaCiclo.setPontuacaoCdLean(pontuacoes.get("Continuous Delivery Lean"));
                recomendaCiclo.setPontuacaoExploratory(pontuacoes.get("Exploratory"));
                recomendaCiclo.setPontuacaoProgram(pontuacoes.get("Program"));
            }

            // Define as datas
            recomendaCiclo.setDataInclusao(LocalDateTime.now());
            recomendaCiclo.setDataAlteracao(LocalDateTime.now());

            // Salva no banco
            recomendaCicloRepository.save(recomendaCiclo);

        } catch (Exception e) {
            String usuario = arquivo != null ? arquivo.getUsuario() :
                    ciclo != null ? ciclo.getUsuario() : "SISTEMA";
            registrarErro("Erro ao salvar recomendação: " + e.getMessage(), usuario, PROCESSO_ERRO_JAVA);
            throw new RuntimeException("Erro ao salvar recomendação: " + e.getMessage());
        }
    }
}