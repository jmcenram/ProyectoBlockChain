package es.jmcenram.blockchain.model.documento;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "documento")
public class Documento extends EntidadBase {

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    private String tipo;


    @Column(nullable = false, length = 64)
    private String hash;

    @Column(name = "ruta_archivo", nullable = false)
    private String rutaArchivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "varchar(20)")
    private EstadoDocumento estado;

    @Column(name = "fecha_registro_blockchain")
    private LocalDateTime fechaRegistroBlockchain;


    @Column(name = "transaction_hash")
    private String transactionHash;

    @ManyToOne
    @JoinColumn(name = "emisor_id", nullable = false)
    private Usuario emisor;

    @OneToMany(mappedBy = "documento")
    private List<RegistroBlockchain> registros = new ArrayList<>();
}