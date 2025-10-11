package com.dario.clinapp.dao.impl;

import com.dario.clinapp.dao.PacienteDAO;
import com.dario.clinapp.dao.database.DatabaseConnection;
import com.dario.clinapp.model.Paciente;
import com.dario.clinapp.model.TipoPaciente;
import com.dario.clinapp.model.TipoSesion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAOImpl implements PacienteDAO {

    @Override
    public void save(Paciente paciente) {
        String sql = """
            INSERT INTO pacientes (nombre, tipo_paciente, tipo_sesion, precio_por_sesion, deuda, notas)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, paciente.getNombre());
            stmt.setString(2, paciente.getTipoPaciente().name());
            stmt.setString(3, paciente.getTipoSesion().name());
            stmt.setDouble(4, paciente.getPrecioPorSesion());
            stmt.setDouble(5, paciente.getDeuda());
            stmt.setString(6, paciente.getNotas());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    paciente.setId(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving paciente", e);
        }
    }

    @Override
    public void update(Paciente paciente) {
        String sql = """
            UPDATE pacientes 
            SET nombre = ?, tipo_paciente = ?, tipo_sesion = ?, 
                precio_por_sesion = ?, deuda = ?, notas = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paciente.getNombre());
            stmt.setString(2, paciente.getTipoPaciente().name());
            stmt.setString(3, paciente.getTipoSesion().name());
            stmt.setDouble(4, paciente.getPrecioPorSesion());
            stmt.setDouble(5, paciente.getDeuda());
            stmt.setString(6, paciente.getNotas());
            stmt.setLong(7, paciente.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating paciente", e);
        }
    }
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM pacientes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting paciente", e);
        }
    }

    @Override
    public Paciente findById(Long id) {
        String sql = "SELECT * FROM pacientes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaciente(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding paciente by id", e);
        }
    }

    @Override
    public Paciente findByNombre(String nombre) {
        String sql = "SELECT * FROM pacientes WHERE nombre = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaciente(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding paciente by nombre", e);
        }
    }

    @Override
    public List<Paciente> findAll() {
        String sql = "SELECT * FROM pacientes ORDER BY nombre";
        List<Paciente> pacientes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pacientes.add(mapResultSetToPaciente(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all pacientes", e);
        }

        return pacientes;
    }

    private Paciente mapResultSetToPaciente(ResultSet rs) throws SQLException {
        Paciente paciente = new Paciente(
                rs.getLong("id"),
                rs.getString("nombre"),
                TipoPaciente.valueOf(rs.getString("tipo_paciente")),
                TipoSesion.valueOf(rs.getString("tipo_sesion")),
                rs.getDouble("precio_por_sesion"),
                rs.getDouble("deuda"),
                rs.getString("notas")
        );
        return paciente;
    }

    @Override
    public List<Paciente> findByTipoPaciente(TipoPaciente tipo) {
        String sql = "SELECT * FROM pacientes WHERE tipo_paciente = ? ORDER BY nombre";
        List<Paciente> pacientes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pacientes.add(mapResultSetToPaciente(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding pacientes by tipo", e);
        }

        return pacientes;
    }

    @Override
    public void updateDeuda(Long id, double nuevaDeuda) {
        String sql = "UPDATE pacientes SET deuda = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nuevaDeuda);
            stmt.setLong(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating deuda", e);
        }
    }
}
