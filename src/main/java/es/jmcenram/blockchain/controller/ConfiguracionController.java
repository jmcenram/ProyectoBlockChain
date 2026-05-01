package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.config.BlockchainConfig;
import es.jmcenram.blockchain.config.ConfigManager;
import es.jmcenram.blockchain.controller.login.Session;
import es.jmcenram.blockchain.service.blockchain.BlockchainService;
import es.jmcenram.blockchain.util.Messages;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controlador encargado de la gestión de la configuración de conexión a blockchain.
 *
 * Permite al usuario:
 * - Cargar una configuración desde fichero
 * - Guardar una configuración
 * - Conectarse a la blockchain
 * - Persistir la configuración activa
 *
 * Interactúa con {@link ConfigManager} para la gestión de archivos
 * y con {@link BlockchainService} para inicializar la conexión.
 *
 * Forma parte de la capa de presentación (JavaFX).
 *
 * @author Jcena
 * @version 1.0
 */
public class ConfiguracionController {

    /** Campo para la URL RPC */
    @FXML private TextField rpcUrlField;

    /** Campo para la dirección del contrato */
    @FXML private TextField contractAddressField;

    /** Campo que muestra la ruta del fichero de configuración */
    @FXML private TextField rutaConfigField;

    /** Label de estado (mensajes de éxito/error) */
    @FXML private Label lblEstado;

    /** Archivo de configuración actualmente cargado o guardado */
    private File archivoActual;

    private static final String ROL_MASTER = "MASTER";

    /**
     * Inicializa el controlador cargando la configuración por defecto si existe.
     *
     * Si no se encuentra configuración, se muestra un mensaje informativo
     * sin interrumpir la ejecución de la interfaz.
     */
    @FXML
    public void initialize() {

        try {
            BlockchainConfig config = ConfigManager.load();

            if (config != null) {
                rpcUrlField.setText(nullSafe(config.getRpcUrl()));
                contractAddressField.setText(nullSafe(config.getContractAddress()));

                rutaConfigField.setText(ConfigManager.getConfigPath());

                mostrarInfo(Messages.getString("config_loaded_ok"));
            } else {
                rutaConfigField.setText(ConfigManager.getConfigPath());
                mostrarInfo(Messages.getString("config_not_found"));
            }

        } catch (Exception e) {
            rutaConfigField.setText(ConfigManager.getConfigPath());
            mostrarInfo(Messages.getString("config_not_found"));
        }
    }

