package br.com.m0gerarsenhahospital.controller;

import br.com.m0gerarsenhahospital.model.Senha;
import br.com.m0gerarsenhahospital.repository.SenhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/senhas")
@CrossOrigin(origins = "*")
public class SenhaController {

    @Autowired
    private SenhaRepository senhaRepository;

    @PostMapping("/gerar")
    public ResponseEntity<Map<String, Object>> gerarSenha(@RequestParam char tipo) {
        try {
            // Validar tipo
            if (tipo != 'N' && tipo != 'P') {
                Map<String, Object> error = new HashMap<>();
                error.put("erro", "Tipo deve ser 'N' (Normal) ou 'P' (Prioritária)");
                return ResponseEntity.badRequest().body(error);
            }

            // Gerar código usando função do banco
            String codigo = senhaRepository.gerarProximoCodigo(tipo);

            // Criar e salvar senha
            Senha senha = new Senha(codigo, tipo);
            senha = senhaRepository.save(senha);

            // Contar senhas pendentes
            long senhasPendentesNormal = senhaRepository.countSenhasNaoChamadasPorTipo('N');
            long senhasPendentesPrioritaria = senhaRepository.countSenhasNaoChamadasPorTipo('P');

            Map<String, Object> response = new HashMap<>();
            response.put("senha", senha);
            response.put("senhasPendentes", Map.of(
                    "normal", senhasPendentesNormal,
                    "prioritaria", senhasPendentesPrioritaria
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro interno do servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/proxima")
    public ResponseEntity<Map<String, Object>> chamarProximaSenha() {
        try {
            // ⚠️ CORREÇÃO: Melhor tratamento da query
            Optional<Senha> proximaSenhaOpt = senhaRepository.findProximaSenha();

            if (proximaSenhaOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("mensagem", "Não há senhas pendentes");
                return ResponseEntity.ok(response);
            }

            // Marcar como chamada
            Senha senha = proximaSenhaOpt.get();
            senha.setChamada(true);
            senha = senhaRepository.save(senha);

            // Contar senhas pendentes após a chamada
            long senhasPendentesNormal = senhaRepository.countSenhasNaoChamadasPorTipo('N');
            long senhasPendentesPrioritaria = senhaRepository.countSenhasNaoChamadasPorTipo('P');

            Map<String, Object> response = new HashMap<>();
            response.put("senha", senha);
            response.put("senhasPendentes", Map.of(
                    "normal", senhasPendentesNormal,
                    "prioritaria", senhasPendentesPrioritaria
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // ⚠️ CORREÇÃO: Log mais detalhado do erro
            System.err.println("Erro ao chamar próxima senha: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro interno do servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            long senhasPendentesNormal = senhaRepository.countSenhasNaoChamadasPorTipo('N');
            long senhasPendentesPrioritaria = senhaRepository.countSenhasNaoChamadasPorTipo('P');

            Map<String, Object> response = new HashMap<>();
            response.put("senhasPendentes", Map.of(
                    "normal", senhasPendentesNormal,
                    "prioritaria", senhasPendentesPrioritaria,
                    "total", senhasPendentesNormal + senhasPendentesPrioritaria
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro interno do servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ⚠️ NOVO: Endpoint para debug
    @GetMapping("/debug/pendentes")
    public ResponseEntity<List<Senha>> getSenhasPendentes() {
        try {
            List<Senha> senhasPendentes = senhaRepository.findAllSenhasPendentes();
            return ResponseEntity.ok(senhasPendentes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ⚠️ NOVO: Endpoint para ver todas as senhas
    @GetMapping("/debug/todas")
    public ResponseEntity<List<Senha>> getTodasSenhas() {
        try {
            List<Senha> todasSenhas = senhaRepository.findAllOrderByDataCriacaoDesc();
            return ResponseEntity.ok(todasSenhas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}