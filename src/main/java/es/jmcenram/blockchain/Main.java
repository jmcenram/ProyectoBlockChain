package es.jmcenram.blockchain;

import es.jmcenram.blockchain.config.BlockchainConfig;
import es.jmcenram.blockchain.config.ConfigManager;
import es.jmcenram.blockchain.controller.LayoutController;
import es.jmcenram.blockchain.controller.utils.AvisosUtil;
import es.jmcenram.blockchain.service.blockchain.BlockchainService;
import es.jmcenram.blockchain.util.Messages;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static javafx.scene.Cursor.*;

/**
 * Clase principal de la aplicación JavaFX.
 *
 * Se encarga de:
 * - Inicializar la configuración de blockchain
 * - Lanzar la interfaz gráfica principal
 * - Cargar el layout base y la vista inicial (login)
 * - Configurar la escena (CSS, estilos, iconos, redimensionado)
 *
 * Gestiona también el modo de ejecución:
 * - Con blockchain activo (si la configuración es válida)
 * - Sin blockchain (modo degradado)
 *
 * Implementa soporte completo para:
 * - Ventana sin bordes (Stage transparente)
 * - Redimensionado manual
 * - Bordes redondeados
 *
 * @author Jcena
 * @version 1.0
 */
public class Main extends Application {

    /**
     * Configuración global de protocolos TLS para comunicaciones HTTPS.
     *
     * Fuerza el uso de TLS 1.2 en el cliente Java para garantizar compatibilidad
     * con servicios externos como nodos blockchain (Web3j, Alchemy, Infura).
     *
     * Es especialmente necesario en aplicaciones empaquetadas (jpackage),
     * donde la negociación automática de protocolos puede fallar y provocar errores como:
     * "Unable to find acceptable protocols".
     *
     * Esta configuración se aplica de forma estática al iniciar la JVM,
     * antes de cualquier conexión de red.
     */
    static {
        System.setProperty("https.protocols", "TLSv1.2");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
    }

    /** Cursor actual utilizado para el redimensionado de la ventana */
    private Cursor currentCursor = Cursor.DEFAULT;

    /**
     * Método principal de arranque de JavaFX.
     *
     * Inicializa:
     * - Configuración de blockchain
     * - Layout principal
     * - Vista inicial (login)
     * - Estilos globales
     *
     * @param stage ventana principal
     * @throws Exception si ocurre algún error durante la carga de vistas
     */
    @Override
    public void start(Stage stage) throws Exception {

        System.out.println("Inicializando configuración blockchain...");

        BlockchainConfig config = null;
        boolean blockchainActivo = false;

        try {
            config = ConfigManager.load();

            if (config.getRpcUrl() == null || config.getRpcUrl().isBlank()) {
                throw new RuntimeException("RPC no configurado");
            }

            BlockchainService.init(config);

            String address = BlockchainService.getInstance() != null
                    ? BlockchainService.getInstance().getContractAddress()
                    : null;

            if (address != null && !address.isBlank()) {
                config.setContractAddress(address);
            }

            ConfigManager.save(config);

            blockchainActivo = true;

        } catch (Exception e) {
            System.out.println("Error blockchain: " + e.getMessage());
        }

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setMinHeight(600);

        FXMLLoader layoutLoader = new FXMLLoader(
                getClass().getResource("/view/layout.fxml")
        );
        layoutLoader.setResources(Messages.getBundle());

        Parent layoutRoot = layoutLoader.load();
        LayoutController layoutController = layoutLoader.getController();

        layoutController.setLayoutRoot(layoutRoot);
        layoutController.setBlockchainActivo(blockchainActivo);

        FXMLLoader loginLoader = new FXMLLoader(
                getClass().getResource("/view/login.fxml")
        );
        loginLoader.setResources(Messages.getBundle());

        Parent loginView = loginLoader.load();
        layoutController.setContent(loginView);

        Scene scene = new Scene(layoutRoot);
        scene.setFill(null);

        scene.getStylesheets().add(
                getClass().getResource("/css/dark.css").toExternalForm()
        );

        Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);

        clip.widthProperty().bind(scene.widthProperty());
        clip.heightProperty().bind(scene.heightProperty());

        layoutRoot.setClip(clip);
        layoutController.setClipRectangle(clip);

        scene.setUserData(layoutController);

        addResizeSupport(stage, scene);

        stage.setScene(scene);
        stage.setWidth(890);
        stage.setHeight(680);
        stage.setMinWidth(890);
        stage.setMinHeight(680);
        stage.setTitle("BlockchainApp");

