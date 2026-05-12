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

/**
 * Entidad de dominio que representa Documento dentro del sistema.
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
@Table(name = "documento")
public class Documento extends EntidadBase {

    /**
     * Nombre del documento.
     * Campo obligatorio.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Descripción opcional del documento.
     */
    private String descripcion;

    /**
     * Tipo de documento (ej: PDF, certificado, título, etc.).
     */
    private String tipo;

    /**
     * Hash criptográfico del documento.
     * Se utiliza para verificar la integridad del contenido.
     * Longitud máxima de 64 caracteres.
     */
    @Column(nullable = true, length = 64)
    private String hash;

    /**
     * Ruta del archivo almacenado en el sistema.
     * Campo obligatorio.
     */
    @Column(name = "ruta_archivo", nullable = false)
    private String rutaArchivo;

    /**
     * Estado actual del documento.
     * Se almacena como texto en base de datos.
     *
     * @see EstadoDocumento
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "varchar(20)")
    private EstadoDocumento estado;

    /**
     * Fecha en la que el documento fue registrado en blockchain.
     */
    @Column(name = "fecha_registro_blockchain")
    private LocalDateTime fechaRegistroBlockchain;

    /**
     * Hash de la transacción en blockchain asociada al documento.
     */
    @Column(name = "transaction_hash")
    private String transactionHash;

    /**
     * Usuario emisor del documento.
     * Relación ManyToOne obligatoria.
     */
    @ManyToOne
    @JoinColumn(name = "emisor_id", nullable = false)
    private Usuario emisor;

    /**
     * Lista de registros asociados en blockchain.
     *
     * Relación OneToMany:
     * - Carga eager (se recuperan automáticamente)
     * - Cascada completa (persist, remove, etc.)
     */
    @OneToMany(mappedBy = "documento", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<RegistroBlockchain> registros = new ArrayList<>();

    /**
     * Contenido binario del documento.
     *
     * Se almacena como BLOB en base de datos.
     */
    @Column(name = "contenido")
    private byte[] contenido;

    /**
     * Indica si el documento está siendo procesado.
     *
     * Campo transitorio (no persistido en base de datos).
     * Usado principalmente para control de estado en la UI.
     */
    private transient boolean procesando;

    /**
     * Método ejecutado automáticamente antes de persistir la entidad.
     *
     * Inicializa la fecha de creación del documento.
     */
    @PrePersist
    public void prePersist() {
        this.setFechaCreacion(LocalDateTime.now());
    }
}