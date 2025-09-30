package com.dario.clinapp.dao.impl;

import com.dario.clinapp.dao.InformeDAO;
import com.dario.clinapp.dao.database.DatabaseConnection;
import com.dario.clinapp.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// En dao/impl/InformeDAOImpl.java
public class InformeDAOImpl implements InformeDAO {

    @Override
    public void save(Informe informe) {
        String sql = """
            INSERT INTO informes (paciente_id, tipo_informe, precio, entregado, saldo, 
                                estado_informe, estado_pago_informe, notas)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, informe.getPaciente().getId());
            stmt.setString(2, informe.getTipoInforme().name());
            stmt.setDouble(3, informe.getPrecio());
            stmt.setDouble(4, informe.getSaldado());
            stmt.setDouble(5, informe.getSaldo());
            stmt.setString(6, informe.getEstadoInforme().name());
            stmt.setString(7, informe.getEstadoPagoInforme().name());
            stmt.setString(8, informe.getNotas());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    informe.setId(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving informe", e);
        }
    }

    @Override
    public Informe findById(Long id) {
        String sql = """
            SELECT i.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM informes i
            JOIN pacientes p ON i.paciente_id = p.id
            WHERE i.id = ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInforme(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding informe by id", e);
        }
    }

    private Informe mapResultSetToInforme(ResultSet rs) throws SQLException {
        // Primero construimos el paciente
        Paciente paciente = new Paciente(
                rs.getLong("paciente_id"),
                rs.getString("nombre"),
                TipoPaciente.valueOf(rs.getString("tipo_paciente")),
                TipoSesion.valueOf(rs.getString("tipo_sesion")),
                rs.getDouble("precio_por_sesion"),
                rs.getDouble("deuda"),
                rs.getString("paciente_notas")
        );

        // Después construimos el informe
        return new Informe(
                rs.getLong("id"),
                paciente,
                TipoInforme.valueOf(rs.getString("tipo_informe")),
                rs.getDouble("precio"),
                rs.getDouble("entregado"),
                rs.getDouble("saldo"),
                EstadoInforme.valueOf(rs.getString("estado_informe")),
                EstadoPagoInforme.valueOf(rs.getString("estado_pago_informe")),
                rs.getString("notas")
        );
    }

    @Override
    public List<Informe> findByPacienteId(Long pacienteId) {
        String sql = """
            SELECT i.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM informes i
            JOIN pacientes p ON i.paciente_id = p.id
            WHERE i.paciente_id = ?
            ORDER BY i.created_at DESC
        """;

        List<Informe> informes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pacienteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    informes.add(mapResultSetToInforme(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding informes by paciente", e);
        }

        return informes;
    }

    @Override
    public List<Informe> findInformesPendientes() {
        String sql = """
            SELECT i.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM informes i
            JOIN pacientes p ON i.paciente_id = p.id
            WHERE i.estado_pago_informe IN ('PENDIENTE', 'PAGO_PARCIAL')
            ORDER BY i.created_at ASC
        """;

        List<Informe> informes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    informes.add(mapResultSetToInforme(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding informes pendientes", e);
        }

        return informes;
    }

    @Override
    public void update(Informe informe) {
        String sql = """
            UPDATE informes 
            SET paciente_id = ?, tipo_informe = ?, precio = ?, entregado = ?, 
                saldo = ?, estado_informe = ?, estado_pago_informe = ?, notas = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, informe.getPaciente().getId());
            stmt.setString(2, informe.getTipoInforme().name());
            stmt.setDouble(3, informe.getPrecio());
            stmt.setDouble(4, informe.getSaldado());
            stmt.setDouble(5, informe.getSaldo());
            stmt.setString(6, informe.getEstadoInforme().name());
            stmt.setString(7, informe.getEstadoPagoInforme().name());
            stmt.setString(8, informe.getNotas());
            stmt.setLong(9, informe.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating informe", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM informes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting informe", e);
        }
    }

    @Override
    public List<Informe> findAll() {
        String sql = """
            SELECT i.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM informes i
            JOIN pacientes p ON i.paciente_id = p.id
            ORDER BY i.created_at DESC
        """;

        List<Informe> informes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    informes.add(mapResultSetToInforme(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all informes", e);
        }

        return informes;
    }

    // Agregar estos métodos a InformeDAOImpl:

    @Override
    public List<Informe> findByTipoInforme(TipoInforme tipo) {
        String sql = """
            SELECT i.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM informes i
            JOIN pacientes p ON i.paciente_id = p.id
            WHERE i.tipo_informe = ?
            ORDER BY i.created_at DESC
        """;

        List<Informe> informes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    informes.add(mapResultSetToInforme(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding informes by tipo", e);
        }

        return informes;
    }

    @Override
    public List<Informe> findByEstadoPago(EstadoPagoInforme estadoPago) {
        String sql = """
            SELECT i.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM informes i
            JOIN pacientes p ON i.paciente_id = p.id
            WHERE i.estado_pago_informe = ?
            ORDER BY i.created_at DESC
        """;

        List<Informe> informes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estadoPago.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    informes.add(mapResultSetToInforme(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding informes by estado pago", e);
        }

        return informes;
    }

    @Override
    public void updateSaldo(Long id, double nuevoSaldo) {
        String sql = "UPDATE informes SET saldo = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nuevoSaldo);
            stmt.setLong(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating saldo informe", e);
        }
    }
}