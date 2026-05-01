package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.controller.login.Session;
import es.jmcenram.blockchain.controller.utils.AvisosUtil;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.util.Messages;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controlador principal de la aplicación.
 *
 * Gestiona:
 * - Visualización del usuario autenticado
 * - Navegación entre pantallas
 * - Control de acceso según rol (MASTER / ADMIN / USER)
 * - Cierre de sesión (logout)
 *
 * Reserva la configuracion blockchain y la gestion de entidades emisoras al rol MASTER.
 * Mantiene para MASTER los accesos funcionales de ADMIN, como el registro de usuarios.
 *
 * Utiliza {@link Session} para obtener el usuario actual y
 * {@link LayoutController} para cambiar dinamicamente el contenido de la UI.
 *
 * @author Jcena
 * @version 1.0
 */
public class MainController {

    @FXML
    private Label lblUsuario;
    @FXML
    private Label lblEntidadNombre;
    @FXML
    private Label lblEntidad;
    @FXML
    private Button btnConfiguracion;
    @FXML
    private Button btnRegistrar;
    @FXML
    private Button btnEntidadesEmisoras;

    private static final String ROL_MASTER = "MASTER";
    private static final String ROL_ADMIN = "ADMIN";

    /**
     * Inicializa la vista principal.
     *
     * - Verifica si existe un usuario en sesión
     * - Si no existe, redirige al login
     * - Muestra el nombre del usuario autenticado
     * - Oculta elementos UI según permisos (rol)
     */
    @FXML
    public void initialize() {

        if (Session.getUsuario() == null) {
            javafx.application.Platform.runLater(this::redirigirLogin);
            return;
        }

        var usuario = Session.getUsuario();

        lblUsuario.setText(usuario.getNombre());

        if (usuario.getEntidadEmisora() != null) {
            lblEntidadNombre.setText(String.valueOf(usuario.getEntidadEmisora().getNombre()));
        } else {
            lblEntidadNombre.setVisible(false);
            lblEntidadNombre.setManaged(false);
            lblEntidad.setVisible(false);
            lblEntidad.setManaged(false);
        }

        boolean admin = esAdmin(usuario);
        boolean master = esMaster(usuario);

        setVisible(btnRegistrar, master);
        setVisible(btnEntidadesEmisoras, master);
    }

    /**
     * Cambia el contenido principal de la aplicación.
     *
     * Carga una vista FXML y la establece en el layout global.
     *
     * @param fxml nombre del archivo FXML a cargar
     */
    private void cambiarPantalla(String fxml) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/" + fxml)
            );
            loader.setResources(Messages.getBundle());

            Parent vista = loader.load();

            LayoutController layout = (LayoutController)
                    lblUsuario.getScene().getUserData();

            if (layout == null) {
                AvisosUtil.mostrarError(Messages.getString("navigation_error"));
                return;
            }

            layout.setContent(vista);

        } catch (Exception e) {
            AvisosUtil.mostrarError(Messages.getString("loading_screen_error"));
            e.printStackTrace();
        }
    }

    /**
     * Navega a la pantalla de gestión de documentos.
     */
    @FXML
    public void irADocumentos() {
        cambiarPantalla("documentos.fxml");
    }

    /**
     * Navega a la pantalla de configuración.
     */
    @FXML
    public void irAConfiguracion() {
        cambiarPantalla("configuracion.fxml");
    }

    /**
     * Navega a la pantalla de registro de usuarios.
     *
     * Solo accesible para administradores.
     */
    @FXML
    public void irARegistroUsuario() {
        if (!esAdmin(Session.getUsuario())) {
            AvisosUtil.mostrarError(Messages.getString("no_permission"));
            return;
        }
        cambiarPantalla("registroUsuario.fxml");
    }

    /**
     * Navega a la pantalla de gestión de entidades emisoras.
     *
     * Solo accesible para administradores.
     * Permite crear, editar y eliminar entidades que pueden emitir documentos en blockchain.
     */
    @FXML
    public void irAEntidadesEmisoras() {
        if (!esMaster(Session.getUsuario())) {
            AvisosUtil.mostrarError(Messages.getString("no_permission"));
            return;
        }
        cambiarPantalla("entidadesEmisoras.fxml");
    }

    /**
     * Navega a la pantalla de perfil del usuario.
     */
    @FXML
    public void irAMiPerfil() {
        cambiarPantalla("perfil.fxml");
    }

    /**
     * Cierra la sesión actual del usuario.
     *
     * - Elimina el usuario de {@link Session}
     * - Redirige a la pantalla de login
     *
     * En caso de error, muestra un mensaje al usuario.
     */
    @FXML
    private void logout() {
        try {

            Session.logout();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/login.fxml")
            );
            loader.setResources(Messages.getBundle());

            Parent loginView = loader.load();

            LayoutController layout = (LayoutController)
                    lblUsuario.getScene().getUserData();

            if (layout != null) {
                layout.setContent(loginView);
            }

        } catch (Exception e) {
            AvisosUtil.mostrarError(Messages.getString("logout_error"));
            e.printStackTrace();
        }
    }

    /**
     * Redirige a la pantalla de login.
     *
     * Se utiliza cuando no existe usuario en sesión.
     */
    private void redirigirLogin() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/login.fxml")
            );
            loader.setResources(Messages.getBundle());

            Parent loginView = loader.load();

            LayoutController layout = (LayoutController)
                    lblUsuario.getScene().getUserData();

            if (layout != null) {
                layout.setContent(loginView);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Comprueba si un usuario tiene rol ADMIN o MASTER.
     *
     * @param usuario usuario a evaluar
     * @return true si tiene permisos administrativos, false en caso contrario
     */
    private boolean esAdmin(Usuario usuario) {
        return tieneRol(usuario, ROL_ADMIN) || tieneRol(usuario, ROL_MASTER);
    }

    /**
     * Comprueba si un usuario tiene rol MASTER.
     *
     * MASTER concentra configuracion blockchain y gestion de entidades emisoras.
     *
     * @param usuario usuario a evaluar
     * @return true si es MASTER, false en caso contrario
     */
    private boolean esMaster(Usuario usuario) {
        return tieneRol(usuario, ROL_MASTER);
    }

    /**
     * Comprueba si el usuario contiene un rol concreto.
     *
     * @param usuario usuario a evaluar
     * @param rol nombre del rol requerido
     * @return true si el usuario tiene el rol indicado
     */
    private boolean tieneRol(Usuario usuario, String rol) {

        if (usuario == null || usuario.getRoles() == null) {
            return false;
        }

        return usuario.getRoles().stream()
                .filter(ur -> ur.getRol() != null)
                .anyMatch(ur -> rol.equalsIgnoreCase(ur.getRol().getNombre()));
    }

    /**
     * Muestra u oculta un boton eliminando tambien su espacio en el layout.
     *
     * @param button boton a actualizar
     * @param visible estado visible deseado
     */
    private void setVisible(Button button, boolean visible) {
        if (button != null) {
            button.setVisible(visible);
            button.setManaged(visible);
        }
    }

}
