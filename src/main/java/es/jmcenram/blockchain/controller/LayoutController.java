package es.jmcenram.blockchain.controller;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LayoutController {

    @FXML private HBox topBar;
    @FXML private StackPane content;

    @FXML private Button btnCerrar;
    @FXML private Button btnAmpliar;
    @FXML private Button btnMinimizar;

    private final String RED = "red";
    private final String GREY = "grey";

    private double xOffset = 0;
    private double yOffset = 0;

    private boolean maximizado = false;
    private boolean snapActivo = false;

    private double prevX, prevY, prevWidth, prevHeight;

    private double lastMouseX, lastMouseY;

    private Stage previewStage;

    @FXML
    public void initialize() {

        if (topBar != null) {

            topBar.setOnMousePressed(e -> {

                // 🔥 CLAVE: NO interferir con botones
                if (e.getTarget() instanceof javafx.scene.control.Control) return;

                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
                snapActivo = true;
            });

            topBar.setOnMouseDragged(e -> {

                // 🔥 CLAVE
                if (e.getTarget() instanceof javafx.scene.control.Control) return;

                Stage stage = getStage();

                double mouseX = e.getScreenX();
                double mouseY = e.getScreenY();

                lastMouseX = mouseX;
                lastMouseY = mouseY;

                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                double threshold = 10;

                if (maximizado) {
                    maximizado = false;

                    stage.setWidth(prevWidth);
                    stage.setHeight(prevHeight);

                    stage.setX(mouseX - prevWidth / 2);
                    stage.setY(mouseY - yOffset);
                    return;
                }

                if (snapActivo) {
                    if (mouseY <= bounds.getMinY() + threshold) {
                        mostrarPreview(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
                    } else {
                        ocultarPreview();
                    }
                }

                stage.setX(mouseX - xOffset);
                stage.setY(mouseY - yOffset);
            });

            topBar.setOnMouseReleased(e -> {

                // 🔥 CLAVE
                if (e.getTarget() instanceof javafx.scene.control.Control) return;

                if (!snapActivo) return;

                Stage stage = getStage();
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                double threshold = 10;

                ocultarPreview();

                if (lastMouseY <= bounds.getMinY() + threshold) {
                    maximizar();
                    snapActivo = false;
                    return;
                }


                snapActivo = false;
            });

            topBar.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    maximizar();
                }
            });
        }

        if (btnCerrar != null) cargarEstilosHover(btnCerrar, RED);
        if (btnAmpliar != null) cargarEstilosHover(btnAmpliar, GREY);
        if (btnMinimizar != null) cargarEstilosHover(btnMinimizar, GREY);
    }


    public void setContent(Parent node) {
        if (node != null) {
            content.getChildren().setAll(node);
        }
    }

    private Stage getStage() {
        return (Stage) topBar.getScene().getWindow();
    }

    @FXML
    private void cerrar() {
        getStage().close();
    }

    @FXML
    private void minimizar() {
        getStage().setIconified(true);
    }

    @FXML
    private void maximizar() {

        Stage stage = getStage();

        if (!maximizado) {

            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            maximizado = true;

        } else {

            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);

            maximizado = false;
        }
    }

    // 🔥 PREVIEW SIN AZUL (ESTILO GLASS)
    private void mostrarPreview(double x, double y, double w, double h) {

        if (previewStage == null) {

            previewStage = new Stage();
            previewStage.initStyle(StageStyle.TRANSPARENT);
            previewStage.setAlwaysOnTop(true);

            StackPane root = new StackPane();
            root.setStyle("""
                -fx-background-color: rgba(255,255,255,0.2);
                -fx-background-radius: 20;
                -fx-border-color: rgba(255,255,255,0.6);
                -fx-border-radius: 20;
                -fx-border-width: 1;
            """);

            Scene scene = new Scene(root);
            scene.setFill(null);

            previewStage.setScene(scene);
        }

        previewStage.setX(x);
        previewStage.setY(y);
        previewStage.setWidth(w);
        previewStage.setHeight(h);

        previewStage.show();
    }

    private void ocultarPreview() {
        if (previewStage != null) {
            previewStage.hide();
        }
    }

    private void cargarEstilosHover(Button button, String hoverColor) {

        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-text-fill: black;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: transparent;" +
                                "-fx-text-fill: " + hoverColor + ";"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: transparent;" +
                                "-fx-text-fill: black;"
                )
        );
    }
}