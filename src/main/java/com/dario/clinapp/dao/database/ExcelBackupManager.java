package com.dario.clinapp.dao.database;

import com.dario.clinapp.model.Informe;
import com.dario.clinapp.model.Paciente;
import com.dario.clinapp.model.Pago;
import com.dario.clinapp.model.Sesion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;

public class ExcelBackupManager {

    /**
     * Exports all tables to a single Excel file in the given target folder.
     * @param targetFolder the folder where the Excel will be saved
     * @return path to the created Excel file
     */
    public static Path exportarTodasLasTablas(Path targetFolder) {
        try (Workbook workbook = new XSSFWorkbook()) {

            exportarPacientes(workbook);
            exportarSesiones(workbook);
            exportarInformes(workbook);
            exportarPagos(workbook);

            Path excelPath = targetFolder.resolve("clinapp.xlsx");
            try (FileOutputStream fileOut = new FileOutputStream(excelPath.toFile())) {
                workbook.write(fileOut);
            }

            System.out.println("Backup Excel creado: " + excelPath);
            return excelPath;

        } catch (Exception e) {
            throw new RuntimeException("Error creando backup Excel", e);
        }
    }

    private static void exportarPacientes(Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet("Pacientes");
        String[] headers = {"ID", "Nombre", "Tipo Paciente", "Tipo Sesión", "Precio por Sesión", "Deuda", "Notas"};
        writeHeaderRow(workbook, sheet, headers);

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
        autoSize(sheet, headers.length);
    }

    private static void exportarSesiones(Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet("Sesiones");
        String[] headers = {"ID", "Paciente", "Tipo Sesión", "Fecha", "Estado Pago", "Notas"};
        writeHeaderRow(workbook, sheet, headers);

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
        autoSize(sheet, headers.length);
    }

    private static void exportarInformes(Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet("Informes");
        String[] headers = {"ID", "Paciente", "Tipo Informe", "Precio", "Entregado", "Saldo",
                "Estado Informe", "Estado Pago", "Notas"};
        writeHeaderRow(workbook, sheet, headers);

        List<Informe> informes = ServiceManager.getInformeDAO().findAll();
        int rowNum = 1;
        for (Informe informe : informes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(informe.getId());
            row.createCell(1).setCellValue(informe.getPaciente().getNombre());
            row.createCell(2).setCellValue(informe.getTipoInforme().toString());
            row.createCell(3).setCellValue(informe.getPrecio());
            row.createCell(4).setCellValue(informe.getSaldado());
            row.createCell(5).setCellValue(informe.getSaldo());
            row.createCell(6).setCellValue(informe.getEstadoInforme().toString());
            row.createCell(7).setCellValue(informe.getEstadoPagoInforme().toString());
            row.createCell(8).setCellValue(informe.getNotas() != null ? informe.getNotas() : "");
        }
        autoSize(sheet, headers.length);
    }

    private static void exportarPagos(Workbook workbook) throws Exception {
        Sheet sheet = workbook.createSheet("Pagos");
        String[] headers = {"ID", "Paciente", "Tipo Pago", "Monto", "Forma de Pago", "Fecha", "Notas"};
        writeHeaderRow(workbook, sheet, headers);

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
        autoSize(sheet, headers.length);
    }

    private static void writeHeaderRow(Workbook workbook, Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static void autoSize(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
