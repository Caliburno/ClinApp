package com.dario.clinapp.controller;

import com.dario.clinapp.dao.database.ServiceManager;
import com.dario.clinapp.model.*;
import com.dario.clinapp.service.PaymentAllocationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import javafx.collections.transformation.FilteredList;

public class InformesController {

    @FXML private ComboBox<Paciente> cmbPaciente;
    @FXML private ComboBox<TipoInforme> cmbTipoInforme;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtSaldado;
    @FXML private TextField txtSaldo;
    @FXML private CheckBox chkEditarSaldado;
    @FXML private ComboBox<EstadoInforme> cmbEstadoInforme;
    @FXML private ComboBox<EstadoPagoInforme> cmbEstadoPagoInforme;
    @FXML private TextField txtNotas;

    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnBorrar;
    @FXML private Button btnLimpiar;

    @FXML private TableView<Informe> tablaInformes;
    @FXML private TableColumn<Informe, String> colPaciente;
    @FXML private TableColumn<Informe, TipoInforme> colTipoInforme;
    @FXML private TableColumn<Informe, Double> colPrecio;
    @FXML private TableColumn<Informe, Double> colSaldado;
    @FXML private TableColumn<Informe, Double> colSaldo;
    @FXML private TableColumn<Informe, EstadoInforme> colEstadoInforme;
    @FXML private TableColumn<Informe, EstadoPagoInforme> colEstadoPagoInforme;

    private ObservableList<Informe> listaInformes = FXCollections.observableArrayList();
    private ObservableList<Paciente> listaPacientes = FXCollections.observableArrayList();
    private Informe informeSeleccionado = null;
    private PaymentAllocationService paymentAllocationService;
    private FilteredList<Paciente> pacientesFiltrados;

    public void initialize() {
        paymentAllocationService = new PaymentAllocationService(
                ServiceManager.getSesionDAO(),
                ServiceManager.getPagoDAO(),
                ServiceManager.getInformeDAO()
        );

        configurarTabla();
        configurarComboBoxes();
        cargarDatos();
        configurarSeleccionTabla();
        configurarListeners();
        configurarBusquedaPaciente(); // NUEVO

        btnEditar.setDisable(true);
        btnBorrar.setDisable(true);

        txtSaldado.setDisable(true);
        txtSaldo.setDisable(true); // Saldo is always calculated
        cmbEstadoPagoInforme.setDisable(true); // Estado pago is auto-calculated
    }

