package com.dario.clinapp.model;

import java.time.LocalDate;

public class Pago {

    private Paciente paciente;
    private TipoPago tipoPago;
    private double monto;
    private FormaDePago formaDePago;
    private LocalDate fecha;
    private String notas;

    @Override
    public String toString() {
        return "Pago{" +
                "paciente=" + paciente +
                ", tipoPago=" + tipoPago +
                ", monto=" + monto +
                ", formaDePago=" + formaDePago +
                ", fecha=" + fecha +
                ", notas='" + notas + '\'' +
                '}';
    }

    public Pago(Paciente paciente, TipoPago tipoPago, double monto, FormaDePago formaDePago, LocalDate fecha, String notas) {
        this.paciente = paciente;
        this.tipoPago = tipoPago;
        this.monto = monto;
        this.formaDePago = formaDePago;
        this.fecha = fecha;
        this.notas = notas;
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
