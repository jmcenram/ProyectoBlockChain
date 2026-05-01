package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.controller.utils.AvisosUtil;
import es.jmcenram.blockchain.model.entidademisora.EntidadEmisora;
import es.jmcenram.blockchain.model.rol.ui.RolItem;
import es.jmcenram.blockchain.repository.entidademisora.EntidadEmisoraRepository;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.entidademisora.EntidadEmisoraService;
import es.jmcenram.blockchain.service.usuario.UsuarioService;
import es.jmcenram.blockchain.util.MailUtil;
import es.jmcenram.blockchain.util.Messages;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;

import java.security.SecureRandom;

import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarError;
import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarInfo;

/**
 * Controlador encargado del registro administrativo de usuarios.
 *
 * Permite a usuarios con permisos administrativos:
 * - Crear usuarios
 * - Asignar roles
 * - Asociar entidades emisoras cuando corresponde
 * - Generar passwords temporales
 *
 * Permite crear usuarios USER, ADMIN y MASTER. ADMIN mantiene la obligacion
 * de tener entidad emisora desde el alta; MASTER puede asociarla despues
 * desde su perfil para no bloquear la configuracion inicial del sistema.
 *
 * Interactua con {@link UsuarioService}, repositorios de roles y entidades emisoras para completar el alta.
 *
 * Forma parte de la capa de presentacion (JavaFX).
 *
 * @author Jcena
 * @version 1.0
 */
public class RegistroUsuarioController {

    @FXML private TextField fieldEmail;
    @FXML private TextField fieldNombre;
    @FXML private ComboBox<RolItem> comboRol;
    @FXML private ComboBox<EntidadEmisora> comboEntidadEmisora;

    private static final String USER = "USER";
    private static final String ADMIN = "ADMIN";
    private static final String MASTER = "MASTER";

    private UsuarioService usuarioService;
    private EntidadEmisoraService entidadEmisoraService;

