package com.dario.clinapp.controller;

import com.dario.clinapp.dao.database.ServiceManager;
import com.dario.clinapp.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class PacientesController {

    @FXML private TextField txtNombre;
    @FXML private ComboBox<TipoPaciente> cmbTipoPaciente;
    @FXML private ComboBox<TipoSesion> cmbTipoSesion;
    @FXML private TextField txtValorSesion;
    @FXML private TextField txtDeuda;
    @FXML private TextField txtNotas;

    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnBorrar;

    @FXML private TableView<Paciente> tablaPacientes;
    @FXML private TableColumn<Paciente, String> colNombre;
    @FXML private TableColumn<Paciente, TipoPaciente> colFrecuencia;
    @FXML private TableColumn<Paciente, TipoSesion> colTipoSesion;
    @FXML private TableColumn<Paciente, Double> colValorSesion;
    @FXML private TableColumn<Paciente, Double> colDeuda;
    @FXML private TableColumn<Paciente, String> colNotas;

    private ObservableList<Paciente> listaPacientes = FXCollections.observableArrayList();
    private Paciente pacienteSeleccionado = null;

    public void initialize() {
        configurarTabla();
        configurarComboBoxes();
        cargarPacientes();
        configurarSeleccionTabla();
        configurarListeners();

        btnEditar.setDisable(true);
        btnBorrar.setDisable(true);

        // Set default values
        txtDeuda.setText("0");
        txtDeuda.setDisable(true); // Disabled by default
    }

    private void configurarListeners() {
        // Listener for TipoPaciente changes
        cmbTipoPaciente.setOnAction(e -> {
            TipoPaciente tipoPaciente = cmbTipoPaciente.getValue();
            if (tipoPaciente != null) {
                if (tipoPaciente == TipoPaciente.DIAGNOSTICO) {
                    // Set TipoSesion to DIAGNOSTICO and disable
                    cmbTipoSesion.setValue(TipoSesion.DIAGNOSTICO);
                    cmbTipoSesion.setDisable(true);
                    txtValorSesion.setPromptText("Precio Total Diagnóstico");
                    txtDeuda.setDisable(true);
                    // Update debt field to show diagnosis price
                    actualizarDeudaDiagnostico();
                } else {
                    // Enable TipoSesion and set default to ESTANDAR
                    cmbTipoSesion.setDisable(false);
                    if (cmbTipoSesion.getValue() == null) {
                        cmbTipoSesion.setValue(TipoSesion.ESTANDAR);
                    }
                    txtValorSesion.setPromptText("Precio por Sesión");
                    txtDeuda.setDisable(false);
                }
            }
        });

        // Listener for price changes when Diagnostico is selected
        txtValorSesion.textProperty().addListener((obs, oldVal, newVal) -> {
            if (cmbTipoPaciente.getValue() == TipoPaciente.DIAGNOSTICO) {
                actualizarDeudaDiagnostico();
            }
        });
    }

    private void actualizarDeudaDiagnostico() {
        try {
            if (!txtValorSesion.getText().trim().isEmpty()) {
                double precioDiagnostico = Double.parseDouble(txtValorSesion.getText());
                txtDeuda.setText(String.valueOf(precioDiagnostico));
            } else {
                txtDeuda.setText("0");
            }
        } catch (NumberFormatException e) {
            txtDeuda.setText("0");
        }
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colFrecuencia.setCellValueFactory(new PropertyValueFactory<>("tipoPaciente"));
        colTipoSesion.setCellValueFactory(new PropertyValueFactory<>("tipoSesion"));
        colValorSesion.setCellValueFactory(new PropertyValueFactory<>("precioPorSesion"));
        colDeuda.setCellValueFactory(new PropertyValueFactory<>("deuda"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));

        tablaPacientes.setItems(listaPacientes);
    }

    private void configurarComboBoxes() {
        cmbTipoPaciente.setItems(FXCollections.observableArrayList(TipoPaciente.values()));
        cmbTipoSesion.setItems(FXCollections.observableArrayList(TipoSesion.values()));
    }

    private void configurarSeleccionTabla() {
        tablaPacientes.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        cargarPacienteEnFormulario(newValue);
                        btnEditar.setDisable(false);
                        btnBorrar.setDisable(false);
                    } else {
                        btnEditar.setDisable(true);
                        btnBorrar.setDisable(true);
                    }
                });
    }

    @FXML
    private void guardarPaciente() {
        try {
            if (!validarFormulario()) {
                return;
            }

            double deuda = 0;
            if (cmbTipoPaciente.getValue() == TipoPaciente.DIAGNOSTICO) {
                // For diagnosis patients, debt is the total diagnosis price
                deuda = Double.parseDouble(txtValorSesion.getText());
            } else {
                // For other patients, use the debt field value
                deuda = Double.parseDouble(txtDeuda.getText());
            }

            Paciente nuevo = new Paciente(
                    null,
                    txtNombre.getText().trim(),
                    cmbTipoPaciente.getValue(),
                    cmbTipoSesion.getValue(),
                    Double.parseDouble(txtValorSesion.getText()),
                    deuda,
                    txtNotas.getText().trim()
            );

            ServiceManager.getPacienteDAO().save(nuevo);
            mostrarMensaje("Paciente creado exitosamente", Alert.AlertType.INFORMATION);

            cargarPacientes();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("Por favor ingrese números válidos en los campos numéricos", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarMensaje("Error al crear paciente: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void editarPaciente() {
        if (pacienteSeleccionado == null) {
            mostrarMensaje("Seleccione un paciente para editar", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (!validarFormulario()) {
                return;
            }

            pacienteSeleccionado.setNombre(txtNombre.getText().trim());
            pacienteSeleccionado.setTipoPaciente(cmbTipoPaciente.getValue());
            pacienteSeleccionado.setTipoSesion(cmbTipoSesion.getValue());
            pacienteSeleccionado.setPrecioPorSesion(Double.parseDouble(txtValorSesion.getText()));

            if (cmbTipoPaciente.getValue() != TipoPaciente.DIAGNOSTICO) {
                pacienteSeleccionado.setDeuda(Double.parseDouble(txtDeuda.getText()));
            }

            pacienteSeleccionado.setNotas(txtNotas.getText().trim());

            ServiceManager.getPacienteDAO().update(pacienteSeleccionado);
            mostrarMensaje("Paciente actualizado exitosamente", Alert.AlertType.INFORMATION);

            cargarPacientes();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("Por favor ingrese números válidos en los campos numéricos", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarMensaje("Error al actualizar paciente: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarPaciente() {
        if (pacienteSeleccionado == null) {
            mostrarMensaje("Seleccione un paciente para eliminar", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este paciente?");
        confirmacion.setContentText("Esta acción eliminará también todas las sesiones, informes y pagos asociados.");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try {
                ServiceManager.getPacienteDAO().delete(pacienteSeleccionado.getId());
                mostrarMensaje("Paciente eliminado exitosamente", Alert.AlertType.INFORMATION);
                cargarPacientes();
                limpiarFormulario();
            } catch (Exception e) {
                mostrarMensaje("Error al eliminar paciente: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        cmbTipoPaciente.setValue(null);
        cmbTipoSesion.setValue(null);
        cmbTipoSesion.setDisable(false);
        txtValorSesion.clear();
        txtValorSesion.setPromptText("Precio por Sesión");
        txtDeuda.setText("0");
        txtDeuda.setDisable(true);
        txtNotas.clear();

        tablaPacientes.getSelectionModel().clearSelection();
        pacienteSeleccionado = null;
        btnEditar.setDisable(true);
        btnBorrar.setDisable(true);
    }

    private void cargarPacientes() {
        try {
            var pacientes = ServiceManager.getPacienteDAO().findAll();
            listaPacientes.clear();
            listaPacientes.addAll(pacientes);
        } catch (Exception e) {
            mostrarMensaje("Error al cargar pacientes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarPacienteEnFormulario(Paciente paciente) {
        pacienteSeleccionado = paciente;
        txtNombre.setText(paciente.getNombre());
        cmbTipoPaciente.setValue(paciente.getTipoPaciente());
        cmbTipoSesion.setValue(paciente.getTipoSesion());
        txtValorSesion.setText(String.valueOf(paciente.getPrecioPorSesion()));
        txtDeuda.setText(String.valueOf(paciente.getDeuda()));
        txtNotas.setText(paciente.getNotas());

        // Handle Diagnostico patients when editing
        if (paciente.getTipoPaciente() == TipoPaciente.DIAGNOSTICO) {
            cmbTipoSesion.setDisable(true);
            txtDeuda.setDisable(true);
        } else {
            cmbTipoSesion.setDisable(false);
            txtDeuda.setDisable(false);
        }
    }

    private boolean validarFormulario() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarMensaje("El nombre es requerido", Alert.AlertType.WARNING);
            return false;
        }

        if (cmbTipoPaciente.getValue() == null) {
            mostrarMensaje("Seleccione un tipo de paciente", Alert.AlertType.WARNING);
            return false;
        }

        if (cmbTipoSesion.getValue() == null) {
            mostrarMensaje("Seleccione un tipo de sesión", Alert.AlertType.WARNING);
            return false;
        }

        try {
            Double.parseDouble(txtValorSesion.getText());
            if (cmbTipoPaciente.getValue() != TipoPaciente.DIAGNOSTICO) {
                Double.parseDouble(txtDeuda.getText());
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("Los campos numéricos deben contener valores válidos", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Gestión de Pacientes");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}