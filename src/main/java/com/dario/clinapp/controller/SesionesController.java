package com.dario.clinapp.controller;

import com.dario.clinapp.dao.database.ServiceManager;
import com.dario.clinapp.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class SesionesController {

    @FXML private ComboBox<Paciente> cmbPaciente;
    @FXML private DatePicker dateFecha;
    @FXML private ComboBox<TipoSesion> cmbTipoSesion;
    @FXML private CheckBox chkPaga;
    @FXML private TextField txtNotas;

    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    @FXML private TableView<Sesion> tablaSesiones;
    @FXML private TableColumn<Sesion, String> colPaciente;
    @FXML private TableColumn<Sesion, LocalDate> colFecha;
    @FXML private TableColumn<Sesion, TipoSesion> colTipoSesion;
    @FXML private TableColumn<Sesion, Double> colPrecio;
    @FXML private TableColumn<Sesion, EstadoPagoSesion> colPaga;
    @FXML private TableColumn<Sesion, String> colNotas;

    @FXML private ComboBox<String> cmbMesFiltro;
    @FXML private ComboBox<Integer> cmbAnioFiltro;
    @FXML private Button btnFiltrar;
    @FXML private Button btnMostrarTodo;

    private ObservableList<Sesion> listaSesiones = FXCollections.observableArrayList();
    private ObservableList<Paciente> listaPacientes = FXCollections.observableArrayList();
    private Sesion sesionSeleccionada = null;

    public void initialize() {
        configurarTabla();
        configurarComboBoxes();
        cargarPacientes();
        cargarSesiones();
        configurarSeleccionTabla();
        configurarFiltros();

        dateFecha.setValue(LocalDate.now());

        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);

        cmbPaciente.setOnAction(e -> {
            Paciente pacienteSeleccionado = cmbPaciente.getValue();
            if (pacienteSeleccionado != null) {
                cmbTipoSesion.setValue(pacienteSeleccionado.getTipoSesion());
            }
        });
    }

    private void configurarTabla() {
        colPaciente.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaciente().getNombre()));

        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipoSesion.setCellValueFactory(new PropertyValueFactory<>("tipoSesion"));

        colPrecio.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPaciente().getPrecioPorSesion()));

        colPaga.setCellValueFactory(new PropertyValueFactory<>("estadoPagoSesion"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));

        tablaSesiones.setItems(listaSesiones);
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

    private void configurarComboBoxes() {
        cmbTipoSesion.setItems(FXCollections.observableArrayList(TipoSesion.values()));

        cmbPaciente.setItems(listaPacientes);

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
        tablaSesiones.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        cargarSesionEnFormulario(newValue);
                        btnEditar.setDisable(false);
                        btnEliminar.setDisable(false);
                    } else {
                        btnEditar.setDisable(true);
                        btnEliminar.setDisable(true);
                    }
                });
    }

    @FXML
    private void agregarSesion() {
        try {
            if (!validarFormulario()) {
                return;
            }

            EstadoPagoSesion estadoPago = chkPaga.isSelected() ?
                    EstadoPagoSesion.PAGA : EstadoPagoSesion.PENDIENTE;

            Sesion nuevaSesion = new Sesion(
                    null, // ID null para nueva sesión
                    cmbTipoSesion.getValue(),
                    cmbPaciente.getValue(),
                    dateFecha.getValue(),
                    estadoPago,
                    txtNotas.getText().trim()
            );

            ServiceManager.getSesionDAO().save(nuevaSesion);

            if (estadoPago == EstadoPagoSesion.PENDIENTE) {
                double montoSesion = cmbPaciente.getValue().getPrecioPorSesion();
                actualizarDeudaPaciente(cmbPaciente.getValue(), montoSesion);
            }

            cargarSesiones();
            cargarPacientes();
            limpiarFormulario();

        } catch (Exception e) {
            mostrarMensaje("Error al registrar sesión: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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

    @FXML
    private void editarSesion() {
        if (sesionSeleccionada == null) {
            mostrarMensaje("Seleccione una sesión para editar", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (!validarFormulario()) {
                return;
            }

            EstadoPagoSesion estadoOriginal = sesionSeleccionada.getEstadoPagoSesion();

            EstadoPagoSesion estadoNuevo = chkPaga.isSelected() ?
                    EstadoPagoSesion.PAGA : EstadoPagoSesion.PENDIENTE;

            if (estadoOriginal != estadoNuevo) {
                double precioSesion = cmbPaciente.getValue().getPrecioPorSesion();

                if (estadoOriginal == EstadoPagoSesion.PENDIENTE && estadoNuevo == EstadoPagoSesion.PAGA) {
                    actualizarDeudaPaciente(cmbPaciente.getValue(), -precioSesion);
                }
                else if (estadoOriginal == EstadoPagoSesion.PAGA && estadoNuevo == EstadoPagoSesion.PENDIENTE) {
                    actualizarDeudaPaciente(cmbPaciente.getValue(), precioSesion);
                }
            }

            sesionSeleccionada.setTipoSesion(cmbTipoSesion.getValue());
            sesionSeleccionada.setPaciente(cmbPaciente.getValue());
            sesionSeleccionada.setFecha(dateFecha.getValue());
            sesionSeleccionada.setEstadoPagoSesion(estadoNuevo);
            sesionSeleccionada.setNotas(txtNotas.getText().trim());

            ServiceManager.getSesionDAO().update(sesionSeleccionada);
            mostrarMensaje("Sesión actualizada exitosamente", Alert.AlertType.INFORMATION);

            cargarSesiones();
            cargarPacientes();
            limpiarFormulario();

        } catch (Exception e) {
            mostrarMensaje("Error al actualizar sesión: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarSesion() {
        if (sesionSeleccionada == null) {
            mostrarMensaje("Seleccione una sesión para eliminar", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar esta sesión?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try {
                ServiceManager.getSesionDAO().delete(sesionSeleccionada.getId());

                cargarSesiones();
                limpiarFormulario();
            } catch (Exception e) {
                mostrarMensaje("Error al eliminar sesión: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
        cmbPaciente.setValue(null);
        dateFecha.setValue(LocalDate.now());
        cmbTipoSesion.setValue(null);
        chkPaga.setSelected(false);
        txtNotas.clear();

        tablaSesiones.getSelectionModel().clearSelection();
        sesionSeleccionada = null;
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
    private void cargarSesiones() {
        if (cmbMesFiltro.getValue() == null || cmbAnioFiltro.getValue() == null) {
            return;
        }

        try {
            int mes = cmbMesFiltro.getSelectionModel().getSelectedIndex() + 1;
            int anio = cmbAnioFiltro.getValue();

            LocalDate desde = LocalDate.of(anio, mes, 1);
            LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());

            var sesiones = ServiceManager.getSesionDAO().findByRangoFechas(desde, hasta);
            listaSesiones.clear();
            listaSesiones.addAll(sesiones);
        } catch (Exception e) {
            mostrarMensaje("Error al filtrar sesiones: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void cargarSesionEnFormulario(Sesion sesion) {
        sesionSeleccionada = sesion;
        cmbPaciente.setValue(sesion.getPaciente());
        dateFecha.setValue(sesion.getFecha());
        cmbTipoSesion.setValue(sesion.getTipoSesion());
        chkPaga.setSelected(sesion.getEstadoPagoSesion() == EstadoPagoSesion.PAGA);
        txtNotas.setText(sesion.getNotas());
    }

    private boolean validarFormulario() {
        if (cmbPaciente.getValue() == null) {
            mostrarMensaje("Seleccione un paciente", Alert.AlertType.WARNING);
            return false;
        }

        if (dateFecha.getValue() == null) {
            mostrarMensaje("Seleccione una fecha", Alert.AlertType.WARNING);
            return false;
        }

        if (cmbTipoSesion.getValue() == null) {
            mostrarMensaje("Seleccione un tipo de sesión", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Gestión de Sesiones");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}