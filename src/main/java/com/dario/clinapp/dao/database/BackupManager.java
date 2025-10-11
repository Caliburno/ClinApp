package com.dario.clinapp.dao.database;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    private static final String DATABASE_FILE = "clinapp.db";
    private static final String BACKUP_FOLDER = "backups";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static Path createCompleteBackup() {
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            Path backupDir = Paths.get(BACKUP_FOLDER, "clinapp_backup_" + timestamp);
            Files.createDirectories(backupDir);

            Path dbZip = backupDir.resolve("clinapp.db.zip");
            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(dbZip))) {
                Path dbPath = Paths.get(DATABASE_FILE);
                if (!Files.exists(dbPath)) {
                    throw new RuntimeException("Database file not found: " + DATABASE_FILE);
                }
                ZipEntry zipEntry = new ZipEntry(DATABASE_FILE);
                zipOut.putNextEntry(zipEntry);
                Files.copy(dbPath, zipOut);
                zipOut.closeEntry();
            }

            Path excelFile = ExcelBackupManager.exportarTodasLasTablas(backupDir);

            System.out.println("Backup completo creado en: " + backupDir);
            return backupDir;

        } catch (Exception e) {
            throw new RuntimeException("Error creating complete backup", e);
        }
    }

    public static void restoreFromBackup(String dbZipPath) {
        try {
            Path zipPath = Paths.get(dbZipPath);
            if (!Files.exists(zipPath)) {
                throw new RuntimeException("Backup not found: " + dbZipPath);
            }

            DatabaseConnection.getInstance().closeConnection();
            Path currentDb = Paths.get(DATABASE_FILE);

            if (Files.exists(currentDb)) {
                Files.copy(currentDb, Paths.get(DATABASE_FILE + ".bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            try (var zipFile = new java.util.zip.ZipFile(zipPath.toFile())) {
                var entry = zipFile.getEntry(DATABASE_FILE);
                if (entry == null) throw new RuntimeException("DB missing inside backup zip");

                try (InputStream in = zipFile.getInputStream(entry)) {
                    Files.copy(in, currentDb, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            System.out.println("Database restored from: " + dbZipPath);

        } catch (IOException e) {
            throw new RuntimeException("Error restoring backup", e);
        }
    }

    public static boolean shouldCreateBackup() {
        try {
            Path backupRoot = Paths.get(BACKUP_FOLDER);
            if (!Files.exists(backupRoot)) return true;

            var latestBackup = Files.list(backupRoot)
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("clinapp_backup_"))
                    .max((a, b) -> Long.compare(a.toFile().lastModified(), b.toFile().lastModified()));

            if (latestBackup.isEmpty()) return true;

            long lastBackupTime = latestBackup.get().toFile().lastModified();
            return (System.currentTimeMillis() - lastBackupTime) > (24 * 60 * 60 * 1000);

        } catch (IOException e) {
            System.err.println("Error checking backup status: " + e.getMessage());
            return false;
        }
    }
}
