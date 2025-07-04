package br.com.m0gerarsenhahospital.repository;

import br.com.m0gerarsenhahospital.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuário por login (para autenticação)
    @Query("SELECT u FROM Usuario u WHERE u.loginUsuario = :login AND u.ativo = true")
    Optional<Usuario> findByLoginUsuarioAndAtivoTrue(@Param("login") String loginUsuario);

    // Buscar usuário por email
    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.ativo = true")
    Optional<Usuario> findByEmailAndAtivoTrue(@Param("email") String email);

    // Verificar se login já existe (para cadastro)
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.loginUsuario = :login AND u.ativo = true")
    boolean existsByLoginUsuarioAndAtivoTrue(@Param("login") String loginUsuario);

    // Verificar se email já existe (para cadastro)
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.email = :email AND u.ativo = true")
    boolean existsByEmailAndAtivoTrue(@Param("email") String email);

    // Listar todos os usuários ativos
    @Query("SELECT u FROM Usuario u WHERE u.ativo = true ORDER BY u.dataCriacao DESC")
    List<Usuario> findAllAtivos();

    // Listar usuários por tipo (administradores ou comuns)
    @Query("SELECT u FROM Usuario u WHERE u.snAdministrador = :tipo AND u.ativo = true ORDER BY u.dataCriacao DESC")
    List<Usuario> findByTipoAndAtivoTrue(@Param("tipo") char tipo);

    // Contar administradores ativos
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.snAdministrador = 'S' AND u.ativo = true")
    long countAdministradoresAtivos();

    // Buscar usuários cadastrados por um usuário específico
    @Query("SELECT u FROM Usuario u WHERE u.usuarioQueCadastrou = :idCadastrante AND u.ativo = true ORDER BY u.dataCriacao DESC")
    List<Usuario> findByUsuarioQueCadastrouAndAtivoTrue(@Param("idCadastrante") Long idCadastrante);

    // Buscar usuários por termo de pesquisa (email ou login)
    @Query("SELECT u FROM Usuario u WHERE u.ativo = true AND " +
            "(LOWER(u.email) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(u.loginUsuario) LIKE LOWER(CONCAT('%', :termo, '%'))) " +
            "ORDER BY u.dataCriacao DESC")
    List<Usuario> findByTermoPesquisa(@Param("termo") String termo);

    // Verificar se usuário pode ser deletado (não é o último administrador)
    @Query("SELECT CASE WHEN COUNT(u) > 1 THEN true ELSE false END " +
            "FROM Usuario u WHERE u.snAdministrador = 'S' AND u.ativo = true AND u.id != :idUsuario")
    boolean podeRemoverAdministrador(@Param("idUsuario") Long idUsuario);
}