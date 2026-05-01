package es.jmcenram.blockchain.model.auditoria;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un registro de auditoría del sistema.
 *
 * Permite almacenar acciones realizadas por los usuarios,
 * facilitando la trazabilidad y el control de operaciones.
 *
 * Incluye información sobre:
 * - Acción realizada
 * - Descripción adicional
 * - Usuario responsable
 *
 * Hereda de {@link EntidadBase}, incorporando:
 * - Identificador único
 * - Fecha de creación
 * - Fecha de borrado lógico
 *
 * @author Jcena
 * @version 1.0
 */
@Getter
@Setter
@Entity
@Table(name = "auditoria")
public class Auditoria extends EntidadBase {

    /**
     * Acción realizada en el sistema.
     *
     * Ejemplos:
     * - CREAR_DOCUMENTO
     * - VALIDAR_DOCUMENTO
     * - REGISTRAR_BLOCKCHAIN
     */
    @Column(nullable = false)
    private String accion;

    /**
     * Descripción adicional de la acción.
     *
     * Puede incluir detalles contextuales como:
     * - Nombre del documento
     * - Resultado de la operación
     */
    private String descripcion;

    /**
     * Usuario que ha realizado la acción.
     *
     * Relación muchos a uno con la entidad {@link Usuario}.
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}