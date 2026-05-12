package es.jmcenram.blockchain.controller.login;

import es.jmcenram.blockchain.controller.LayoutController;
import es.jmcenram.blockchain.controller.utils.AvisosUtil;
import es.jmcenram.blockchain.config.BlockchainConfig;
import es.jmcenram.blockchain.config.ConfigManager;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.usuario.UsuarioService;
import es.jmcenram.blockchain.util.Messages;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Locale;

import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarError;
import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarInfo;

/**
 * Controlador JavaFX para la pantalla de login.
 * Gestiona autenticación de usuarios, cambio de idioma y activación de cuentas nuevas.
 * <p>
 * Funcionalidades:
 * - Login de usuarios con email y contraseña
 * - Activación de nuevos usuarios con establecimiento de contraseña segura
 * - Cambio de idioma dinámico (Español/English)
 * - Validación de contraseñas con criterios de seguridad
 * <p>
 * Interacción con vistas FXML:
 * - login.fxml: formulario de login
 * - main.fxml: pantalla principal tras login exitoso
 *
 * @author Jcena
 * @version 1.0
 */
public class LoginController {

    /**
     * Campo de texto para usuario/email
     */
    @FXML
    private TextField txtEmail;

    /**
     * Campo de contraseña
     */
    @FXML
    private PasswordField txtPassword;

    /**
     * Campo de contraseña nueva (para activación de cuenta)
     */
    @FXML
    private PasswordField txtPasswordNueva;

    /**
     * ComboBox para seleccionar idioma
     */
    @FXML
    private ComboBox<String> comboIdioma;

    /**
     * Servicio para operaciones de usuario
     */
    private UsuarioService usuarioService;

    private final String ADMIN = "ADMIN";

    /**
     * Método de inicialización invocado por FXMLLoader al cargar la vista.
     * Configura repositorios, servicios, event listeners e idioma inicial.
     * <p>
     * Inicialización:
     * 1. Crea instancias de repositorios:
     * - {@link UsuarioRepository}
     * - {@link RolRepository}
     * - {@link UsuarioRolRepository}
     * 2. Instancia {@link UsuarioService} con los repositorios
     * 3. Configura listener en txtPassword para ejecutar login al presionar Enter
     * 4. Carga idiomas disponibles (Español/English) en ComboBox
     * 5. Establece locale actual desde {@link Messages#getLocale()}
     * 6. Sincroniza ComboBox con idioma actual de la aplicación
     */
    @FXML
    public void initialize() {

        UsuarioRepository usuarioRepo = new UsuarioRepository();
        RolRepository rolRepo = new RolRepository();
        UsuarioRolRepository usuarioRolRepo = new UsuarioRolRepository();

        usuarioService = new UsuarioService(usuarioRepo, rolRepo, usuarioRolRepo);

        txtPassword.setOnAction(e -> login());

        comboIdioma.getItems().addAll("Español", "English");
        comboIdioma.setValue("Español");

        Locale current = Messages.getLocale();

        if (current.getLanguage().equals("en")) {
            comboIdioma.setValue("English");
        } else {
            comboIdioma.setValue("Español");
        }

        addAutoHideNuevaPassword();
    }

    /**
     * Realiza login del usuario con email y contraseña.
     * Si el usuario existe y está activo, inicia sesión.
     * Si el usuario no está activado, muestra campo para establecer contraseña.
     * <p>
     * Validaciones:
     * - Email y contraseña no pueden estar vacíos
     * - Contraseña nueva (si se establece) debe cumplir critenos de seguridad
     * - No se permite reutilizar contraseña
     * <p>
     * Flujo:
     * 1. Valida que usuario y contraseña no estén vacíos
     * 2. Intenta autenticar con {@link UsuarioService#login(String, String)}
     * 3. Si usuario activo: inicia sesión
     * 4. Si usuario inactivo: solicita contraseña nueva para activación
     * 5. Captura excepciones y muestra error de credenciales inválidas
     *
     */
    @FXML
    private void login() {

        String username = txtEmail.getText();
        String password = txtPassword.getText();

        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {

            mostrarError(Messages.getString("enter_user_password"));
            return;
        }

            try {
                Usuario usuario = usuarioService.login(username, password);
                if (esAdminEntidadActiva(usuario)) {

                    if (usuario.isActivo()) {

                        inicioSesion(usuario);

                    } else {

                        boolean visible = txtPasswordNueva.isVisible();
                        String pass = txtPasswordNueva.getText();


                        if (visible && !pass.isBlank()) {

                            if (!esPasswordSegura(pass)) {
                                mostrarError(Messages.getString("insecure_password"));
                                return;
                            }

                            Usuario activado = usuarioService.activarUsuario(username, pass);
                            mostrarInfo(Messages.getString("user_activated"));
                            inicioSesion(activado);
                        } else if (!visible && pass.isBlank()) {
                            txtPasswordNueva.setVisible(!visible);
                            txtPasswordNueva.setManaged(!visible);
                        } else {
                            mostrarError(Messages.getString("blank_new_password"));
                        }
                    }
                } else {
                    mostrarError(Messages.getString("innactive_entity"));
                }
            } catch (Exception e) {
                System.out.println(e);
                mostrarError(Messages.getString("invalid_credentials"));
            }
    }

