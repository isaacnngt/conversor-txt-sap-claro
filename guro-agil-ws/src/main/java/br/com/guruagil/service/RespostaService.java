package br.com.guruagil.service;

import br.com.guruagil.entity.Resposta;
import br.com.guruagil.repository.RespostaRepository;
import org.springframework.stereotype.Service;

@Service
public class RespostaService {

    private final RespostaRepository respostaRepository;

    public RespostaService(RespostaRepository respostaRepository) {
        this.respostaRepository = respostaRepository;
    }

    public void salvarResposta(Resposta resposta) {
        respostaRepository.save(resposta);
    }
}