    /**
     * Permite al usuario seleccionar un fichero .properties
     * y cargar su configuración en la interfaz.
     */
    @FXML
    public void cargarConfig() {

        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Seleccionar configuración");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Properties", "*.properties")
            );

            File file = chooser.showOpenDialog(rpcUrlField.getScene().getWindow());
            if (file == null) return;

            archivoActual = file;

            BlockchainConfig config = ConfigManager.load(file);

            rpcUrlField.setText(nullSafe(config.getRpcUrl()));
            contractAddressField.setText(nullSafe(config.getContractAddress()));

            rutaConfigField.setText(file.getAbsolutePath());

            mostrarInfo(Messages.getString("config_loaded_ok"));

        } catch (Exception e) {
            mostrarError(Messages.getString("config_load_error") + ": " + e.getMessage());
        }
    }

    /**
     * Guarda la configuración actual en un fichero seleccionado por el usuario.
     *
     * El FileChooser abre directamente en la carpeta config/,
     * que es la ubicación estándar de la aplicación.
     */
    @FXML
    public void guardar() {

        if (!validarCampos()) return;

        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Guardar configuración");
            chooser.setInitialFileName("blockchain.properties");

            File configDir = new File(ConfigManager.getConfigPath()).getParentFile();

            if (configDir.exists()) {
                chooser.setInitialDirectory(configDir);
            }

            File file = chooser.showSaveDialog(rpcUrlField.getScene().getWindow());
            if (file == null) return;

            BlockchainConfig config = getConfigDesdeCampos();

            ConfigManager.save(config, file);

            rutaConfigField.setText(file.getAbsolutePath());

            mostrarInfo(Messages.getString("config_saved"));

        } catch (Exception e) {
            mostrarError(Messages.getString("config_save_error") + ": " + e.getMessage());
        }
    }

    /**
     * Inicializa la conexión con la blockchain utilizando los datos introducidos.
     *
     * Si la conexión es correcta, se guarda automáticamente la configuración.
     */
    @FXML
    public void conectar() {

        if (!validarCampos()) return;

        try {
            BlockchainConfig config = getConfigDesdeCampos();

            mostrarInfo(Messages.getString("initializing_blockchain"));

            BlockchainService.init(config);

            String address = BlockchainService.getInstance() != null
                    ? BlockchainService.getInstance().getContractAddress()
                    : null;

            if (address != null && !address.isBlank()) {
                config.setContractAddress(address);
                contractAddressField.setText(address);
            }

            ConfigManager.save(config);

            rutaConfigField.setText(ConfigManager.getConfigPath());

            mostrarInfo(Messages.getString("blockchain_connected"));

        } catch (Exception e) {
            mostrarError(Messages.getString("blockchain_connection_error") + ": " + e.getMessage());
        }
    }

    /**
     * Construye un objeto {@link BlockchainConfig} a partir de los campos del formulario.
     *
     * @return configuración creada
     */
    private BlockchainConfig getConfigDesdeCampos() {
        BlockchainConfig config = new BlockchainConfig();
        config.setRpcUrl(rpcUrlField.getText().trim());
        config.setContractAddress(contractAddressField.getText().trim());
        return config;
    }

    /**
     * Evita valores null en campos de texto.
     *
     * @param value valor original
     * @return string no nulo
     */
    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Navega de vuelta a la pantalla principal de la aplicación.
     */
    @FXML
    public void volver() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/main.fxml")
            );
            loader.setResources(Messages.getBundle());

            Parent mainView = loader.load();

            LayoutController layout = (LayoutController)
                    rpcUrlField.getScene().getUserData();

            if (layout == null) {
                mostrarError(Messages.getString("navigation_error"));
                return;
            }

            layout.setContent(mainView);

        } catch (Exception e) {
            mostrarError(Messages.getString("navigation_error"));
        }
    }

    /**
     * Valida los campos obligatorios del formulario.
     *
     * @return true si los campos son válidos, false en caso contrario
     */
    protected boolean validarCampos() {

        boolean valido = true;

        limpiarErrores();

        if (rpcUrlField.getText() == null || rpcUrlField.getText().isBlank()) {
            marcarError(rpcUrlField);
            valido = false;
        }

        if (!valido) {
            mostrarError(Messages.getString("config_required_fields"));
        }

        return valido;
    }

    /**
     * Aplica estilo de error a un campo de texto.
     *
     * @param field campo a marcar
     */
    private void marcarError(TextField field) {
        if (!field.getStyleClass().contains("input-error")) {
            field.getStyleClass().add("input-error");
        }
    }

    /**
     * Elimina los estilos de error de los campos.
     */
    private void limpiarErrores() {
        rpcUrlField.getStyleClass().remove("input-error");
        contractAddressField.getStyleClass().remove("input-error");
    }

    /**
     * Muestra un mensaje informativo en la interfaz.
     *
     * @param msg mensaje a mostrar
     */
    private void mostrarInfo(String msg) {
        lblEstado.setText(msg);
        lblEstado.getStyleClass().removeAll("text-error");
        lblEstado.getStyleClass().add("text-success");
    }

    /**
     * Muestra un mensaje de error en la interfaz.
     *
     * @param msg mensaje a mostrar
     */
    private void mostrarError(String msg) {
        lblEstado.setText(msg);
        lblEstado.getStyleClass().removeAll("text-success");
        lblEstado.getStyleClass().add("text-error");
    }

    /**
     * Comprueba si el usuario autenticado puede gestionar la configuracion blockchain.
     *
     * Solo MASTER puede cambiar RPC, contrato o fichero de configuracion activo.
     *
     * @return true si el usuario en sesion es MASTER
     */
    private boolean esMaster() {
        var usuario = Session.getUsuario();

        return usuario != null
                && usuario.getRoles() != null
                && usuario.getRoles().stream()
                .filter(ur -> ur.getRol() != null)
                .anyMatch(ur -> ROL_MASTER.equalsIgnoreCase(ur.getRol().getNombre()));
    }
}