package com.dario.clinapp.dao.database;

import com.dario.clinapp.dao.*;
import com.dario.clinapp.dao.impl.*;

public class ServiceManager {

    private static PacienteDAO pacienteDAO;
    private static InformeDAO informeDAO;
    private static SesionDAO sesionDAO;
    private static PagoDAO pagoDAO;

    public static void initialize() {
        try {
            DatabaseInitializer.initializeDatabase();

            pacienteDAO = new PacienteDAOImpl();
            informeDAO = new InformeDAOImpl();
            sesionDAO = new SesionDAOImpl();
            pagoDAO = new PagoDAOImpl();

            System.out.println("Persistence layer initialized successfully");

            if (BackupManager.shouldCreateBackup()) {
                BackupManager.createCompleteBackup();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize persistence layer", e);
        }
    }

    public static void shutdown() {
        try {
            BackupManager.createCompleteBackup();
            DatabaseConnection.getInstance().closeConnection();
            System.out.println("Persistence layer shutdown completed");
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    public static PacienteDAO getPacienteDAO() { return pacienteDAO; }
    public static InformeDAO getInformeDAO() { return informeDAO; }
    public static SesionDAO getSesionDAO() { return sesionDAO; }
    public static PagoDAO getPagoDAO() { return pagoDAO; }
}
