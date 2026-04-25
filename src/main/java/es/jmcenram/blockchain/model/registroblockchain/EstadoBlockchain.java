package es.jmcenram.blockchain.model.registroblockchain;

public enum EstadoBlockchain {
    ERROR,
    PENDIENTE,
    REGISTRADO,    // Registrado en blockchain
    REVOCADO,      // Documento invalidado
}