        stage.centerOnScreen();

        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/img/icono.png"))
        );

        stage.show();

        if (!blockchainActivo) {
            AvisosUtil.mostrarAlerta(
                    Messages.getString("blockchain_offline_title"),
                    Messages.getString("blockchain_offline_msg")
            );
        }
    }

    /**
     * Añade soporte de redimensionado manual a una ventana sin bordes.
     *
     * Detecta la posición del ratón en los bordes y aplica:
     * - Cambios de cursor
     * - Redimensionado dinámico
     *
     * Soporta:
     * - Bordes laterales
     * - Esquinas
     * - Límites mínimos de tamaño
     *
     * @param stage ventana a modificar
     * @param scene escena asociada
     */
    private void addResizeSupport(Stage stage, Scene scene) {

        final int BORDER = 6;

        final double[] startX = new double[1];
        final double[] startY = new double[1];
        final double[] startW = new double[1];
        final double[] startH = new double[1];
        final double[] startStageX = new double[1];
        final double[] startStageY = new double[1];

        scene.setOnMouseMoved(e -> {

            double x = e.getSceneX();
            double y = e.getSceneY();
            double w = scene.getWidth();
            double h = scene.getHeight();

            if (x < BORDER && y < BORDER) currentCursor = NW_RESIZE;
            else if (x > w - BORDER && y < BORDER) currentCursor = NE_RESIZE;
            else if (x < BORDER && y > h - BORDER) currentCursor = SW_RESIZE;
            else if (x > w - BORDER && y > h - BORDER) currentCursor = SE_RESIZE;
            else if (x < BORDER) currentCursor = W_RESIZE;
            else if (x > w - BORDER) currentCursor = E_RESIZE;
            else if (y < BORDER) currentCursor = N_RESIZE;
            else if (y > h - BORDER) currentCursor = S_RESIZE;
            else currentCursor = Cursor.DEFAULT;

            scene.setCursor(currentCursor);
        });

        scene.setOnMousePressed(e -> {
            startX[0] = e.getScreenX();
            startY[0] = e.getScreenY();
            startW[0] = stage.getWidth();
            startH[0] = stage.getHeight();
            startStageX[0] = stage.getX();
            startStageY[0] = stage.getY();
        });

        scene.setOnMouseDragged(e -> {

            if (currentCursor == Cursor.DEFAULT) return;

            double dx = e.getScreenX() - startX[0];
            double dy = e.getScreenY() - startY[0];

            double minW = 890;
            double minH = 680;

            if (currentCursor.equals(E_RESIZE)) {
                double newW = startW[0] + dx;
                if (newW >= minW) stage.setWidth(newW);

            } else if (currentCursor.equals(W_RESIZE)) {
                double newW = startW[0] - dx;
                if (newW >= minW) {
                    stage.setX(startStageX[0] + dx);
                    stage.setWidth(newW);
                }

            } else if (currentCursor.equals(S_RESIZE)) {
                double newH = startH[0] + dy;
                if (newH >= minH) stage.setHeight(newH);

            } else if (currentCursor.equals(N_RESIZE)) {
                double newH = startH[0] - dy;
                if (newH >= minH) {
                    stage.setY(startStageY[0] + dy);
                    stage.setHeight(newH);
                }

            } else if (currentCursor.equals(SE_RESIZE)) {
                double newW = startW[0] + dx;
                double newH = startH[0] + dy;
                if (newW >= minW) stage.setWidth(newW);
                if (newH >= minH) stage.setHeight(newH);

            } else if (currentCursor.equals(SW_RESIZE)) {
                double newW = startW[0] - dx;
                double newH = startH[0] + dy;
                if (newW >= minW) {
                    stage.setX(startStageX[0] + dx);
                    stage.setWidth(newW);
                }
                if (newH >= minH) stage.setHeight(newH);

            } else if (currentCursor.equals(NE_RESIZE)) {
                double newW = startW[0] + dx;
                double newH = startH[0] - dy;
                if (newW >= minW) stage.setWidth(newW);
                if (newH >= minH) {
                    stage.setY(startStageY[0] + dy);
                    stage.setHeight(newH);
                }

            } else if (currentCursor.equals(NW_RESIZE)) {
                double newW = startW[0] - dx;
                double newH = startH[0] - dy;
                if (newW >= minW) {
                    stage.setX(startStageX[0] + dx);
                    stage.setWidth(newW);
                }
                if (newH >= minH) {
                    stage.setY(startStageY[0] + dy);
                    stage.setHeight(newH);
                }
            }
        });
    }

    /**
     * Punto de entrada estándar de la aplicación.
     *
     * @param args argumentos de ejecución
     */
    public static void main(String[] args) {
        System.setProperty("prism.order", "sw");
        launch();
    }
}