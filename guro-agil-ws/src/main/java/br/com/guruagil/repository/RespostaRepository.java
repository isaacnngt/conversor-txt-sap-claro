package br.com.guruagil.repository;

import br.com.guruagil.entity.Arquivo;
import br.com.guruagil.entity.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, Long> {
    List<Resposta> findByArquivo(Arquivo arquivo);
}