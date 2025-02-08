package br.com.guruagil.controller;

import br.com.guruagil.dto.ImportacaoResultadoDTO;
import br.com.guruagil.dto.RecomendacaoDTO;
import br.com.guruagil.service.MetodologiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/metodologia")
@CrossOrigin(origins = "*")
public class MetodologiaController {

    private final MetodologiaService metodologiaService;

    public MetodologiaController(MetodologiaService metodologiaService) {
        this.metodologiaService = metodologiaService;
    }

    @PostMapping("/importar")
    public ResponseEntity<ImportacaoResultadoDTO> importarArquivo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("usuario") String usuario) {
        try {
            ImportacaoResultadoDTO resultado = metodologiaService.processarArquivo(file, usuario);
            return resultado.isSucesso()
                    ? ResponseEntity.ok(resultado)
                    : ResponseEntity.badRequest().body(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ImportacaoResultadoDTO(false, e.getMessage(), null));
        }
    }

    @GetMapping("/recomendacao/{id}")
    public ResponseEntity<RecomendacaoDTO> obterRecomendacao(@PathVariable Long id) {
        try {
            RecomendacaoDTO recomendacao = metodologiaService.calcularRecomendacao(id);
            return ResponseEntity.ok(recomendacao);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new RecomendacaoDTO(null, null, e.getMessage()));
        }
    }
}