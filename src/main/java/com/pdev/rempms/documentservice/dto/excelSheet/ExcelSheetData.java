package com.pdev.rempms.documentservice.dto.excelSheet;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ExcelSheetData {
    private String sheetName;
    private List<String> headers = new ArrayList<>();
    private List<List<String>> rows = new ArrayList<>();
}
