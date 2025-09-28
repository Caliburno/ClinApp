package com.dario.clinapp.dao.database;// En dao/database/ExcelBackupManager.java
import com.dario.clinapp.dao.database.ServiceManager;
import com.dario.clinapp.model.Informe;
import com.dario.clinapp.model.Paciente;
import com.dario.clinapp.model.Pago;
import com.dario.clinapp.model.Sesion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelBackupManager {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static String exportarTodasLasTablas() {
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String fileName = String.format("ClinApp_Backup_%s.xlsx", timestamp);

            Workbook workbook = new XSSFWorkbook();

            // Exportar cada tabla a una hoja separada
            exportarPacientes(workbook);
            exportarSesiones(workbook);
            exportarInformes(workbook);
            exportarPagos(workbook);

            // Guardar el archivo
            try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
                workbook.write(fileOut);
            }

            workbook.close();

            System.out.println("Backup Excel creado: " + fileName);
            return fileName;

        } catch (Exception e) {
            throw new RuntimeException("Error creando backup Excel", e);
        }
    }

    private static void exportarPacientes(Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet("Pacientes");

        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nombre", "Tipo Paciente", "Tipo Sesión", "Precio por Sesión", "Deuda", "Notas"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            // Estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }

        // Obtener datos y llenar filas
        List<Paciente> pacientes = ServiceManager.getPacienteDAO().findAll();

        int rowNum = 1;
        for (Paciente paciente : pacientes) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(paciente.getId());
            row.createCell(1).setCellValue(paciente.getNombre());
            row.createCell(2).setCellValue(paciente.getTipoPaciente().toString());
            row.createCell(3).setCellValue(paciente.getTipoSesion().toString());
            row.createCell(4).setCellValue(paciente.getPrecioPorSesion());
            row.createCell(5).setCellValue(paciente.getDeuda());
            row.createCell(6).setCellValue(paciente.getNotas() != null ? paciente.getNotas() : "");
        }

        // Autoajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void exportarSesiones(Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet("Sesiones");

        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Paciente", "Tipo Sesión", "Fecha", "Estado Pago", "Notas"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }

        List<Sesion> sesiones = ServiceManager.getSesionDAO().findAll();

        int rowNum = 1;
        for (Sesion sesion : sesiones) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(sesion.getId());
            row.createCell(1).setCellValue(sesion.getPaciente().getNombre());
            row.createCell(2).setCellValue(sesion.getTipoSesion().toString());
            row.createCell(3).setCellValue(sesion.getFecha().toString());
            row.createCell(4).setCellValue(sesion.getEstadoPagoSesion().toString());
            row.createCell(5).setCellValue(sesion.getNotas() != null ? sesion.getNotas() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void exportarInformes(Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet("Informes");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Paciente", "Tipo Informe", "Precio", "Entregado", "Saldo",
                "Estado Informe", "Estado Pago", "Notas"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }

        List<Informe> informes = ServiceManager.getInformeDAO().findAll();

        int rowNum = 1;
        for (Informe informe : informes) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(informe.getId());
            row.createCell(1).setCellValue(informe.getPaciente().getNombre());
            row.createCell(2).setCellValue(informe.getTipoInforme().toString());
            row.createCell(3).setCellValue(informe.getPrecio());
            row.createCell(4).setCellValue(informe.getEntregado());
            row.createCell(5).setCellValue(informe.getSaldo());
            row.createCell(6).setCellValue(informe.getEstadoInforme().toString());
            row.createCell(7).setCellValue(informe.getEstadoPagoInforme().toString());
            row.createCell(8).setCellValue(informe.getNotas() != null ? informe.getNotas() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void exportarPagos(Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet("Pagos");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Paciente", "Tipo Pago", "Monto", "Forma de Pago", "Fecha", "Notas"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }

        List<Pago> pagos = ServiceManager.getPagoDAO().findAll();

        int rowNum = 1;
        for (Pago pago : pagos) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(pago.getId());
            row.createCell(1).setCellValue(pago.getPaciente().getNombre());
            row.createCell(2).setCellValue(pago.getTipoPago().toString());
            row.createCell(3).setCellValue(pago.getMonto());
            row.createCell(4).setCellValue(pago.getFormaDePago().toString());
            row.createCell(5).setCellValue(pago.getFecha().toString());
            row.createCell(6).setCellValue(pago.getNotas() != null ? pago.getNotas() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}