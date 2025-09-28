package com.dario.clinapp.dao.database;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            conn.createStatement().execute("PRAGMA foreign_keys = ON");

            createPacientesTable(conn);
            createInformesTable(conn);
            createSesionesTable(conn);
            createPagosTable(conn);

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }

    private static void createPacientesTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS pacientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                tipo_paciente TEXT NOT NULL,
                tipo_sesion TEXT NOT NULL,
                precio_por_sesion REAL NOT NULL,
                deuda REAL NOT NULL DEFAULT 0,
                notas TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        conn.createStatement().execute(sql);
    }

    private static void createInformesTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS informes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                paciente_id INTEGER NOT NULL,
                tipo_informe TEXT NOT NULL,
                precio REAL NOT NULL,
                entregado REAL NOT NULL DEFAULT 0,
                saldo REAL NOT NULL,
                estado_informe TEXT NOT NULL,
                estado_pago_informe TEXT NOT NULL,
                notas TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
            )
        """;
        conn.createStatement().execute(sql);
    }

    private static void createSesionesTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS sesiones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo_sesion TEXT NOT NULL,
                paciente_id INTEGER NOT NULL,
                fecha DATE NOT NULL,
                estado_pago_sesion TEXT NOT NULL,
                notas TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
            )
        """;
        conn.createStatement().execute(sql);
    }

    private static void createPagosTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS pagos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                paciente_id INTEGER NOT NULL,
                tipo_pago TEXT NOT NULL,
                monto REAL NOT NULL,
                forma_de_pago TEXT NOT NULL,
                fecha DATE NOT NULL,
                notas TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
            )
        """;
        conn.createStatement().execute(sql);
    }
}
