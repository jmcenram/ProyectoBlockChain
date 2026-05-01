package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.controller.login.Session;
import es.jmcenram.blockchain.controller.utils.AvisosUtil;
import es.jmcenram.blockchain.model.entidademisora.EntidadEmisora;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.entidademisora.EntidadEmisoraRepository;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.entidademisora.EntidadEmisoraService;
import es.jmcenram.blockchain.service.usuario.UsuarioService;
import es.jmcenram.blockchain.util.Messages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controlador encargado de la gestión del perfil del usuario autenticado.
 *
 * Permite:
 * - Visualizar datos del usuario (nombre y email)
 * - Modificar el nombre
 * - Cambiar la contraseña con validación de seguridad
 * - Cambiar la entidad emisora propia cuando el usuario tiene rol MASTER
 *
 * Utiliza {@link Session} para obtener el usuario actual y
 * {@link UsuarioService} para persistir los cambios.
 *
 * La seleccion de entidad emisora solo se muestra a MASTER porque este rol
 * concentra la configuracion blockchain y la administracion de emisores.
 *
 * Incluye validaciones de integridad y seguridad en el cambio de contraseña.
 *
 * @author Jcena
 * @version 1.0
 */
public class PerfilUsuarioController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private Label lblEntidadEmisora;
    @FXML private ComboBox<EntidadEmisora> comboEntidadEmisora;

    @FXML private PasswordField txtActual;
    @FXML private PasswordField txtNueva;
    @FXML private PasswordField txtRepetir;

    private UsuarioService usuarioService;
    private EntidadEmisoraService entidadEmisoraService;
    private Usuario usuarioActual;

    private static final String ROL_MASTER = "MASTER";

    /**
     * Inicializa la vista del perfil de usuario.
     *
     * Configura:
     * - Servicios necesarios
     * - Usuario autenticado
     * - Carga de datos en los campos de la UI
     *
     * Además elimina el foco inicial del primer campo para mejorar la UX.
     */
    @FXML
    public void initialize() {

        UsuarioRepository usuarioRepo = new UsuarioRepository();
        RolRepository rolRepo = new RolRepository();
        UsuarioRolRepository usuarioRolRepo = new UsuarioRolRepository();
        EntidadEmisoraRepository entidadEmisoraRepo = new EntidadEmisoraRepository();

        usuarioService = new UsuarioService(usuarioRepo, rolRepo, usuarioRolRepo);
        entidadEmisoraService = new EntidadEmisoraService(entidadEmisoraRepo);

        usuarioActual = Session.getUsuario();

        txtNombre.setText(usuarioActual.getNombre());
        txtEmail.setText(usuarioActual.getEmail());
        configurarEntidadEmisora();

        // QUITAR FOCO INICIAL
        javafx.application.Platform.runLater(() -> {
            txtNombre.getParent().requestFocus();
        });
    }

    /**
     * Guarda los cambios realizados en el perfil del usuario.
     *
     * Permite:
     * - Actualizar el nombre
     * - Cambiar la contraseña (opcional)
     *
     * Validaciones:
     * - Nombre no vacío
     * - Contraseña segura (si se modifica)
     * - Coincidencia entre nueva contraseña y repetición
     *
     * Tras guardar:
     * - Persiste los cambios en base de datos
     * - Muestra mensaje informativo
     * - Limpia los campos de contraseña
     */
    @FXML
    private void guardar() {

        try {

            String nuevoNombre = txtNombre.getText();

            if (nuevoNombre == null || nuevoNombre.isBlank()) {
                AvisosUtil.mostrarError(Messages.getString("invalid_name"));
                return;
            }

            if (!txtNueva.getText().isBlank()) {

                if (!esPasswordSegura(txtNueva.getText())) {
                    AvisosUtil.mostrarError(Messages.getString("password_requirements"));
                    return;
                }

                if (!txtNueva.getText().equals(txtRepetir.getText())) {
                    AvisosUtil.mostrarError(Messages.getString("passwords_not_match"));
                    return;
                }

                usuarioActual = usuarioService.cambiarPassword(
                        usuarioActual.getId(),
                        txtActual.getText(),
                        txtNueva.getText()
                );
            }

            usuarioActual.setNombre(nuevoNombre);

            if (esMaster()) {
                usuarioActual.setEntidadEmisora(comboEntidadEmisora.getValue());
            }

            usuarioActual = usuarioService.update(usuarioActual);
            Session.setUsuario(usuarioActual);

            AvisosUtil.mostrarInfo(Messages.getString("data_updated"));

            limpiarPassword();

        } catch (Exception e) {
            AvisosUtil.mostrarError(e.getMessage());
        }
    }

    /**
     * Limpia los campos relacionados con la contraseña.
     *
     * Se utiliza tras un cambio exitoso o para resetear el formulario.
     */
    private void limpiarPassword() {
        txtActual.clear();
        txtNueva.clear();
        txtRepetir.clear();
    }

    /**
     * Configura el selector de entidad emisora disponible solo para MASTER.
     *
     * Este rol puede cambiar su propia entidad desde perfil porque tambien es
     * responsable de gestionar la infraestructura blockchain y sus emisores.
     */
    private void configurarEntidadEmisora() {
        boolean master = esMaster();

        lblEntidadEmisora.setVisible(master);
        lblEntidadEmisora.setManaged(master);
        comboEntidadEmisora.setVisible(master);
        comboEntidadEmisora.setManaged(master);

        if (!master) {
            return;
        }

        comboEntidadEmisora.getItems().setAll(entidadEmisoraService.findAllActivas());

        comboEntidadEmisora.setCellFactory(lv -> new ListCell<>() {
            /**
             * Actualiza la celda visible del selector de entidades emisoras.
             *
             * Muestra el nombre del emisor y limpia el texto cuando JavaFX
             * reutiliza la celda sin un elemento asociado.
             *
             * @param item entidad emisora mostrada por la celda
             * @param empty indica si la celda esta vacia
             */
            @Override
            protected void updateItem(EntidadEmisora item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        comboEntidadEmisora.setButtonCell(new ListCell<>() {
            /**
             * Actualiza el texto mostrado en el boton del ComboBox.
             *
             * Permite que la entidad seleccionada se vea igual que las opciones
             * del desplegable y evita mostrar referencias de objeto.
             *
             * @param item entidad emisora seleccionada
             * @param empty indica si no hay seleccion visible
             */
            @Override
            protected void updateItem(EntidadEmisora item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        if (usuarioActual.getEntidadEmisora() != null) {
            Long entidadId = usuarioActual.getEntidadEmisora().getId();
            comboEntidadEmisora.getItems().stream()
                    .filter(entidad -> entidad.getId().equals(entidadId))
                    .findFirst()
                    .ifPresent(comboEntidadEmisora::setValue);
        }
    }

    /**
     * Comprueba si el usuario autenticado tiene rol MASTER.
     *
     * @return true si el usuario actual puede cambiar su entidad emisora
     */
    private boolean esMaster() {
        return usuarioActual != null
                && usuarioActual.getRoles() != null
                && usuarioActual.getRoles().stream()
                .filter(ur -> ur.getRol() != null)
                .anyMatch(ur -> ROL_MASTER.equalsIgnoreCase(ur.getRol().getNombre()));
    }

    /**
     * Navega de vuelta a la pantalla principal.
     *
     * Carga la vista principal y la establece en el layout global.
     *
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
                    txtNombre.getScene().getUserData();

            layout.setContent(mainView);

        } catch (Exception e) {
            AvisosUtil.mostrarError(Messages.getString("back_error"));
        }
    }

    /**
     * Valida si una contraseña cumple los requisitos de seguridad.
     *
     * Requisitos:
     * - Al menos 8 caracteres
     * - Una letra minúscula
     * - Una letra mayúscula
     * - Un número
     * - Un carácter especial
     *
     * @param password contraseña a validar
     * @return true si cumple los requisitos, false en caso contrario
     */
    private boolean esPasswordSegura(String password) {

        if (password == null) return false;

        // Regex completa
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

        return password.matches(regex);
    }
}
