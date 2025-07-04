package br.com.convert_txt.service;

import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@Service
public class ConverterService {

    public Map<String, Object> analyzeData(String content) {
        List<String[]> parsedData = parseData(content);

        if (parsedData.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio ou inválido");
        }

        String[] headers = parsedData.get(0);
        List<String[]> dataRows = parsedData.subList(1, parsedData.size());

        // Criar preview (primeiros 3 registros)
        List<Map<String, String>> preview = new ArrayList<>();
        for (int i = 0; i < Math.min(3, dataRows.size()); i++) {
            Map<String, String> row = new HashMap<>();
            String[] rowData = dataRows.get(i);
            for (int j = 0; j < Math.min(headers.length, rowData.length); j++) {
                row.put(headers[j], rowData[j]);
            }
            preview.add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRecords", dataRows.size());
        result.put("totalColumns", headers.length);
        result.put("columns", Arrays.asList(headers));
        result.put("preview", preview);

        return result;
    }

    public byte[] convertToExcel(String content) throws IOException {
        List<String[]> parsedData = parseData(content);

        if (parsedData.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio ou inválido");
        }

        String[] headers = parsedData.get(0);
        List<String[]> dataRows = parsedData.subList(1, parsedData.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dados");

            // Estilo para cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Criar cabeçalho
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Adicionar dados
            for (int i = 0; i < dataRows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                String[] rowData = dataRows.get(i);

                for (int j = 0; j < Math.min(headers.length, rowData.length); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData[j]);
                }
            }

            // Auto-ajustar colunas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Limitar largura máxima
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            // Converter para bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] convertToCSV(String content) throws IOException {
        List<String[]> parsedData = parseData(content);

        if (parsedData.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio ou inválido");
        }

        StringWriter stringWriter = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            // Escrever todos os dados (cabeçalho + dados)
            for (String[] row : parsedData) {
                csvWriter.writeNext(row);
            }
        }

        return stringWriter.toString().getBytes();
    }

    private List<String[]> parseData(String content) {
        List<String[]> result = new ArrayList<>();

        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                String[] columns = line.split("\\|");
                // Limpar espaços de cada coluna
                for (int i = 0; i < columns.length; i++) {
                    columns[i] = columns[i].trim();
                }
                result.add(columns);
            }
        }

        return result;
    }
}