package com.dario.clinapp.controller;

import com.dario.clinapp.dao.database.ServiceManager;
import com.dario.clinapp.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.util.List;

import java.time.LocalDate;
import java.util.stream.Collectors;

public class PagosController {

    // Controles del formulario
    @FXML private ComboBox<Paciente> cmbPaciente;
    @FXML private ComboBox<TipoPago> cmbTipoPago;
    @FXML private ComboBox<FormaDePago> cmbFormaPago;
    @FXML private TextField txtMonto;
    @FXML private DatePicker dateFecha;
    @FXML private TextField txtNotas;

    // Botones
    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    // Tabla y columnas
    @FXML private TableView<Pago> tablaPagos;
    @FXML private TableColumn<Pago, String> colPaciente;
    @FXML private TableColumn<Pago, TipoPago> colTipoPago;
    @FXML private TableColumn<Pago, FormaDePago> colFormaPago;
    @FXML private TableColumn<Pago, Double> colMonto;
    @FXML private TableColumn<Pago, LocalDate> colFecha;
    @FXML private TableColumn<Pago, String> colNotas;

    // Variables de clase
    private ObservableList<Pago> listaPagos = FXCollections.observableArrayList();
    private ObservableList<Paciente> listaPacientes = FXCollections.observableArrayList();
    private Pago pagoSeleccionado = null;

    public void initialize() {
        configurarTabla();
        configurarComboBoxes();
        cargarPacientes();
        cargarPagos();
        configurarSeleccionTabla();

        // Configurar fecha por defecto
        dateFecha.setValue(LocalDate.now());

        // Configurar botones inicialmente
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);

