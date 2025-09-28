package com.dario.clinapp.dao.impl;

import com.dario.clinapp.dao.SesionDAO;
import com.dario.clinapp.dao.database.DatabaseConnection;
import com.dario.clinapp.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// En dao/impl/SesionDAOImpl.java
public class SesionDAOImpl implements SesionDAO {

    @Override
    public void save(Sesion sesion) {
        String sql = """
            INSERT INTO sesiones (tipo_sesion, paciente_id, fecha, estado_pago_sesion, notas)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, sesion.getTipoSesion().name());
            stmt.setLong(2, sesion.getPaciente().getId());
            stmt.setDate(3, Date.valueOf(sesion.getFecha()));
            stmt.setString(4, sesion.getEstadoPagoSesion().name());
            stmt.setString(5, sesion.getNotas());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    sesion.setId(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving sesion", e);
        }
    }

    @Override
    public void update(Sesion sesion) {
        String sql = """
            UPDATE sesiones 
            SET tipo_sesion = ?, paciente_id = ?, fecha = ?, estado_pago_sesion = ?, notas = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sesion.getTipoSesion().name());
            stmt.setLong(2, sesion.getPaciente().getId());
            stmt.setDate(3, Date.valueOf(sesion.getFecha()));
            stmt.setString(4, sesion.getEstadoPagoSesion().name());
            stmt.setString(5, sesion.getNotas());
            stmt.setLong(6, sesion.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating sesion", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM sesiones WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting sesion", e);
        }
    }

    @Override
    public Sesion findById(Long id) {
        String sql = """
            SELECT s.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM sesiones s
            JOIN pacientes p ON s.paciente_id = p.id
            WHERE s.id = ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSesion(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding sesion by id", e);
        }
    }

    @Override
    public List<Sesion> findAll() {
        String sql = """
            SELECT s.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM sesiones s
            JOIN pacientes p ON s.paciente_id = p.id
            ORDER BY s.fecha DESC
        """;

        List<Sesion> sesiones = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sesiones.add(mapResultSetToSesion(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all sesiones", e);
        }

        return sesiones;
    }

    @Override
    public List<Sesion> findByPacienteId(Long pacienteId) {
        String sql = """
            SELECT s.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM sesiones s
            JOIN pacientes p ON s.paciente_id = p.id
            WHERE s.paciente_id = ?
            ORDER BY s.fecha DESC
        """;

        List<Sesion> sesiones = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pacienteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sesiones.add(mapResultSetToSesion(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding sesiones by paciente", e);
        }

        return sesiones;
    }

    @Override
    public List<Sesion> findByFecha(LocalDate fecha) {
        String sql = """
            SELECT s.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM sesiones s
            JOIN pacientes p ON s.paciente_id = p.id
            WHERE s.fecha = ?
            ORDER BY p.nombre
        """;

        List<Sesion> sesiones = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sesiones.add(mapResultSetToSesion(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding sesiones by fecha", e);
        }

        return sesiones;
    }

    @Override
    public List<Sesion> findByRangoFechas(LocalDate desde, LocalDate hasta) {
        String sql = """
            SELECT s.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM sesiones s
            JOIN pacientes p ON s.paciente_id = p.id
            WHERE s.fecha BETWEEN ? AND ?
            ORDER BY s.fecha DESC, p.nombre
        """;

        List<Sesion> sesiones = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(desde));
            stmt.setDate(2, Date.valueOf(hasta));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sesiones.add(mapResultSetToSesion(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding sesiones by rango fechas", e);
        }

        return sesiones;
    }

    @Override
    public List<Sesion> findSesionesPendientesPago() {
        String sql = """
            SELECT s.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM sesiones s
            JOIN pacientes p ON s.paciente_id = p.id
            WHERE s.estado_pago_sesion = 'PENDIENTE'
            ORDER BY s.fecha ASC
        """;

        List<Sesion> sesiones = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sesiones.add(mapResultSetToSesion(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding sesiones pendientes pago", e);
        }

        return sesiones;
    }

    @Override
    public int countSesionesByPaciente(Long pacienteId) {
        String sql = "SELECT COUNT(*) FROM sesiones WHERE paciente_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pacienteId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error counting sesiones by paciente", e);
        }
    }

    private Sesion mapResultSetToSesion(ResultSet rs) throws SQLException {
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

        // Después construimos la sesion
        return new Sesion(
                rs.getLong("id"),
                TipoSesion.valueOf(rs.getString("tipo_sesion")),
                paciente,
                rs.getDate("fecha").toLocalDate(),
                EstadoPagoSesion.valueOf(rs.getString("estado_pago_sesion")),
                rs.getString("notas")
        );
    }
}