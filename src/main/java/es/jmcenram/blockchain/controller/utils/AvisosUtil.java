package es.jmcenram.blockchain.controller.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

public abstract class AvisosUtil {

    private static void aplicarEstilo(Alert alert) {

        // 🔥 ventana sin decoración (clave para esquinas reales)
        alert.initStyle(StageStyle.TRANSPARENT);

        DialogPane pane = alert.getDialogPane();

        // 🔥 aplicar CSS
        pane.getStylesheets().add(
                AvisosUtil.class.getResource("/css/dark.css").toExternalForm()
        );

        pane.getStyleClass().add("dialog-dark");

        // 🔥 eliminar fondo blanco interno de JavaFX
        pane.setBackground(null);

        // 🔥 hacer transparente la escena (cuando exista)
        pane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {

                newScene.setFill(Color.TRANSPARENT);

                // 🔥 AQUÍ está el fix real del clip (cuando ya hay layout)
                pane.applyCss();
                pane.layout();

                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
                clip.setArcWidth(24);
                clip.setArcHeight(24);

                // bind dinámico (IMPORTANTE)
                clip.widthProperty().bind(pane.widthProperty());
                clip.heightProperty().bind(pane.heightProperty());

                pane.setClip(clip);
            }
        });

        // 🔥 tamaño
        pane.setPrefWidth(420);
    }

    public static void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        aplicarEstilo(alert);
        alert.showAndWait();
    }

    public static void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        aplicarEstilo(alert);
        alert.showAndWait();
    }

    public static void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        aplicarEstilo(alert);
        alert.showAndWait();
    }

    public static boolean confirmarAccion(String titulo, String mensaje) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(titulo);
        confirm.setHeaderText(null);
        confirm.setContentText(mensaje);

        aplicarEstilo(confirm);

        return confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}