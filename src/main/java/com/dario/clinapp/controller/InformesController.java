package com.dario.clinapp.controller;

import com.dario.clinapp.dao.database.ServiceManager;
import com.dario.clinapp.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

public class InformesController {

    // Controles del formulario
    @FXML private ComboBox<Paciente> cmbPaciente;
    @FXML private ComboBox<TipoInforme> cmbTipoInforme;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtAdelantado;
    @FXML private TextField txtSaldo; // No editable, se calcula automáticamente
    @FXML private ComboBox<EstadoPagoInforme> cmbEstadoPago;
    @FXML private ComboBox<EstadoInforme> cmbEstadoInforme;
    @FXML private TextField txtNotas;

    // Botones
    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnBorrar;
    @FXML private Button btnLimpiar;

    // Tabla y columnas
    @FXML private TableView<Informe> tablaInformes;
    @FXML private TableColumn<Informe, String> colPaciente;
    @FXML private TableColumn<Informe, TipoInforme> colTipoInforme;
    @FXML private TableColumn<Informe, Double> colPrecio;
    @FXML private TableColumn<Informe, Double> colAdelantado;
    @FXML private TableColumn<Informe, Double> colSaldo;
    @FXML private TableColumn<Informe, EstadoPagoInforme> colEstadoPago;
    @FXML private TableColumn<Informe, EstadoInforme> colEstadoInforme;
    @FXML private TableColumn<Informe, String> colNotas;

    // Variables de clase
    private ObservableList<Informe> listaInformes = FXCollections.observableArrayList();
    private ObservableList<Paciente> listaPacientes = FXCollections.observableArrayList();
    private Informe informeSeleccionado = null;

    public void initialize() {
        configurarTabla();
        configurarComboBoxes();
        cargarPacientes();
        cargarInformes();
        configurarSeleccionTabla();
        configurarCalculoSaldo();

        // Configurar botones inicialmente
        btnEditar.setDisable(true);
        btnBorrar.setDisable(true);

        // Configurar campos iniciales
        txtAdelantado.setText("0");
        txtSaldo.setEditable(false);
    }