        // Listener para mostrar deuda del paciente seleccionado
        cmbPaciente.setOnAction(e -> mostrarDeudaPaciente());
    }

    private void configurarTabla() {
        // Configurar las columnas
        colPaciente.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaciente().getNombre()));

        colTipoPago.setCellValueFactory(new PropertyValueFactory<>("tipoPago"));
        colFormaPago.setCellValueFactory(new PropertyValueFactory<>("formaDePago"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));

        tablaPagos.setItems(listaPagos);
    }

    private void configurarComboBoxes() {
        // Configurar ComboBox de Tipos de Pago
        cmbTipoPago.setItems(FXCollections.observableArrayList(TipoPago.values()));

        // Configurar ComboBox de Formas de Pago
        cmbFormaPago.setItems(FXCollections.observableArrayList(FormaDePago.values()));

        // Configurar ComboBox de Pacientes
        cmbPaciente.setItems(listaPacientes);

        // Converter para mostrar solo el nombre del paciente en el ComboBox
        cmbPaciente.setConverter(new StringConverter<Paciente>() {
            @Override
            public String toString(Paciente paciente) {
                if (paciente != null) {
                    return paciente.getNombre() + " (Deuda: $" + String.format("%.2f", paciente.getDeuda()) + ")";
                }
                return "";
            }

            @Override
            public Paciente fromString(String string) {
                return listaPacientes.stream()
                        .filter(p -> string.startsWith(p.getNombre()))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void configurarSeleccionTabla() {
        tablaPagos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        cargarPagoEnFormulario(newValue);
                        btnEditar.setDisable(false);
                        btnEliminar.setDisable(false);
                    } else {
                        btnEditar.setDisable(true);
                        btnEliminar.setDisable(true);
                    }
                });
    }

    @FXML
    private void registrarPago() {
        try {
            if (!validarFormulario()) {
                return;
            }

            double montoPago = Double.parseDouble(txtMonto.getText());
            Paciente paciente = cmbPaciente.getValue();

            Pago nuevoPago = new Pago(
                    null,
                    paciente,
                    cmbTipoPago.getValue(),
                    montoPago,
                    cmbFormaPago.getValue(),
                    dateFecha.getValue(),
                    txtNotas.getText().trim()
            );

            ServiceManager.getPagoDAO().save(nuevoPago);

            TipoPago tipoPago = cmbTipoPago.getValue();

            if (tipoPago == TipoPago.SESION) {
                // Si es pago de sesión, marcar sesiones como pagas
                marcarSesionesComoPagas(paciente, montoPago);
            }

            // Actualizar deuda del paciente (restarle lo que pagó)
            actualizarDeudaPaciente(paciente, -montoPago);

            mostrarMensaje("Pago registrado exitosamente", Alert.AlertType.INFORMATION);

            cargarPagos();
            cargarPacientes(); // Recargar para mostrar deuda actualizada
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("Por favor ingrese un monto válido", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarMensaje("Error al registrar pago: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void marcarSesionesComoPagas(Paciente paciente, double montoPago) {
        try {
            // Obtener sesiones pendientes del paciente
            List<Sesion> sesionesPendientes = ServiceManager.getSesionDAO()
                    .findByPacienteId(paciente.getId())
                    .stream()
                    .filter(s -> s.getEstadoPagoSesion() == EstadoPagoSesion.PENDIENTE)
                    .sorted((s1, s2) -> s1.getFecha().compareTo(s2.getFecha())) // Más antiguas primero
                    .collect(Collectors.toList());

            double montoRestante = montoPago;
            double precioPorSesion = paciente.getPrecioPorSesion();

            for (Sesion sesion : sesionesPendientes) {
                if (montoRestante >= precioPorSesion) {
                    // Marcar sesión como paga
                    sesion.setEstadoPagoSesion(EstadoPagoSesion.PAGA);
                    ServiceManager.getSesionDAO().update(sesion);
                    montoRestante -= precioPorSesion;

                    if (montoRestante <= 0) break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error marcando sesiones como pagas: " + e.getMessage());
        }
    }

    @FXML
    private void editarPago() {
        if (pagoSeleccionado == null) {
            mostrarMensaje("Seleccione un pago para editar", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (!validarFormulario()) {
                return;
            }

            // Revertir el pago anterior (sumar lo que se había restado)
            actualizarDeudaPaciente(pagoSeleccionado.getPaciente(), pagoSeleccionado.getMonto());

            // Aplicar el nuevo pago
            pagoSeleccionado.setPaciente(cmbPaciente.getValue());
            pagoSeleccionado.setTipoPago(cmbTipoPago.getValue());
            pagoSeleccionado.setMonto(Double.parseDouble(txtMonto.getText()));
            pagoSeleccionado.setFormaDePago(cmbFormaPago.getValue());
            pagoSeleccionado.setFecha(dateFecha.getValue());
            pagoSeleccionado.setNotas(txtNotas.getText().trim());

            ServiceManager.getPagoDAO().update(pagoSeleccionado);

            // Actualizar deuda con el nuevo monto
            actualizarDeudaPaciente(cmbPaciente.getValue(), -Double.parseDouble(txtMonto.getText()));

            mostrarMensaje("Pago actualizado exitosamente", Alert.AlertType.INFORMATION);

            cargarPagos();
            cargarPacientes();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("Por favor ingrese un monto válido", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarMensaje("Error al actualizar pago: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarPago() {
        if (pagoSeleccionado == null) {
            mostrarMensaje("Seleccione un pago para eliminar", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este pago?");
        confirmacion.setContentText("Esta acción revertirá el pago en la deuda del paciente.");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try {
                // Revertir el pago (sumar lo que se había restado a la deuda)
                actualizarDeudaPaciente(pagoSeleccionado.getPaciente(), pagoSeleccionado.getMonto());

                ServiceManager.getPagoDAO().delete(pagoSeleccionado.getId());
                mostrarMensaje("Pago eliminado exitosamente", Alert.AlertType.INFORMATION);

                cargarPagos();
                cargarPacientes();
                limpiarFormulario();

            } catch (Exception e) {
                mostrarMensaje("Error al eliminar pago: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
        cmbPaciente.setValue(null);
        cmbTipoPago.setValue(null);
        cmbFormaPago.setValue(null);
        txtMonto.clear();
        dateFecha.setValue(LocalDate.now());
        txtNotas.clear();

        tablaPagos.getSelectionModel().clearSelection();
        pagoSeleccionado = null;
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
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

    private void cargarPagos() {
        try {
            var pagos = ServiceManager.getPagoDAO().findAll();
            listaPagos.clear();
            listaPagos.addAll(pagos);
        } catch (Exception e) {
            mostrarMensaje("Error al cargar pagos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarPagoEnFormulario(Pago pago) {
        pagoSeleccionado = pago;
        cmbPaciente.setValue(pago.getPaciente());
        cmbTipoPago.setValue(pago.getTipoPago());
        cmbFormaPago.setValue(pago.getFormaDePago());
        txtMonto.setText(String.valueOf(pago.getMonto()));
        dateFecha.setValue(pago.getFecha());
        txtNotas.setText(pago.getNotas());
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

    private void mostrarDeudaPaciente() {
        if (cmbPaciente.getValue() != null) {
            Paciente paciente = cmbPaciente.getValue();
            // Ya se muestra en el converter del ComboBox
            System.out.println("Paciente seleccionado: " + paciente.getNombre() + " - Deuda: $" + paciente.getDeuda());
        }
    }

    private boolean validarFormulario() {
        if (cmbPaciente.getValue() == null) {
            mostrarMensaje("Seleccione un paciente", Alert.AlertType.WARNING);
            return false;
        }

        if (cmbTipoPago.getValue() == null) {
            mostrarMensaje("Seleccione un tipo de pago", Alert.AlertType.WARNING);
            return false;
        }

        if (cmbFormaPago.getValue() == null) {
            mostrarMensaje("Seleccione una forma de pago", Alert.AlertType.WARNING);
            return false;
        }

        if (txtMonto.getText().trim().isEmpty()) {
            mostrarMensaje("Ingrese el monto del pago", Alert.AlertType.WARNING);
            return false;
        }

        try {
            double monto = Double.parseDouble(txtMonto.getText());
            if (monto <= 0) {
                mostrarMensaje("El monto debe ser mayor que cero", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("Ingrese un monto válido", Alert.AlertType.WARNING);
            return false;
        }

        if (dateFecha.getValue() == null) {
            mostrarMensaje("Seleccione una fecha", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Gestión de Pagos");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}