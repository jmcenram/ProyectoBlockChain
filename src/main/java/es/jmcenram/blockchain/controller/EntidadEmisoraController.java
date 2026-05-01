package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.controller.login.Session;
import es.jmcenram.blockchain.controller.utils.AvisosUtil;
import es.jmcenram.blockchain.model.entidademisora.EntidadEmisora;
import es.jmcenram.blockchain.service.blockchain.BlockchainService;
import es.jmcenram.blockchain.service.entidademisora.EntidadEmisoraService;
import es.jmcenram.blockchain.util.CryptoUtil;
import es.jmcenram.blockchain.util.Messages;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador encargado de la gestion de entidades emisoras.
 *
 * Permite al usuario:
 * - Crear entidades emisoras
 * - Editar nombre, estado y private key
 * - Eliminar entidades mediante baja logica
 * - Seleccionar entidades existentes para su mantenimiento
 *
 * Interactua con EntidadEmisoraService y cifra o descifra claves mediante CryptoUtil.
 *
 * Forma parte de la capa de presentacion (JavaFX).
 *
 * @author Jcena
 * @version 1.0
 */
public class EntidadEmisoraController implements Initializable {

    @FXML
    private ListView<EntidadEmisora> listEntidades;

    @FXML
    private TextField txtNombre;

    @FXML
    private PasswordField txtPrivateKey;

    @FXML
    private CheckBox chkActivo;

    private final EntidadEmisoraService service = new EntidadEmisoraService();

    private EntidadEmisora entidadSeleccionada;

    private static final String ROL_MASTER = "MASTER";

