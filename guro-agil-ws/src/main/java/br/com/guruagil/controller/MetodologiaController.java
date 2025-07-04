package br.com.guruagil.controller;

import br.com.guruagil.dto.ImportacaoResultadoDTO;
import br.com.guruagil.dto.QuestionRequestDTO;
import br.com.guruagil.dto.RecomendacaoDTO;
import br.com.guruagil.service.MetodologiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;

@RestController
@RequestMapping("/metodologia")
public class MetodologiaController {

    private final MetodologiaService metodologiaService;

    public MetodologiaController(MetodologiaService metodologiaService) {
        this.metodologiaService = metodologiaService;
    }

    @PostMapping("/importar")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ImportacaoResultadoDTO> importarArquivo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("usuario") String usuario) {
        try {
            System.out.println("Iniciando importação para usuário: " + usuario);
            ImportacaoResultadoDTO resultado = metodologiaService.processarArquivo(file, usuario);
            return resultado.isSucesso()
                    ? ResponseEntity.ok(resultado)
                    : ResponseEntity.badRequest().body(resultado);
        } catch (Exception e) {
            System.out.println("Erro na importação: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ImportacaoResultadoDTO(false, e.getMessage(), null));
        }
    }

    @PostMapping("/formulario")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ImportacaoResultadoDTO> processarFormulario(@RequestBody QuestionRequestDTO request) {
        try {
            System.out.println("Processando formulário para usuário: " + request.getUsuario());
            ImportacaoResultadoDTO resultado = metodologiaService.processarFormulario(request);
            return resultado.isSucesso()
                    ? ResponseEntity.ok(resultado)
                    : ResponseEntity.badRequest().body(resultado);
        } catch (Exception e) {
            System.out.println("Erro no processamento do formulário: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ImportacaoResultadoDTO(false, e.getMessage(), null));
        }
    }

    @GetMapping("/recomendacao/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<RecomendacaoDTO> obterRecomendacao(@PathVariable Long id) {
        try {
            System.out.println("Obtendo recomendação para ID: " + id);
            RecomendacaoDTO recomendacao = metodologiaService.calcularRecomendacao(id);
            return ResponseEntity.ok(recomendacao);
        } catch (Exception e) {
            System.out.println("Erro ao obter recomendação: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new RecomendacaoDTO(
                            Collections.emptyList(),
                            new HashMap<>(),
                            new HashMap<>(),
                            "Erro: " + e.getMessage()
                    ));
        }
    }

    @GetMapping("/recomendacao-ciclo/{cicloId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<RecomendacaoDTO> obterRecomendacaoPorCiclo(@PathVariable Long cicloId) {
        try {
            System.out.println("Obtendo recomendação para ciclo ID: " + cicloId);
            RecomendacaoDTO recomendacao = metodologiaService.calcularRecomendacaoPorCiclo(cicloId);
            return ResponseEntity.ok(recomendacao);
        } catch (Exception e) {
            System.out.println("Erro ao obter recomendação por ciclo: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new RecomendacaoDTO(
                            Collections.emptyList(),
                            new HashMap<>(),
                            new HashMap<>(),
                            "Erro: " + e.getMessage()
                    ));
        }
    }
}