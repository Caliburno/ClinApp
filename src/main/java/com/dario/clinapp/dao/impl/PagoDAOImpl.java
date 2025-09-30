package com.dario.clinapp.dao.impl;

import com.dario.clinapp.dao.PagoDAO;
import com.dario.clinapp.dao.database.DatabaseConnection;
import com.dario.clinapp.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// En dao/impl/PagoDAOImpl.java
public class PagoDAOImpl implements PagoDAO {

    @Override
    public void save(Pago pago) {
        String sql = """
            INSERT INTO pagos (paciente_id, tipo_pago, monto, forma_de_pago, fecha, notas)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, pago.getPaciente().getId());
            stmt.setString(2, pago.getTipoPago().name());
            stmt.setDouble(3, pago.getMonto());
            stmt.setString(4, pago.getFormaDePago().name());
            stmt.setDate(5, Date.valueOf(pago.getFecha()));
            stmt.setString(6, pago.getNotas());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pago.setId(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving pago", e);
        }
    }

    @Override
    public void update(Pago pago) {
        String sql = """
            UPDATE pagos 
            SET paciente_id = ?, tipo_pago = ?, monto = ?, forma_de_pago = ?, fecha = ?, notas = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pago.getPaciente().getId());
            stmt.setString(2, pago.getTipoPago().name());
            stmt.setDouble(3, pago.getMonto());
            stmt.setString(4, pago.getFormaDePago().name());
            stmt.setDate(5, Date.valueOf(pago.getFecha()));
            stmt.setString(6, pago.getNotas());
            stmt.setLong(7, pago.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating pago", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM pagos WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting pago", e);
        }
    }

    @Override
    public Pago findById(Long id) {
        String sql = """
            SELECT pg.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM pagos pg
            JOIN pacientes p ON pg.paciente_id = p.id
            WHERE pg.id = ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPago(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding pago by id", e);
        }
    }

    @Override
    public List<Pago> findAll() {
        String sql = """
            SELECT pg.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM pagos pg
            JOIN pacientes p ON pg.paciente_id = p.id
            ORDER BY pg.fecha DESC
        """;

        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all pagos", e);
        }

        return pagos;
    }

    @Override
    public List<Pago> findByPacienteId(Long pacienteId) {
        String sql = """
            SELECT pg.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM pagos pg
            JOIN pacientes p ON pg.paciente_id = p.id
            WHERE pg.paciente_id = ?
            ORDER BY pg.fecha DESC
        """;

        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pacienteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding pagos by paciente", e);
        }

        return pagos;
    }

    @Override
    public List<Pago> findByFecha(LocalDate fecha) {
        String sql = """
            SELECT pg.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM pagos pg
            JOIN pacientes p ON pg.paciente_id = p.id
            WHERE pg.fecha = ?
            ORDER BY p.nombre
        """;

        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding pagos by fecha", e);
        }

        return pagos;
    }

    @Override
    public List<Pago> findByRangoFechas(LocalDate desde, LocalDate hasta) {
        String sql = """
            SELECT pg.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM pagos pg
            JOIN pacientes p ON pg.paciente_id = p.id
            WHERE pg.fecha BETWEEN ? AND ?
            ORDER BY pg.fecha DESC, p.nombre
        """;

        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(desde));
            stmt.setDate(2, Date.valueOf(hasta));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding pagos by rango fechas", e);
        }

        return pagos;
    }

    @Override
    public double getTotalPagosByPaciente(Long pacienteId) {
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM pagos WHERE paciente_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pacienteId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
                return 0.0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error getting total pagos by paciente", e);
        }
    }

    @Override
    public List<Pago> findByTipoPago(TipoPago tipo) {
        String sql = """
            SELECT pg.*, p.id as paciente_id, p.nombre, p.tipo_paciente, p.tipo_sesion, 
                   p.precio_por_sesion, p.deuda, p.notas as paciente_notas
            FROM pagos pg
            JOIN pacientes p ON pg.paciente_id = p.id
            WHERE pg.tipo_pago = ?
            ORDER BY pg.fecha DESC
        """;

        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding pagos by tipo pago", e);
        }

        return pagos;
    }

    private Pago mapResultSetToPago(ResultSet rs) throws SQLException {
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

        // Después construimos el pago
        return new Pago(
                rs.getLong("id"),
                paciente,
                TipoPago.valueOf(rs.getString("tipo_pago")),
                rs.getDouble("monto"),
                FormaDePago.valueOf(rs.getString("forma_de_pago")),
                rs.getDate("fecha").toLocalDate(),
                rs.getString("notas")
        );
    }
}