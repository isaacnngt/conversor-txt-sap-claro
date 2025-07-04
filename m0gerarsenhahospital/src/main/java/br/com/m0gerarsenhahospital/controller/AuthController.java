package br.com.m0gerarsenhahospital.controller;

import br.com.m0gerarsenhahospital.model.Usuario;
import br.com.m0gerarsenhahospital.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    // Login do usuário
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Validar dados de entrada
            if (loginRequest.getLoginUsuario() == null || loginRequest.getLoginUsuario().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Login é obrigatório"));
            }

            if (loginRequest.getSenha() == null || loginRequest.getSenha().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Senha é obrigatória"));
            }

            // Tentar autenticar
            Optional<Usuario> usuarioOpt = usuarioService.autenticar(
                    loginRequest.getLoginUsuario(),
                    loginRequest.getSenha()
            );

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                // Criar resposta de sucesso
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", true);
                response.put("mensagem", "Login realizado com sucesso");
                response.put("usuario", Map.of(
                        "id", usuario.getId(),
                        "email", usuario.getEmail(),
                        "loginUsuario", usuario.getLoginUsuario(),
                        "isAdministrador", usuario.isAdministrador(),
                        "tipoUsuario", usuario.getTipoUsuario(),
                        "dataCriacao", usuario.getDataCriacao()
                ));

                return ResponseEntity.ok(response);
            } else {
                // Credenciais inválidas
                return ResponseEntity.status(401).body(Map.of(
                        "erro", "Login ou senha incorretos"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "erro", "Erro interno do servidor: " + e.getMessage()
            ));
        }
    }

    // Verificar se usuário está logado (validar sessão)
    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificarSessao(@RequestParam Long userId) {
        try {
            Usuario usuario = usuarioService.buscarPorId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("valido", true);
            response.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "email", usuario.getEmail(),
                    "loginUsuario", usuario.getLoginUsuario(),
                    "isAdministrador", usuario.isAdministrador(),
                    "tipoUsuario", usuario.getTipoUsuario()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "erro", "Sessão inválida",
                    "valido", false
            ));
        }
    }

    // Logout (placeholder - em uma implementação real seria mais complexo)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("sucesso", true);
        response.put("mensagem", "Logout realizado com sucesso");

        return ResponseEntity.ok(response);
    }

    // Atualizar senha do usuário logado
    @PutMapping("/alterar-senha")
    public ResponseEntity<Map<String, Object>> alterarSenha(@RequestBody AlterarSenhaRequest request) {
        try {
            // Validar dados
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "ID do usuário é obrigatório"));
            }

            if (request.getSenhaAtual() == null || request.getSenhaAtual().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Senha atual é obrigatória"));
            }

            if (request.getNovaSenha() == null || request.getNovaSenha().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Nova senha deve ter pelo menos 6 caracteres"));
            }

            if (!request.getNovaSenha().equals(request.getConfirmarSenha())) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Nova senha e confirmação não coincidem"));
            }

            // Atualizar senha
            usuarioService.atualizarSenha(request.getUserId(), request.getSenhaAtual(), request.getNovaSenha());

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Senha alterada com sucesso"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "erro", "Erro interno do servidor: " + e.getMessage()
            ));
        }
    }

    // Classes internas para DTOs
    public static class LoginRequest {
        private String loginUsuario;
        private String senha;

        // Construtores
        public LoginRequest() {}

        public LoginRequest(String loginUsuario, String senha) {
            this.loginUsuario = loginUsuario;
            this.senha = senha;
        }

        // Getters e Setters
        public String getLoginUsuario() { return loginUsuario; }
        public void setLoginUsuario(String loginUsuario) { this.loginUsuario = loginUsuario; }

        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
    }

    public static class AlterarSenhaRequest {
        private Long userId;
        private String senhaAtual;
        private String novaSenha;
        private String confirmarSenha;

        // Construtores
        public AlterarSenhaRequest() {}

        // Getters e Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getSenhaAtual() { return senhaAtual; }
        public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }

        public String getNovaSenha() { return novaSenha; }
        public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }

        public String getConfirmarSenha() { return confirmarSenha; }
        public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }
    }
}