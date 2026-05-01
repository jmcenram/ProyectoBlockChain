package es.jmcenram.blockchain.model.mensaje;

import es.jmcenram.blockchain.model.documento.Documento;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad de dominio que representa ResultadoDocumento dentro del sistema.
 *
 * Almacena la informacion persistente necesaria para las reglas de negocio y la trazabilidad de la aplicacion.
 *
 * Forma parte del modelo JPA y se utiliza desde repositorios y servicios.
 *
 * @author Jcena
 * @version 1.0
 */
@Getter
@Setter
public class ResultadoDocumento {

    /**
     * Documento resultante de la operación.
     */
    private Documento documento;

    /**
     * Mensaje descriptivo del resultado de la operación.
     * Puede contener información de éxito, advertencia o error.
     */
    private String mensaje;

    /**
     * Indica si el documento procesado es duplicado.
     *
     * true  → el documento ya existía previamente
     * false → documento nuevo o no duplicado
     */
    private boolean duplicado;

    /**
     * Estado del documento en la blockchain.
     *
     * Valores posibles:
     * - NO_EXISTE → el hash no está registrado en blockchain
     * - REGISTRADO → el documento está registrado correctamente
     * - REVOCADO → el documento ha sido revocado
     *
     * Este estado es independiente del estado interno del documento
     * en la base de datos (BORRADOR, VALIDADO).
     */
    private String estadoBlockchain;

    /**
     * Hash de la transacción en la blockchain asociada al documento.
     *
     * Se rellena cuando el documento ha sido registrado o revocado.
     * Permite consultar la transacción en el explorador de bloques.
     */
    private String txHash;

    /**
     * Fecha en la que el documento fue registrado en la blockchain.
     *
     * Representa el timestamp devuelto por el smart contract,
     * convertido a formato legible.
     */
    private String fechaBlockchain;

}