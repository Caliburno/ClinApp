package com.dario.clinapp.model;

import java.time.LocalDate;

public class Sesion {

    private Long id;
    private TipoSesion tipoSesion;
    private Paciente paciente;
    private LocalDate fecha;
    private EstadoPagoSesion estadoPagoSesion;
    private String notas;

    @Override
    public String toString() {
        return "Sesion{" +
                "id= " + id +
                ", tipoSesion=" + tipoSesion +
                ", paciente=" + paciente +
                ", fecha=" + fecha +
                ", estadoPagoSesion=" + estadoPagoSesion +
                ", notas='" + notas + '\'' +
                '}';
    }

    public Sesion(Long id, TipoSesion tipoSesion, Paciente paciente, LocalDate fecha, EstadoPagoSesion estadoPagoSesion, String notas) {
        this.id = id;
        this.tipoSesion = tipoSesion;
        this.paciente = paciente;
        this.fecha = fecha;
        this.estadoPagoSesion = estadoPagoSesion;
        this.notas = notas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoSesion getTipoSesion() {
        return tipoSesion;
    }

    public void setTipoSesion(TipoSesion tipoSesion) {
        this.tipoSesion = tipoSesion;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public EstadoPagoSesion getEstadoPagoSesion() {
        return estadoPagoSesion;
    }

    public void setEstadoPagoSesion(EstadoPagoSesion estadoPagoSesion) {
        this.estadoPagoSesion = estadoPagoSesion;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
