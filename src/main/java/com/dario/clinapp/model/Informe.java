package com.dario.clinapp.model;

public class Informe {

    private Paciente paciente;
    private TipoInforme tipoInforme;
    private double precio;
    private double entregado;
    private double saldo;
    private EstadoInforme estadoInforme;
    private EstadoPagoInforme estadoPagoInforme;
    private String notas;

    @Override
    public String toString() {
        return "Informe{" +
                "paciente=" + paciente +
                ", tipoInforme=" + tipoInforme +
                ", precio=" + precio +
                ", entregado=" + entregado +
                ", saldo=" + saldo +
                ", estadoInforme=" + estadoInforme +
                ", estadoPagoInforme=" + estadoPagoInforme +
                ", notas='" + notas + '\'' +
                '}';
    }

    public Informe(Paciente paciente, TipoInforme tipoInforme, double precio, double entregado, double saldo, EstadoInforme estadoInforme, EstadoPagoInforme estadoPagoInforme, String notas) {
        this.paciente = paciente;
        this.tipoInforme = tipoInforme;
        this.precio = precio;
        this.entregado = entregado;
        this.saldo = saldo;
        this.estadoInforme = estadoInforme;
        this.estadoPagoInforme = estadoPagoInforme;
        this.notas = notas;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public TipoInforme getTipoInforme() {
        return tipoInforme;
    }

    public void setTipoInforme(TipoInforme tipoInforme) {
        this.tipoInforme = tipoInforme;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getEntregado() {
        return entregado;
    }

    public void setEntregado(double entregado) {
        this.entregado = entregado;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public EstadoInforme getEstadoInforme() {
        return estadoInforme;
    }

    public void setEstadoInforme(EstadoInforme estadoInforme) {
        this.estadoInforme = estadoInforme;
    }

    public EstadoPagoInforme getEstadoPagoInforme() {
        return estadoPagoInforme;
    }

    public void setEstadoPagoInforme(EstadoPagoInforme estadoPagoInforme) {
        this.estadoPagoInforme = estadoPagoInforme;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
