package com.dario.clinapp.dao;

import com.dario.clinapp.model.Paciente;
import com.dario.clinapp.model.TipoPaciente;

import java.util.List;

public interface PacienteDAO {
    void save(Paciente paciente);
    void update(Paciente paciente);
    void delete(Long id);
    Paciente findById(Long id);
    Paciente findByNombre(String nombre);
    List<Paciente> findAll();

    List<Paciente> findByTipoPaciente(TipoPaciente tipo);
    void updateDeuda(Long id, double nuevaDeuda);
}
