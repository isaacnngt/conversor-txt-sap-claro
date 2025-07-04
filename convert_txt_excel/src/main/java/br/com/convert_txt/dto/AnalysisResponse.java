package br.com.convert_txt.dto;

import java.util.List;
import java.util.Map;

public class AnalysisResponse {
    private boolean success;
    private int totalRecords;
    private int totalColumns;
    private List<String> columns;
    private List<Map<String, String>> preview;
    private long dataSize;

    // Constructors
    public AnalysisResponse() {}

    public AnalysisResponse(boolean success, int totalRecords, int totalColumns,
                            List<String> columns, List<Map<String, String>> preview, long dataSize) {
        this.success = success;
        this.totalRecords = totalRecords;
        this.totalColumns = totalColumns;
        this.columns = columns;
        this.preview = preview;
        this.dataSize = dataSize;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getTotalColumns() { return totalColumns; }
    public void setTotalColumns(int totalColumns) { this.totalColumns = totalColumns; }

    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }

    public List<Map<String, String>> getPreview() { return preview; }
    public void setPreview(List<Map<String, String>> preview) { this.preview = preview; }

    public long getDataSize() { return dataSize; }
    public void setDataSize(long dataSize) { this.dataSize = dataSize; }
}
