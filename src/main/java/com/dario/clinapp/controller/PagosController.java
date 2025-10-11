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
import javafx.collections.transformation.FilteredList;
import java.time.LocalDate;
import java.util.stream.Collectors;
import com.dario.clinapp.service.PaymentAllocationService;

public class PagosController {

    @FXML private ComboBox<Paciente> cmbPaciente;
    @FXML private ComboBox<TipoPago> cmbTipoPago;
    @FXML private ComboBox<FormaDePago> cmbFormaPago;
    @FXML private TextField txtMonto;
    @FXML private DatePicker dateFecha;
    @FXML private TextField txtNotas;

    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    @FXML private TableView<Pago> tablaPagos;
    @FXML private TableColumn<Pago, String> colPaciente;
    @FXML private TableColumn<Pago, TipoPago> colTipoPago;
    @FXML private TableColumn<Pago, FormaDePago> colFormaPago;
    @FXML private TableColumn<Pago, Double> colMonto;
    @FXML private TableColumn<Pago, LocalDate> colFecha;
    @FXML private TableColumn<Pago, String> colNotas;

    @FXML private ComboBox<String> cmbMesFiltro;
    @FXML private ComboBox<Integer> cmbAnioFiltro;

    private ObservableList<Pago> listaPagos = FXCollections.observableArrayList();
    private ObservableList<Paciente> listaPacientes = FXCollections.observableArrayList();
    private Pago pagoSeleccionado = null;
    private FilteredList<Paciente> pacientesFiltrados;

    public void initialize() {
        configurarTabla();
        configurarComboBoxes();
        cargarPacientes();
        configurarSeleccionTabla();
        configurarFiltros();
        configurarBusquedaPaciente();

        dateFecha.setValue(LocalDate.now());

        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);

        cmbPaciente.setOnAction(e -> mostrarDeudaPaciente());

