package com.dario.clinapp.dao.database;

import com.dario.clinapp.dao.InformeDAO;
import com.dario.clinapp.dao.PacienteDAO;
import com.dario.clinapp.dao.PagoDAO;
import com.dario.clinapp.dao.SesionDAO;
import com.dario.clinapp.dao.impl.InformeDAOImpl;
import com.dario.clinapp.dao.impl.PacienteDAOImpl;
import com.dario.clinapp.dao.impl.PagoDAOImpl;
import com.dario.clinapp.dao.impl.SesionDAOImpl;

// En dao/database/ServiceManager.java
public class ServiceManager {

    private static PacienteDAO pacienteDAO;
    private static InformeDAO informeDAO;
    private static SesionDAO sesionDAO;
    private static PagoDAO pagoDAO;

    /**
     * Inicializar toda la persistencia
     */
    public static void initialize() {
        try {
            // Inicializar base de datos
            DatabaseInitializer.initializeDatabase();

            // Crear instancias de DAOs
            pacienteDAO = new PacienteDAOImpl();
            informeDAO = new InformeDAOImpl();
            sesionDAO = new SesionDAOImpl();
            pagoDAO = new PagoDAOImpl();

            System.out.println("Persistence layer initialized successfully");

            // Backup automático si es necesario
            if (BackupManager.shouldCreateBackup()) {
                BackupManager.performAutoBackup();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize persistence layer", e);
        }
    }

    /**
     * Cerrar todo limpiamente
     */
    // En ServiceManager.java, cambiar el método shutdown:

    public static void shutdown() {
        try {
            // Backup completo al cerrar (SQLite + Excel)
            BackupManager.createCompleteBackup();

            // Cerrar conexión
            DatabaseConnection.getInstance().closeConnection();

            System.out.println("Persistence layer shutdown completed");

        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    // Getters para los DAOs
    public static PacienteDAO getPacienteDAO() {
        if (pacienteDAO == null) {
            throw new RuntimeException("ServiceManager not initialized");
        }
        return pacienteDAO;
    }

    public static InformeDAO getInformeDAO() {
        if (informeDAO == null) {
            throw new RuntimeException("ServiceManager not initialized");
        }
        return informeDAO;
    }

    public static SesionDAO getSesionDAO() {
        if (sesionDAO == null) {
            throw new RuntimeException("ServiceManager not initialized");
        }
        return sesionDAO;
    }

    public static PagoDAO getPagoDAO() {
        if (pagoDAO == null) {
            throw new RuntimeException("ServiceManager not initialized");
        }
        return pagoDAO;
    }
}