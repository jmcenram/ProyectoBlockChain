package es.jmcenram.blockchain.controller.login;

import es.jmcenram.blockchain.controller.LayoutController;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.usuario.UsuarioService;
import es.jmcenram.blockchain.util.MailUtil;
import es.jmcenram.blockchain.util.Messages;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.security.SecureRandom;

import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarError;
import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarInfo;

/**
 * Controlador encargado de activar usuarios creados previamente.
 *
 * Permite al usuario:
 * - Validar credenciales temporales
 * - Establecer una nueva password
 * - Completar el alta de cuenta
 *
 * Interactua con UsuarioService para activar la cuenta y persistir la nueva clave.
 *
 * Forma parte de la capa de presentacion (JavaFX).
 *
 * @author Jcena
 * @version 1.0
 */
public class CrearUsuarioController {

    /**
     * Campo de texto para el email del usuario.
     */
    @FXML
    private TextField txtEmail;

    /**
     * Campo de texto para el nombre del usuario.
     */
    @FXML
    private TextField txtNombre;

    /**
     * Servicio de gestión de usuarios.
     */
    private UsuarioService usuarioService;

    /**
     * Inicializa el controlador creando las instancias necesarias de repositorios y servicios.
     */
    @FXML
    public void initialize() {

        UsuarioRepository usuarioRepo = new UsuarioRepository();
        RolRepository rolRepo = new RolRepository();
        UsuarioRolRepository usuarioRolRepo = new UsuarioRolRepository();

        usuarioService = new UsuarioService(usuarioRepo, rolRepo, usuarioRolRepo);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Valida los datos introducidos, crea el usuario y envía un correo electrónico
     * con la contraseña temporal generada.
     */
    @FXML
    private void registrar() {

        if (!validarCampos()) return;

        final String email = txtEmail.getText().trim();
        final String nombre = txtNombre.getText().trim();

        if (!esEmailValido(email)) {
            mostrarError(Messages.getString("invalid_email"));
            return;
        }


        new Thread(() -> {

            try {

                if (usuarioService.existsByMail(email)) {
                    Platform.runLater(() ->
                            mostrarError(Messages.getString("user_exists"))
                    );
                    return;
                }

                String passwordPlano = generarPassword();

                usuarioService.crearUsuario(
                        nombre,
                        email,
                        passwordPlano,
                        false
                );

                MailUtil.enviarEmailRegistro(email, nombre, passwordPlano);


            } catch (Exception e) {

                e.printStackTrace();

                Platform.runLater(() ->
                        mostrarError(Messages.getString("error_creating_user"))
                );
            }

        }).start();
        mostrarInfo(Messages.getString("user_created_email_sent"));
        volverLogin();
    }

    /**
     * Cancela el proceso de registro y vuelve a la pantalla de login.
     */
    @FXML
    private void cancelarRegistro() {
        volverLogin();
    }

    /**
     * Carga la vista de login y la establece como contenido principal.
     */
    private void volverLogin() {

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

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError(Messages.getString("navigation_error"));
        }
    }

    /**
     * Genera una contraseña temporal para el usuario.
     *
     * @return contraseña temporal
     */
    /**
     * Genera una contraseña aleatoria segura.
     *
     * Características:
     * - Longitud fija de 10 caracteres
     * - Incluye mayúsculas, minúsculas, números y símbolos
     * - Usa {@link SecureRandom} para mayor seguridad
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
     * Valida el formato del email.
     *
     * @param email email a validar
     * @return true si es válido, false en caso contrario
     */
    protected boolean esEmailValido(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Comprueba que los campos obligatorios no estén vacíos.
     *
     * @return true si los campos son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        return !(txtEmail.getText().isBlank() || txtNombre.getText().isBlank());
    }
}