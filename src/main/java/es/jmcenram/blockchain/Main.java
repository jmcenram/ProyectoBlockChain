package es.jmcenram.blockchain;

import es.jmcenram.blockchain.config.BlockchainConfig;
import es.jmcenram.blockchain.config.demo.GanacheStarter;
import es.jmcenram.blockchain.service.blockchain.BlockchainService;
import es.jmcenram.blockchain.controller.LayoutController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        System.out.println("🔗 Inicializando nodo blockchain...");
        GanacheStarter.startIfNotRunning();
        System.out.println("✅ Nodo listo");

        // =========================
        // 🔗 CONFIG BLOCKCHAIN
        // =========================
        BlockchainConfig config = new BlockchainConfig();
        config.setRpcUrl("http://127.0.0.1:8545");
        config.setPrivateKey("0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80");
        config.setContractAddress("");

        BlockchainService.init(config);
        System.out.println("✅ BlockchainService inicializado");

        // =========================
        // 🔥 VENTANA TRANSPARENTE
        // =========================
        stage.initStyle(StageStyle.TRANSPARENT);

        // =========================
        // 🎨 LAYOUT
        // =========================
        FXMLLoader layoutLoader = new FXMLLoader(
                getClass().getResource("/view/layout.fxml")
        );

        Parent layoutRoot = layoutLoader.load();
        LayoutController layoutController = layoutLoader.getController();

        Parent loginView = FXMLLoader.load(
                getClass().getResource("/view/login.fxml")
        );

        layoutController.setContent(loginView);

        // =========================
        // 🖥️ ESCENA
        // =========================
        Scene scene = new Scene(layoutRoot);

        // 🔥 IMPORTANTE: evitar artefactos
        scene.setFill(null);

        // =========================
        // 🔥 CLIP PARA BORDES REDONDEADOS (SOLUCIÓN)
        // =========================
        Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);

        clip.widthProperty().bind(scene.widthProperty());
        clip.heightProperty().bind(scene.heightProperty());

        layoutRoot.setClip(clip);

        // 🔥 navegación global
        scene.setUserData(layoutController);

        // 🔥 RESIZE
        addResizeSupport(stage, scene);

        // =========================
        // 🪟 CONFIG VENTANA
        // =========================
        stage.setScene(scene);

        stage.setWidth(600);
        stage.setHeight(600);

        stage.setMinWidth(400);
        stage.setMinHeight(600);

        stage.centerOnScreen();

        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/img/icono.png"))
        );

        stage.setOnCloseRequest(event ->
                System.out.println("🛑 Cerrando aplicación...")
        );

        stage.show();
    }

    // =========================
    // 🔥 RESIZE MANUAL
    // =========================
    private void addResizeSupport(Stage stage, Scene scene) {

        final int BORDER = 6;

        scene.setOnMouseMoved(e -> {

            double x = e.getSceneX();
            double y = e.getSceneY();

            double width = scene.getWidth();
            double height = scene.getHeight();

            if (x < BORDER && y < BORDER) {
                scene.setCursor(Cursor.NW_RESIZE);
            } else if (x > width - BORDER && y < BORDER) {
                scene.setCursor(Cursor.NE_RESIZE);
            } else if (x < BORDER && y > height - BORDER) {
                scene.setCursor(Cursor.SW_RESIZE);
            } else if (x > width - BORDER && y > height - BORDER) {
                scene.setCursor(Cursor.SE_RESIZE);
            } else if (x < BORDER) {
                scene.setCursor(Cursor.W_RESIZE);
            } else if (x > width - BORDER) {
                scene.setCursor(Cursor.E_RESIZE);
            } else if (y > height - BORDER) {
                scene.setCursor(Cursor.S_RESIZE);
            } else {
                scene.setCursor(Cursor.DEFAULT);
            }
        });

        scene.setOnMouseDragged(e -> {

            if (scene.getCursor() == Cursor.DEFAULT) return;

            double x = e.getScreenX();
            double y = e.getScreenY();

            double minW = 500;
            double minH = 500;

            if (scene.getCursor() == Cursor.E_RESIZE) {
                double newWidth = x - stage.getX();
                if (newWidth >= minW) stage.setWidth(newWidth);
            } else if (scene.getCursor() == Cursor.W_RESIZE) {
                double newWidth = stage.getX() - x + stage.getWidth();
                if (newWidth >= minW) {
                    stage.setX(x);
                    stage.setWidth(newWidth);
                }
            } else if (scene.getCursor() == Cursor.S_RESIZE) {
                double newHeight = y - stage.getY();
                if (newHeight >= minH) stage.setHeight(newHeight);
            } else if (scene.getCursor() == Cursor.SE_RESIZE) {
                double newWidth = x - stage.getX();
                double newHeight = y - stage.getY();
                if (newWidth >= minW) stage.setWidth(newWidth);
                if (newHeight >= minH) stage.setHeight(newHeight);
            } else if (scene.getCursor() == Cursor.SW_RESIZE) {
                double newWidth = stage.getX() - x + stage.getWidth();
                double newHeight = y - stage.getY();
                if (newWidth >= minW) {
                    stage.setX(x);
                    stage.setWidth(newWidth);
                }
                if (newHeight >= minH) stage.setHeight(newHeight);
            } else if (scene.getCursor() == Cursor.NE_RESIZE) {
                double newWidth = x - stage.getX();
                double newHeight = stage.getY() - y + stage.getHeight();
                if (newWidth >= minW) stage.setWidth(newWidth);
                if (newHeight >= minH) {
                    stage.setY(y);
                    stage.setHeight(newHeight);
                }
            } else if (scene.getCursor() == Cursor.NW_RESIZE) {
                double newWidth = stage.getX() - x + stage.getWidth();
                double newHeight = stage.getY() - y + stage.getHeight();
                if (newWidth >= minW) {
                    stage.setX(x);
                    stage.setWidth(newWidth);
                }
                if (newHeight >= minH) {
                    stage.setY(y);
                    stage.setHeight(newHeight);
                }
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}