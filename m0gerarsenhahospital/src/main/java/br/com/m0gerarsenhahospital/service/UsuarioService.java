package br.com.m0gerarsenhahospital.service;

import br.com.m0gerarsenhahospital.model.Usuario;
import br.com.m0gerarsenhahospital.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Autenticar usuário
    public Optional<Usuario> autenticar(String loginUsuario, String senhaPlana) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByLoginUsuarioAndAtivoTrue(loginUsuario);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(senhaPlana, usuario.getSenha())) {
                return Optional.of(usuario);
            }
        }

        return Optional.empty();
    }

    // Cadastrar novo usuário
    public Usuario cadastrarUsuario(Usuario novoUsuario, Long idUsuarioCadastrante) {
        // Validações
        validarDadosUsuario(novoUsuario);

        // Verificar se login já existe
        if (usuarioRepository.existsByLoginUsuarioAndAtivoTrue(novoUsuario.getLoginUsuario())) {
            throw new IllegalArgumentException("Login já está em uso");
        }

        // Verificar se email já existe
        if (usuarioRepository.existsByEmailAndAtivoTrue(novoUsuario.getEmail())) {
            throw new IllegalArgumentException("Email já está em uso");
        }

        // Criptografar senha
        novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));

        // Definir dados de auditoria
        novoUsuario.setUsuarioQueCadastrou(idUsuarioCadastrante);
        novoUsuario.setDataCriacao(LocalDateTime.now());
        novoUsuario.setDataAtualizacao(LocalDateTime.now());
        novoUsuario.setAtivo(true);

        return usuarioRepository.save(novoUsuario);
    }

    // Atualizar usuário
    public Usuario atualizarUsuario(Long id, Usuario dadosAtualizacao, Long idUsuarioAtualizador) {
        Usuario usuarioExistente = buscarPorId(id);

        // Verificar se o login mudou e se o novo já existe
        if (!usuarioExistente.getLoginUsuario().equals(dadosAtualizacao.getLoginUsuario())) {
            if (usuarioRepository.existsByLoginUsuarioAndAtivoTrue(dadosAtualizacao.getLoginUsuario())) {
                throw new IllegalArgumentException("Login já está em uso");
            }
            usuarioExistente.setLoginUsuario(dadosAtualizacao.getLoginUsuario());
        }

        // Verificar se o email mudou e se o novo já existe
        if (!usuarioExistente.getEmail().equals(dadosAtualizacao.getEmail())) {
            if (usuarioRepository.existsByEmailAndAtivoTrue(dadosAtualizacao.getEmail())) {
                throw new IllegalArgumentException("Email já está em uso");
            }
            usuarioExistente.setEmail(dadosAtualizacao.getEmail());
        }

        // Atualizar tipo de usuário
        usuarioExistente.setSnAdministrador(dadosAtualizacao.getSnAdministrador());

        // Atualizar dados de auditoria
        usuarioExistente.setDataAtualizacao(LocalDateTime.now());

        return usuarioRepository.save(usuarioExistente);
    }

    // Atualizar senha
    public void atualizarSenha(Long idUsuario, String senhaAtual, String novaSenha) {
        Usuario usuario = buscarPorId(idUsuario);

        // Verificar senha atual
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        // Validar nova senha
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new IllegalArgumentException("Nova senha deve ter pelo menos 6 caracteres");
        }

        // Atualizar senha
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setDataAtualizacao(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    // Inativar usuário (soft delete)
    public void inativarUsuario(Long id) {
        Usuario usuario = buscarPorId(id);

        // Verificar se não é o último administrador
        if (usuario.isAdministrador()) {
            if (!usuarioRepository.podeRemoverAdministrador(id)) {
                throw new IllegalArgumentException("Não é possível inativar o último administrador do sistema");
            }
        }

        usuario.setAtivo(false);
        usuario.setDataAtualizacao(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    // Reativar usuário
    public void reativarUsuario(Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setAtivo(true);
            usuario.setDataAtualizacao(LocalDateTime.now());
            usuarioRepository.save(usuario);
        } else {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
    }

    // Buscar usuário por ID
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    // Buscar usuário por login
    public Optional<Usuario> buscarPorLogin(String loginUsuario) {
        return usuarioRepository.findByLoginUsuarioAndAtivoTrue(loginUsuario);
    }

    // Listar todos os usuários ativos
    public List<Usuario> listarTodosAtivos() {
        return usuarioRepository.findAllAtivos();
    }

    // Listar administradores
    public List<Usuario> listarAdministradores() {
        return usuarioRepository.findByTipoAndAtivoTrue('S');
    }

    // Listar usuários comuns
    public List<Usuario> listarUsuariosComuns() {
        return usuarioRepository.findByTipoAndAtivoTrue('N');
    }

    // Pesquisar usuários
    public List<Usuario> pesquisarUsuarios(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarTodosAtivos();
        }
        return usuarioRepository.findByTermoPesquisa(termo.trim());
    }

    // ⚠️ MÉTODO CORRIGIDO: Obter estatísticas (sem query problemática)
    public EstatisticasUsuarios obterEstatisticas() {
        try {
            // Contar total de usuários ativos
            long total = usuarioRepository.findAllAtivos().size();

            // Contar administradores ativos
            long administradores = usuarioRepository.countAdministradoresAtivos();

            // Calcular usuários comuns
            long comuns = total - administradores;

            return new EstatisticasUsuarios(
                    (int) total,
                    (int) administradores,
                    (int) comuns
            );
        } catch (Exception e) {
            // Em caso de erro, retornar estatísticas zeradas
            return new EstatisticasUsuarios(0, 0, 0);
        }
    }

    // Validações privadas
    private void validarDadosUsuario(Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }

        if (usuario.getLoginUsuario() == null || usuario.getLoginUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("Login é obrigatório");
        }

        if (usuario.getLoginUsuario().length() < 3) {
            throw new IllegalArgumentException("Login deve ter pelo menos 3 caracteres");
        }

        if (usuario.getSenha() == null || usuario.getSenha().length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }

        if (usuario.getSnAdministrador() != 'S' && usuario.getSnAdministrador() != 'N') {
            throw new IllegalArgumentException("Tipo de usuário deve ser 'S' (Administrador) ou 'N' (Comum)");
        }
    }

    // Classe interna para estatísticas
    public static class EstatisticasUsuarios {
        private int total;
        private int administradores;
        private int comuns;

        public EstatisticasUsuarios(int total, int administradores, int comuns) {
            this.total = total;
            this.administradores = administradores;
            this.comuns = comuns;
        }

        // Getters
        public int getTotal() { return total; }
        public int getAdministradores() { return administradores; }
        public int getComuns() { return comuns; }

        @Override
        public String toString() {
            return "EstatisticasUsuarios{" +
                    "total=" + total +
                    ", administradores=" + administradores +
                    ", comuns=" + comuns +
                    '}';
        }
    }
}