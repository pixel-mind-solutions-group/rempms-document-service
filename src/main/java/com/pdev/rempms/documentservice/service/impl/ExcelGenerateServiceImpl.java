package com.pdev.rempms.documentservice.service.impl;

import com.pdev.rempms.documentservice.dto.excelSheet.CvExcelDTO;
import com.pdev.rempms.documentservice.dto.excelSheet.ExcelSheetData;
import com.pdev.rempms.documentservice.exception.BaseException;
import com.pdev.rempms.documentservice.service.ExcelGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class ExcelGenerateServiceImpl implements ExcelGenerateService {

    @Override
    public InputStreamResource createCvDetailsExcelSheet(List<CvExcelDTO> cvExcelRequest) throws IOException {

        // Columns
        String[] columns = {"Count", "Name", "Email", "Description", "Cv url"};

        // Create a ne work book object
        Workbook workbook = new XSSFWorkbook();

        // Create a sheet
        Sheet sheet = workbook.createSheet("Cvs Details");

        // Format header values
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLUE.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create headers
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create rows with cv data
        int rowNum = 1;
        for (CvExcelDTO cvExcelDTO : cvExcelRequest) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 1);
            row.createCell(1).setCellValue(cvExcelDTO.getName());
            row.createCell(2).setCellValue(cvExcelDTO.getEmail());
            row.createCell(3).setCellValue(cvExcelDTO.getDescription());
            row.createCell(4).setCellValue(cvExcelDTO.getCvUrl());
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream.close();
        InputStreamResource inputStreamResource = new InputStreamResource(byteArrayInputStream);
        byteArrayInputStream.close();

        return inputStreamResource;
    }


    public static ByteArrayInputStream generateExcel(ExcelSheetData excelData) {
        log.info("ExcelGenerator.generateExcel() => started");
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(excelData.getSheetName());
            log.info("Generated excel sheet for sheet: {}", excelData.getSheetName());

            List<String> headers = excelData.getHeaders();
            List<List<String>> rows = excelData.getRows();

            if (headers == null || headers.isEmpty()) {
                log.warn("Excel generation failed: No headers defined.");
                throw new BaseException(400, "Excel table headers not found");
            }

            //table header font style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            //table row style
            CellStyle rowStyle = workbook.createCellStyle();
            rowStyle.setAlignment(HorizontalAlignment.CENTER);
            rowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            rowStyle.setWrapText(true);

            //writing table header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerCellStyle);
            }

            //Data rows cell writing
            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                List<String> rowData = rows.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData.get(j) == null ? "" : rowData.get(j));
                    cell.setCellStyle(rowStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                try {
                    sheet.autoSizeColumn(i);
                } catch (Exception e) {
                    log.warn("Auto-size failed for column {} ({}): {}", i, headers.get(i), e.getMessage());
                }
            }

            workbook.write(outputStream);
            log.info("Excel sheet generated with rows :{}", rows.size());
            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (IOException e) {
            throw new BaseException(500, "Error while downloading the excel");
        }
    }
}