    /**
     * Inicia sesión del usuario autenticado y carga la pantalla principal.
     * Establece el usuario en Session y carga la vista main.fxml.
     * <p>
     * Proceso:
     * 1. Almacena usuario autenticado en {@link Session#setUsuario(Usuario)}
     * 2. Crea nuevo FXMLLoader para cargar main.fxml
     * 3. Configura Bundle de recursos localizados
     * 4. Carga la vista principal
     * 5. Obtiene controlador principal (LayoutController) del userData de la Scene
     * 6. Verifica que LayoutController no sea nulo
     * 7. Inyecta mainView en el contenedor principal
     *
     * @param usuario usuario autenticado a establecer en sesión
     * @throws IOException          si hay error al cargar main.fxml o recurso no encontrado
     * @throws NullPointerException si LayoutController es nulo (error interno)
     */
    private void inicioSesion(Usuario usuario) throws IOException {

        Session.setUsuario(usuario);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/main.fxml")
        );
        loader.setResources(Messages.getBundle());

        Parent mainView = loader.load();

        LayoutController layout = (LayoutController)
                txtEmail.getScene().getUserData();

        if (layout == null) {
            mostrarError(Messages.getString("internal_navigation_error"));
            return;
        }

        layout.setContent(mainView);
    }

    /**
     * Valida que una contraseña sea segura según criterios de seguridad específicos.
     * Utiliza expresión regular para validar múltiples requerimientos simultáneamente.
     * <p>
     * Requerimientos de seguridad:
     * - Longitud mínima: 8 caracteres
     * - Debe contener al menos una minúscula [a-z]
     * - Debe contener al menos una mayúscula [A-Z]
     * - Debe contener al menos un dígito [0-9]
     * - Debe contener al menos un carácter especial [@#$%^&amp;+=!]
     * <p>
     * Ejemplo de contraseña válida: "Segura@123"
     *
     * @param password contraseña a validar (puede ser null)
     * @return true si cumple todos los criterios de seguridad, false en caso contrario
     * @see <a href="https://owasp.org/www-community/password-special-characters">OWASP - Caracteres especiales</a>
     */
    private boolean esPasswordSegura(String password) {

        if (password == null) return false;

        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(regex);
    }

    /**
     * Cambia el idioma de la aplicación entre Español e Inglés.
     * Recarga la vista login.fxml con el nuevo idioma seleccionado.
     * <p>
     * Proceso:
     * 1. Obtiene el idioma seleccionado del ComboBox
     * 2. Crea locale correspondiente (en/es)
     * 3. Actualiza locale global mediante {@link Messages#setLocale(Locale)}
     * 4. Invoca recargarVista() para aplicar strings localizados
     *
     * @see #recargarVista()
     */
    @FXML
    private void cambiarIdioma() {

        String selected = comboIdioma.getValue();

        Locale locale = switch (selected) {
            case "English" -> new Locale("en");
            default -> new Locale("es");
        };

        Messages.setLocale(locale);

        recargarVista();
    }

    /**
     * Recarga la vista actual (login.fxml) con el nuevo Locale.
     * Se invoca durante cambio de idioma para aplicar strings localizados.
     * <p>
     * Proceso:
     * 1. Crea nuevo FXMLLoader con login.fxml
     * 2. Establece recursos localizados del Bundle de Messages
     * 3. Carga la vista y obtiene el contenedor principal (LayoutController)
     * 4. Reemplaza el contenido actual con la vista recargada
     * 5. Captura excepciones de carga de FXML e imprime stack trace
     *
     * @see #cambiarIdioma()
     */
    private void recargarVista() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/login.fxml")
            );
            loader.setResources(Messages.getBundle());

            Parent loginView = loader.load();

            LayoutController layout = (LayoutController)
                    txtEmail.getScene().getUserData();

            if (layout != null) {
                layout.setContent(loginView);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Añade listeners a los campos de usuario y contraseña para ocultar automáticamente
     * el campo de nueva contraseña cuando alguno de estos valores cambia.
     * <p>
     * Objetivo:
     * - Mejorar la experiencia de usuario evitando estados inconsistentes.
     * - Si el usuario modifica sus credenciales, se asume un nuevo intento de login,
     * por lo que el campo de "nueva contraseña" deja de ser relevante.
     * <p>
     * Comportamiento:
     * - Cualquier cambio en txtEmail o txtPassword provoca la ocultación
     * del campo txtPasswordNueva.
     */
    private void addAutoHideNuevaPassword() {

        // Listener sobre el campo de usuario (email)
        // Se dispara cada vez que cambia el texto (teclado, pegado, etc.)
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> ocultarNuevaPassword());

        // Listener sobre el campo de contraseña
        // Permite reaccionar inmediatamente ante cualquier modificación
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> ocultarNuevaPassword());
    }


    /**
     * Oculta el campo de nueva contraseña si actualmente está visible.
     * <p>
     * Acciones realizadas:
     * - Limpia el contenido del campo para evitar reutilización accidental
     * - Establece visible=false para ocultarlo visualmente
     * - Establece managed=false para que no ocupe espacio en el layout
     * <p>
     * Nota:
     * El uso de setManaged(false) es clave en JavaFX para que el nodo
     * desaparezca completamente del flujo de diseño (no solo visualmente).
     */
    private void ocultarNuevaPassword() {

        // Solo actúa si el campo está visible (evita operaciones innecesarias)
        if (txtPasswordNueva.isVisible()) {

            // Limpia el contenido por seguridad (no dejar contraseñas en memoria visual)
            txtPasswordNueva.clear();

            // Oculta el nodo en la interfaz
            txtPasswordNueva.setVisible(false);

            // Elimina el nodo del layout para que no ocupe espacio
            txtPasswordNueva.setManaged(false);
        }
    }

    /**
     * Navega a la vista de generación de QR desde la pantalla de login.
     * <p>
     * Este método carga la vista dentro del LayoutController principal,
     * manteniendo la estructura de la aplicación (header, sidebar, etc.).
     * <p>
     * Proceso:
     * 1. Carga la vista verificacionQr.fxml
     * 2. Aplica el ResourceBundle para i18n
     * 3. Obtiene el LayoutController desde la Scene
     * 4. Inyecta la vista en el contenedor principal mediante setContent()
     *
     * @throws IOException si ocurre un error al cargar el FXML
     */
    @FXML
    private void irAQr() {

        try {
            if (!contratoConfigurado()) {
                mostrarError(Messages.getString("qr_contract_not_configured"));
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/verificacionQr.fxml")
            );
            loader.setResources(Messages.getBundle());

            Parent qrView = loader.load();

            // Recuperar layout como haces en inicioSesion
            LayoutController layout = (LayoutController)
                    txtEmail.getScene().getUserData();

            if (layout == null) {
                mostrarError(Messages.getString("internal_navigation_error"));
                return;
            }

            // Mantener layout
            layout.setContent(qrView);

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError(Messages.getString("internal_navigation_error"));
        } catch (RuntimeException e) {
            e.printStackTrace();
            mostrarError(Messages.getString("qr_contract_not_configured"));
        }
    }

    /**
     * Comprueba si hay contrato blockchain configurado antes de permitir generar QR.
     *
     * @return true si existe una direccion de contrato no vacia
     */
    private boolean contratoConfigurado() {
        BlockchainConfig config = ConfigManager.load();
        String contractAddress = config.getContractAddress();

        return contractAddress != null && !contractAddress.isBlank();
    }


    /**
     * Navega desde login a la pantalla de registro de usuario.
     * <p>
     * Carga la vista con el idioma activo y conserva el layout base para no recrear la ventana principal.
     */
    @FXML
    private void irARegistro() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/crearUsuario.fxml")
            );
            loader.setResources(Messages.getBundle());

            Parent loginView = loader.load();

            LayoutController layout = (LayoutController)
                    txtEmail.getScene().getUserData();

            if (layout != null) {
                layout.setContent(loginView);
            }

        } catch (Exception e) {
            AvisosUtil.mostrarError(Messages.getString("logout_error"));
            e.printStackTrace();
        }

    }

    private boolean esAdminEntidadActiva(Usuario usuario) {
        boolean esAdmin = usuario != null
                && usuario.getRoles() != null
                && usuario.getRoles().stream()
                .filter(ur -> ur.getRol() != null)
                .anyMatch(ur -> ADMIN.equalsIgnoreCase(ur.getRol().getNombre()));

        return !esAdmin || (usuario.getEntidadEmisora() != null
                && Boolean.TRUE.equals(usuario.getEntidadEmisora().getActivo()));
    }
}
