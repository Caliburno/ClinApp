package com.dario.clinapp.service;

import com.dario.clinapp.dao.*;
import com.dario.clinapp.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class PaymentAllocationService {

    private final SesionDAO sesionDAO;
    private final PagoDAO pagoDAO;
    private final InformeDAO informeDAO;

    public PaymentAllocationService(SesionDAO sesionDAO, PagoDAO pagoDAO, InformeDAO informeDAO) {
        this.sesionDAO = sesionDAO;
        this.pagoDAO = pagoDAO;
        this.informeDAO = informeDAO;
    }

    public double calcularSaldadoInforme(Paciente paciente, Long informeId) {
        List<Pago> pagos = pagoDAO.findByPacienteId(paciente.getId());
        double totalPagado = pagos.stream().mapToDouble(Pago::getMonto).sum();

        List<Sesion> sesiones = sesionDAO.findByPacienteId(paciente.getId()).stream()
                .sorted(Comparator.comparing(Sesion::getFecha).thenComparing(Sesion::getId))
                .collect(Collectors.toList());

        List<Informe> informes = informeDAO.findByPacienteId(paciente.getId()).stream()
                .sorted(Comparator.comparing(Informe::getId))
                .collect(Collectors.toList());

        double saldoDisponible = totalPagado;

        for (Sesion sesion : sesiones) {
            double precioSesion = paciente.getPrecioPorSesion();
            if (saldoDisponible >= precioSesion) {
                saldoDisponible -= precioSesion;
            } else if (saldoDisponible > 0) {
                saldoDisponible = 0;
                break;
            } else {
                break;
            }
        }

        for (Informe informe : informes) {
            if (informe.getId().equals(informeId)) {
                double pagadoAEsteInforme = Math.min(saldoDisponible, informe.getPrecio());
                return pagadoAEsteInforme;
            }

            if (saldoDisponible >= informe.getPrecio()) {
                saldoDisponible -= informe.getPrecio();
            } else if (saldoDisponible > 0) {
                saldoDisponible = 0;
            } else {
                break;
            }
        }

        return 0;
    }

    public double calcularSaldadoNuevoInforme(Paciente paciente, double precioInforme) {
        List<Pago> pagos = pagoDAO.findByPacienteId(paciente.getId());
        double totalPagado = pagos.stream().mapToDouble(Pago::getMonto).sum();

        List<Sesion> sesiones = sesionDAO.findByPacienteId(paciente.getId()).stream()
                .sorted(Comparator.comparing(Sesion::getFecha).thenComparing(Sesion::getId))
                .collect(Collectors.toList());

        List<Informe> informes = informeDAO.findByPacienteId(paciente.getId()).stream()
                .sorted(Comparator.comparing(Informe::getId))
                .collect(Collectors.toList());

        double saldoDisponible = totalPagado;

        for (Sesion sesion : sesiones) {
            double precioSesion = paciente.getPrecioPorSesion();
            if (saldoDisponible >= precioSesion) {
                saldoDisponible -= precioSesion;
            } else if (saldoDisponible > 0) {
                saldoDisponible = 0;
                break;
            } else {
                break;
            }
        }

        for (Informe informe : informes) {
            if (saldoDisponible >= informe.getPrecio()) {
                saldoDisponible -= informe.getPrecio();
            } else if (saldoDisponible > 0) {
                saldoDisponible = 0;
                break;
            } else {
                break;
            }
        }

        return Math.min(saldoDisponible, precioInforme);
    }
}