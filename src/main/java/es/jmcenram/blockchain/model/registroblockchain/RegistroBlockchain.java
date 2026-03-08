package es.jmcenram.blockchain.model.registroblockchain;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.documento.Documento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "registro_blockchain")
public class RegistroBlockchain extends EntidadBase {

    @ManyToOne(optional = false)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Column(name = "hash_documento", nullable = false)
    private String hashDocumento;

    @Column(name = "direccion_contrato")
    private String direccionContrato;

    @Column(name = "transaction_hash")
    private String transactionHash;

    @Column(name = "bloque_number")
    private Long bloqueNumber;

}