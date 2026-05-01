package es.jmcenram.blockchain.controller.utils;

import es.jmcenram.blockchain.util.Messages;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.*;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Utilidad encargada de mostrar avisos y dialogos consistentes en la interfaz.
 *
 * Permite:
 * - Mostrar errores
 * - Mostrar informacion
 * - Solicitar confirmaciones
 * - Aplicar estilos comunes a las alertas
 *
 * Centraliza la mensajeria visual para que los controladores no dupliquen codigo JavaFX.
 *
 * @author Jcena
 * @version 1.0
 */
public abstract class AvisosUtil {

    /**
     * Obtiene la ventana principal actualmente visible.
     * Esta ventana se utiliza como propietaria (owner) de los diálogos modales
     * para asegurar que aparezcan centrados en la ventana principal.
     *
     * @return la ventana activa o null si no hay ventanas visibles
     */
    private static Window getOwner() {
        return Window.getWindows()
                .stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }

    /**
     * Método privado que genera y muestra un diálogo modal personalizado.
     * Crea un diálogo transparente con overlay, icono, título, mensaje y botón de aceptación.
     *
     * @param titulo   el título del diálogo
     * @param mensaje  el mensaje a mostrar
     * @param tipo     el tipo de diálogo: "info", "error" o "warning"
     */
    private static void mostrar(String titulo, String mensaje, String tipo) {

        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Window owner = getOwner();
            stage.initOwner(owner);

            // OVERLAY - Capa semitransparente que oscurece el fondo
            StackPane root = new StackPane();
            root.getStyleClass().add("overlay");

            // CARD - Contenedor principal del diálogo
            VBox card = new VBox(18);
            card.setAlignment(Pos.CENTER);
            card.getStyleClass().add("dialog-card");

            // TAMAÑO FIJO REAL
            card.setMinSize(300, 200);
            card.setPrefSize(300, 200);
            card.setMaxSize(300, 300);

            // ICONO - Se selecciona según el tipo de diálogo
            FontIcon icon = new FontIcon();

            switch (tipo) {
                case "info" -> {
                    icon.setIconLiteral("fas-info-circle");
                    icon.setIconColor(Color.web("#38bdf8"));
                }
                case "error" -> {
                    icon.setIconLiteral("fas-times-circle");
                    icon.setIconColor(Color.web("#ef4444"));
                }
                case "warning" -> {
                    icon.setIconLiteral("fas-exclamation-triangle");
                    icon.setIconColor(Color.web("#f59e0b"));
                }
            }

            icon.setIconSize(28);

            // TITULO
            Label title = new Label(titulo);
            title.getStyleClass().add("dialog-title");

            // TEXTO - Mensaje principal con ajuste de ancho
            Label text = new Label(mensaje.replace("\\n", "\n"));
            text.getStyleClass().add("dialog-text");
            text.setWrapText(true);
            text.setMaxWidth(260);

            // BOTÓN - Botón de aceptación para cerrar el diálogo
            Button btn = new Button(Messages.getString("accept"));
            btn.getStyleClass().add("btn-primary");
            btn.setPrefWidth(110);
            btn.setOnAction(e -> stage.close());

            // MONTAJE - Se añaden todos los componentes al card
            card.getChildren().addAll(icon, title, text, btn);
            root.getChildren().add(card);

            // SCENE FULL - Escena que ocupa toda la ventana principal
            Scene scene = new Scene(root, owner.getWidth(), owner.getHeight());
            scene.setFill(Color.TRANSPARENT);

            scene.getStylesheets().add(
                    AvisosUtil.class.getResource("/css/dark.css").toExternalForm()
            );

            // ESC - Permitir cerrar el diálogo presionando ESC
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    stage.close();
                }
            });

            // CLICK FUERA - Permitir cerrar el diálogo haciendo clic fuera del card
            root.setOnMouseClicked(e -> stage.close());
            // Evitar que los clics dentro del card cierren el diálogo
            card.setOnMouseClicked(e -> e.consume());

            // POSICIÓN - Diálogo se alinea con la ventana propietaria
            stage.setScene(scene);
            stage.setX(owner.getX());
            stage.setY(owner.getY());

            stage.showAndWait();
        });
    }

    /**
     * Muestra un diálogo informativo con icono azul.
     *
     * @param mensaje el mensaje a mostrar
     */
    public static void mostrarInfo(String mensaje) {
        mostrar(Messages.getString("information"), mensaje, "info");
    }

    /**
     * Muestra un diálogo de error con icono rojo.
     *
     * @param mensaje el mensaje de error a mostrar
     */
    public static void mostrarError(String mensaje) {
        mostrar(Messages.getString("error"), mensaje, "error");
    }

    /**
     * Muestra un diálogo de alerta/advertencia con icono amarillo.
     *
     * @param titulo   el título del diálogo
     * @param mensaje  el mensaje de alerta a mostrar
     */
    public static void mostrarAlerta(String titulo, String mensaje) {
        mostrar(titulo, mensaje, "warning");
    }

    /**
     * Muestra un diálogo de confirmación modal con dos botones (Cancelar y Aceptar).
     * Bloquea la ejecución hasta que el usuario responda.
     *
     * @param titulo   el título del diálogo de confirmación
     * @param mensaje  el mensaje descriptivo de la acción a confirmar
     * @return true si el usuario presiona Aceptar, false si presiona Cancelar o cierra el diálogo
     */
    public static boolean confirmarAccion(String titulo, String mensaje) {

        final boolean[] resultado = {false};

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initOwner(getOwner());

        // OVERLAY - Capa semitransparente oscura
        StackPane root = new StackPane();
        root.getStyleClass().add("overlay");
        root.setPrefSize(9999, 9999);

        // CARD - Contenedor del diálogo
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("dialog-card");

        // TAMAÑO FIJO REAL
        card.setMinSize(300, 200);
        card.setPrefSize(300, 200);
        card.setMaxSize(300, 300);

        // ICONO - Icono de exclamación en amarillo para advertencia
        FontIcon icon = new FontIcon("fas-exclamation-triangle");
        icon.setIconSize(28);
        icon.setIconColor(Color.web("#f59e0b"));

        // TITULO
        Label title = new Label(titulo);
        title.getStyleClass().add("dialog-title");

        // TEXTO - Mensaje con ajuste de ancho
        Label text = new Label(mensaje.replace("\\n", "\n"));
        text.getStyleClass().add("dialog-text");
        text.setWrapText(true);
        text.setMaxWidth(260);
        text.setAlignment(Pos.CENTER);

        // BOTONES - Contenedor horizontal con botones Cancelar y Aceptar
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);

        Button btnCancelar = new Button(Messages.getString("cancel"));
        btnCancelar.getStyleClass().add("btn-secondary");

        Button btnAceptar = new Button(Messages.getString("accept"));
        btnAceptar.getStyleClass().add("btn-primary");

        // ACCIONES DE BOTONES
        btnCancelar.setOnAction(e -> {
            resultado[0] = false;
            stage.close();
        });

        btnAceptar.setOnAction(e -> {
            resultado[0] = true;
            stage.close();
        });

        botones.getChildren().addAll(btnCancelar, btnAceptar);

        // MONTAJE - Se añaden los componentes al card
        card.getChildren().addAll(icon, title, text, botones);
        root.getChildren().add(card);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        scene.getStylesheets().add(
                AvisosUtil.class.getResource("/css/dark.css").toExternalForm()
        );

        // ESC - Permitir cerrar sin responder presionando ESC
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> stage.close();
            }
        });

        // CLICK FUERA - Permitir cerrar haciendo clic fuera del card
        root.setOnMouseClicked(e -> stage.close());
        // Evitar que los clics dentro del card cierren el diálogo
        card.setOnMouseClicked(e -> e.consume());

        stage.setScene(scene);
        stage.setWidth(420);
        stage.setHeight(300);
        stage.centerOnScreen();

        stage.showAndWait();

        return resultado[0];
    }

    /**
     * Muestra un diálogo modal solicitando contraseña al usuario.
     *
     * Mantiene el mismo estilo visual que el resto de avisos de la aplicación.
     *
     * @param titulo  título del diálogo
     * @param mensaje mensaje descriptivo
     * @return contraseña introducida o null si cancela
     */
    public static String pedirPassword(String titulo, String mensaje) {

        final String[] resultado = {null};

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initOwner(getOwner());

        // OVERLAY
        StackPane root = new StackPane();
        root.getStyleClass().add("overlay");
        root.setPrefSize(9999, 9999);

        // CARD
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("dialog-card");

        card.setMinSize(300, 220);
        card.setPrefSize(300, 220);
        card.setMaxSize(320, 320);

        // ICONO 🔐
        FontIcon icon = new FontIcon("fas-lock");
        icon.setIconSize(28);
        icon.setIconColor(Color.web("#38bdf8"));

        // TITULO
        Label title = new Label(titulo);
        title.getStyleClass().add("dialog-title");

        // TEXTO
        Label text = new Label(mensaje.replace("\\n", "\n"));
        text.getStyleClass().add("dialog-text");
        text.setWrapText(true);
        text.setMaxWidth(260);
        text.setAlignment(Pos.CENTER);

        // PASSWORD FIELD
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(Messages.getString("password"));
        passwordField.setMaxWidth(200);
        passwordField.setMaxHeight(42);
        passwordField.getStyleClass().add("input");

        // BOTONES
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);

        Button btnCancelar = new Button(Messages.getString("cancel"));
        btnCancelar.getStyleClass().add("btn-secondary");

        Button btnAceptar = new Button(Messages.getString("accept"));
        btnAceptar.getStyleClass().add("btn-primary");

        btnCancelar.setOnAction(e -> {
            resultado[0] = null;
            stage.close();
        });

        btnAceptar.setOnAction(e -> {
            resultado[0] = passwordField.getText();
            stage.close();
        });

        botones.getChildren().addAll(btnCancelar, btnAceptar);

        // MONTAJE
        card.getChildren().addAll(icon, title, text, passwordField, botones);
        root.getChildren().add(card);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        scene.getStylesheets().add(
                AvisosUtil.class.getResource("/css/dark.css").toExternalForm()
        );

        // ESC
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                stage.close();
            }
        });

        // CLICK FUERA
        root.setOnMouseClicked(e -> stage.close());
        card.setOnMouseClicked(e -> e.consume());

        stage.setScene(scene);
        stage.setWidth(420);
        stage.setHeight(320);
        stage.centerOnScreen();

        Platform.runLater(passwordField::requestFocus);

        stage.showAndWait();

        return resultado[0];
    }
}