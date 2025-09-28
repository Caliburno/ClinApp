package com.dario.clinapp.dao;

import com.dario.clinapp.model.Sesion;

import java.time.LocalDate;
import java.util.List;

public interface SesionDAO {
    void save(Sesion sesion);
    void update(Sesion sesion);
    void delete(Long id);
    Sesion findById(Long id);
    List<Sesion> findAll();

    List<Sesion> findByPacienteId(Long pacienteId);
    List<Sesion> findByFecha(LocalDate fecha);
    List<Sesion> findByRangoFechas(LocalDate desde, LocalDate hasta);
    List<Sesion> findSesionesPendientesPago();
    int countSesionesByPaciente(Long pacienteId);
}