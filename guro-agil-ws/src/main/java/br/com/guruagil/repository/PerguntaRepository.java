package br.com.guruagil.repository;

import br.com.guruagil.entity.Arquivo;
import br.com.guruagil.entity.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {
    List<Pergunta> findByArquivo(Arquivo arquivo);
}