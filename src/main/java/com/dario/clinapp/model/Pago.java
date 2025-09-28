package com.dario.clinapp.model;

import java.time.LocalDate;

public class Pago {

    private Long id;
    private Paciente paciente;
    private TipoPago tipoPago;
    private double monto;
    private FormaDePago formaDePago;
    private LocalDate fecha;
    private String notas;

    @Override
    public String toString() {
        return "Pago{" +
                "id=" + id +
                ", paciente=" + paciente +
                ", tipoPago=" + tipoPago +
                ", monto=" + monto +
                ", formaDePago=" + formaDePago +
                ", fecha=" + fecha +
                ", notas='" + notas + '\'' +
                '}';
    }

    public Pago(Long id, Paciente paciente, TipoPago tipoPago, double monto, FormaDePago formaDePago, LocalDate fecha, String notas) {
        this.id = id;
        this.paciente = paciente;
        this.tipoPago = tipoPago;
        this.monto = monto;
        this.formaDePago = formaDePago;
        this.fecha = fecha;
        this.notas = notas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public TipoPago getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(TipoPago tipoPago) {
        this.tipoPago = tipoPago;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public FormaDePago getFormaDePago() {
        return formaDePago;
    }

    public void setFormaDePago(FormaDePago formaDePago) {
        this.formaDePago = formaDePago;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