    /**
     * Inicializa el controlador configurando servicios y componentes de la interfaz.
     *
     * <p>Realiza las siguientes acciones:</p>
     * <ul>
     *     <li>Inicializa los repositorios y servicios</li>
     *     <li>Configura los roles disponibles</li>
     *     <li>Carga las entidades emisoras activas</li>
     *     <li>Establece el comportamiento dinámico del selector de roles</li>
     * </ul>
     */
    @FXML
    public void initialize() {

        UsuarioRepository usuarioRepo = new UsuarioRepository();
        RolRepository rolRepo = new RolRepository();
        UsuarioRolRepository usuarioRolRepo = new UsuarioRolRepository();
        EntidadEmisoraRepository entidadEmisoraRepo = new EntidadEmisoraRepository();

        usuarioService = new UsuarioService(usuarioRepo, rolRepo, usuarioRolRepo);
        entidadEmisoraService = new EntidadEmisoraService(entidadEmisoraRepo);

        mostrarInfo(Messages.getString("enter_data_send_email"));

        comboRol.getItems().addAll(
                new RolItem(USER, Messages.getString("role_user")),
                new RolItem(ADMIN, Messages.getString("role_admin")),
                new RolItem(MASTER, Messages.getString("role_master"))
        );

        comboRol.setValue(comboRol.getItems().get(0));

        cargarEntidadesEmisoras();

        comboRol.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                actualizarSelectorEntidad(newVal.getValue());
            }
        });

        actualizarSelectorEntidad(comboRol.getValue().getValue());
    }

    /**
     * Carga las entidades emisoras activas en el ComboBox.
     * Configura la visualización para mostrar el nombre de cada entidad.
     */
    private void cargarEntidadesEmisoras() {

        var entidades = entidadEmisoraService.findAllActivas();
        comboEntidadEmisora.getItems().setAll(entidades);

        comboEntidadEmisora.setCellFactory(lv -> new ListCell<>() {
            /**
             * Refresca una celda del selector de entidades emisoras.
             *
             * Muestra datos legibles de la entidad y limpia la celda cuando JavaFX la reutiliza sin contenido.
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

        comboEntidadEmisora.setButtonCell(new ListCell<>() {
            /**
             * Refresca una celda del selector de entidades emisoras.
             *
             * Muestra datos legibles de la entidad y limpia la celda cuando JavaFX la reutiliza sin contenido.
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
     * Sincroniza el selector de entidad emisora con el rol seleccionado.
     *
     * Evita que USER vea una asociacion que no se guardara y mantiene visible
     * la seleccion solo para roles que pueden operar con entidad emisora.
     *
     * @param rol rol seleccionado en el formulario
     */
    private void actualizarSelectorEntidad(String rol) {
        boolean permiteEntidad = permiteEntidadEmisora(rol);
        comboEntidadEmisora.setDisable(!permiteEntidad);

        if (!permiteEntidad) {
            comboEntidadEmisora.setValue(null);
        }
    }

    /**
     * Valida los campos del formulario de registro.
     *
     * @return true si los campos son válidos, false en caso contrario
     */
    protected boolean validarCampos() {

        String email = fieldEmail.getText();
        String nombre = fieldNombre.getText();

        if (email == null || email.isBlank() ||
                nombre == null || nombre.isBlank()) {

            mostrarError(Messages.getString("fill_all_fields"));
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            mostrarError(Messages.getString("invalid_email"));
            return false;
        }

        if (usuarioService.existsByMail(email)) {
            mostrarError(Messages.getString("user_exists"));
            return false;
        }

        return true;
    }

    /**
     * Genera una contraseña aleatoria segura.
     *
     * @return contraseña generada
     */
    protected String generarPassword() {

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#";
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
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
                    fieldEmail.getScene().getUserData();

            if (layout == null) {
                mostrarError(Messages.getString("navigation_error"));
                return;
            }

            layout.setContent(mainView);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError(Messages.getString("back_error"));
        }
    }

    /**
     * Registra un nuevo usuario y envía sus credenciales por correo electrónico.
     *
     * <p>El proceso se ejecuta en un hilo independiente para evitar bloquear la interfaz.</p>
     */
    @FXML
    public void enviar() {

        if (!validarCampos()) return;

        final String email = fieldEmail.getText().trim();
        final String nombre = fieldNombre.getText().trim();
        final RolItem rolItem = comboRol.getValue();
        final String rol = rolItem.getValue();

        if (requiereEntidadEmisora(rol) && comboEntidadEmisora.getValue() == null) {
            mostrarError(Messages.getString("entity_required_for_role"));
            return;
        }

        mostrarInfo(Messages.getString("creating_user_sending_email"));

        new Thread(() -> {

            try {

                String passwordPlano = generarPassword();

                EntidadEmisora entidad = permiteEntidadEmisora(rol) ? comboEntidadEmisora.getValue() : null;

                usuarioService.crearUsuario(
                        nombre,
                        email,
                        passwordPlano,
                        rol,
                        entidad
                );

                MailUtil.enviarEmailRegistro(email, nombre, passwordPlano);

                Platform.runLater(() ->
                        mostrarInfo(Messages.getString("user_created_email_sent"))
                );

            } catch (Exception e) {

                e.printStackTrace();

                Platform.runLater(() ->
                        mostrarError(Messages.getString("error_creating_user"))
                );
            }

        }).start();
    }

    /**
     * Valida el formato de un email.
     *
     * @param email email a validar
     * @return true si es válido, false en caso contrario
     */
    protected boolean esEmailValido(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Indica si el rol seleccionado puede tener una entidad emisora asociada.
     *
     * ADMIN y MASTER pueden ejecutar operaciones firmadas. MASTER puede quedar
     * sin entidad al crearse para evitar bloquear el arranque inicial del sistema.
     *
     * @param rol rol seleccionado en la interfaz
     * @return true si se permite seleccionar una entidad emisora
     */
    private boolean permiteEntidadEmisora(String rol) {
        return ADMIN.equals(rol) || MASTER.equals(rol);
    }

    /**
     * Indica si el rol seleccionado exige una entidad emisora desde el alta.
     *
     * ADMIN mantiene la regla previa de tener entidad asignada. MASTER puede
     * elegirla despues desde su perfil, donde tambien administra sus emisores.
     *
     * @param rol rol seleccionado en la interfaz
     * @return true si debe seleccionarse una entidad emisora
     */
    private boolean requiereEntidadEmisora(String rol) {
        return ADMIN.equals(rol);
    }
}
