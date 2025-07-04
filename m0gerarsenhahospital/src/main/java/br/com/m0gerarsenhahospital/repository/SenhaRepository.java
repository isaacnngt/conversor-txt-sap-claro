package br.com.m0gerarsenhahospital.repository;

import br.com.m0gerarsenhahospital.model.Senha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SenhaRepository extends JpaRepository<Senha, Long> {

    // ⚠️ CORREÇÃO: Adicionado LIMIT 1 para retornar apenas uma senha
    @Query(value = "SELECT * FROM senhas WHERE chamada = false " +
            "ORDER BY CASE WHEN tipo = 'P' THEN 0 ELSE 1 END, data_criacao ASC " +
            "LIMIT 1", nativeQuery = true)
    Optional<Senha> findProximaSenha();

    @Query(value = "SELECT gerar_codigo_senha(?1)", nativeQuery = true)
    String gerarProximoCodigo(char tipo);

    @Query("SELECT COUNT(s) FROM Senha s WHERE s.tipo = ?1 AND s.chamada = false")
    long countSenhasNaoChamadasPorTipo(char tipo);

    // Método adicional para debug - ver todas as senhas pendentes
    @Query("SELECT s FROM Senha s WHERE s.chamada = false ORDER BY " +
            "CASE WHEN s.tipo = 'P' THEN 0 ELSE 1 END, s.dataCriacao ASC")
    List<Senha> findAllSenhasPendentes();

    // Método para ver últimas senhas criadas
    @Query("SELECT s FROM Senha s ORDER BY s.dataCriacao DESC")
    List<Senha> findAllOrderByDataCriacaoDesc();
}