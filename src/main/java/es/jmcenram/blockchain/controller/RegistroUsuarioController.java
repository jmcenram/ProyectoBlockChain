package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.usuario.UsuarioService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.security.SecureRandom;
import java.util.Properties;

import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarError;
import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarInfo;

public class RegistroUsuarioController {

    @FXML private TextField fieldEmail;
    @FXML private TextField fieldNombre;


    private UsuarioService usuarioService;

    // =========================
    // 🔥 INIT
    // =========================
    @FXML
    public void initialize() {
        UsuarioRepository usuarioRepo = new UsuarioRepository();
        RolRepository rolRepo = new RolRepository();
        UsuarioRolRepository usuarioRolRepo = new UsuarioRolRepository();

        usuarioService = new UsuarioService(usuarioRepo, rolRepo, usuarioRolRepo);

        Platform.runLater(() -> {
            Stage stage = (Stage) fieldEmail.getScene().getWindow();

            stage.setWidth(540);
            stage.setHeight(570);
            stage.setResizable(false);
            stage.centerOnScreen();
        });

        mostrarInfo("Introduce los datos y envía el correo");
    }

    // =========================
    // ✅ VALIDACIÓN
    // =========================
    private boolean validarCampos() {

        String email = fieldEmail.getText();
        String nombre = fieldNombre.getText();

        if (email == null || email.isBlank() ||
                nombre == null || nombre.isBlank()) {

            mostrarError("❌ Rellena todos los campos");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            mostrarError("❌ Email no válido");
            return false;
        }

        if (usuarioService.existsByMail(email)) {
            mostrarError("❌ Ya existe un usuario con ese email");
            return false;
        }

        return true;
    }

    // =========================
    // 🔐 GENERAR PASSWORD
    // =========================
    private String generarPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#";
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    // =========================
    // 🔙 VOLVER
    // =========================
    @FXML
    public void volver() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/main.fxml")
            );

            Parent mainView = loader.load();

            LayoutController layout = (LayoutController)
                    fieldEmail.getScene().getUserData();

            if (layout == null) {
                mostrarError("Error de navegación");
                return;
            }

            Stage stage = (Stage) fieldEmail.getScene().getWindow();

            stage.setWidth(600);
            stage.setHeight(550);
            stage.centerOnScreen();

            layout.setContent(mainView);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al volver");
        }
    }

    // =========================
    // 📧 ENVIAR + REGISTRAR
    // =========================
    @FXML
    public void enviar() {

        if (!validarCampos()) return;

        String email = fieldEmail.getText();
        String nombre = fieldNombre.getText();

        mostrarInfo("Creando usuario y enviando correo...");

        new Thread(() -> {

            try {

                // 🔐 generar password
                String passwordPlano = generarPassword();

                // 👤 crear usuario
                Usuario usuario = usuarioService.crearUsuario(
                        nombre,
                        email,
                        passwordPlano,
                        false
                );

                // =========================
                // 📧 EMAIL
                // =========================
                final String remitente = "registroblockchain@gmail.com";
                final String pass = "ayez ddsw ypln kciv";

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                Session session = Session.getInstance(props,
                        new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(remitente, pass);
                            }
                        }
                );

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(remitente));
                message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(email)
                );

                message.setSubject("🔐 Acceso a plataforma Blockchain");

                message.setText(
                        "Hola " + nombre + ",\n\n" +
                                "Tu cuenta ha sido creada correctamente.\n\n" +
                                "🔑 CONTRASEÑA: " + passwordPlano + "\n\n" +
                                "⚠️ Debes cambiarla al iniciar sesión.\n\n" +
                                "Un saludo."
                );

                Transport.send(message);

                Platform.runLater(() ->
                        mostrarInfo("✅ Usuario creado y correo enviado")
                );

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() ->
                        mostrarError("❌ Error en registro o envío")
                );
            }

        }).start();
    }
}