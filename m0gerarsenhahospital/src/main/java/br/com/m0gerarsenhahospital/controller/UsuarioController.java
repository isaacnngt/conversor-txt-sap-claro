// ===============================
// controller/UsuarioController.java - VERSÃO COMPLETA
// ===============================
package br.com.m0gerarsenhahospital.controller;

import br.com.m0gerarsenhahospital.model.Usuario;
import br.com.m0gerarsenhahospital.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Listar todos os usuários (apenas administradores)
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarUsuarios(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String pesquisa) {
        try {
            List<Usuario> usuarios;

            if (pesquisa != null && !pesquisa.trim().isEmpty()) {
                usuarios = usuarioService.pesquisarUsuarios(pesquisa);
            } else if ("admin".equals(tipo)) {
                usuarios = usuarioService.listarAdministradores();
            } else if ("comum".equals(tipo)) {
                usuarios = usuarioService.listarUsuariosComuns();
            } else {
                usuarios = usuarioService.listarTodosAtivos();
            }

            // Converter para DTO (sem senha)
            List<Map<String, Object>> usuariosDto = usuarios.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("usuarios", usuariosDto);
            response.put("total", usuariosDto.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "erro", "Erro interno do servidor: " + e.getMessage()
            ));
        }
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarUsuario(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);

            return ResponseEntity.ok(Map.of(
                    "usuario", convertToDto(usuario)
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "erro", "Erro interno do servidor: " + e.getMessage()
            ));
        }
    }

    // Cadastrar novo usuário (apenas administradores)
    @PostMapping
    public ResponseEntity<Map<String, Object>> cadastrarUsuario(
            @RequestBody CadastrarUsuarioRequest request,
            @RequestParam Long adminId) {
        try {
            // Validar se quem está cadastrando é administrador
            Usuario admin = usuarioService.buscarPorId(adminId);
            if (!admin.isAdministrador()) {
                return ResponseEntity.status(403).body(Map.of(
                        "erro", "Apenas administradores podem cadastrar usuários"
                ));
            }

            // Validar dados de entrada
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Email é obrigatório"));
            }

            if (request.getLoginUsuario() == null || request.getLoginUsuario().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Login é obrigatório"));
            }

            if (request.getSenha() == null || request.getSenha().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Senha deve ter pelo menos 6 caracteres"));
            }

            if (request.getTipoUsuario() == null ||
                    (!request.getTipoUsuario().equals("S") && !request.getTipoUsuario().equals("N"))) {
                return ResponseEntity.badRequest().body(Map.of(
                        "erro", "Tipo de usuário deve ser 'S' (Administrador) ou 'N' (Comum)"
                ));
            }

            // Criar novo usuário
            Usuario novoUsuario = new Usuario(
                    request.getEmail(),
                    request.getLoginUsuario(),
                    request.getSenha(),
                    request.getTipoUsuario().charAt(0),
                    adminId
            );

            Usuario usuarioCriado = usuarioService.cadastrarUsuario(novoUsuario, adminId);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Usuário cadastrado com sucesso",
                    "usuario", convertToDto(usuarioCriado)
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "erro", "Erro interno do servidor: " + e.getMessage()
            ));
        }
    }

    // Atualizar usuário (apenas administradores)
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody AtualizarUsuarioRequest request,
            @RequestParam Long adminId) {
        try {
            // Validar se quem está atualizando é administrador
            Usuario admin = usuarioService.buscarPorId(adminId);
            if (!admin.isAdministrador()) {
                return ResponseEntity.status(403).body(Map.of(
                        "erro", "Apenas administradores podem atualizar usuários"
                ));
            }

            // Validar dados de entrada
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Email é obrigatório"));
            }

            if (request.getLoginUsuario() == null || request.getLoginUsuario().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Login é obrigatório"));
            }

            if (request.getTipoUsuario() == null ||
                    (!request.getTipoUsuario().equals("S") && !request.getTipoUsuario().equals("N"))) {
                return ResponseEntity.badRequest().body(Map.of(
                        "erro", "Tipo de usuário deve ser 'S' (Administrador) ou 'N' (Comum)"
                ));
            }

            // Criar objeto com dados atualizados
            Usuario dadosAtualizacao = new Usuario();
            dadosAtualizacao.setEmail(request.getEmail());
            dadosAtualizacao.setLoginUsuario(request.getLoginUsuario());
            dadosAtualizacao.setSnAdministrador(request.getTipoUsuario().charAt(0));

            Usuario usuarioAtualizado = usuarioService.atualizarUsuario(id, dadosAtualizacao, adminId);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Usuário atualizado com sucesso",
                    "usuario", convertToDto(usuarioAtualizado)
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "erro", "Erro interno do servidor: " + e.getMessage()
            ));
        }
    }

    // Inativar usuário (apenas administradores)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> inativarUsuario(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        try {
            // Validar se quem está inativando é administrador
            Usuario admin = usuarioService.buscarPorId(adminId);
            if (!admin.isAdministrador()) {
                return ResponseEntity.status(403).body(Map.of(
                        "erro", "Apenas administradores podem inativar usuários"
                ));
            }

            // Não permitir que o administrador inative a si mesmo
            if (id.equals(adminId)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "erro", "Você não pode inativar sua própria conta"
                ));
            }

            usuarioService.inativarUsuario(id);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Usuário inativado com sucesso"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "erro", "Erro interno do servidor: " + e.getMessage()
            ));
        }
    }

    // Obter estatísticas dos usuários
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        try {
            UsuarioService.EstatisticasUsuarios stats = usuarioService.obterEstatisticas();

            return ResponseEntity.ok(Map.of(
                    "estatisticas", Map.of(
                            "total", stats.getTotal(),
                            "administradores", stats.getAdministradores(),
                            "comuns", stats.getComuns()
                    )
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "erro", "Erro interno do servidor: " + e.getMessage()
            ));
        }
    }

    // Método auxiliar para converter Usuario para DTO (sem senha)
    private Map<String, Object> convertToDto(Usuario usuario) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", usuario.getId());
        dto.put("email", usuario.getEmail());
        dto.put("loginUsuario", usuario.getLoginUsuario());
        dto.put("isAdministrador", usuario.isAdministrador());
        dto.put("tipoUsuario", usuario.getTipoUsuario());
        dto.put("snAdministrador", usuario.getSnAdministrador());
        dto.put("ativo", usuario.isAtivo());
        dto.put("dataCriacao", usuario.getDataCriacao());
        dto.put("dataAtualizacao", usuario.getDataAtualizacao());
        dto.put("usuarioQueCadastrou", usuario.getUsuarioQueCadastrou());

        return dto;
    }

    // Classes internas para DTOs
    public static class CadastrarUsuarioRequest {
        private String email;
        private String loginUsuario;
        private String senha;
        private String tipoUsuario; // "S" ou "N"

        // Construtores
        public CadastrarUsuarioRequest() {}

        // Getters e Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getLoginUsuario() { return loginUsuario; }
        public void setLoginUsuario(String loginUsuario) { this.loginUsuario = loginUsuario; }

        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }

        public String getTipoUsuario() { return tipoUsuario; }
        public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    }

    public static class AtualizarUsuarioRequest {
        private String email;
        private String loginUsuario;
        private String tipoUsuario; // "S" ou "N"

        // Construtores
        public AtualizarUsuarioRequest() {}

        // Getters e Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getLoginUsuario() { return loginUsuario; }
        public void setLoginUsuario(String loginUsuario) { this.loginUsuario = loginUsuario; }

        public String getTipoUsuario() { return tipoUsuario; }
        public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    }
}