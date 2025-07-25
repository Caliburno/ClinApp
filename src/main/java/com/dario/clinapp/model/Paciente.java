package com.dario.clinapp.model;

public class Paciente {

    private String nombre;
    private TipoPaciente tipoPaciente;
    private TipoSesion tipoSesion;
    private double precioPorSesion;
    private double deuda;
    private String notas;

    @Override
    public String toString() {
        return "Paciente{" +
                "nombre='" + nombre + '\'' +
                ", tipoPaciente=" + tipoPaciente +
                ", tipoSesion=" + tipoSesion +
                ", precioPorSesion=" + precioPorSesion +
                ", deuda=" + deuda +
                ", notas='" + notas + '\'' +
                '}';
    }

    public Paciente(String nombre, TipoPaciente tipoPaciente, TipoSesion tipoSesion, double precioPorSesion, double deuda, String notas) {
        this.nombre = nombre;
        this.tipoPaciente = tipoPaciente;
        this.tipoSesion = tipoSesion;
        this.precioPorSesion = precioPorSesion;
        this.deuda = deuda;
        this.notas = notas;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoPaciente getTipoPaciente() {
        return tipoPaciente;
    }

    public void setTipoPaciente(TipoPaciente tipoPaciente) {
        this.tipoPaciente = tipoPaciente;
    }

    public TipoSesion getTipoSesion() {
        return tipoSesion;
    }

    public void setTipoSesion(TipoSesion tipoSesion) {
        this.tipoSesion = tipoSesion;
    }

    public double getPrecioPorSesion() {
        return precioPorSesion;
    }

    public void setPrecioPorSesion(double precioPorSesion) {
        this.precioPorSesion = precioPorSesion;
    }

    public double getDeuda() {
        return deuda;
    }

    public void setDeuda(double deuda) {
        this.deuda = deuda;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
