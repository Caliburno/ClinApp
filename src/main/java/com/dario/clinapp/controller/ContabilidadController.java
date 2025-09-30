package com.dario.clinapp.controller;

import com.dario.clinapp.dao.*;
import com.dario.clinapp.dao.database.ServiceManager;
import com.dario.clinapp.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ContabilidadController {

    @FXML private ComboBox<String> mesComboBox;
    @FXML private ComboBox<Integer> anioComboBox;

    @FXML private Label totalGeneralLabel;
    @FXML private Label diagnosticoLabel;
    @FXML private Label estandarLabel;
    @FXML private Label totalDeudaLabel;
    @FXML private Label mensualLabel;


    @FXML private ToggleButton mensualToggle;
    @FXML private ToggleButton diagnosticoToggle;
    @FXML private ToggleButton otrosToggle;

    @FXML private TableView<DeudaRow> deudaTable;
    @FXML private TableColumn<DeudaRow, String> nombreColumn;
    @FXML private TableColumn<DeudaRow, String> tipoPacienteColumn;
    @FXML private TableColumn<DeudaRow, String> deudaTotalColumn;
    @FXML private TableColumn<DeudaRow, String> sesionesColumn;
    @FXML private TableColumn<DeudaRow, String> informesColumn;
    @FXML private TableColumn<DeudaRow, String> diagnosticoColumn;

    private ToggleGroup filtroGroup;

    private PacienteDAO pacienteDAO;
    private SesionDAO sesionDAO;
    private PagoDAO pagoDAO;
    private InformeDAO informeDAO;

    private YearMonth selectedYearMonth;

    @FXML
    public void initialize() {
        // Get DAO instances from ServiceManager
        pacienteDAO = ServiceManager.getPacienteDAO();
        sesionDAO = ServiceManager.getSesionDAO();
        pagoDAO = ServiceManager.getPagoDAO();
        informeDAO = ServiceManager.getInformeDAO();

        // Initialize toggle group
        filtroGroup = new ToggleGroup();
        mensualToggle.setToggleGroup(filtroGroup);
        diagnosticoToggle.setToggleGroup(filtroGroup);
        otrosToggle.setToggleGroup(filtroGroup);
        otrosToggle.setSelected(true); // Default selection

        // Initialize month ComboBox
        mesComboBox.setItems(FXCollections.observableArrayList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ));

        // Initialize year ComboBox (current year ± 5 years)
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int i = currentYear - 5; i <= currentYear + 1; i++) {
            years.add(i);
        }
        anioComboBox.setItems(FXCollections.observableArrayList(years));

        // Set current month and year
        LocalDate now = LocalDate.now();
        mesComboBox.setValue(mesComboBox.getItems().get(now.getMonthValue() - 1));
        anioComboBox.setValue(now.getYear());
        selectedYearMonth = YearMonth.now();

        // Configure table columns
        nombreColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        tipoPacienteColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoPaciente()));
        deudaTotalColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%.2f", data.getValue().getDeudaTotal())));
        sesionesColumn.setCellValueFactory(data -> {
            DeudaRow row = data.getValue();
            if (row.getNumSesionesPendientes() > 0) {
                return new SimpleStringProperty(row.getNumSesionesPendientes() + " sesiones");
            }
            return new SimpleStringProperty("-");
        });
        informesColumn.setCellValueFactory(data -> {
            DeudaRow row = data.getValue();
            if (row.getSaldoInformes() > 0) {
                return new SimpleStringProperty(String.format("$%.2f", row.getSaldoInformes()));
            }
            return new SimpleStringProperty("-");
        });
        diagnosticoColumn.setCellValueFactory(data -> {
            DeudaRow row = data.getValue();
            if (row.isDiagnostico() && row.getDeudaTotal() > 0) {
                return new SimpleStringProperty(String.format("$%.2f", row.getDeudaTotal()));
            }
            return new SimpleStringProperty("-");
        });

        // Load initial data
        loadContabilidadData();
    }

    @FXML
    private void onActualizarClicked() {
        int mes = mesComboBox.getSelectionModel().getSelectedIndex() + 1;
        int anio = anioComboBox.getValue();
        selectedYearMonth = YearMonth.of(anio, mes);
        loadContabilidadData();
    }

    @FXML
    private void onFiltroChanged() {
        loadDeudaTable();
    }

    private void loadContabilidadData() {
        loadIngresosMensuales();
        loadDeudaTable();
    }

    private void loadIngresosMensuales() {
        // Get date range for selected month
        LocalDate desde = selectedYearMonth.atDay(1);
        LocalDate hasta = selectedYearMonth.atEndOfMonth();

        // Get all payments for the month
        List<Pago> pagos = pagoDAO.findByRangoFechas(desde, hasta);

        // Calculate totals by TipoPaciente
        Map<TipoPaciente, Double> totalesPorTipo = new EnumMap<>(TipoPaciente.class);
        for (TipoPaciente tipo : TipoPaciente.values()) {
            totalesPorTipo.put(tipo, 0.0);
        }

        double totalGeneral = 0.0;

        for (Pago pago : pagos) {
            TipoPaciente tipo = pago.getPaciente().getTipoPaciente();
            double monto = pago.getMonto();
            totalesPorTipo.put(tipo, totalesPorTipo.get(tipo) + monto);
            totalGeneral += monto;
        }

        // Update labels
        totalGeneralLabel.setText(String.format("$%.2f", totalGeneral));
        diagnosticoLabel.setText(String.format("$%.2f", totalesPorTipo.get(TipoPaciente.DIAGNOSTICO)));
        estandarLabel.setText(String.format("$%.2f", totalesPorTipo.get(TipoPaciente.PACIENTE_ESTANDAR)));
        mensualLabel.setText(String.format("$%.2f", totalesPorTipo.get(TipoPaciente.MENSUAL)));
    }

    private void loadDeudaTable() {
        // Get all patients with debt
        List<Paciente> todosPacientes = pacienteDAO.findAll();
        List<Paciente> pacientesConDeuda = todosPacientes.stream()
                .filter(p -> p.getDeuda() > 0)
                .collect(Collectors.toList());

        // Filter based on selected toggle
        List<Paciente> pacientesFiltrados;
        if (mensualToggle.isSelected()) {
            pacientesFiltrados = pacientesConDeuda.stream()
                    .filter(p -> p.getTipoPaciente() == TipoPaciente.MENSUAL)
                    .collect(Collectors.toList());
        } else if (diagnosticoToggle.isSelected()) {
            pacientesFiltrados = pacientesConDeuda.stream()
                    .filter(p -> p.getTipoPaciente() == TipoPaciente.DIAGNOSTICO)
                    .collect(Collectors.toList());
        } else {
            // Otros: everyone except MENSUAL and DIAGNOSTICO
            pacientesFiltrados = pacientesConDeuda.stream()
                    .filter(p -> p.getTipoPaciente() != TipoPaciente.MENSUAL
                            && p.getTipoPaciente() != TipoPaciente.DIAGNOSTICO)
                    .collect(Collectors.toList());
        }

        // Calculate total debt
        double totalDeuda = pacientesConDeuda.stream()
                .mapToDouble(Paciente::getDeuda)
                .sum();
        totalDeudaLabel.setText(String.format("$%.2f", totalDeuda));

        // Build table rows
        ObservableList<DeudaRow> rows = FXCollections.observableArrayList();
        for (Paciente paciente : pacientesFiltrados) {
            DeudaRow row = buildDeudaRow(paciente);
            rows.add(row);
        }

        deudaTable.setItems(rows);
    }

    private DeudaRow buildDeudaRow(Paciente paciente) {
        DeudaRow row = new DeudaRow();
        row.setNombre(paciente.getNombre());
        row.setTipoPaciente(paciente.getTipoPaciente().toString());
        row.setDeudaTotal(paciente.getDeuda());
        row.setDiagnostico(paciente.getTipoPaciente() == TipoPaciente.DIAGNOSTICO);

        // Count unpaid sessions
        List<Sesion> todasSesiones = sesionDAO.findByPacienteId(paciente.getId());
        int sesionesPendientes = (int) todasSesiones.stream()
                .filter(s -> s.getEstadoPagoSesion() == EstadoPagoSesion.PENDIENTE)
                .count();
        row.setNumSesionesPendientes(sesionesPendientes);

        // Sum informes saldo
        List<Informe> informes = informeDAO.findByPacienteId(paciente.getId());
        double saldoInformes = informes.stream()
                .mapToDouble(Informe::getSaldo)
                .sum();
        row.setSaldoInformes(saldoInformes);

        return row;
    }

    // Inner class to represent a row in the debt table
    public static class DeudaRow {
        private String nombre;
        private String tipoPaciente;
        private double deudaTotal;
        private int numSesionesPendientes;
        private double saldoInformes;
        private boolean isDiagnostico;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getTipoPaciente() { return tipoPaciente; }
        public void setTipoPaciente(String tipoPaciente) { this.tipoPaciente = tipoPaciente; }

        public double getDeudaTotal() { return deudaTotal; }
        public void setDeudaTotal(double deudaTotal) { this.deudaTotal = deudaTotal; }

        public int getNumSesionesPendientes() { return numSesionesPendientes; }
        public void setNumSesionesPendientes(int numSesionesPendientes) {
            this.numSesionesPendientes = numSesionesPendientes;
        }

        public double getSaldoInformes() { return saldoInformes; }
        public void setSaldoInformes(double saldoInformes) { this.saldoInformes = saldoInformes; }

        public boolean isDiagnostico() { return isDiagnostico; }
        public void setDiagnostico(boolean diagnostico) { isDiagnostico = diagnostico; }
    }
}