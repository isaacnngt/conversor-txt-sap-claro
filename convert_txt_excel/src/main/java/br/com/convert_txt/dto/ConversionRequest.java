package br.com.convert_txt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ConversionRequest {
    @NotBlank(message = "Dados são obrigatórios")
    private String data;

    private String filename;
    private List<String> selectedColumns;
    private String outputFormat = "excel"; // excel ou csv

    // Constructors
    public ConversionRequest() {}

    public ConversionRequest(String data, String filename, List<String> selectedColumns, String outputFormat) {
        this.data = data;
        this.filename = filename;
        this.selectedColumns = selectedColumns;
        this.outputFormat = outputFormat;
    }

    // Getters and Setters
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public List<String> getSelectedColumns() { return selectedColumns; }
    public void setSelectedColumns(List<String> selectedColumns) { this.selectedColumns = selectedColumns; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
}
