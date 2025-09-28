package com.dario.clinapp.dao;

import com.dario.clinapp.model.EstadoPagoInforme;
import com.dario.clinapp.model.Informe;
import com.dario.clinapp.model.TipoInforme;

import java.util.List;

public interface InformeDAO {
    void save(Informe informe);
    void update(Informe informe);
    void delete(Long id);
    Informe findById(Long id);
    List<Informe> findAll();

    List<Informe> findByPacienteId(Long pacienteId);
    List<Informe> findByTipoInforme(TipoInforme tipo);
    List<Informe> findByEstadoPago(EstadoPagoInforme estadoPago);
    void updateSaldo(Long id, double nuevoSaldo);
    List<Informe> findInformesPendientes();
}
