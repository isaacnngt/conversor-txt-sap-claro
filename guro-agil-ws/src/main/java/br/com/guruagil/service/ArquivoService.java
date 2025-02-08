package br.com.guruagil.service;

import br.com.guruagil.dto.ImportacaoResultadoDTO;
import br.com.guruagil.entity.Arquivo;
import br.com.guruagil.repository.ArquivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.opencsv.CSVReader;
import java.io.InputStreamReader;


@Service
public class ArquivoService {

    private final ArquivoRepository arquivoRepository;
    private final PerguntaService perguntaService;

    public ArquivoService(ArquivoRepository arquivoRepository, PerguntaService perguntaService) {
        this.arquivoRepository = arquivoRepository;
        this.perguntaService = perguntaService;
    }

    public ImportacaoResultadoDTO processarArquivo(MultipartFile file, String usuario) {
        try {
            if (file.isEmpty()) {
                return new ImportacaoResultadoDTO(false, "Arquivo vazio", null);
            }

            Arquivo arquivo = new Arquivo();
            arquivo.setNome(file.getOriginalFilename());
            arquivo.setUsuario(usuario);
            arquivo.setStatusImportacao("N");
            arquivo = arquivoRepository.save(arquivo);

            try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
                String[] header = reader.readNext();
                if (header == null || header.length < 5) {
                    throw new RuntimeException("Formato do arquivo inválido");
                }

                perguntaService.processarPerguntas(reader, arquivo);
            }

            arquivo.setStatusImportacao("S");
            arquivoRepository.save(arquivo);

            return new ImportacaoResultadoDTO(true, "Arquivo processado com sucesso", arquivo.getId());

        } catch (Exception e) {
            return new ImportacaoResultadoDTO(false, "Erro ao processar arquivo: " + e.getMessage(), null);
        }
    }
}
