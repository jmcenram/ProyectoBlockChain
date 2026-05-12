package es.jmcenram.blockchain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import es.jmcenram.blockchain.config.BlockchainConfig;
import es.jmcenram.blockchain.config.ConfigManager;
import es.jmcenram.blockchain.model.entidademisora.EntidadEmisora;
import es.jmcenram.blockchain.service.blockchain.BlockchainService;
import es.jmcenram.blockchain.service.entidademisora.EntidadEmisoraService;
import es.jmcenram.blockchain.util.CryptoUtil;
import es.jmcenram.blockchain.util.Messages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import es.jmcenram.blockchain.service.documento.HashService;
import javafx.stage.Stage;
import lombok.val;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.http.HttpService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * Controlador encargado de generar codigos QR de verificacion documental.
 *
 * Permite al usuario:
 * - Seleccionar un archivo local
 * - Calcular su hash
 * - Incorporar metadatos del documento
 * - Generar un QR con datos de blockchain para verificacion
 *
 * Interactua con HashService y con la configuracion blockchain activa.
 *
 * Forma parte de la capa de presentacion (JavaFX).
 *
 * @author Jcena
 * @version 1.0
 */
public class GenerarQRController {

    @FXML
    private Label lblInfoArchivo;

    @FXML
    private Label lblEstado;

    @FXML
    private ImageView qrImageView;

    @FXML
    private TextField txtRutaArchivo;

    private File archivoSeleccionado;

    private final HashService hashService = new HashService();

    private final EntidadEmisoraService entidadService = new EntidadEmisoraService();

    BlockchainConfig config = null;




    /**
     * Inicializa el controlador y sus componentes.
     */
    @FXML
    public void initialize() {
        config = ConfigManager.load();
    }

    /**
     * Abre el selector de archivos y guarda la referencia del documento elegido.
     *
     * La UI muestra el nombre corto y conserva la ruta completa en un tooltip para no saturar el campo de texto.
     */
    @FXML
    private void onSeleccionarArchivo() {

        FileChooser fileChooser = new FileChooser();

        Stage stage = (Stage) txtRutaArchivo.getScene().getWindow();

        archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado == null) return;

        txtRutaArchivo.setText(archivoSeleccionado.getName());

        txtRutaArchivo.setTooltip(new Tooltip(archivoSeleccionado.getAbsolutePath()));

        lblEstado.setText("");
    }

    /**
     * Genera un QR con el hash del archivo y los metadatos necesarios para verificarlo.
     *
     * Incluye datos de blockchain como contrato, RPC y chainId para que la verificacion pueda reproducirse fuera de esta pantalla.
     */
    @FXML
    private void onGenerarQR() {

        if (archivoSeleccionado == null) {
            lblEstado.setText(Messages.getString("select_one"));
            return;
        }

        try {

            // =========================
            // 1. Leer archivo y hash
            // =========================
            byte[] contenido = Files.readAllBytes(archivoSeleccionado.toPath());
            String hash = hashService.generarHash(contenido);

            // =========================
            // 2. Config blockchain
            // =========================
            String contractAddress = config.getContractAddress();
            String rpcUrl = config.getRpcUrl();

            long chainId;
            try {
                Web3j web3j = Web3j.build(new HttpService(rpcUrl));
                chainId = web3j.ethChainId().send().getChainId().longValue();
            } catch (Exception e) {
                chainId = 11155111L;
            }

            // =========================
            // 3. ENTIDADES ULTRA COMPACTAS
            // =========================
            List<EntidadEmisora> entidades = entidadService.findAllActivas();

            StringBuilder entidadesCompact = new StringBuilder();

            for (EntidadEmisora e : entidades) {
                entidadesCompact.append(e.getNombre())
                        .append("|")
                        .append(e.getAddress())
                        .append(";");
            }

            // quitar último ";"
            if (entidadesCompact.length() > 0) {
                entidadesCompact.setLength(entidadesCompact.length() - 1);
            }

            // =========================
            // 4. JSON MINIMIZADO
            // =========================
            Map<String, Object> qrData = new LinkedHashMap<>();

            qrData.put("h", hash); // hash
            qrData.put("s", archivoSeleccionado.length()); // size
            qrData.put("t", System.currentTimeMillis()); // timestamp corto
            qrData.put("c", contractAddress); // contrato
            qrData.put("r", rpcUrl);
            qrData.put("cid", chainId); // chainId
            qrData.put("e", entidadesCompact.toString()); // entidades compactadas

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(qrData);

            // =========================
            // 5. COMPRESION MAXIMA
            // =========================
            String contenidoQR = compress(json);

            // =========================
            // 6. Generar QR
            // =========================
            WritableImage qr = generarQR(contenidoQR);

            qrImageView.setImage(qr);
            lblEstado.setText(Messages.getString("qr_generated"));

        } catch (Exception e) {
            e.printStackTrace();
            lblEstado.setText(Messages.getString("qr_generation_error"));
        }
    }

    /**
     * Genera un QR en formato JavaFX
     */
    private WritableImage generarQR(String contenido) throws Exception {

        int size = 1000;

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, size, size);

        WritableImage image = new WritableImage(size, size);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                image.getPixelWriter().setColor(
                        x,
                        y,
                        matrix.get(x, y) ? Color.BLACK : Color.WHITE
                );
            }
        }

        return image;
    }

    /**
     * Vuelve a la pantalla principal dentro del layout.
     *
     * Este método mantiene la estructura de la aplicación (LayoutController)
     * y sustituye únicamente el contenido central.
     *
     * Proceso:
     * 1. Carga la vista principal (main.fxml)
     * 2. Aplica el ResourceBundle para mantener el idioma
     * 3. Obtiene el LayoutController desde la Scene
     * 4. Inserta la vista en el contenedor principal
     */
    @FXML
    private void volver() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/main.fxml")
            );
            loader.setResources(Messages.getBundle());

            Parent mainView = loader.load();

            LayoutController layout = (LayoutController)
                    qrImageView.getScene().getUserData();

            if (layout == null) {
                lblEstado.setText(Messages.getString("navigation_error"));
                return;
            }

            layout.setContent(mainView);

        } catch (Exception e) {
            e.printStackTrace();
            lblEstado.setText(Messages.getString("back_error"));
        }
    }

    private String compress(String data) throws Exception {
        byte[] input = data.getBytes(StandardCharsets.UTF_8);

        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(input);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length);
        byte[] buffer = new byte[256];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        deflater.end();

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

}
