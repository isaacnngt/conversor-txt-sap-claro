package br.com.convert_txt.controller;

import br.com.convert_txt.service.ConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebController {

    @Autowired
    private ConverterService converterService;

    @GetMapping("/")
    public String index(Model model) {
        // Carregar diretórios comuns
        Map<String, String> commonDirectories = getCommonDirectories();
        model.addAttribute("commonDirectories", commonDirectories);
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("outputPath") String outputPath,
                             @RequestParam("outputFormat") String outputFormat,
                             @RequestParam(value = "customFilename", required = false) String customFilename,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        try {
            // Validações
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor, selecione um arquivo!");
                return "redirect:/";
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".txt")) {
                redirectAttributes.addFlashAttribute("error", "Apenas arquivos .txt são aceitos!");
                return "redirect:/";
            }

            if (outputPath == null || outputPath.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor, especifique o diretório de destino!");
                return "redirect:/";
            }

            // Ler conteúdo do arquivo
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            // Primeiro, fazer análise para preview
            Map<String, Object> analysis = converterService.analyzeData(content);

            // Converter dados
            byte[] convertedData;
            String extension;
            if ("csv".equals(outputFormat)) {
                convertedData = converterService.convertToCSV(content);
                extension = ".csv";
            } else {
                convertedData = converterService.convertToExcel(content);
                extension = ".xlsx";
            }

            // Definir nome do arquivo
            String filename;
            if (customFilename != null && !customFilename.trim().isEmpty()) {
                filename = customFilename.endsWith(extension) ? customFilename : customFilename + extension;
            } else {
                String originalName = file.getOriginalFilename().replaceAll("\\.[^.]*$", "");
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                filename = originalName + "_convertido_" + timestamp + extension;
            }

            // Verificar se diretório existe, se não, criar
            Path directoryPath = Paths.get(outputPath);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Caminho completo do arquivo
            Path filePath = directoryPath.resolve(filename);

            // Salvar arquivo
            Files.write(filePath, convertedData);

            // Adicionar dados para exibição de sucesso
            model.addAttribute("success", true);
            model.addAttribute("message", "Arquivo " + outputFormat.toUpperCase() + " salvo com sucesso!");
            model.addAttribute("filePath", filePath.toAbsolutePath().toString());
            model.addAttribute("filename", filename);
            model.addAttribute("fileSize", formatFileSize(convertedData.length));
            model.addAttribute("analysis", analysis);
            model.addAttribute("commonDirectories", getCommonDirectories());

            return "index";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao processar arquivo: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/preview")
    @ResponseBody
    public Map<String, Object> previewFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("error", "Arquivo vazio");
                return response;
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".txt")) {
                response.put("error", "Apenas arquivos .txt são aceitos");
                return response;
            }

            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Map<String, Object> analysis = converterService.analyzeData(content);

            response.put("success", true);
            response.put("analysis", analysis);

        } catch (Exception e) {
            response.put("error", "Erro ao analisar arquivo: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadDirect(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("format") String format) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            byte[] convertedData;
            String contentType;
            String extension;

            if ("csv".equals(format)) {
                convertedData = converterService.convertToCSV(content);
                contentType = "text/csv";
                extension = ".csv";
            } else {
                convertedData = converterService.convertToExcel(content);
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                extension = ".xlsx";
            }

            String originalName = file.getOriginalFilename().replaceAll("\\.[^.]*$", "");
            String filename = originalName + "_convertido" + extension;

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(convertedData.length)
                    .body(new ByteArrayResource(convertedData));

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/api/directories")
    @ResponseBody
    public Map<String, Object> getDirectories() {
        Map<String, Object> response = new HashMap<>();
        response.put("commonDirectories", getCommonDirectories());
        response.put("currentWorkingDirectory", System.getProperty("user.dir"));
        response.put("userHome", System.getProperty("user.home"));
        return response;
    }

    private Map<String, String> getCommonDirectories() {
        Map<String, String> directories = new HashMap<>();
        String userHome = System.getProperty("user.home");

        directories.put("desktop", userHome + File.separator + "Desktop");
        directories.put("documents", userHome + File.separator + "Documents");
        directories.put("downloads", userHome + File.separator + "Downloads");
        directories.put("home", userHome);
        directories.put("temp", System.getProperty("java.io.tmpdir"));

        return directories;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}