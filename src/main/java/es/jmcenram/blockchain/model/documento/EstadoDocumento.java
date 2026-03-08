package es.jmcenram.blockchain.model.documento;

public enum EstadoDocumento {

    BORRADOR,      // Creado, editable
    VALIDADO,      // Hash generado
    REGISTRADO,    // Registrado en blockchain
    REVOCADO,      // Documento invalidado
    RECHAZADO      // Documento descartado
}
