package br.com.convert_txt.controller;


import br.com.convert_txt.service.ConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/converter")
@CrossOrigin(origins = "*")
public class ConverterController {

    @Autowired
    private ConverterService converterService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "API funcionando");
        return ResponseEntity.ok(response);
    }

    // OPÇÃO 1: Upload e download direto (como estava antes)
    @PostMapping("/upload/excel")
    public ResponseEntity<ByteArrayResource> uploadToExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Arquivo vazio");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".txt")) {
                throw new IllegalArgumentException("Apenas arquivos .txt são aceitos");
            }

            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            byte[] excelData = converterService.convertToExcel(content);

            String originalName = file.getOriginalFilename().replaceAll("\\.[^.]*$", "");
            String filename = originalName + "_convertido.xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(excelData.length)
                    .body(new ByteArrayResource(excelData));

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar arquivo: " + e.getMessage());
        }
    }

    // OPÇÃO 2: Upload e salvar em diretório específico
    @PostMapping("/upload/excel/save")
    public ResponseEntity<Map<String, Object>> uploadAndSaveToPath(
            @RequestParam("file") MultipartFile file,
            @RequestParam("outputPath") String outputPath,
            @RequestParam(value = "filename", required = false) String customFilename) {

        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Arquivo vazio");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".txt")) {
                throw new IllegalArgumentException("Apenas arquivos .txt são aceitos");
            }

            // Converter dados
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            byte[] excelData = converterService.convertToExcel(content);

            // Definir nome do arquivo
            String filename;
            if (customFilename != null && !customFilename.trim().isEmpty()) {
                filename = customFilename.endsWith(".xlsx") ? customFilename : customFilename + ".xlsx";
            } else {
                String originalName = file.getOriginalFilename().replaceAll("\\.[^.]*$", "");
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                filename = originalName + "_convertido_" + timestamp + ".xlsx";
            }

            // Verificar se diretório existe, se não, criar
            Path directoryPath = Paths.get(outputPath);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Caminho completo do arquivo
            Path filePath = directoryPath.resolve(filename);

            // Salvar arquivo
            Files.write(filePath, excelData);

            // Resposta de sucesso
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Arquivo Excel salvo com sucesso");
            response.put("filePath", filePath.toAbsolutePath().toString());
            response.put("filename", filename);
            response.put("fileSize", excelData.length);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erro ao processar arquivo: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // OPÇÃO 3: Upload e salvar CSV em diretório específico
    @PostMapping("/upload/csv/save")
    public ResponseEntity<Map<String, Object>> uploadAndSaveCSVToPath(
            @RequestParam("file") MultipartFile file,
            @RequestParam("outputPath") String outputPath,
            @RequestParam(value = "filename", required = false) String customFilename) {

        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Arquivo vazio");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".txt")) {
                throw new IllegalArgumentException("Apenas arquivos .txt são aceitos");
            }

            // Converter dados
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            byte[] csvData = converterService.convertToCSV(content);

            // Definir nome do arquivo
            String filename;
            if (customFilename != null && !customFilename.trim().isEmpty()) {
                filename = customFilename.endsWith(".csv") ? customFilename : customFilename + ".csv";
            } else {
                String originalName = file.getOriginalFilename().replaceAll("\\.[^.]*$", "");
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                filename = originalName + "_convertido_" + timestamp + ".csv";
            }

            // Verificar se diretório existe, se não, criar
            Path directoryPath = Paths.get(outputPath);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Caminho completo do arquivo
            Path filePath = directoryPath.resolve(filename);

            // Salvar arquivo
            Files.write(filePath, csvData);

            // Resposta de sucesso
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Arquivo CSV salvo com sucesso");
            response.put("filePath", filePath.toAbsolutePath().toString());
            response.put("filename", filename);
            response.put("fileSize", csvData.length);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erro ao processar arquivo: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Endpoint para listar diretórios disponíveis (útil para interface)
    @GetMapping("/directories")
    public ResponseEntity<Map<String, Object>> listAvailableDirectories() {
        try {
            Map<String, Object> response = new HashMap<>();

            // Diretórios comuns do sistema
            Map<String, String> commonDirs = new HashMap<>();
            commonDirs.put("desktop", System.getProperty("user.home") + File.separator + "Desktop");
            commonDirs.put("documents", System.getProperty("user.home") + File.separator + "Documents");
            commonDirs.put("downloads", System.getProperty("user.home") + File.separator + "Downloads");
            commonDirs.put("home", System.getProperty("user.home"));
            commonDirs.put("temp", System.getProperty("java.io.tmpdir"));

            response.put("commonDirectories", commonDirs);
            response.put("currentWorkingDirectory", System.getProperty("user.dir"));
            response.put("userHome", System.getProperty("user.home"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao listar diretórios: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Preview continua igual
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewData(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Arquivo vazio");
            }

            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Map<String, Object> analysis = converterService.analyzeData(content);

            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao analisar arquivo: " + e.getMessage());
        }
    }
}