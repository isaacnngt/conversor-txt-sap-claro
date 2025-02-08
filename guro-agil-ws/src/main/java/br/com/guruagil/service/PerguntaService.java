package br.com.guruagil.service;

import br.com.guruagil.dto.PerguntaDTO;
import br.com.guruagil.dto.RecomendacaoDTO;
import br.com.guruagil.entity.Arquivo;
import br.com.guruagil.entity.Pergunta;
import br.com.guruagil.repository.PerguntaRepository;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PerguntaService {

    private final PerguntaRepository perguntaRepository;

    public PerguntaService(PerguntaRepository perguntaRepository) {
        this.perguntaRepository = perguntaRepository;
    }

    public void processarPerguntas(CSVReader reader, Arquivo arquivo) {
        try {
            List<Pergunta> perguntas = new ArrayList<>();
            String[] linha;

            while ((linha = reader.readNext()) != null) {
                if (linha.length >= 5) {
                    Pergunta pergunta = new Pergunta();
                    pergunta.setDescricao(linha[0]);
                    pergunta.setArquivo(arquivo);
                    perguntas.add(pergunta);
                }
            }

            if (perguntas.size() != 10) {
                throw new RuntimeException("O arquivo deve conter exatamente 10 perguntas");
            }

            perguntaRepository.saveAll(perguntas);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar perguntas: " + e.getMessage());
        }
    }

    public RecomendacaoDTO calcularRecomendacao(Long arquivoId) {
        // Implementar lógica de cálculo da recomendação
        return null;
    }
}