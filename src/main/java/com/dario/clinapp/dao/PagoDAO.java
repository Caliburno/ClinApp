package com.dario.clinapp.dao;

import com.dario.clinapp.model.Pago;
import com.dario.clinapp.model.TipoPago;

import java.time.LocalDate;
import java.util.List;

public interface PagoDAO {
    void save(Pago pago);
    void update(Pago pago);
    void delete(Long id);
    Pago findById(Long id);
    List<Pago> findAll();

    List<Pago> findByPacienteId(Long pacienteId);
    List<Pago> findByFecha(LocalDate fecha);
    List<Pago> findByRangoFechas(LocalDate desde, LocalDate hasta);
    double getTotalPagosByPaciente(Long pacienteId);
    List<Pago> findByTipoPago(TipoPago tipo);
}