    /**
     * Prepara la pantalla de entidades emisoras al cargarse el FXML.
     *
     * Configura la lista, carga entidades persistidas y enlaza la seleccion con el formulario para editar sin navegar a otra vista.
     *
     * @param location ubicacion FXML recibida por el ciclo de vida de JavaFX
     * @param resources bundle de internacionalizacion asociado a la vista
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (!esMaster()) {
            Platform.runLater(() -> {
                AvisosUtil.mostrarError(Messages.getString("no_permission"));
                volver();
            });
            return;
        }

        configurarLista();
        cargarEntidades();
        limpiarFormulario();

        listEntidades.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            entidadSeleccionada = newVal;
            cargarFormulario(newVal);
        });
    }

    /**
     * Define como se muestran las entidades emisoras en la lista.
     *
     * La celda ense?a solo el nombre para que la clave privada cifrada y otros detalles internos no aparezcan en la UI.
     */
    private void configurarLista() {
        listEntidades.setCellFactory(lv -> new ListCell<>() {
            /**
             * Actualiza una celda de entidad emisora dentro del ListView.
             *
             * Limpia el texto en celdas vacias y muestra el nombre cuando JavaFX reutiliza la celda con una entidad valida.
             *
             * @param item valor que JavaFX entrega a la celda durante su refresco
             * @param empty indica si la celda no tiene contenido asociado
             */
            @Override
            protected void updateItem(EntidadEmisora item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
    }

    /**
     * Carga las entidades emisoras disponibles desde el servicio.
     *
     * La lista de la vista se reemplaza completa para reflejar altas, bajas logicas y ediciones recientes.
     */
    private void cargarEntidades() {
        try {
            List<EntidadEmisora> entidades = service.findAll();
            listEntidades.getItems().setAll(entidades);
        } catch (Exception e) {
            AvisosUtil.mostrarError("Error cargando entidades");
        }
    }

    /**
     * Vuelca en el formulario la entidad seleccionada por el usuario.
     *
     * Descifra temporalmente la private key solo para editarla en pantalla y limpia el campo si la clave no puede recuperarse.
     *
     * @param entidad entidad emisora que se muestra o persiste en el formulario
     */
    private void cargarFormulario(EntidadEmisora entidad) {
        if (entidad == null) return;

        txtNombre.setText(entidad.getNombre());

        try {
            String encryptedKey = entidad.getPrivateKey();

            if (encryptedKey != null && !encryptedKey.isBlank()) {
                String decrypted = CryptoUtil.decrypt(encryptedKey);
                txtPrivateKey.setText(decrypted);
            } else {
                txtPrivateKey.clear();
            }

        } catch (Exception e) {
            txtPrivateKey.clear();
            AvisosUtil.mostrarError("Error descifrando la private key");
        }

        chkActivo.setSelected(Boolean.TRUE.equals(entidad.getActivo()));
    }

    /**
     * Deja el formulario listo para crear una entidad nueva.
     *
     * Borra la seleccion actual, limpia campos sensibles y marca la entidad como activa por defecto.
     */
    private void limpiarFormulario() {
        entidadSeleccionada = null;

        txtNombre.clear();
        txtPrivateKey.clear();
        chkActivo.setSelected(true);

        listEntidades.getSelectionModel().clearSelection();
    }

    /**
     * Inicia el flujo de alta de una entidad emisora.
     *
     * Reutiliza la limpieza del formulario para separar claramente una creacion de una edicion previa.
     */
    @FXML
    private void nuevaEntidad() {
        limpiarFormulario();
    }

    /**
     * Persiste una entidad emisora nueva o actualiza la actualmente seleccionada.
     *
     * Antes de guardar:
     * - Valida los campos obligatorios del formulario.
     * - Verifica el formato de la private key (64 caracteres hexadecimales).
     * - Genera la direccion publica (address) asociada a la clave.
     * - Comprueba que la cuenta tiene balance en blockchain (cuenta activa).
     * - Cifra la private key para evitar su almacenamiento en claro.
     *
     * La address se almacena sin cifrar al tratarse de un dato publico necesario
     * para identificar al emisor en blockchain.
     */
    @FXML
    private void guardar() {

        if (!validarFormulario()) return;

        try {

            boolean esNueva = (entidadSeleccionada == null);
            EntidadEmisora entidad = esNueva ? new EntidadEmisora() : entidadSeleccionada;

            entidad.setNombre(txtNombre.getText().trim());
            entidad.setActivo(chkActivo.isSelected());

            if (!txtPrivateKey.getText().isBlank()) {

                String privateKey = txtPrivateKey.getText().trim();

                // Quitar prefijo 0x si existe
                if (privateKey.startsWith("0x")) {
                    privateKey = privateKey.substring(2);
                }

                // Validar formato (64 hex)
                if (!privateKey.matches("^[0-9a-fA-F]{64}$")) {
                    AvisosUtil.mostrarError("La private key no tiene un formato válido");
                    return;
                }

                try {
                    Credentials credentials = Credentials.create(privateKey);
                    String address = credentials.getAddress();

                    // 🔥 VALIDACIÓN REAL EN BLOCKCHAIN
                    boolean tieneBalance = BlockchainService
                            .getInstance()
                            .tieneBalance(address);

                    if (!tieneBalance) {
                        AvisosUtil.mostrarError("La cuenta no tiene fondos o no está activa en la red");
                        return;
                    }

                    // Guardar datos
                    String encrypted = CryptoUtil.encrypt(privateKey);
                    entidad.setPrivateKey(encrypted);
                    entidad.setAddress(address);

                } catch (Exception e) {
                    AvisosUtil.mostrarError("Error procesando la private key o consultando blockchain");
                    return;
                }
            }

            service.save(entidad);

            AvisosUtil.mostrarInfo("Datos guardados correctamente");

            cargarEntidades();
            limpiarFormulario();

        } catch (Exception e) {
            e.printStackTrace();
            AvisosUtil.mostrarError("Error al guardar");
        }
    }

    /**
     * Elimina logicamente la entidad emisora seleccionada.
     *
     * La baja conserva el registro para trazabilidad, pero evita que siga apareciendo como entidad operativa.
     */
    @FXML
    private void eliminar() {

        if (entidadSeleccionada == null) {
            AvisosUtil.mostrarError("Selecciona una entidad");
            return;
        }

        boolean confirmar = AvisosUtil.confirmarAccion(
                "Confirmar eliminación",
                "¿Deseas eliminar la entidad?"
        );

        if (!confirmar) return;

        try {
            service.delete(entidadSeleccionada);

            AvisosUtil.mostrarInfo("Entidad eliminada");

            cargarEntidades();
            limpiarFormulario();

        } catch (Exception e) {
            AvisosUtil.mostrarError("Error eliminando");
        }
    }

    /**
     * Vuelve a la vista principal manteniendo el layout global.
     *
     * Carga main.fxml con el bundle activo y sustituye solo el contenido central de la aplicacion.
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
                    listEntidades.getScene().getUserData();

            if (layout == null) {
                AvisosUtil.mostrarError("Error de navegación");
                return;
            }

            layout.setContent(mainView);

        } catch (Exception e) {
            AvisosUtil.mostrarError("Error al volver");
        }
    }

    /**
     * Validación del formulario.
     */
    private boolean validarFormulario() {

        String nombre = txtNombre.getText().trim();
        String privateKey = txtPrivateKey.getText().trim();

        if (nombre.isBlank()) {
            AvisosUtil.mostrarError("El nombre es obligatorio");
            return false;
        }

        if (entidadSeleccionada == null && privateKey.isBlank()) {
            AvisosUtil.mostrarError("La private key es obligatoria");
            return false;
        }

        if (!privateKey.isBlank()) {

            try {
                String pk = privateKey.startsWith("0x")
                        ? privateKey.substring(2)
                        : privateKey;

                if (!pk.matches("[0-9a-fA-F]{64}")) {
                    AvisosUtil.mostrarError("Formato de private key inválido");
                    return false;
                }

                org.web3j.crypto.Credentials.create(pk);

            } catch (Exception e) {
                AvisosUtil.mostrarError("Private key inválida");
                return false;
            }
        }

        return true;
    }

    /**
     * Comprueba si el usuario autenticado puede gestionar entidades emisoras.
     *
     * La gestion de claves privadas y emisores queda reservada al rol MASTER.
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
