package com.dario.clinapp.dao.database;

// En dao/database/BackupManager.java
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.dario.clinapp.dao.database.*;

public class BackupManager {

    private static final String DATABASE_FILE = "clinapp.db";
    private static final String BACKUP_FOLDER = "backups";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Crear backup local de la base de datos
     */
    public static String createLocalBackup() {
        try {
            // Crear carpeta de backups si no existe
            Path backupDir = Paths.get(BACKUP_FOLDER);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            // Generar nombre del archivo de backup
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String backupFileName = String.format("clinapp_backup_%s.zip", timestamp);
            Path backupPath = backupDir.resolve(backupFileName);

            // Crear archivo ZIP con la base de datos
            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(backupPath))) {

                Path dbPath = Paths.get(DATABASE_FILE);
                if (Files.exists(dbPath)) {
                    ZipEntry zipEntry = new ZipEntry(DATABASE_FILE);
                    zipOut.putNextEntry(zipEntry);

                    Files.copy(dbPath, zipOut);
                    zipOut.closeEntry();

                    System.out.println("Backup creado: " + backupPath.toString());
                    return backupPath.toString();
                } else {
                    throw new RuntimeException("Database file not found: " + DATABASE_FILE);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error creating backup", e);
        }
    }

    /**
     * Restaurar base de datos desde un backup
     */
    public static void restoreFromBackup(String backupFilePath) {
        try {
            Path backupPath = Paths.get(backupFilePath);
            if (!Files.exists(backupPath)) {
                throw new RuntimeException("Backup file not found: " + backupFilePath);
            }

            // Cerrar conexión actual antes de restaurar
            DatabaseConnection.getInstance().closeConnection();

            // Hacer backup de la BD actual antes de restaurar
            Path currentDb = Paths.get(DATABASE_FILE);
            if (Files.exists(currentDb)) {
                Path backupCurrent = Paths.get(DATABASE_FILE + ".bak");
                Files.copy(currentDb, backupCurrent, StandardCopyOption.REPLACE_EXISTING);
            }

            // Extraer el archivo de la BD desde el ZIP
            try (var zipFile = new java.util.zip.ZipFile(backupPath.toFile())) {
                var entry = zipFile.getEntry(DATABASE_FILE);
                if (entry != null) {
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        Files.copy(in, currentDb, StandardCopyOption.REPLACE_EXISTING);
                    }
                    System.out.println("Database restored from: " + backupFilePath);
                } else {
                    throw new RuntimeException("Database file not found in backup");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error restoring backup", e);
        }
    }

    /**
     * Limpiar backups antiguos (mantener solo los últimos N)
     */
    public static void cleanOldBackups(int keepCount) {
        try {
            Path backupDir = Paths.get(BACKUP_FOLDER);
            if (!Files.exists(backupDir)) {
                return;
            }

            // Obtener lista de backups ordenados por fecha (más reciente primero)
            var backupFiles = Files.list(backupDir)
                    .filter(path -> path.toString().endsWith(".zip"))
                    .filter(path -> path.getFileName().toString().startsWith("clinapp_backup_"))
                    .sorted((a, b) -> b.toFile().lastModified() > a.toFile().lastModified() ? 1 : -1)
                    .toList();

            // Eliminar los backups más antiguos
            if (backupFiles.size() > keepCount) {
                for (int i = keepCount; i < backupFiles.size(); i++) {
                    Files.delete(backupFiles.get(i));
                    System.out.println("Deleted old backup: " + backupFiles.get(i).getFileName());
                }
            }

        } catch (IOException e) {
            System.err.println("Error cleaning old backups: " + e.getMessage());
        }
    }

    /**
     * Backup automático - llamar al cerrar la aplicación
     */
    public static void performAutoBackup() {
        try {
            String backupPath = createLocalBackup();
            System.out.println("Auto backup completed: " + backupPath);

            // Mantener solo los últimos 10 backups
            cleanOldBackups(10);

        } catch (Exception e) {
            System.err.println("Auto backup failed: " + e.getMessage());
            // No lanzar excepción para no interrumpir el cierre de la aplicación
        }
    }

    /**
     * Verificar si necesitamos hacer backup (ejemplo: una vez al día)
     */
    public static boolean shouldCreateBackup() {
        try {
            Path backupDir = Paths.get(BACKUP_FOLDER);
            if (!Files.exists(backupDir)) {
                return true; // Primera vez, necesitamos backup
            }

            // Buscar el backup más reciente
            var latestBackup = Files.list(backupDir)
                    .filter(path -> path.toString().endsWith(".zip"))
                    .filter(path -> path.getFileName().toString().startsWith("clinapp_backup_"))
                    .max((a, b) -> Long.compare(a.toFile().lastModified(), b.toFile().lastModified()));

            if (latestBackup.isEmpty()) {
                return true; // No hay backups
            }

            // Verificar si el último backup fue hace más de 24 horas
            long lastBackupTime = latestBackup.get().toFile().lastModified();
            long currentTime = System.currentTimeMillis();
            long dayInMillis = 24 * 60 * 60 * 1000;

            return (currentTime - lastBackupTime) > dayInMillis;

        } catch (IOException e) {
            System.err.println("Error checking backup status: " + e.getMessage());
            return false;
        }
    }

    // Agregar estos métodos a BackupManager.java

    /**
     * Crear backup completo: SQLite + Excel
     */
    public static String createCompleteBackup() {
        try {
            // Crear backup de SQLite
            String sqliteBackup = createLocalBackup();

            // Crear backup de Excel
            String excelBackup = ExcelBackupManager.exportarTodasLasTablas();

            System.out.println("Backup completo creado:");
            System.out.println("- SQLite: " + sqliteBackup);
            System.out.println("- Excel: " + excelBackup);

            return excelBackup; // Devolver el Excel para subirlo a Drive

        } catch (Exception e) {
            throw new RuntimeException("Error creating complete backup", e);
        }
    }

    /**
     * Método específico para backup automático con Excel
     */
    public static void performExcelBackup() {
        try {
            String excelFile = ExcelBackupManager.exportarTodasLasTablas();
            System.out.println("Excel backup completed: " + excelFile);

            // Aquí podrías agregar la subida a Google Drive
            // subirAGoogleDrive(excelFile);

        } catch (Exception e) {
            System.err.println("Excel backup failed: " + e.getMessage());
        }
    }

}