    private void configurarListeners() {
        cmbPaciente.setOnAction(e -> {
            Paciente paciente = cmbPaciente.getValue();
            if (paciente != null) {
                if (paciente.getTipoPaciente() == TipoPaciente.DIAGNOSTICO) {
                    cmbTipoInforme.setValue(TipoInforme.PSICODIAGNOSTICO);
                    txtPrecio.setText(String.valueOf(paciente.getPrecioPorSesion()));
                    calcularSaldadoYSaldo();
                } else {
                    if (!txtPrecio.getText().isEmpty()) {
                        calcularSaldadoYSaldo();
                    }
                }
            }
        });

        txtPrecio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty() && cmbPaciente.getValue() != null) {
                calcularSaldadoYSaldo();
            }
        });

        txtSaldado.textProperty().addListener((obs, oldVal, newVal) -> {
            if (chkEditarSaldado.isSelected() && !newVal.trim().isEmpty()) {
                calcularSaldoManual();
            }
        });

        chkEditarSaldado.selectedProperty().addListener((obs, oldVal, newVal) -> {
            txtSaldado.setDisable(!newVal);
            if (!newVal) {

                calcularSaldadoYSaldo();
            }
        });
    }

    private void configurarBusquedaPaciente() {
        cmbPaciente.setEditable(true);

        cmbPaciente.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final TextField editor = cmbPaciente.getEditor();
            final Paciente seleccionado = cmbPaciente.getSelectionModel().getSelectedItem();

            if (seleccionado != null && seleccionado.getNombre().equals(newValue)) {
                return;
            }

            pacientesFiltrados.setPredicate(paciente -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String filtro = newValue.toLowerCase();
                return paciente.getNombre().toLowerCase().contains(filtro);
            });

            if (!pacientesFiltrados.isEmpty() && !cmbPaciente.isShowing()) {
                cmbPaciente.show();
            }
        });
    }

    private void calcularSaldadoYSaldo() {
        if (cmbPaciente.getValue() == null || txtPrecio.getText().trim().isEmpty()) {
            return;
        }

        try {
            double precio = Double.parseDouble(txtPrecio.getText());
            Paciente paciente = cmbPaciente.getValue();

            double saldado = paymentAllocationService.calcularSaldadoNuevoInforme(paciente, precio);

            txtSaldado.setText(String.format("%.2f", saldado));

            double saldo = precio - saldado;
            txtSaldo.setText(String.format("%.2f", saldo));

            actualizarEstadoPago(saldado, saldo);

        } catch (NumberFormatException e) {
            txtSaldado.clear();
            txtSaldo.clear();
        }
    }

    private void calcularSaldoManual() {
        try {
            double precio = Double.parseDouble(txtPrecio.getText());
            double saldado = Double.parseDouble(txtSaldado.getText());
            double saldo = precio - saldado;

            txtSaldo.setText(String.format("%.2f", saldo));
            actualizarEstadoPago(saldado, saldo);

        } catch (NumberFormatException e) {
            txtSaldo.clear();
        }
    }

    private void actualizarEstadoPago(double saldado, double saldo) {
        if (saldo <= 0) {
            cmbEstadoPagoInforme.setValue(EstadoPagoInforme.PAGADO);
        } else if (saldado > 0) {
            cmbEstadoPagoInforme.setValue(EstadoPagoInforme.PAGO_PARCIAL);
        } else {
            cmbEstadoPagoInforme.setValue(EstadoPagoInforme.PENDIENTE);
        }
    }

    private void configurarTabla() {
        colPaciente.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getPaciente().getNombre()));
        colTipoInforme.setCellValueFactory(new PropertyValueFactory<>("tipoInforme"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colSaldado.setCellValueFactory(new PropertyValueFactory<>("saldado"));
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));
        colEstadoInforme.setCellValueFactory(new PropertyValueFactory<>("estadoInforme"));
        colEstadoPagoInforme.setCellValueFactory(new PropertyValueFactory<>("estadoPagoInforme"));

        tablaInformes.setItems(listaInformes);
    }

    private void configurarComboBoxes() {
        cmbTipoInforme.setItems(FXCollections.observableArrayList(TipoInforme.values()));
        cmbEstadoInforme.setItems(FXCollections.observableArrayList(EstadoInforme.values()));
        cmbEstadoPagoInforme.setItems(FXCollections.observableArrayList(EstadoPagoInforme.values()));

        cmbEstadoInforme.setValue(EstadoInforme.SIN_HACER);

        pacientesFiltrados = new FilteredList<>(listaPacientes, p -> true);
        cmbPaciente.setItems(pacientesFiltrados);

        cmbPaciente.setConverter(new StringConverter<Paciente>() {
            @Override
            public String toString(Paciente paciente) {
                return paciente != null ? paciente.getNombre() : "";
            }

            @Override
            public Paciente fromString(String string) {
                return listaPacientes.stream()
                        .filter(p -> p.getNombre().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void configurarSeleccionTabla() {
        tablaInformes.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        cargarInformeEnFormulario(newValue);
                        btnEditar.setDisable(false);
                        btnBorrar.setDisable(false);
                    } else {
                        btnEditar.setDisable(true);
                        btnBorrar.setDisable(true);
                    }
                });
    }

    @FXML
    private void agregarInforme() {
        try {
            if (!validarFormulario()) {
                return;
            }

            double precio = Double.parseDouble(txtPrecio.getText());
            double saldado = Double.parseDouble(txtSaldado.getText());
            double saldo = Double.parseDouble(txtSaldo.getText());

            Informe nuevo = new Informe(
                    null,
                    cmbPaciente.getValue(),
                    cmbTipoInforme.getValue(),
                    precio,
                    saldado,
                    saldo,
                    cmbEstadoInforme.getValue(),
                    cmbEstadoPagoInforme.getValue(),
                    txtNotas.getText().trim()
            );

            ServiceManager.getInformeDAO().save(nuevo);

            Paciente paciente = cmbPaciente.getValue();
            double nuevaDeuda = paciente.getDeuda() + saldo;
            ServiceManager.getPacienteDAO().updateDeuda(paciente.getId(), nuevaDeuda);

            mostrarMensaje("Informe creado exitosamente", Alert.AlertType.INFORMATION);

            cargarInformes();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("Por favor ingrese números válidos", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarMensaje("Error al crear informe: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void editarInforme() {
        if (informeSeleccionado == null) {
            mostrarMensaje("Seleccione un informe para editar", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (!validarFormulario()) {
                return;
            }

            double precioAnterior = informeSeleccionado.getPrecio();
            double saldadoAnterior = informeSeleccionado.getSaldado();
            double saldoAnterior = informeSeleccionado.getSaldo();

            informeSeleccionado.setPaciente(cmbPaciente.getValue());
            informeSeleccionado.setTipoInforme(cmbTipoInforme.getValue());
            informeSeleccionado.setPrecio(Double.parseDouble(txtPrecio.getText()));
            informeSeleccionado.setSaldado(Double.parseDouble(txtSaldado.getText()));
            informeSeleccionado.setSaldo(Double.parseDouble(txtSaldo.getText()));
            informeSeleccionado.setEstadoInforme(cmbEstadoInforme.getValue());
            informeSeleccionado.setEstadoPagoInforme(cmbEstadoPagoInforme.getValue());
            informeSeleccionado.setNotas(txtNotas.getText().trim());

            ServiceManager.getInformeDAO().update(informeSeleccionado);

            double diferenciaSaldo = informeSeleccionado.getSaldo() - saldoAnterior;
            if (diferenciaSaldo != 0) {
                Paciente paciente = cmbPaciente.getValue();
                double nuevaDeuda = paciente.getDeuda() + diferenciaSaldo;
                ServiceManager.getPacienteDAO().updateDeuda(paciente.getId(), nuevaDeuda);
            }

            mostrarMensaje("Informe actualizado exitosamente", Alert.AlertType.INFORMATION);

            cargarInformes();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("Por favor ingrese números válidos", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarMensaje("Error al actualizar informe: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarInforme() {
        if (informeSeleccionado == null) {
            mostrarMensaje("Seleccione un informe para eliminar", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este informe?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try {
                Paciente paciente = informeSeleccionado.getPaciente();
                double nuevaDeuda = paciente.getDeuda() - informeSeleccionado.getSaldo();
                ServiceManager.getPacienteDAO().updateDeuda(paciente.getId(), nuevaDeuda);

                ServiceManager.getInformeDAO().delete(informeSeleccionado.getId());
                mostrarMensaje("Informe eliminado exitosamente", Alert.AlertType.INFORMATION);
                cargarInformes();
                limpiarFormulario();
            } catch (Exception e) {
                mostrarMensaje("Error al eliminar informe: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
        cmbPaciente.setValue(null);
        cmbPaciente.getEditor().setText(""); // NUEVO: Limpiar el texto del editor
        cmbTipoInforme.setValue(null);
        txtPrecio.clear();
        txtSaldado.clear();
        txtSaldo.clear();
        cmbEstadoInforme.setValue(EstadoInforme.SIN_HACER);
        cmbEstadoPagoInforme.setValue(null);
        txtNotas.clear();
        chkEditarSaldado.setSelected(false);
        txtSaldado.setDisable(true);

        tablaInformes.getSelectionModel().clearSelection();
        informeSeleccionado = null;
        btnEditar.setDisable(true);
        btnBorrar.setDisable(true);
    }

    private void cargarDatos() {
        cargarPacientes();
        cargarInformes();
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

    private void cargarInformes() {
        try {
            var informes = ServiceManager.getInformeDAO().findAll();
            listaInformes.clear();
            listaInformes.addAll(informes);
        } catch (Exception e) {
            mostrarMensaje("Error al cargar informes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarInformeEnFormulario(Informe informe) {
        informeSeleccionado = informe;
        cmbPaciente.setValue(informe.getPaciente());
        cmbTipoInforme.setValue(informe.getTipoInforme());
        txtPrecio.setText(String.valueOf(informe.getPrecio()));
        txtSaldado.setText(String.valueOf(informe.getSaldado()));
        txtSaldo.setText(String.valueOf(informe.getSaldo()));
        cmbEstadoInforme.setValue(informe.getEstadoInforme());
        cmbEstadoPagoInforme.setValue(informe.getEstadoPagoInforme());
        txtNotas.setText(informe.getNotas());
    }

    private boolean validarFormulario() {
        if (cmbPaciente.getValue() == null) {
            mostrarMensaje("Seleccione un paciente", Alert.AlertType.WARNING);
            return false;
        }

        if (cmbTipoInforme.getValue() == null) {
            mostrarMensaje("Seleccione un tipo de informe", Alert.AlertType.WARNING);
            return false;
        }

        if (cmbEstadoInforme.getValue() == null) {
            mostrarMensaje("Seleccione un estado", Alert.AlertType.WARNING);
            return false;
        }

        try {
            Double.parseDouble(txtPrecio.getText());
            Double.parseDouble(txtSaldado.getText());
            Double.parseDouble(txtSaldo.getText());
        } catch (NumberFormatException e) {
            mostrarMensaje("Los campos numéricos deben contener valores válidos", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Gestión de Informes");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}