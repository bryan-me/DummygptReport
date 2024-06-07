package com.example.dummygpttest.controller;

import com.example.dummygpttest.service.ChatGPTService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ChatGPTService chatGPTService;
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @PostMapping("/upload")
    public ResponseEntity<ByteArrayResource> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String excelData = processExcelFile(bytes);

            String reportData = chatGPTService.getReport(excelData);

            byte[] pdfReport = generatePdfReport(reportData);

            ByteArrayResource resource = new ByteArrayResource(pdfReport);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfReport.length)
                    .body(resource);
        }  catch (IOException e) {
            logger.error("IO Exception: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (JRException e) {
            logger.error("JasperReports Exception: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private String processExcelFile(byte[] fileData) throws IOException {
        Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileData));
        Sheet sheet = workbook.getSheetAt(0);
        StringBuilder reportData = new StringBuilder();

        for (Row row : sheet) {
            for (Cell cell : row) {
                reportData.append(cell.toString()).append(" ");
            }
            reportData.append("\n");
        }

        ((XSSFWorkbook) workbook).close();
        return reportData.toString();
    }

    private byte[] generatePdfReport(String reportData) throws JRException {
        JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream("/report_template.jrxml"));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportData", reportData);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
