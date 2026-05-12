package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.config.BlockchainConfig;
import es.jmcenram.blockchain.config.ConfigManager;
import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.model.registroblockchain.EstadoBlockchain;
import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.repository.auditoria.AuditoriaRepository;
import es.jmcenram.blockchain.repository.documento.DocumentoRepository;
import es.jmcenram.blockchain.repository.registroblockchain.RegistroBlockchainRepository;
import es.jmcenram.blockchain.service.blockchain.BlockchainService;
import es.jmcenram.blockchain.service.documento.DocumentoService;
import es.jmcenram.blockchain.service.documento.HashService;
import es.jmcenram.blockchain.util.Messages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controlador JavaFX para la pantalla de verificación de documentos en blockchain.
 * Permite a los usuarios seleccionar un archivo y validar su integridad y estado en blockchain.
 *
 * Funcionalidades principales:
 * - Selección de archivos mediante FileChooser
 * - Generación de hash SHA-256 del archivo seleccionado
 * - Búsqueda del documento en la base de datos
 * - Validación del registro en blockchain
 * - Verificación del estado del documento (válido, revocado, no registrado)
 *
 * Interacción con vistas FXML:
 * - verificacion.fxml: formulario de verificación
 * - main.fxml: pantalla principal tras completar verificación
 *
 * @author Jcena
 * @version 1.0
 */
public class VerificacionController {

    @FXML
    private Label lblArchivo;
    @FXML private Label lblResultado;

    private File archivoSeleccionado;

    private final HashService hashService = new HashService();
    private final DocumentoService documentoService = new DocumentoService(
            new DocumentoRepository(),
            new RegistroBlockchainRepository(),
            new AuditoriaRepository(),
            BlockchainService.getInstance()
    );

    /**
     * Abre diálogo de selección de archivos para que el usuario elija un documento.
     * Almacena el archivo seleccionado y muestra su nombre en el label correspondiente.
     *
     * Proceso:
     * 1. Crea una instancia de FileChooser
     * 2. Abre el diálogo en la ventana actual
     * 3. Si el usuario selecciona un archivo (no cancela):
     *    - Almacena la referencia del archivo en archivoSeleccionado
     *    - Muestra el nombre del archivo en lblArchivo
     */
    @FXML
    private void seleccionarArchivo() {
        FileChooser fc = new FileChooser();
        archivoSeleccionado = fc.showOpenDialog(null);

        if (archivoSeleccionado != null) {
            lblArchivo.setText(archivoSeleccionado.getName());
        }
    }

    /**
     * Valida la integridad y estado del archivo seleccionado en blockchain.
     *
     * Proceso de validación:
     * 1. Verifica que un archivo esté seleccionado
     * 2. Genera hash SHA-256 del archivo
     * 3. Busca el documento en la base de datos por hash
     * 4. Obtiene la dirección del contrato desde configuración
     * 5. Busca el registro blockchain más reciente del documento
     * 6. Verifica el estado del documento (válido, revocado, etc.)
     * 7. Muestra el resultado en lblResultado
     *
      * Estados posibles:
      * - Documento válido: encontrado en blockchain y estado activo
      * - Documento revocado: encontrado pero marcado como REVOCADO
      * - No registrado en el sistema: hash no encontrado en BD
      * - No registrado en blockchain: no hay registro para el contrato actual
      * - Error al validar: excepción durante el proceso
     *
     * @see #obtenerUltimoRegistroValido(Documento, String)
     */
    @FXML
    private void validarDocumento() {
        if (archivoSeleccionado == null) {
            lblResultado.setText(Messages.getString("select_file_required"));
            return;
        }

        try {
            String hash = hashService.generarHash(archivoSeleccionado);

            Documento doc = documentoService.buscarPorHash(hash);

            if (doc == null) {
                lblResultado.setText(Messages.getString("document_not_registered_system"));
                return;
            }

            // Obtener contrato actual desde configuración
            BlockchainConfig config = ConfigManager.load();
            String contratoActual = config.getContractAddress();

            RegistroBlockchain registro = obtenerUltimoRegistroValido(doc, contratoActual);

            if (registro == null) {
                lblResultado.setText(Messages.getString("document_not_registered_blockchain"));
                return;
            }

            if (registro.getEstado() == EstadoBlockchain.REVOCADO) {
                lblResultado.setText(Messages.getString("verification_document_revoked"));
                return;
            }

            lblResultado.setText(Messages.getString("verification_document_valid"));

        } catch (Exception e) {
            lblResultado.setText(Messages.getString("document_validation_error"));
        }
    }

    /**
     * Navega de vuelta a la pantalla principal.
     *
     * Carga la vista principal y la establece en el layout global.
     * En caso de error, muestra un mensaje al usuario.
     */
    @FXML
    public void volver() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/main.fxml")
            );
            loader.setResources(Messages.getBundle());

            Parent mainView = loader.load();

            LayoutController layout = (LayoutController)
                    lblArchivo.getScene().getUserData();

            if (layout == null) {
                return;
            }

            layout.setContent(mainView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el registro blockchain más reciente y válido para un documento específico.
     * Filtra por dirección del contrato y ordena cronológicamente (descendente).
     *
     * Lógica:
     * 1. Obtiene todos los registros asociados al documento
     * 2. Filtra solo los que pertenecen a la dirección del contrato actual
     * 3. Los ordena por fecha de creación (más reciente primero)
     * 4. Retorna el primero (más reciente) o null si no existe
     *
     * Este método es importante porque un documento puede tener múltiples registros
     * en diferentes contratos o en momentos diferentes. Es necesario obtener el más
     * reciente para validar el estado actual del documento.
     *
     * @param doc documento cuyo registro se busca
     * @param contratoActual dirección del contrato inteligente (ej: 0x6c0134035F62f876...)
     * @return el RegistroBlockchain más reciente para ese documento y contrato, o null si no existe
     */
    private RegistroBlockchain obtenerUltimoRegistroValido(Documento doc, String contratoActual) {

        return doc.getRegistros().stream()
                .filter(r -> contratoActual.equals(r.getDireccionContrato()))
                .sorted((r1, r2) -> r2.getFechaCreacion().compareTo(r1.getFechaCreacion())) // DESC
                .findFirst()
                .orElse(null);
    }

}
