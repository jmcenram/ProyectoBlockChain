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
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

/**
 * Controlador del layout principal de la aplicación.
 *
 * Gestiona:
 * - Barra superior personalizada (drag, doble click, botones ventana)
 * - Control de ventana (cerrar, minimizar, maximizar)
 * - Sistema de "snap" tipo Windows (izquierda, derecha, maximizar)
 * - Preview visual al hacer snap
 *
 * Permite además cargar dinámicamente vistas dentro del contenedor principal.
 *
 * Sustituye la decoración nativa del sistema (StageStyle.UNDECORATED)
 * proporcionando comportamiento equivalente de forma manual.
 *
 * @author Jcena
 * @version 1.0
 */
@Setter
@Getter
public class LayoutController {

    @FXML private HBox topBar;
    @FXML private StackPane content;
    @FXML private StackPane root;

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

    private Rectangle clipRectangle;
    private Parent layoutRoot;

    private boolean blockchainActivo = true;



    /**
     * Inicializa el layout principal.
     *
     * Configura:
     * - Drag de ventana desde la barra superior
     * - Snap inteligente multi-pantalla
     * - Doble click para maximizar/restaurar
     * - Eventos de botones de ventana
     *
     * También inicializa estilos hover de los botones.
     */
    @FXML
    public void initialize() {

        if (topBar != null) {

            topBar.setOnMousePressed(e -> {

                if (e.getTarget() instanceof javafx.scene.control.Control) return;

                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
                snapActivo = true;
            });

            topBar.setOnMouseDragged(e -> {

                if (e.getTarget() instanceof javafx.scene.control.Control) return;

                Stage stage = getStage();

                double mouseX = e.getScreenX();
                double mouseY = e.getScreenY();

                lastMouseX = mouseX;
                lastMouseY = mouseY;

                // CLAVE: pantalla correcta según el ratón
                Rectangle2D bounds = getBoundsForMouse(mouseX, mouseY);
                double threshold = 10;

                // salir de maximizado arrastrando
                if (maximizado) {

                    maximizado = false;

                    stage.setWidth(prevWidth);
                    stage.setHeight(prevHeight);

                    stage.setX(mouseX - prevWidth / 2);
                    stage.setY(mouseY - yOffset);

                    if (layoutRoot != null && clipRectangle != null) {
                        layoutRoot.setClip(clipRectangle);
                    }

                    root.getStyleClass().remove("maximized");

                    return;
                }

                // SNAP PREVIEW MULTI-PANTALLA
                if (snapActivo) {

                    if (mouseY <= bounds.getMinY() + threshold) {

                        mostrarPreview(
                                bounds.getMinX(),
                                bounds.getMinY(),
                                bounds.getWidth(),
                                bounds.getHeight()
                        );

                    } else if (mouseX <= bounds.getMinX() + threshold) {

                        mostrarPreview(
                                bounds.getMinX(),
                                bounds.getMinY(),
                                bounds.getWidth() / 2,
                                bounds.getHeight()
                        );

                    } else if (mouseX >= bounds.getMaxX() - threshold) {

                        mostrarPreview(
                                bounds.getMinX() + bounds.getWidth() / 2,
                                bounds.getMinY(),
                                bounds.getWidth() / 2,
                                bounds.getHeight()
                        );

                    } else {
                        ocultarPreview();
                    }
                }

                stage.setX(mouseX - xOffset);
                stage.setY(mouseY - yOffset);
            });

            topBar.setOnMouseReleased(e -> {

                if (e.getTarget() instanceof javafx.scene.control.Control) return;
                if (!snapActivo) return;

                double mouseX = lastMouseX;
                double mouseY = lastMouseY;

                // CLAVE: pantalla correcta
                Rectangle2D bounds = getBoundsForMouse(mouseX, mouseY);
                double threshold = 10;

                ocultarPreview();

                // SNAP REAL MULTI-PANTALLA
                if (mouseY <= bounds.getMinY() + threshold) {
                    maximizar();
                }
                else if (mouseX <= bounds.getMinX() + threshold) {
                    snapIzquierda(bounds);
                }
                else if (mouseX >= bounds.getMaxX() - threshold) {
                    snapDerecha(bounds);
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

    /**
     * Establece el contenido principal dentro del layout.
     *
     * Sustituye completamente el contenido actual del contenedor.
     *
     * @param node vista a mostrar
     */
    public void setContent(Parent node) {
        if (node != null) {
            content.getChildren().setAll(node);
        }
    }

    /**
     * Obtiene el Stage actual de la aplicación.
     *
     * @return ventana principal
     */
    private Stage getStage() {
        return (Stage) topBar.getScene().getWindow();
    }

    /**
     * Cierra la aplicación.
     */
    @FXML
    private void cerrar() {
        getStage().close();
    }

    /**
     * Minimiza la ventana actual.
     */
    @FXML
    private void minimizar() {
        getStage().setIconified(true);
    }

    /**
     * Alterna entre estado maximizado y restaurado.
     *
     * Al maximizar:
     * - Guarda dimensiones previas
     * - Ajusta al tamaño de la pantalla actual
     * - Elimina bordes redondeados (clip)
     *
     * Al restaurar:
     * - Recupera dimensiones anteriores
     * - Restaura estilos originales
     */
    @FXML
    private void maximizar() {

        Stage stage = getStage();

        if (!maximizado) {

            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();

            Rectangle2D bounds = Screen.getScreensForRectangle(
                    stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()
            ).get(0).getVisualBounds();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            // quitar clip → elimina bordes redondeados reales
            if (layoutRoot != null) {
                layoutRoot.setClip(null);
            }

            // aplicar clase CSS
            if (!root.getStyleClass().contains("maximized")) {
                root.getStyleClass().add("maximized");
            }

            maximizado = true;

        } else {

            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);

            // restaurar clip
            if (layoutRoot != null && clipRectangle != null) {
                layoutRoot.setClip(clipRectangle);
            }

            root.getStyleClass().remove("maximized");

            maximizado = false;
        }
    }

    /**
     * Muestra una ventana de previsualización para el snap.
     *
     * Se utiliza durante el arrastre para indicar
     * la posición final de la ventana.
     *
     * @param x coordenada X
     * @param y coordenada Y
     * @param w ancho
     * @param h alto
     */
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

    /**
     * Oculta la ventana de previsualización de snap.
     */
    private void ocultarPreview() {
        if (previewStage != null) {
            previewStage.hide();
        }
    }

    /**
     * Aplica estilos dinámicos hover a un botón.
     *
     * Cambia el color del texto al pasar el ratón.
     *
     * @param button botón a estilizar
     * @param hoverColor color al hacer hover
     */
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

    /**
     * Ajusta la ventana a la mitad izquierda de la pantalla.
     *
     * @param bounds límites de la pantalla actual
     */
    private void snapIzquierda(Rectangle2D bounds) {
        Stage stage = getStage();

        guardarEstadoPrevio(stage);

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth() / 2);
        stage.setHeight(bounds.getHeight());

        aplicarMaximizedStyle(false);
    }

    /**
     * Ajusta la ventana a la mitad derecha de la pantalla.
     *
     * @param bounds límites de la pantalla actual
     */
    private void snapDerecha(Rectangle2D bounds) {
        Stage stage = getStage();

        guardarEstadoPrevio(stage);

        stage.setX(bounds.getMinX() + bounds.getWidth() / 2);
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth() / 2);
        stage.setHeight(bounds.getHeight());

        aplicarMaximizedStyle(false);
    }

    /**
     * Guarda la posición y tamaño actual de la ventana.
     *
     * Se utiliza antes de aplicar snap o maximizar.
     *
     * @param stage ventana actual
     */
    private void guardarEstadoPrevio(Stage stage) {
        prevX = stage.getX();
        prevY = stage.getY();
        prevWidth = stage.getWidth();
        prevHeight = stage.getHeight();
    }

    /**
     * Aplica o elimina la clase CSS de maximizado.
     *
     * @param maximized true para aplicar estilo, false para eliminarlo
     */
    private void aplicarMaximizedStyle(boolean maximized) {
        if (maximized) {
            if (!root.getStyleClass().contains("maximized")) {
                root.getStyleClass().add("maximized");
            }
        } else {
            root.getStyleClass().remove("maximized");
        }
    }

    /**
     * Obtiene la pantalla correspondiente a unas coordenadas.
     *
     * Soporta configuraciones multi-monitor.
     *
     * @param x coordenada X
     * @param y coordenada Y
     * @return pantalla detectada
     */
    private Screen getScreenForPoint(double x, double y) {
        return Screen.getScreensForRectangle(x, y, 1, 1)
                .stream()
                .findFirst()
                .orElse(Screen.getPrimary());
    }

    /**
     * Obtiene los límites de la pantalla donde se encuentra el ratón.
     *
     * @param mouseX posición X del ratón
     * @param mouseY posición Y del ratón
     * @return límites visibles de la pantalla
     */
    private Rectangle2D getBoundsForMouse(double mouseX, double mouseY) {
        return getScreenForPoint(mouseX, mouseY).getVisualBounds();
    }
}