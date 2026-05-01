package es.jmcenram.blockchain.model.registroblockchain;

/**
 * Enumeración que define los posibles estados de un registro en blockchain.
 *
 * Representa el ciclo de vida de un documento dentro del sistema blockchain,
 * desde su envío hasta su confirmación o revocación.
 *
 * Estados disponibles:
 * - ERROR: fallo en la operación de registro o comunicación
 * - PENDIENTE: operación enviada pero aún no confirmada
 * - REGISTRADO: documento registrado correctamente en blockchain
 * - REVOCADO: documento invalidado posteriormente en blockchain
 *
 * Este estado es independiente del estado funcional del documento
 * (gestionado mediante {@code EstadoDocumento}).
 *
 * Se utiliza principalmente en la entidad {@code RegistroBlockchain}
 * y en la lógica de visualización y control de acciones en la UI.
 *
 * @author Jcena
 * @version 1.0
 */
public enum EstadoBlockchain {

    /**
     * Error en la operación blockchain.
     *
     * Puede producirse por:
     * - Fallo de red
     * - Error en el nodo
     * - Problemas con el contrato
     */
    ERROR,

    /**
     * Operación en curso.
     *
     * Indica que la transacción ha sido enviada
     * pero aún no ha sido confirmada en la blockchain.
     */
    PENDIENTE,

    /**
     * Documento registrado correctamente en blockchain.
     *
     * Características:
     * - Transacción confirmada
     * - Hash almacenado en el contrato
     */
    REGISTRADO,

    /**
     * Documento revocado en blockchain.
     *
     * Características:
     * - El documento ya no es válido
     * - Se mantiene trazabilidad del registro previo
     */
    REVOCADO
}