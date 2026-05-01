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
import es.jmcenram.blockchain.util.Messages;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Controlador encargado de la gestión de documentos dentro de la aplicación.
 * <p>
 * Permite al usuario:
 * - Subir y crear documentos
 * - Visualizar, descargar y eliminar documentos
 * - Validar documentos
 * - Registrar y revocar documentos en blockchain (solo administradores)
 * - Aplicar filtros avanzados sobre la tabla
 * <p>
 * Integra lógica de UI (JavaFX) con la capa de servicios:
 * {@link DocumentoService}, {@link UsuarioService} y {@link BlockchainService}.
 * <p>
 * Gestiona además ejecución asíncrona mediante colas para evitar bloqueos de interfaz.
 * <p>
 * Incluye control de permisos basado en el usuario autenticado
 * obtenido desde {@link Session}.
 *
 * @author Jcena
 * @version 1.0
 */
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
    private boolean conectado;

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

    /**
     * Inicializa el controlador.
     * <p>
     * Configura:
     * - Servicios y repositorios
     * - Usuario autenticado
     * - Filtros y componentes UI
     * - Comportamiento del DatePicker
     * - Eventos globales (cierre de ventana, click fuera de filtros)
     * <p>
     * Finalmente carga los documentos y configura la tabla.
     */
    @FXML
    public void initialize() {

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
            e.printStackTrace();
        }
        comprobarConexionBlockchain();

        documentoService = new DocumentoService(
                documentoRepo,
                registroRepo,
                auditoriaRepo,
                blockchainService
        );

        usuarioService = new UsuarioService(usuarioRepo, rolRepo, usuarioRolRepo);
        usuarioActual = Session.getUsuario();

        filtroEstado.setItems(FXCollections.observableArrayList(
                Messages.getString("draft"), Messages.getString("validated")
        ));

        filtroEstadoBlockchain.setItems(FXCollections.observableArrayList(
                Messages.getString("not_registered"), Messages.getString("registered"), Messages.getString("revoked"), Messages.getString("pending")
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
            /**
             * Refresca una celda personalizada de la tabla o del calendario segun el valor recibido por JavaFX.
             *
             * El metodo limpia estados visuales cuando la celda queda vacia y aplica el formato o acciones solo cuando hay datos reales.
             *
             * @param date dia que JavaFX esta pintando dentro del calendario
             * @param empty indica si la celda no tiene contenido asociado
             */
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

            root.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

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

    /**
     * Navega de vuelta a la vista principal de la aplicación.
     * <p>
     * Carga el layout principal y sustituye el contenido actual.
     * Utiliza el {@link LayoutController} almacenado en la escena.
     * <p>
     * En caso de error, muestra un mensaje al usuario.
     */
    @FXML
    private void volver() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/main.fxml")
            );
            loader.setResources(Messages.getBundle());

            Parent mainView = loader.load();

            LayoutController layout = (LayoutController)
                    tablaDocumentos.getScene().getUserData();

            if (layout == null) {
                AvisosUtil.mostrarError(Messages.getString("navigation_error"));
                return;
            }

            Stage stage = (Stage) tablaDocumentos.getScene().getWindow();

            layout.setContent(mainView);

        } catch (Exception e) {
            AvisosUtil.mostrarError(Messages.getString("navigation_error"));
        }
    }

    /**
     * Muestra u oculta el panel de filtros avanzados.
     * <p>
     * Alterna la visibilidad y gestión del layout del panel.
     */
    @FXML
    private void toggleFiltros() {
        boolean visible = !panelFiltros.isVisible();
        panelFiltros.setVisible(visible);
        panelFiltros.setManaged(visible);
    }

    /**
     * Abre un selector de archivos para permitir la selección múltiple.
     * <p>
     * Guarda los archivos seleccionados en memoria y actualiza el campo
     * de texto con el nombre del archivo o número de archivos seleccionados.
     */
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
                txtNombre.setText(files.size() + " " + Messages.getString("files_selected"));
            }
        }

    }

    /**
     * Crea uno o varios documentos a partir de los archivos seleccionados.
     * <p>
     * Ejecuta la operación en segundo plano para evitar bloquear la UI.
     * Tras completarse:
     * - Limpia el formulario
     * - Recarga la tabla
     * - Muestra mensaje informativo
     */
    @FXML
    private void crearDocumento() {

        if (archivosSeleccionados == null || archivosSeleccionados.isEmpty()) {
            AvisosUtil.mostrarError(Messages.getString("select_at_least_one_file"));
            return;
        }

        Task<Void> task = new Task<>() {
            /**
             * Ejecuta trabajo pesado de documentos fuera del hilo de JavaFX.
             *
             * Se usa dentro de tareas asincronas para crear, validar, registrar o revocar sin congelar la interfaz mientras termina la operacion.
             *
             * @return resultado calculado a partir de la operacion documentada
             */
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

        task.setOnRunning(e -> mostrarLoading(Messages.getString("creating_documents")));
        task.setOnSucceeded(e -> {
            ocultarLoading();
            AvisosUtil.mostrarInfo(
                    archivosSeleccionados.size() + " " + Messages.getString("documents_created")
            );

            txtNombre.clear();
            archivosSeleccionados = null;
            cargarDocumentos();
        });

        task.setOnFailed(e -> {
            ocultarLoading();
            AvisosUtil.mostrarError(Messages.getString("error_creating_documents"));
        });

        executorLocal.submit(task);
    }

    /**
     * Configura la tabla de documentos.
     * <p>
     * Define:
     * - ValueFactories para cada columna
     * - CellFactories con funcionalidad de copiado
     * - Columna de acciones con botones dinámicos según estado
     * <p>
     * También adapta la UI según permisos y estado del documento.
     */
    private void configurarTabla() {

        // =========================
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
                new SimpleStringProperty(getEstadoRealI18n(d.getValue())));

        colEstadoRegistro.setCellValueFactory(d ->
                new SimpleStringProperty(getEstadoRegistroActualI18n(d.getValue())));

        colFechaRegistro.setCellValueFactory(d ->
                new SimpleStringProperty(getFechaRegistroActual(d.getValue())));

        colRuta.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRutaArchivo()));

        // =========================
        // =========================

        Callback<TableColumn<Documento, String>, TableCell<Documento, String>> cellFactoryCopiable = col -> new TableCell<>() {

            /**
             * Refresca una celda personalizada de la tabla o del calendario segun el valor recibido por JavaFX.
             *
             * El metodo limpia estados visuales cuando la celda queda vacia y aplica el formato o acciones solo cuando hay datos reales.
             *
             * @param item valor que JavaFX entrega a la celda durante su refresco
             * @param empty indica si la celda no tiene contenido asociado
             */
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty ? null : item);

                if (empty || item == null) {
                    setContextMenu(null);
                    return;
                }

                MenuItem copiar = new MenuItem(Messages.getString("copy"));

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
        // COLUMNA ACCIONES (FIX UI)
        // =========================
        colAcciones.setCellFactory(param -> new TableCell<>() {

            private final Button btnVer = new Button(Messages.getString("view"));
            private final Button btnDescargar = new Button(Messages.getString("download"));
            private final Button btnValidar = new Button(Messages.getString("validate"));
            private final Button btnBlockchain = new Button(Messages.getString("register"));
            private final Button btnRevocar = new Button(Messages.getString("revoke"));
            private final Button btnEliminar = new Button(Messages.getString("delete"));


            private final HBox pane = new HBox(6,
                    btnVer, btnDescargar, btnValidar,
                    btnBlockchain, btnRevocar, btnEliminar);

            {
                btnVer.getStyleClass().add("btn-secondary");
                btnDescargar.getStyleClass().add("btn-secondary");

                btnValidar.getStyleClass().add("btn-primary");
                btnBlockchain.getStyleClass().add("btn-primary");

                btnRevocar.getStyleClass().add("btn-revoke");
                btnEliminar.getStyleClass().add("btn-revoke");

                // tamaño uniforme
                for (Button b : List.of(btnVer, btnDescargar, btnValidar, btnBlockchain, btnRevocar, btnEliminar)) {
                    b.setPrefHeight(28);
                    b.setStyle("-fx-font-size: 11px;");
                }

                btnVer.setOnAction(e -> verDocumento(getDoc()));
                btnDescargar.setOnAction(e -> descargarDocumento(getDoc()));
                btnValidar.setOnAction(e -> validarDocumento(getDoc()));
                btnBlockchain.setOnAction(e -> registrarBlockchain(getDoc()));
                btnRevocar.setOnAction(e -> revocarDocumento(getDoc()));
                btnEliminar.setOnAction(e -> eliminarDocumento(getDoc()));
            }

            /**
             * Obtiene el documento asociado a la fila actual de la tabla.
             *
             * Devuelve null cuando la celda esta vacia para que los botones de accion no operen sobre filas inexistentes.
             *
             * @return resultado calculado a partir de la operacion documentada
             */
            private Documento getDoc() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) return null;
                return getTableView().getItems().get(index);
            }

            /**
             * Refresca una celda personalizada de la tabla o del calendario segun el valor recibido por JavaFX.
             *
             * El metodo limpia estados visuales cuando la celda queda vacia y aplica el formato o acciones solo cuando hay datos reales.
             *
             * @param item valor que JavaFX entrega a la celda durante su refresco
             * @param empty indica si la celda no tiene contenido asociado
             */
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
                boolean admin = esAdmin();

                reset();

                switch (estado) {

                    case BORRADOR -> {
                        ocultar(btnBlockchain);
                        ocultar(btnRevocar);
                        mostrar(btnEliminar);
                        if (admin) {
                            mostrar(btnValidar);
                        } else {
                            ocultar(btnValidar);
                        }
                    }

                    case VALIDADO -> {

                        ocultar(btnValidar);
                        ocultar(btnEliminar);

                        if (!admin) {
                            ocultar(btnBlockchain);
                            ocultar(btnRevocar);
                            break;
                        }

                        EstadoBlockchain estadoRegistroEnum = getEstadoRegistroActualEnum(doc);

                        if (estadoRegistroEnum == null) {
                            mostrar(btnBlockchain);
                            ocultar(btnRevocar);
                            break;
                        }


                        switch (estadoRegistroEnum) {

                            case PENDIENTE, REVOCADO -> {
                                ocultar(btnBlockchain);
                                ocultar(btnRevocar);
                            }

                            case REGISTRADO -> {
                                ocultar(btnBlockchain);
                                mostrar(btnRevocar);
                            }

                            default -> {
                                mostrar(btnBlockchain);
                                ocultar(btnRevocar);
                            }
                        }
                    }
                }
                if (!conectado) {
                    ocultar(btnBlockchain);
                    ocultar(btnRevocar);
                }

                setGraphic(pane);
            }

            /**
             * Restaura la columna de acciones a un estado visible y consistente antes de aplicar reglas por documento.
             *
             * Cada fila puede mostrar botones distintos; reiniciar evita que el reciclado de celdas de JavaFX arrastre visibilidad de otra fila.
             */
            private void reset() {
                for (Button b : List.of(btnVer, btnDescargar, btnValidar, btnBlockchain, btnRevocar, btnEliminar)) {
                    b.setVisible(true);
                    b.setManaged(true);
                }
            }

            /**
             * Oculta un boton y lo saca del calculo de layout.
             *
             * Usar visible y managed juntos evita huecos en la fila cuando una accion no aplica al estado del documento.
             *
             * @param b boton cuya visibilidad y gestion de layout se modifican juntas
             */
            private void ocultar(Button b) {
                b.setVisible(false);
                b.setManaged(false);
            }

            /**
             * Muestra un boton y vuelve a incluirlo en el calculo de layout.
             *
             * Se usa despues de aplicar permisos y estados para que la fila solo ofrezca acciones validas.
             *
             * @param b boton cuya visibilidad y gestion de layout se modifican juntas
             */
            private void mostrar(Button b) {
                b.setVisible(true);
                b.setManaged(true);
            }
        });
    }

    /**
     * Inicializa el selector de visibilidad de columnas.
     * <p>
     * Permite al usuario mostrar u ocultar columnas dinámicamente
     * mediante un menú de selección.
     */
    private void configurarSelectorColumnas() {

        crearToggle(Messages.getString("column_name"), colNombre, true);
        crearToggle(Messages.getString("column_date"), colFecha, true);
        crearToggle(Messages.getString("column_status"), colEstado, true);
        crearToggle(Messages.getString("column_blockchain_status"), colEstadoRegistro, true);

        crearToggle(Messages.getString("column_blockchain_date"), colFechaRegistro, false);
        crearToggle(Messages.getString("column_hash"), colHash, false);
        crearToggle(Messages.getString("column_path"), colRuta, false);
    }

    /**
     * Crea un elemento de menú para mostrar u ocultar una columna.
     *
     * @param texto   texto mostrado en el menú
     * @param columna columna asociada
     * @param visible estado inicial de visibilidad
     */
    private void crearToggle(String texto, TableColumn<?, ?> columna, boolean visible) {

        columna.setVisible(visible);

        CheckMenuItem item = new CheckMenuItem(texto);
        item.setSelected(visible);

        item.selectedProperty().addListener((obs, oldVal, newVal) -> {
            columna.setVisible(newVal);
        });

        btnColumnas.getItems().add(item);
    }

    /**
     * Obtiene el estado real del documento.
     * <p>
     * Si el documento no tiene hash, se considera BORRADOR.
     *
     * @param doc documento a evaluar
     * @return estado del documento
     */
    private EstadoDocumento getEstadoReal(Documento doc) {
        if (doc.getHash() == null || doc.getHash().isBlank()) {
            return EstadoDocumento.BORRADOR;
        }
        return doc.getEstado();
    }

    /**
     * Registra un documento en la blockchain.
     * <p>
     * Solo permitido para usuarios con rol ADMIN o MASTER.
     * La operación se ejecuta de forma asíncrona en una cola dedicada.
     * <p>
     * Controla:
     * - Confirmación del usuario
     * - Estado de procesamiento del documento
     * - Límite de cola de tareas
     *
     * @param doc documento a registrar
     */
    private void registrarBlockchain(Documento doc) {

        if (!esAdmin()) {
            AvisosUtil.mostrarError(Messages.getString("no_permission"));
            return;
        }

        if (esAdminEntidadInactiva()) {
            return;
        }

        boolean ok = AvisosUtil.confirmarAccion(
                Messages.getString("confirm_register"),
                Messages.getString("register_document_blockchain") + " " + doc.getNombre()
        );

        if (!ok) return;
        if (doc.isProcesando()) return;

        if (executor.getQueue().size() >= 100) {
            AvisosUtil.mostrarError(Messages.getString("queue_full"));
            return;
        }

        doc.setProcesando(true);
        actualizarFila(doc);

        Task<Void> task = new Task<>() {
            /**
             * Ejecuta trabajo pesado de documentos fuera del hilo de JavaFX.
             *
             * Se usa dentro de tareas asincronas para crear, validar, registrar o revocar sin congelar la interfaz mientras termina la operacion.
             *
             * @return resultado calculado a partir de la operacion documentada
             */
            @Override
            protected Void call() {

                documentoService.registrarEnBlockchain(
                        doc,
                        usuarioActual,

                        txHash -> Platform.runLater(() -> {
                            doc.setProcesando(false);
                            cargarDocumentos();
                            AvisosUtil.mostrarInfo(Messages.getString("registered_successfully"));
                        }),

                        error -> Platform.runLater(() -> {
                            doc.setProcesando(false);
                            actualizarFila(doc);
                            AvisosUtil.mostrarError(Messages.getString("blockchain_error"));
                        })
                );

                return null;
            }
        };

        executor.submit(task);
    }

    /**
     * Comprueba si el usuario actual es administrador y su entidad emisora está inactiva.
     * <p>
     * En caso de cumplirse ambas condiciones:
     * - Muestra un mensaje de error al usuario
     * - Bloquea la operación que requiera entidad activa
     * <p>
     * Se utiliza como validación previa en operaciones sensibles como:
     * - Registro en blockchain
     * - Revocación de documentos
     *
     * @return true si el usuario es administrador y su entidad está inactiva,
     * false en caso contrario
     */
    private boolean esAdminEntidadInactiva() {
        if (esAdmin() && !usuarioActual.getEntidadEmisora().getActivo()) {
            AvisosUtil.mostrarError(Messages.getString("innactive_entity_document"));
            return true;
        }
        return false;
    }

    /**
     * Revoca un documento previamente registrado en blockchain.
     * <p>
     * Solo permitido para usuarios con rol ADMIN o MASTER.
     * Se ejecuta de forma asíncrona.
     *
     * @param doc documento a revocar
     */
    private void revocarDocumento(Documento doc) {

        if (!esAdmin()) {
            AvisosUtil.mostrarError(Messages.getString("no_permission"));
            return;
        }

        if (esAdminEntidadInactiva()) {
            return;
        }

        String password = AvisosUtil.pedirPassword(
                Messages.getString("confirm_revocation"),
                Messages.getString("enter_password_to_continue")
        );

        if (password == null || password.isBlank()) {
            return;
        }

        if (!BCrypt.checkpw(password, usuarioActual.getPassword())) {
            AvisosUtil.mostrarError(Messages.getString("invalid_password"));
            return;
        }

        if (doc.isProcesando()) return;

        if (executor.getQueue().size() >= 100) {
            AvisosUtil.mostrarError(Messages.getString("queue_full"));
            return;
        }

        doc.setProcesando(true);
        actualizarFila(doc);

        Task<Void> task = new Task<>() {
            /**
             * Ejecuta trabajo pesado de documentos fuera del hilo de JavaFX.
             *
             * Se usa dentro de tareas asincronas para crear, validar, registrar o revocar sin congelar la interfaz mientras termina la operacion.
             *
             * @return resultado calculado a partir de la operacion documentada
             */
            @Override
            protected Void call() {

                documentoService.revocarDocumento(
                        doc,
                        usuarioActual,

                        txHash -> Platform.runLater(() -> {
                            doc.setProcesando(false);
                            cargarDocumentos();
                            AvisosUtil.mostrarInfo(Messages.getString("revoked_successfully"));
                        }),

                        error -> Platform.runLater(() -> {
                            doc.setProcesando(false);
                            actualizarFila(doc);
                            AvisosUtil.mostrarError(Messages.getString("error_revoking"));
                        })
                );

                return null;
            }
        };

        executor.submit(task);
    }

    /**
     * Obtiene el estado actual del documento en blockchain.
     * <p>
     * Prioridad:
     * 1. Estado en memoria (procesando → PENDIENTE)
     * 2. Último registro en el contrato actual
     * 3. Valores por defecto si no hay datos
     *
     * @param doc documento a evaluar
     * @return estado en formato String
     */
    private String getEstadoRegistroActual(Documento doc) {

        try {
            if (doc.isProcesando()) {
                return EstadoBlockchain.PENDIENTE.name();
            }

            if (doc.getRegistros() == null || doc.getRegistros().isEmpty()) {
                return "-";
            }

            if (blockchainService == null) {
                return "-";
            }

            String contratoActual = blockchainService.getContractAddress();

            return doc.getRegistros().stream()
                    .filter(r -> contratoActual.equalsIgnoreCase(r.getDireccionContrato()))
                    .max(Comparator.comparing(RegistroBlockchain::getFechaCreacion))
                    .map(r -> {
                        EstadoBlockchain estado = r.getEstado();
                        return estado != null ? estado.name() : "-";
                    })
                    .orElse("-");

        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * Obtiene la fecha del último registro en blockchain del documento.
     * <p>
     * Filtra por contrato actual y devuelve la fecha más reciente.
     *
     * @param doc documento a evaluar
     * @return fecha formateada o mensaje alternativo si no existe
     */
    private String getFechaRegistroActual(Documento doc) {

        try {
            if (doc.getRegistros() == null || doc.getRegistros().isEmpty()) {
                return "-";
            }

            String contratoActual = blockchainService.getContractAddress();

            return doc.getRegistros().stream()
                    .filter(r -> contratoActual.equalsIgnoreCase(r.getDireccionContrato()))
                    .max(Comparator.comparing(RegistroBlockchain::getFechaCreacion))
                    .map(r -> r.getFechaCreacion()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .orElse("-");

        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * Marca un documento como VALIDADO.
     * <p>
     * La operación se ejecuta en segundo plano.
     * Actualiza el estado local del documento tras completarse.
     *
     * @param doc documento a validar
     */
    private void validarDocumento(Documento doc) {

        if (doc.isProcesando()) return;

        doc.setProcesando(true);
        actualizarFila(doc);

        Task<Void> task = new Task<>() {
            /**
             * Ejecuta trabajo pesado de documentos fuera del hilo de JavaFX.
             *
             * Se usa dentro de tareas asincronas para crear, validar, registrar o revocar sin congelar la interfaz mientras termina la operacion.
             *
             * @return resultado calculado a partir de la operacion documentada
             */
            @Override
            protected Void call() {
                documentoService.validarDocumento(doc, usuarioActual);
                return null;
            }
        };

        task.setOnSucceeded(e -> {

            doc.setProcesando(false);

            doc.setEstado(EstadoDocumento.VALIDADO);

            actualizarFila(doc);

            AvisosUtil.mostrarInfo(Messages.getString("document_validated"));
        });

        task.setOnFailed(e -> {

            doc.setProcesando(false);

            actualizarFila(doc);

            Throwable ex = task.getException();
            AvisosUtil.mostrarError(
                    ex != null ? Messages.getString("error_validating") + ": " + ex.getMessage() : Messages.getString("error_validating")
            );
        });

        executorLocal.submit(task);
    }

    /**
     * Muestra el overlay de carga con un mensaje.
     *
     * @param mensaje texto a mostrar durante la operación
     */
    private void mostrarLoading(String mensaje) {
        lblLoading.setText(mensaje);
        loadingOverlay.setVisible(true);
    }

    /**
     * Oculta el overlay de carga.
     */
    private void ocultarLoading() {
        loadingOverlay.setVisible(false);
    }


    /**
     * Actualiza visualmente una fila de la tabla.
     * <p>
     * Reemplaza el objeto en la lista para forzar el refresco.
     *
     * @param doc documento a actualizar
     */
    private void actualizarFila(Documento doc) {
        int index = tablaDocumentos.getItems().indexOf(doc);
        if (index >= 0) {
            tablaDocumentos.getItems().set(index, doc);
        }
    }

    /**
     * Actualiza el estado del último registro blockchain en memoria.
     * <p>
     * No persiste en base de datos, solo afecta al estado visual.
     *
     * @param doc         documento a actualizar
     * @param nuevoEstado nuevo estado a aplicar
     */
    private void actualizarEstadoLocal(Documento doc, EstadoBlockchain nuevoEstado) {

        if (doc.getRegistros() == null || doc.getRegistros().isEmpty()) return;

        doc.getRegistros().stream()
                .max(Comparator.comparing(RegistroBlockchain::getFechaCreacion))
                .ifPresent(r -> r.setEstado(nuevoEstado));
    }

    /**
     * Elimina un documento previa confirmación del usuario.
     * <p>
     * Tras eliminar:
     * - Muestra mensaje de éxito
     * - Recarga la tabla
     *
     * @param doc documento a eliminar
     */
    private void eliminarDocumento(Documento doc) {
        try {

            boolean confirmado = AvisosUtil.confirmarAccion(
                    Messages.getString("delete_document"),
                    Messages.getString("delete_question") + " " + doc.getNombre() + "?"
            );

            if (!confirmado) return;

            documentoService.eliminar(doc.getId());

            AvisosUtil.mostrarInfo(Messages.getString("document_deleted"));
            cargarDocumentos();

        } catch (Exception e) {
            AvisosUtil.mostrarError(Messages.getString("error_deleting"));
        }
    }

    /**
     * Carga todos los documentos junto con sus registros blockchain.
     * <p>
     * Actualiza la tabla con los datos obtenidos.
     */
    private void cargarDocumentos() {

        documentosOriginales = documentoService.obtenerTodosConRegistros();

        tablaDocumentos.setItems(
                FXCollections.observableArrayList(documentosOriginales)
        );
    }

    /**
     * Abre el documento en la aplicación predeterminada del sistema.
     * <p>
     * Crea un archivo temporal con el contenido del documento.
     *
     * @param doc documento a visualizar
     */
    private void verDocumento(Documento doc) {
        try {

            if (doc.getContenido() == null) {
                AvisosUtil.mostrarError(Messages.getString("no_content"));
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
            AvisosUtil.mostrarError(Messages.getString("error_opening_file"));
        }
    }

    /**
     * Permite guardar el documento en el sistema de archivos.
     * <p>
     * Abre un selector de ubicación y escribe el contenido del documento.
     *
     * @param doc documento a descargar
     */
    private void descargarDocumento(Documento doc) {
        try {

            if (doc.getContenido() == null) {
                AvisosUtil.mostrarError(Messages.getString("no_content"));
                return;
            }

            FileChooser fc = new FileChooser();
            fc.setInitialFileName(doc.getNombre());

            File destino = fc.showSaveDialog(tablaDocumentos.getScene().getWindow());

            if (destino != null) {
                Files.write(destino.toPath(), doc.getContenido());
            }

        } catch (Exception e) {
            AvisosUtil.mostrarError(Messages.getString("error_downloading"));
        }
    }


    /**
     * Aplica filtros avanzados sobre la lista de documentos.
     * <p>
     * Filtra por:
     * - Nombre
     * - Fecha
     * - Estado del documento
     * - Estado en blockchain
     * <p>
     * Actualiza la tabla con los resultados filtrados.
     */
    @FXML
    private void aplicarFiltros() {

        String nombre = filtroNombre.getText();
        LocalDate fecha = filtroFecha.getValue();
        String estado = filtroEstado.getValue();
        String estadoBlockchain = filtroEstadoBlockchain.getValue();

        List<Documento> filtrados = documentosOriginales.stream()
                .filter(doc -> {

                    if (nombre != null && !nombre.isBlank() &&
                            !doc.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                        return false;

                    if (fecha != null) {
                        LocalDate fechaDoc = doc.getFechaCreacion().toLocalDate();
                        if (!fechaDoc.equals(fecha))
                            return false;
                    }

                    if (estado != null &&
                            !getEstadoRealI18n(doc).equalsIgnoreCase(estado))
                        return false;

                    if (estadoBlockchain != null) {
                        String est = getEstadoRegistroActualI18n(doc);
                        if (!est.equalsIgnoreCase(estadoBlockchain))
                            return false;
                    }

                    return true;
                })
                .toList();

        tablaDocumentos.setItems(FXCollections.observableArrayList(filtrados));
    }

    /**
     * Limpia todos los filtros aplicados.
     * <p>
     * Restablece los campos y muestra la lista completa de documentos.
     */
    @FXML
    private void limpiarFiltros() {

        filtroNombre.clear();

        filtroFecha.setValue(null);

        filtroEstado.setValue(null);
        filtroEstadoBlockchain.setValue(null);

        tablaDocumentos.setItems(FXCollections.observableArrayList(documentosOriginales));
    }

    /**
     * Obtiene el estado del documento traducido.
     *
     * @param doc documento a evaluar
     * @return estado internacionalizado
     */
    private String getEstadoRealI18n(Documento doc) {
        EstadoDocumento estado = getEstadoReal(doc);
        if (estado == EstadoDocumento.BORRADOR) {
            return Messages.getString("draft");
        } else if (estado == EstadoDocumento.VALIDADO) {
            return Messages.getString("validated");
        }
        return estado.name();
    }

    /**
     * Traduce el estado del registro blockchain a su versión internacionalizada.
     *
     * @param doc documento a evaluar
     * @return estado traducido
     */
    private String getEstadoRegistroActualI18n(Documento doc) {
        String estado = getEstadoRegistroActual(doc);
        switch (estado.toUpperCase()) {
            case "PENDIENTE":
                return Messages.getString("pending");
            case "REGISTRADO":
                return Messages.getString("registered");
            case "REVOCADO":
                return Messages.getString("revoked");
            case "-":
                return Messages.getString("not_registered");
            default:
                return estado;
        }
    }

    /**
     * Comprueba si el usuario actual tiene rol ADMIN o MASTER.
     *
     * @return true si tiene permisos administrativos, false en caso contrario
     */
    private boolean esAdmin() {

        if (usuarioActual == null || usuarioActual.getRoles() == null) {
            return false;
        }

        return usuarioActual.getRoles().stream()
                .filter(ur -> ur.getRol() != null)
                .anyMatch(ur ->
                        "ADMIN".equalsIgnoreCase(ur.getRol().getNombre()) ||
                                "MASTER".equalsIgnoreCase(ur.getRol().getNombre()));
    }

    /**
     * Obtiene el estado actual del documento en blockchain como enum.
     * <p>
     * Devuelve null si:
     * - No hay registros
     * - Blockchain no está disponible
     *
     * @param doc documento a evaluar
     * @return estado como {@link EstadoBlockchain} o null
     */
    private EstadoBlockchain getEstadoRegistroActualEnum(Documento doc) {
        try {
            if (doc.isProcesando()) return EstadoBlockchain.PENDIENTE;

            if (doc.getRegistros() == null || doc.getRegistros().isEmpty()) {
                return null;
            }

            if (blockchainService == null) return null;

            String contratoActual = blockchainService.getContractAddress();

            return doc.getRegistros().stream()
                    .filter(r -> contratoActual.equalsIgnoreCase(r.getDireccionContrato()))
                    .max(Comparator.comparing(RegistroBlockchain::getFechaCreacion))
                    .map(RegistroBlockchain::getEstado)
                    .orElse(null);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Comprueba la disponibilidad y configuración de la conexión blockchain.
     * <p>
     * Valida:
     * - Inicialización del servicio blockchain
     * - Disponibilidad de Web3j
     * - Conectividad con el nodo RPC (mediante ethBlockNumber)
     * - Validez del contrato configurado
     * <p>
     * En caso de error:
     * - Marca la conexión como no disponible
     * - Muestra un mensaje al usuario
     * - Imprime información detallada en consola para depuración
     */
    private void comprobarConexionBlockchain() {

        try {
            // 1. Servicio
            if (blockchainService == null) {
                throw new RuntimeException("Service not initialized");
            }

            // 2. Web3j
            var web3j = blockchainService.getWeb3j();

            if (web3j == null) {
                throw new RuntimeException("Web3j not available");
            }

            // 3. Llamada ligera
            var block = web3j.ethBlockNumber().send();

            // 4. Contrato
            String contractAddress = blockchainService.getContractAddress();

            if (contractAddress == null || contractAddress.isBlank()) {
                throw new RuntimeException("Invalid contract");
            }

            if (!contractAddress.startsWith("0x") || contractAddress.length() != 42) {
                throw new RuntimeException("Bad contract format");
            }

            conectado = true;

        } catch (Exception e) {

            conectado = false;

            e.printStackTrace();

            AvisosUtil.mostrarError(Messages.getString("blockchain_offline_msg"));
        }
    }
}
