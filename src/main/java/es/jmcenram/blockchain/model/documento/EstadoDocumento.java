package es.jmcenram.blockchain.model.documento;

/**
 * Enumerado que representa los estados posibles de documento.
 *
 * Permite usar valores controlados en lugar de cadenas libres dentro del dominio.
 *
 * Se utiliza en entidades, servicios y controladores para decidir reglas de negocio y presentacion.
 *
 * @author Jcena
 * @version 1.0
 */
public enum EstadoDocumento {

    /**
     * Documento en estado inicial.
     *
     * Características:
     * - Editable
     * - Puede modificarse o eliminarse
     * - No tiene hash definitivo asociado
     */
    BORRADOR,

    /**
     * Documento validado.
     *
     * Características:
     * - Hash generado
     * - No debería modificarse
     * - Listo para ser registrado en blockchain
     */
    VALIDADO
}