package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.controller.login.Session;
import es.jmcenram.blockchain.controller.utils.AvisosUtil;
import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.model.documento.EstadoDocumento;
import es.jmcenram.blockchain.model.registroblockchain.EstadoBlockchain;
import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.model.usuario.Usuario;

import es.jmcenram.blockchain.repository.auditoria.AuditoriaRepository;
import es.jmcenram.blockchain.repository.documento.DocumentoRepository;
import es.jmcenram.blockchain.repository.registroblockchain.RegistroBlockchainRepository;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;

import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.documento.DocumentoService;
import es.jmcenram.blockchain.service.usuario.UsuarioService;
import es.jmcenram.blockchain.service.blockchain.BlockchainService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

public class DocumentoController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TableView<Documento> tablaDocumentos;
    @FXML
    private TableColumn<Documento, String> colNombre;
    @FXML
    private TableColumn<Documento, String> colHash;
    @FXML
    private TableColumn<Documento, String> colFecha;
    @FXML
    private TableColumn<Documento, String> colFechaRegistro;
    @FXML
    private TableColumn<Documento, String> colEstado;
    @FXML
    private TableColumn<Documento, String> colEstadoRegistro;
    @FXML
    private TableColumn<Documento, String> colRuta;
    @FXML
    private TableColumn<Documento, Void> colAcciones;
    @FXML
    private StackPane root;
    @FXML
    private VBox loadingOverlay;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label lblLoading;
    @FXML
    private MenuButton btnColumnas;
    @FXML
    private VBox panelFiltros;

    @FXML
    private TextField filtroNombre;
    @FXML
    private DatePicker filtroFecha;
    @FXML
    private ComboBox<String> filtroEstado;
    @FXML
    private ComboBox<String> filtroEstadoBlockchain;

    private List<Documento> documentosOriginales;

    private DocumentoService documentoService;
    private UsuarioService usuarioService;
    private BlockchainService blockchainService;

    private Usuario usuarioActual;
    private List<File> archivosSeleccionados;
    // Cola para el blockchain(Procesos pesados)
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, // core
            1, // max
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadPoolExecutor.AbortPolicy()
    );

    // Cola para local (Procesos ligeros)
    private final ExecutorService executorLocal = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {

        Platform.runLater(() -> {
            if (tablaDocumentos.getScene() != null) {
                Stage stage = (Stage) tablaDocumentos.getScene().getWindow();
                stage.setWidth(1200);
                stage.setHeight(700);
                stage.centerOnScreen();
            }
        });

        DocumentoRepository documentoRepo = new DocumentoRepository();
        RegistroBlockchainRepository registroRepo = new RegistroBlockchainRepository();
        AuditoriaRepository auditoriaRepo = new AuditoriaRepository();
        UsuarioRepository usuarioRepo = new UsuarioRepository();
        RolRepository rolRepo = new RolRepository();
        UsuarioRolRepository usuarioRolRepo = new UsuarioRolRepository();

        try {
            blockchainService = BlockchainService.getInstance();
        } catch (Exception e) {
            blockchainService = null;
        }

        documentoService = new DocumentoService(
                documentoRepo,
                registroRepo,
                auditoriaRepo,
                blockchainService
        );

        usuarioService = new UsuarioService(usuarioRepo, rolRepo, usuarioRolRepo);
        usuarioActual = Session.getUsuario();

        filtroEstado.setItems(FXCollections.observableArrayList(
                "BORRADOR", "VALIDADO"
        ));

        filtroEstadoBlockchain.setItems(FXCollections.observableArrayList(
                "NO REGISTRADO", "REGISTRADO", "REVOCADO", "PENDIENTE"
        ));

        tablaDocumentos.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {

                newScene.windowProperty().addListener((obsW, oldW, newW) -> {
                    if (newW != null) {

                        newW.addEventHandler(javafx.stage.WindowEvent.WINDOW_HIDDEN, e -> {
                            executor.shutdownNow();
                            executorLocal.shutdownNow();
                        });

                    }
                });

            }
        });

        /* 🔥 FIX REAL DATEPICKER */
        filtroFecha.setShowWeekNumbers(false);

        filtroFecha.setOnShown(e -> {
            javafx.scene.Node popup = filtroFecha.lookup(".date-picker-popup");

            if (popup != null) {
                Scene popupScene = popup.getScene();

                if (popupScene != null &&
                        !popupScene.getStylesheets().contains(
                                getClass().getResource("/css/dark.css").toExternalForm())) {

                    popupScene.getStylesheets().add(
                            getClass().getResource("/css/dark.css").toExternalForm()
                    );
                }
            }
        });

        filtroFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                LocalDate selectedDate = picker.getValue();
                LocalDate currentMonth = (selectedDate != null)
                        ? selectedDate
                        : LocalDate.now();

                boolean isOtherMonth = date.getMonth() != currentMonth.getMonth();
                boolean isSelected = selectedDate != null && date.equals(selectedDate);
                boolean isToday = date.equals(LocalDate.now());

                String style = "";

                // 🔥 PRIORIDAD 1 → seleccionado
                if (isSelected) {
                    style += """
                -fx-background-color: #14b8a6;
                -fx-text-fill: white;
            """;
                } else {

                    // días fuera del mes
                    if (isOtherMonth) {
                        style += """
                    -fx-text-fill: #64748b;
                    -fx-opacity: 0.5;
                """;
                    } else {
                        style += """
                    -fx-text-fill: white;
                """;
                    }

                    // hover lo dejamos al CSS
                }

                // 🔥 HOY (sin romper selección)
                if (isToday && !isSelected) {
                    style += """
                -fx-border-color: #22c55e;
                -fx-border-width: 1;
            """;
                }

                setStyle(style);
            }
        });

        Platform.runLater(() -> {

            root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {

                // si el panel no está visible → no hacer nada
                if (!panelFiltros.isVisible()) return;

                // nodo donde se hizo click
                javafx.scene.Node target = (javafx.scene.Node) e.getTarget();

                // comprobar si el click fue dentro del panel
                boolean clickDentroPanel = false;
                while (target != null) {
                    if (target == panelFiltros) {
                        clickDentroPanel = true;
                        break;
                    }
                    target = target.getParent();
                }

                // si NO está dentro → cerrar
                if (!clickDentroPanel) {
                    panelFiltros.setVisible(false);
                    panelFiltros.setManaged(false);
                }
            });

        });

        configurarTabla();
        cargarDocumentos();
        configurarSelectorColumnas();
    }

    // NAVEGACIÓN
    @FXML
    private void volver() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/main.fxml")
            );

            Parent mainView = loader.load();

            // 🔥 obtener layout global
            LayoutController layout = (LayoutController)
                    tablaDocumentos.getScene().getUserData();

            if (layout == null) {
                AvisosUtil.mostrarError("Error de navegación");
                return;
            }

            // 🔥 OBTENER STAGE DESDE EL NODO
            Stage stage = (Stage) tablaDocumentos.getScene().getWindow();

            // 🔥 ajustar tamaño
            stage.setWidth(600);
            stage.setHeight(550);
            stage.centerOnScreen();

            // 🔁 cambiar SOLO contenido
            layout.setContent(mainView);

        } catch (Exception e) {
            AvisosUtil.mostrarError("Error al volver");
        }
    }

    // FILTROS AVANZADOS
    @FXML
    private void toggleFiltros() {
        boolean visible = !panelFiltros.isVisible();
        panelFiltros.setVisible(visible);
        panelFiltros.setManaged(visible);
    }

    // ========================
    // FORMULARIO
    // ========================

    @FXML
    private void subirArchivo() {
        FileChooser fileChooser = new FileChooser();

        List<File> files = fileChooser.showOpenMultipleDialog(
                tablaDocumentos.getScene().getWindow()
        );

        if (files != null && !files.isEmpty()) {
            archivosSeleccionados = files;

            // Mostrar info en el campo
            if (files.size() == 1) {
                txtNombre.setText(files.get(0).getName());
            } else {
                txtNombre.setText(files.size() + " archivos seleccionados");
            }
        }

        System.out.println(blockchainService.obtenerTodosHashesComoString());
    }

    @FXML
    private void crearDocumento() {

        if (archivosSeleccionados == null || archivosSeleccionados.isEmpty()) {
            AvisosUtil.mostrarError("Debes seleccionar al menos un archivo");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                for (File archivo : archivosSeleccionados) {

                    Documento doc = new Documento();
                    doc.setNombre(archivo.getName());

                    documentoService.crearDocumentoCompletoConArchivo(
                            doc, archivo, usuarioActual
                    );
                }

                return null;
            }
        };

        task.setOnRunning(e -> mostrarLoading("Creando documentos..."));
        task.setOnSucceeded(e -> {
            ocultarLoading();
            AvisosUtil.mostrarInfo(
                    archivosSeleccionados.size() + " documento(s) creados"
            );

            txtNombre.clear();
            archivosSeleccionados = null;
            cargarDocumentos();
        });

        task.setOnFailed(e -> {
            ocultarLoading();
            AvisosUtil.mostrarError("Error al crear documentos");
        });

        executorLocal.submit(task);
    }

    // TABLA
    private void configurarTabla() {

        // =========================
        // 📌 VALUE FACTORIES
        // =========================

        colNombre.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNombre()));

        colFecha.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getFechaCreacion()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ));

        colHash.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getHash()));

        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(getEstadoReal(d.getValue()).name()));

        colEstadoRegistro.setCellValueFactory(d ->
                new SimpleStringProperty(getEstadoRegistroActual(d.getValue())));

        colFechaRegistro.setCellValueFactory(d ->
                new SimpleStringProperty(getFechaRegistroActual(d.getValue())));

        colRuta.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRutaArchivo()));

        // =========================
        // 🔥 COPY ON RIGHT CLICK (GENÉRICO)
        // =========================

        Callback<TableColumn<Documento, String>, TableCell<Documento, String>> cellFactoryCopiable = col -> new TableCell<>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty ? null : item);

                if (empty || item == null) {
                    setContextMenu(null);
                    return;
                }

                MenuItem copiar = new MenuItem("Copiar");

                copiar.setOnAction(e -> {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(item);
                    Clipboard.getSystemClipboard().setContent(content);
                });

                ContextMenu menu = new ContextMenu(copiar);

                menu.setOnShowing(ev -> {
                    Scene scene = menu.getScene();
                    if (scene != null && !scene.getStylesheets().contains(
                            getClass().getResource("/css/dark.css").toExternalForm())) {

                        scene.getStylesheets().add(
                                getClass().getResource("/css/dark.css").toExternalForm()
                        );
                    }
                });

                setContextMenu(menu);
            }
        };

        // aplicar a TODAS las columnas de texto
        colNombre.setCellFactory(cellFactoryCopiable);
        colFecha.setCellFactory(cellFactoryCopiable);
        colHash.setCellFactory(cellFactoryCopiable);
        colEstado.setCellFactory(cellFactoryCopiable);
        colEstadoRegistro.setCellFactory(cellFactoryCopiable);
        colFechaRegistro.setCellFactory(cellFactoryCopiable);
        colRuta.setCellFactory(cellFactoryCopiable);

        // =========================
        // ⚙️ COLUMNA ACCIONES
        // =========================

        colAcciones.setCellFactory(param -> new TableCell<>() {

            private final Button btnVer = new Button("Ver");
            private final Button btnDescargar = new Button("Descargar");
            private final Button btnValidar = new Button("Validar");
            private final Button btnBlockchain = new Button("Registrar");
            private final Button btnRevocar = new Button("Revocar");
            private final Button btnEliminar = new Button("Eliminar");

            private final HBox pane = new HBox(5,
                    btnVer, btnDescargar, btnValidar,
                    btnBlockchain, btnRevocar, btnEliminar);

            {
                btnVer.setOnAction(e -> verDocumento(getDoc()));
                btnDescargar.setOnAction(e -> descargarDocumento(getDoc()));
                btnValidar.setOnAction(e -> validarDocumento(getDoc()));
                btnBlockchain.setOnAction(e -> registrarBlockchain(getDoc()));
                btnRevocar.setOnAction(e -> revocarDocumento(getDoc()));
                btnEliminar.setOnAction(e -> eliminarDocumento(getDoc()));
            }

            private Documento getDoc() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) return null;
                return getTableView().getItems().get(index);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                setText(null);
                setGraphic(null);

                if (empty || getIndex() >= getTableView().getItems().size()) return;

                Documento doc = getDoc();
                if (doc == null) return;

                if (doc.isProcesando()) {
                    reset();
                    ocultar(btnBlockchain);
                    ocultar(btnRevocar);
                    ocultar(btnValidar);
                    ocultar(btnEliminar);
                    setGraphic(pane);
                    return;
                }

                EstadoDocumento estado = getEstadoReal(doc);
                String estadoRegistro = getEstadoRegistroActual(doc);

                reset();

                // BORRADOR
                if (estado == EstadoDocumento.BORRADOR) {
                    ocultar(btnBlockchain);
                    ocultar(btnRevocar);
                    mostrar(btnEliminar);
                    mostrar(btnValidar);
                }

                // VALIDADO
                else if (estado == EstadoDocumento.VALIDADO) {

                    ocultar(btnValidar);
                    ocultar(btnEliminar);

                    if ("PENDIENTE".equalsIgnoreCase(estadoRegistro)) {
                        ocultar(btnBlockchain);
                        ocultar(btnRevocar);
                    }
                    else if ("REGISTRADO".equalsIgnoreCase(estadoRegistro)) {
                        ocultar(btnBlockchain);
                        mostrar(btnRevocar);
                    }
                    else if ("REVOCADO".equalsIgnoreCase(estadoRegistro)) {
                        ocultar(btnBlockchain);
                        ocultar(btnRevocar);
                    }
                    else {
                        mostrar(btnBlockchain);
                        ocultar(btnRevocar);
                    }
                }

                setGraphic(pane);
            }

            private void reset() {
                for (Button b : List.of(btnVer, btnDescargar, btnValidar, btnBlockchain, btnRevocar, btnEliminar)) {
                    b.setVisible(true);
                    b.setManaged(true);
                }
            }

            private void ocultar(Button b) {
                b.setVisible(false);
                b.setManaged(false);
            }

            private void mostrar(Button b) {
                b.setVisible(true);
                b.setManaged(true);
            }
        });
    }

    private void configurarSelectorColumnas() {

        crearToggle("Nombre", colNombre, true);
        crearToggle("Fecha", colFecha, true);
        crearToggle("Estado", colEstado, true);
        crearToggle("Estado Blockchain", colEstadoRegistro, true);

        crearToggle("Fecha Blockchain", colFechaRegistro, false);
        crearToggle("Hash", colHash, false);
        crearToggle("Ruta", colRuta, false);
    }

    private void crearToggle(String texto, TableColumn<?, ?> columna, boolean visible) {

        columna.setVisible(visible);

        CheckMenuItem item = new CheckMenuItem(texto);
        item.setSelected(visible);

        item.selectedProperty().addListener((obs, oldVal, newVal) -> {
            columna.setVisible(newVal);
        });

        btnColumnas.getItems().add(item);
    }

    // LÓGICA
    private EstadoDocumento getEstadoReal(Documento doc) {
        if (doc.getHash() == null || doc.getHash().isBlank()) {
            return EstadoDocumento.BORRADOR;
        }
        return doc.getEstado();
    }

    private void registrarBlockchain(Documento doc) {

        boolean ok = AvisosUtil.confirmarAccion(
                "Confirmar registro",
                "¿Registrar el documento en blockchain?" + doc.getNombre()
        );

        if (!ok) return;
        if (doc.isProcesando()) return;

        if (executor.getQueue().size() >= 100) {
            AvisosUtil.mostrarError("Cola llena");
            return;
        }

        int activos = executor.getActiveCount();
        int enCola = executor.getQueue().size();

        doc.setProcesando(true);
        actualizarFila(doc);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                documentoService.registrarEnBlockchain(
                        doc,
                        usuarioActual,

                        txHash -> Platform.runLater(() -> {

                            doc.setProcesando(false);

                            // 🔥 recarga real desde BD
                            cargarDocumentos();

                            AvisosUtil.mostrarInfo("Registrado correctamente");

                        }),

                        error -> Platform.runLater(() -> {

                            doc.setProcesando(false);

                            actualizarFila(doc);

                            AvisosUtil.mostrarError("Error en blockchain");

                        })
                );

                return null;
            }
        };

        task.setOnFailed(e -> {

            doc.setProcesando(false);
            actualizarFila(doc);

            Throwable ex = task.getException();
            AvisosUtil.mostrarError(
                    ex != null ? "Error blockchain: " + ex.getMessage() : "Error blockchain"
            );
        });

        if (activos > 0 || enCola > 0) {
            AvisosUtil.mostrarInfo("Hay procesos en ejecución, este documento se ha añadido a la cola");
        }

        executor.submit(task);
    }

    // =========================
    // REVOCAR (FIX IGUAL)
    // =========================
    private void revocarDocumento(Documento doc) {

        boolean ok = AvisosUtil.confirmarAccion(
                "Confirmar revocación",
                "¿Revocar el documento en blockchain?\n\n" + doc.getNombre()
        );

        if (!ok) return;
        if (doc.isProcesando()) return;

        if (executor.getQueue().size() >= 100) {
            AvisosUtil.mostrarError("Cola llena");
            return;
        }

        int activos = executor.getActiveCount();
        int enCola = executor.getQueue().size();

        doc.setProcesando(true);
        actualizarFila(doc);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                documentoService.revocarDocumento(
                        doc,
                        usuarioActual,

                        txHash -> javafx.application.Platform.runLater(() -> {

                            doc.setProcesando(false);

                            // 🔥 recargar desde BD → estado real
                            cargarDocumentos();

                            AvisosUtil.mostrarInfo("Revocado correctamente");

                        }),

                        error -> javafx.application.Platform.runLater(() -> {

                            doc.setProcesando(false);

                            actualizarFila(doc);

                            AvisosUtil.mostrarError("Error revocando");

                        })
                );

                return null;
            }
        };

        task.setOnFailed(e -> {

            doc.setProcesando(false);
            actualizarFila(doc);

            Throwable ex = task.getException();
            AvisosUtil.mostrarError(
                    ex != null ? "Error blockchain: " + ex.getMessage() : "Error blockchain"
            );
        });

        if (activos > 0 || enCola > 0) {
            AvisosUtil.mostrarInfo("Hay procesos en ejecución, este documento se ha añadido a la cola");
        }

        executor.submit(task);
    }

    private String getEstadoRegistroActual(Documento doc) {

        try {
            // 🔥 1. Estado en memoria (prioridad visual)
            if (doc.isProcesando()) {
                return EstadoBlockchain.PENDIENTE.name();
            }

            // 🔥 2. Sin registros
            if (doc.getRegistros() == null || doc.getRegistros().isEmpty()) {
                return "NO REGISTRADO";
            }

            // 🔥 3. Seguridad por si blockchainService es null
            if (blockchainService == null) {
                return "ERROR";
            }

            String contratoActual = blockchainService.getContractAddress();

            return doc.getRegistros().stream()
                    .filter(r -> contratoActual.equalsIgnoreCase(r.getDireccionContrato()))
                    .max(Comparator.comparing(RegistroBlockchain::getFechaCreacion))
                    .map(r -> {
                        EstadoBlockchain estado = r.getEstado();
                        return estado != null ? estado.name() : "ERROR";
                    })
                    .orElse("SIN REGISTRO");

        } catch (Exception e) {
            return "ERROR";
        }
    }

    private String getFechaRegistroActual(Documento doc) {

        try {
            if (doc.getRegistros() == null || doc.getRegistros().isEmpty()) {
                return "SIN FECHA";
            }

            String contratoActual = blockchainService.getContractAddress();

            return doc.getRegistros().stream()
                    .filter(r -> contratoActual.equalsIgnoreCase(r.getDireccionContrato()))
                    .max(Comparator.comparing(RegistroBlockchain::getFechaCreacion))
                    .map(r -> r.getFechaCreacion()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .orElse("NO EN ESTE CONTRATO");

        } catch (Exception e) {
            return "ERROR";
        }
    }

    private void validarDocumento(Documento doc) {

        if (doc.isProcesando()) return;

        doc.setProcesando(true);
        actualizarFila(doc);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                documentoService.validarDocumento(doc, usuarioActual);
                return null;
            }
        };

        task.setOnSucceeded(e -> {

            doc.setProcesando(false);

            // 🔥 ACTUALIZAR ESTADO LOCAL
            doc.setEstado(EstadoDocumento.VALIDADO);

            actualizarFila(doc);

            AvisosUtil.mostrarInfo("Documento validado");
        });

        task.setOnFailed(e -> {

            doc.setProcesando(false);

            actualizarFila(doc);

            Throwable ex = task.getException();
            AvisosUtil.mostrarError(
                    ex != null ? "Error validando: " + ex.getMessage() : "Error validando"
            );
        });

        executorLocal.submit(task);
    }

    private void mostrarLoading(String mensaje) {
        lblLoading.setText(mensaje);
        loadingOverlay.setVisible(true);
    }

    private void ocultarLoading() {
        loadingOverlay.setVisible(false);
    }


    private void actualizarFila(Documento doc) {
        int index = tablaDocumentos.getItems().indexOf(doc);
        if (index >= 0) {
            tablaDocumentos.getItems().set(index, doc);
        }
    }

    private void actualizarEstadoLocal(Documento doc, EstadoBlockchain nuevoEstado) {

        if (doc.getRegistros() == null || doc.getRegistros().isEmpty()) return;

        doc.getRegistros().stream()
                .max(Comparator.comparing(RegistroBlockchain::getFechaCreacion))
                .ifPresent(r -> r.setEstado(nuevoEstado));
    }

    private void eliminarDocumento(Documento doc) {
        try {

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Eliminar documento");
            confirm.setHeaderText("Confirmar eliminación");
            confirm.setContentText("¿Eliminar " + doc.getNombre() + "?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            documentoService.eliminar(doc.getId());

            AvisosUtil.mostrarInfo("Documento eliminado");
            cargarDocumentos();

        } catch (Exception e) {
            AvisosUtil.mostrarError("Error eliminando");
        }
    }

    private void cargarDocumentos() {

        documentosOriginales = documentoService.obtenerTodosConRegistros();

        tablaDocumentos.setItems(
                FXCollections.observableArrayList(documentosOriginales)
        );
    }

    private void verDocumento(Documento doc) {
        try {

            if (doc.getContenido() == null) {
                AvisosUtil.mostrarError("El documento no tiene contenido");
                return;
            }

            // Crear archivo temporal
            File tempFile = File.createTempFile("doc_", "_" + doc.getNombre());

            // Escribir contenido
            Files.write(tempFile.toPath(), doc.getContenido());

            // Abrir con app del sistema
            Desktop.getDesktop().open(tempFile);

            // Opcional: borrar al cerrar JVM
            tempFile.deleteOnExit();

        } catch (Exception e) {
            AvisosUtil.mostrarError("Error abriendo archivo");
        }
    }

    private void descargarDocumento(Documento doc) {
        try {

            if (doc.getContenido() == null) {
                AvisosUtil.mostrarError("El documento no tiene contenido");
                return;
            }

            FileChooser fc = new FileChooser();
            fc.setInitialFileName(doc.getNombre());

            File destino = fc.showSaveDialog(tablaDocumentos.getScene().getWindow());

            if (destino != null) {
                Files.write(destino.toPath(), doc.getContenido());
            }

        } catch (Exception e) {
            AvisosUtil.mostrarError("Error descargando");
        }
    }


    // FILTROS AVANZADOS
    @FXML
    private void aplicarFiltros() {

        String nombre = filtroNombre.getText();
        LocalDate fecha = filtroFecha.getValue(); // 🔥 CAMBIO CLAVE
        String estado = filtroEstado.getValue();
        String estadoBlockchain = filtroEstadoBlockchain.getValue();

        List<Documento> filtrados = documentosOriginales.stream()
                .filter(doc -> {

                    // 🔎 NOMBRE
                    if (nombre != null && !nombre.isBlank() &&
                            !doc.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                        return false;

                    // 📅 FECHA (comparación real, no string cutre)
                    if (fecha != null) {
                        LocalDate fechaDoc = doc.getFechaCreacion().toLocalDate();
                        if (!fechaDoc.equals(fecha))
                            return false;
                    }

                    // 📊 ESTADO
                    if (estado != null &&
                            !getEstadoReal(doc).name().equalsIgnoreCase(estado))
                        return false;

                    // ⛓️ BLOCKCHAIN
                    if (estadoBlockchain != null) {
                        String est = getEstadoRegistroActual(doc);
                        if (!est.equalsIgnoreCase(estadoBlockchain))
                            return false;
                    }

                    return true;
                })
                .toList();

        tablaDocumentos.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    private void limpiarFiltros() {

        filtroNombre.clear();

        // 🔥 IMPORTANTE (DatePicker)
        filtroFecha.setValue(null);

        filtroEstado.setValue(null);
        filtroEstadoBlockchain.setValue(null);

        tablaDocumentos.setItems(FXCollections.observableArrayList(documentosOriginales));
    }

}