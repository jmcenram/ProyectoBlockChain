package es.jmcenram.blockchain.model.registroblockchain;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.documento.Documento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa RegistroBlockchain dentro del sistema.
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
@Entity
@Table(name = "registro_blockchain")
public class RegistroBlockchain extends EntidadBase {

    /**
     * Documento asociado al registro en blockchain.
     *
     * Relación ManyToOne:
     * - Obligatoria
     * - Carga perezosa (LAZY) para optimizar rendimiento
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    /**
     * Hash del documento registrado.
     *
     * Se utiliza para verificar la integridad del documento en blockchain.
     */
    @Column(name = "hash_documento", nullable = false)
    private String hashDocumento;

    /**
     * Dirección del contrato inteligente en el que se registra el documento.
     */
    @Column(name = "direccion_contrato")
    private String direccionContrato;

    /**
     * Hash de la transacción en blockchain.
     *
     * Permite consultar la operación en el explorador de bloques.
     */
    @Column(name = "transaction_hash")
    private String transactionHash;

    /**
     * Número de bloque en el que se incluyó la transacción.
     */
    @Column(name = "bloque_number")
    private Long bloqueNumber;

    /**
     * Estado actual del registro en blockchain.
     *
     * @see EstadoBlockchain
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoBlockchain estado;

}