       filtrarPagos();
    }

    private void configurarFiltros() {
        ObservableList<String> meses = FXCollections.observableArrayList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        cmbMesFiltro.setItems(meses);

        ObservableList<Integer> anios = FXCollections.observableArrayList();
        int anioActual = LocalDate.now().getYear();
        for (int i = anioActual - 4; i <= anioActual; i++) {
            anios.add(i);
        }
        cmbAnioFiltro.setItems(anios);

        cmbMesFiltro.setValue(meses.get(LocalDate.now().getMonthValue() - 1));
        cmbAnioFiltro.setValue(anioActual);
    }

    private void configurarTabla() {
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
        cmbTipoPago.setItems(FXCollections.observableArrayList(TipoPago.values()));

        cmbFormaPago.setItems(FXCollections.observableArrayList(FormaDePago.values()));
        cmbFormaPago.setValue(FormaDePago.TRANSFERENCIA);

        pacientesFiltrados = new FilteredList<>(listaPacientes, p -> true);
        cmbPaciente.setItems(pacientesFiltrados);

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

    private void configurarBusquedaPaciente() {
        cmbPaciente.setEditable(true);

        cmbPaciente.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final TextField editor = cmbPaciente.getEditor();
            final Paciente seleccionado = cmbPaciente.getSelectionModel().getSelectedItem();

            // Si hay un paciente seleccionado y el texto coincide, no filtrar
            if (seleccionado != null && seleccionado.getNombre().equals(newValue)) {
                return;
            }

            // Filtrar la lista
            pacientesFiltrados.setPredicate(paciente -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String filtro = newValue.toLowerCase();
                return paciente.getNombre().toLowerCase().contains(filtro);
            });

            // Si hay resultados y el ComboBox no está abierto, abrirlo
            if (!pacientesFiltrados.isEmpty() && !cmbPaciente.isShowing()) {
                cmbPaciente.show();
            }
        });

        // Cuando se selecciona un paciente del dropdown, actualizar el texto
        cmbPaciente.setOnAction(e -> {
            Paciente seleccionado = cmbPaciente.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                cmbPaciente.getEditor().setText(seleccionado.getNombre());
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

            marcarSesionesComoPagas(paciente, montoPago);

            actualizarEstadoInformes(paciente);

            actualizarDeudaPaciente(paciente, -montoPago);

            cargarPagos();
            cargarPacientes();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarMensaje("Por favor ingrese un monto válido", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarMensaje("Error al registrar pago: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void marcarSesionesComoPagas(Paciente paciente, double montoPago) {
        try {
            List<Sesion> sesionesPendientes = ServiceManager.getSesionDAO()
                    .findByPacienteId(paciente.getId())
                    .stream()
                    .filter(s -> s.getEstadoPagoSesion() == EstadoPagoSesion.PENDIENTE)
                    .sorted((s1, s2) -> s1.getFecha().compareTo(s2.getFecha()))
                    .collect(Collectors.toList());

            double montoRestante = montoPago;
            double precioPorSesion = paciente.getPrecioPorSesion();

            for (Sesion sesion : sesionesPendientes) {
                if (montoRestante >= precioPorSesion) {
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

    private void actualizarEstadoInformes(Paciente paciente) {
        try {
            PaymentAllocationService paymentService = new PaymentAllocationService(
                    ServiceManager.getSesionDAO(),
                    ServiceManager.getPagoDAO(),
                    ServiceManager.getInformeDAO()
            );

            List<Informe> informes = ServiceManager.getInformeDAO().findByPacienteId(paciente.getId());

            for (Informe informe : informes) {
                double entregadoActualizado = paymentService.calcularSaldadoInforme(paciente, informe.getId());
                double saldoActualizado = informe.getPrecio() - entregadoActualizado;

                informe.setSaldado(entregadoActualizado);
                informe.setSaldo(saldoActualizado);

                if (saldoActualizado <= 0) {
                    informe.setEstadoPagoInforme(EstadoPagoInforme.PAGADO);
                } else if (entregadoActualizado > 0) {
                    informe.setEstadoPagoInforme(EstadoPagoInforme.PAGO_PARCIAL);
                } else {
                    informe.setEstadoPagoInforme(EstadoPagoInforme.PENDIENTE);
                }

                ServiceManager.getInformeDAO().update(informe);
            }

        } catch (Exception e) {
            System.err.println("Error actualizando estado de informes: " + e.getMessage());
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

            actualizarDeudaPaciente(pagoSeleccionado.getPaciente(), pagoSeleccionado.getMonto());

            pagoSeleccionado.setPaciente(cmbPaciente.getValue());
            pagoSeleccionado.setTipoPago(cmbTipoPago.getValue());
            pagoSeleccionado.setMonto(Double.parseDouble(txtMonto.getText()));
            pagoSeleccionado.setFormaDePago(cmbFormaPago.getValue());
            pagoSeleccionado.setFecha(dateFecha.getValue());
            pagoSeleccionado.setNotas(txtNotas.getText().trim());

            ServiceManager.getPagoDAO().update(pagoSeleccionado);

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
        cmbFormaPago.setValue(FormaDePago.TRANSFERENCIA);
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

    @FXML
    private void cargarPagos() {
        try {
            var pagos = ServiceManager.getPagoDAO().findAll();
            listaPagos.clear();
            listaPagos.addAll(pagos);
        } catch (Exception e) {
            mostrarMensaje("Error al cargar pagos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void filtrarPagos() {
        if (cmbMesFiltro.getValue() == null || cmbAnioFiltro.getValue() == null) {
            return;
        }

        try {
            int mes = cmbMesFiltro.getSelectionModel().getSelectedIndex() + 1;
            int anio = cmbAnioFiltro.getValue();

            LocalDate desde = LocalDate.of(anio, mes, 1);
            LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());

            var pagos = ServiceManager.getPagoDAO().findByRangoFechas(desde, hasta);
            listaPagos.clear();
            listaPagos.addAll(pagos);
        } catch (Exception e) {
            mostrarMensaje("Error al filtrar sesiones: " + e.getMessage(), Alert.AlertType.ERROR);
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