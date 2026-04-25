package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.config.BlockchainConfig;
import es.jmcenram.blockchain.config.ConfigManager;
import es.jmcenram.blockchain.service.blockchain.BlockchainService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ConfiguracionController {

    @FXML private TextField rpcUrlField;
    @FXML private TextField privateKeyField;
    @FXML private TextField contractAddressField;
    @FXML private TextField rutaConfigField;
    @FXML private Label lblEstado;

    private File ultimaRuta;

    // =========================
    // 🔥 INIT
    // =========================
    @FXML
    public void initialize() {

        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) rpcUrlField.getScene().getWindow();

            stage.setWidth(540);
            stage.setHeight(570);
            stage.setResizable(false);
            stage.centerOnScreen();
        });

        mostrarInfo("Introduce configuración o carga un fichero");
    }

    // =========================
    // 🔄 CARGAR CONFIG
    // =========================
    @FXML
    public void cargarConfig() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Cargar configuración");

            File file = fileChooser.showOpenDialog(rpcUrlField.getScene().getWindow());
            if (file == null) return;

            ultimaRuta = file;

            BlockchainConfig config = ConfigManager.load(file);

            rpcUrlField.setText(config.getRpcUrl());
            privateKeyField.setText(config.getPrivateKey());
            contractAddressField.setText(config.getContractAddress());
            rutaConfigField.setText(file.getAbsolutePath());

            mostrarInfo("⏳ Inicializando blockchain...");

            BlockchainService.init(config);

            String address = BlockchainService.getInstance().getContractAddress();

            if (address != null && !address.isEmpty()) {
                contractAddressField.setText(address);
            }

            mostrarInfo("✅ Configuración cargada y blockchain inicializado");

        } catch (Exception e) {
            mostrarError("❌ Error cargando: " + e.getMessage());
        }
    }

    // =========================
    // 💾 GUARDAR CONFIG
    // =========================
    @FXML
    public void guardar() {

        if (!validarCampos()) return;

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar configuración");
            fileChooser.setInitialFileName("blockchain.properties");

            File file = fileChooser.showSaveDialog(rpcUrlField.getScene().getWindow());
            if (file == null) return;

            ultimaRuta = file;

            BlockchainConfig config = new BlockchainConfig();
            config.setRpcUrl(rpcUrlField.getText());
            config.setPrivateKey(privateKeyField.getText());
            config.setContractAddress(contractAddressField.getText());

            BlockchainService.init(config);

            String address = BlockchainService.getInstance().getContractAddress();

            if (address != null && !address.isEmpty()) {
                config.setContractAddress(address);
                contractAddressField.setText(address);
            }

            ConfigManager.save(config, file);

            rutaConfigField.setText(file.getAbsolutePath());

            mostrarInfo("✅ Configuración guardada");

        } catch (Exception e) {
            mostrarError("❌ Error: " + e.getMessage());
        }
    }

    // =========================
    // ✅ VALIDACIÓN (CSS PRO)
    // =========================
    private boolean validarCampos() {

        boolean valido = true;

        limpiarErrores();

        if (rpcUrlField.getText() == null || rpcUrlField.getText().isBlank()) {
            marcarError(rpcUrlField);
            valido = false;
        }

        if (privateKeyField.getText() == null || privateKeyField.getText().isBlank()) {
            marcarError(privateKeyField);
            valido = false;
        }

        if (contractAddressField.getText() == null || contractAddressField.getText().isBlank()) {
            marcarError(contractAddressField);
            valido = false;
        }

        if (!valido) {
            mostrarError("❌ Rellena todos los campos obligatorios");
        }

        return valido;
    }

    private void marcarError(TextField field) {
        field.getStyleClass().add("input-error");
    }

    private void limpiarErrores() {
        rpcUrlField.getStyleClass().remove("input-error");
        privateKeyField.getStyleClass().remove("input-error");
        contractAddressField.getStyleClass().remove("input-error");
    }

    // =========================
    // 🧠 MENSAJES (CSS)
    // =========================
    private void mostrarInfo(String msg) {
        lblEstado.setText(msg);
        lblEstado.getStyleClass().removeAll("text-error");
        lblEstado.getStyleClass().add("text-success");
    }

    private void mostrarError(String msg) {
        lblEstado.setText(msg);
        lblEstado.getStyleClass().removeAll("text-success");
        lblEstado.getStyleClass().add("text-error");
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
                    rpcUrlField.getScene().getUserData();

            if (layout == null) {
                mostrarError("Error de navegación");
                return;
            }

            Stage stage = (Stage) rpcUrlField.getScene().getWindow();

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
    // 🚀 GENERAR CONTRATO
    // =========================
    @FXML
    public void generarContrato() {

        try {
            limpiarErrores();

            boolean error = false;

            if (rpcUrlField.getText() == null || rpcUrlField.getText().isBlank()) {
                marcarError(rpcUrlField);
                error = true;
            }

            if (privateKeyField.getText() == null || privateKeyField.getText().isBlank()) {
                marcarError(privateKeyField);
                error = true;
            }

            if (error) {
                mostrarError("❌ RPC y Private Key son obligatorios");
                return;
            }

            BlockchainConfig config = new BlockchainConfig();
            config.setRpcUrl(rpcUrlField.getText());
            config.setPrivateKey(privateKeyField.getText());
            config.setContractAddress("");

            mostrarInfo("⏳ Desplegando contrato...");

            BlockchainService.init(config);

            String address = BlockchainService.getInstance().getContractAddress();

            contractAddressField.setText(address);

            mostrarInfo("✅ Contrato desplegado");

        } catch (Exception e) {
            mostrarError("❌ Error generando contrato: " + e.getMessage());
        }
    }
}