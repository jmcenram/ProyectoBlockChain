package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.controller.login.Session;
import es.jmcenram.blockchain.controller.utils.AvisosUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;

public class MainController {

    @FXML
    private Label lblUsuario;

    @FXML
    public void initialize() {

        // 🔐 Protección de sesión
        if (Session.getUsuario() == null) {
            javafx.application.Platform.runLater(this::redirigirLogin);
            return;
        }

        // 👤 Mostrar usuario
        lblUsuario.setText(Session.getUsuario().getNombre());
    }

    // =========================
    // NAVEGACIÓN
    // =========================
    private void cambiarPantalla(String fxml) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/" + fxml)
            );

            Parent vista = loader.load();

            // 🔥 obtener layout global
            LayoutController layout = (LayoutController)
                    lblUsuario.getScene().getUserData();

            if (layout == null) {
                AvisosUtil.mostrarError("Error de navegación");
                return;
            }

            // 🔁 cambiar contenido
            layout.setContent(vista);

        } catch (Exception e) {
            AvisosUtil.mostrarError("Error cargando pantalla");
            e.printStackTrace();
        }
    }

    @FXML
    public void irADocumentos() {
        cambiarPantalla("documentos.fxml");
    }

    @FXML
    public void irAConfiguracion() {
        cambiarPantalla("configuracion.fxml");
    }

    @FXML
    public void irARegistroUsuario() {
        cambiarPantalla("registroUsuario.fxml");
    }

    // =========================
    // LOGOUT
    // =========================
    @FXML
    private void logout() {
        try {

            Session.logout();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/login.fxml")
            );

            Parent loginView = loader.load();

            LayoutController layout = (LayoutController)
                    lblUsuario.getScene().getUserData();

            if (layout != null) {
                layout.setContent(loginView);
            }

        } catch (Exception e) {
            AvisosUtil.mostrarError("Error cerrando sesión");
            e.printStackTrace();
        }
    }

    // =========================
    // REDIRECCIÓN LOGIN
    // =========================
    private void redirigirLogin() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/login.fxml")
            );

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
}