    private void configurarTabla() {
        // Configurar las columnas
        colPaciente.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaciente().getNombre()));

        colTipoInforme.setCellValueFactory(new PropertyValueFactory<>("tipoInforme"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colAdelantado.setCellValueFactory(new PropertyValueFactory<>("entregado"));
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));
        colEstadoPago.setCellValueFactory(new PropertyValueFactory<>("estadoPagoInforme"));
        colEstadoInforme.setCellValueFactory(new PropertyValueFactory<>("estadoInforme"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));

        tablaInformes.setItems(listaInformes);
    }

    private void configurarComboBoxes() {
        // Configurar ComboBox de Tipos de Informe
        cmbTipoInforme.setItems(FXCollections.observableArrayList(TipoInforme.values()));

        // Configurar ComboBox de Estados de Pago
        cmbEstadoPago.setItems(FXCollections.observableArrayList(EstadoPagoInforme.values()));

        // Configurar ComboBox de Estados de Informe
        cmbEstadoInforme.setItems(FXCollections.observableArrayList(EstadoInforme.values()));

        // Configurar ComboBox de Pacientes
        cmbPaciente.setItems(listaPacientes);

        // Converter para mostrar solo el nombre del paciente en el ComboBox
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

    private void configurarCalculoSaldo() {
        // Listener para calcular automáticamente el saldo
        txtPrecio.textProperty().addListener((obs, oldVal, newVal) -> calcularSaldo());
        txtAdelantado.textProperty().addListener((obs, oldVal, newVal) -> calcularSaldo());
    }

    private void calcularSaldo() {
        try {
            double precio = txtPrecio.getText().isEmpty() ? 0 : Double.parseDouble(txtPrecio.getText());
            double adelantado = txtAdelantado.getText().isEmpty() ? 0 : Double.parseDouble(txtAdelantado.getText());
            double saldo = precio - adelantado;

            txtSaldo.setText(String.format("%.2f", saldo));

            // Actualizar automáticamente el estado de pago basado en el saldo
            if (saldo <= 0) {
                cmbEstadoPago.setValue(EstadoPagoInforme.PAGADO);
            } else if (adelantado > 0 && saldo > 0) {
                cmbEstadoPago.setValue(EstadoPagoInforme.PAGO_PARCIAL);
            } else {
                cmbEstadoPago.setValue(EstadoPagoInforme.PENDIENTE);
            }

        } catch (NumberFormatException e) {
            txtSaldo.setText("0.00");
        }
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
            double adelantado = Double.parseDouble(txtAdelantado.getText());
            double saldo = precio - adelantado;

            Informe nuevoInforme = new Informe(
                    null, // ID null para nuevo informe
                    cmbPaciente.getValue(),
                    cmbTipoInforme.getValue(),
                    precio,
                    adelantado,
                    saldo,
                    cmbEstadoInforme.getValue(),
                    cmbEstadoPago.getValue(),
                    txtNotas.getText().trim()
            );

            ServiceManager.getInformeDAO().save(nuevoInforme);

            // Si hay saldo pendiente, agregarlo a la deuda del paciente
            if (saldo > 0) {
                actualizarDeudaPaciente(cmbPaciente.getValue(), saldo);
            }

            mostrarMensaje("Informe agregado exitosamente", Alert.AlertType.INFORMATION);

            cargarInformes();
            cargarPacientes(); // Recargar para mostrar deuda actualizada
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("Por favor ingrese números válidos en precio y adelantado", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarMensaje("Error al agregar informe: " + e.getMessage(), Alert.AlertType.ERROR);
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

            // Revertir la deuda anterior si había saldo pendiente
            if (informeSeleccionado.getSaldo() > 0) {
                actualizarDeudaPaciente(informeSeleccionado.getPaciente(), -informeSeleccionado.getSaldo());
            }

            double precio = Double.parseDouble(txtPrecio.getText());
            double adelantado = Double.parseDouble(txtAdelantado.getText());
            double saldo = precio - adelantado;

            informeSeleccionado.setPaciente(cmbPaciente.getValue());
            informeSeleccionado.setTipoInforme(cmbTipoInforme.getValue());
            informeSeleccionado.setPrecio(precio);
            informeSeleccionado.setEntregado(adelantado);
            informeSeleccionado.setSaldo(saldo);
            informeSeleccionado.setEstadoInforme(cmbEstadoInforme.getValue());
            informeSeleccionado.setEstadoPagoInforme(cmbEstadoPago.getValue());
            informeSeleccionado.setNotas(txtNotas.getText().trim());

            ServiceManager.getInformeDAO().update(informeSeleccionado);

            // Agregar nuevo saldo a la deuda si existe
            if (saldo > 0) {
                actualizarDeudaPaciente(cmbPaciente.getValue(), saldo);
            }

            mostrarMensaje("Informe actualizado exitosamente", Alert.AlertType.INFORMATION);

            cargarInformes();
            cargarPacientes();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("Por favor ingrese números válidos en precio y adelantado", Alert.AlertType.ERROR);
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
        confirmacion.setContentText("Esta acción revertirá el saldo en la deuda del paciente si corresponde.");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try {
                // Revertir la deuda si había saldo pendiente
                if (informeSeleccionado.getSaldo() > 0) {
                    actualizarDeudaPaciente(informeSeleccionado.getPaciente(), -informeSeleccionado.getSaldo());
                }

                ServiceManager.getInformeDAO().delete(informeSeleccionado.getId());
                mostrarMensaje("Informe eliminado exitosamente", Alert.AlertType.INFORMATION);

                cargarInformes();
                cargarPacientes();
                limpiarFormulario();

            } catch (Exception e) {
                mostrarMensaje("Error al eliminar informe: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
        cmbPaciente.setValue(null);
        cmbTipoInforme.setValue(null);
        txtPrecio.clear();
        txtAdelantado.setText("0");
        txtSaldo.setText("0.00");
        cmbEstadoPago.setValue(null);
        cmbEstadoInforme.setValue(null);
        txtNotas.clear();

        tablaInformes.getSelectionModel().clearSelection();
        informeSeleccionado = null;
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
        txtAdelantado.setText(String.valueOf(informe.getEntregado()));
        txtSaldo.setText(String.format("%.2f", informe.getSaldo()));
        cmbEstadoPago.setValue(informe.getEstadoPagoInforme());
        cmbEstadoInforme.setValue(informe.getEstadoInforme());
        txtNotas.setText(informe.getNotas());
    }

    private void actualizarDeudaPaciente(Paciente paciente, double cambio) {
        try {
            double nuevaDeuda = paciente.getDeuda() + cambio;
            paciente.setDeuda(nuevaDeuda);
            ServiceManager.getPacienteDAO().updateDeuda(paciente.getId(), nuevaDeuda);
        } catch (Exception e) {
            mostrarMensaje("Error al actualizar deuda del paciente: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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

        if (txtPrecio.getText().trim().isEmpty()) {
            mostrarMensaje("Ingrese el precio del informe", Alert.AlertType.WARNING);
            return false;
        }

        if (cmbEstadoInforme.getValue() == null) {
            mostrarMensaje("Seleccione el estado del informe", Alert.AlertType.WARNING);
            return false;
        }

        try {
            double precio = Double.parseDouble(txtPrecio.getText());
            double adelantado = Double.parseDouble(txtAdelantado.getText());

            if (precio < 0 || adelantado < 0) {
                mostrarMensaje("Los montos no pueden ser negativos", Alert.AlertType.WARNING);
                return false;
            }

            if (adelantado > precio) {
                mostrarMensaje("El monto adelantado no puede ser mayor al precio total", Alert.AlertType.WARNING);
                return false;
            }

        } catch (NumberFormatException e) {
            mostrarMensaje("Ingrese números válidos en precio y adelantado", Alert.AlertType.WARNING);
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