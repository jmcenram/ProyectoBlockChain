package es.jmcenram.blockchain.model.usuariorol;

import es.jmcenram.blockchain.model.rol.Rol;
import es.jmcenram.blockchain.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad intermedia que representa la relación entre {@link Usuario} y {@link Rol}.
 *
 * Implementa una relación muchos a muchos enriquecida (ManyToMany con atributos),
 * permitiendo asociar múltiples roles a un usuario y viceversa,
 * además de almacenar información adicional sobre la relación.
 *
 * Características:
 * - Clave primaria compuesta mediante {@link UsuarioRolId}
 * - Permite añadir metadatos como fechas (creación, borrado lógico)
 * - Soporta auditoría y trazabilidad de asignaciones de roles
 *
 * Este diseño evita el uso de @ManyToMany directo,
 * proporcionando mayor flexibilidad y control sobre la relación.
 *
 * @author Jcena
 * @version 1.0
 */
@Entity
@Table(name = "usuario_rol")
@Getter
@Setter
public class UsuarioRol {

    /**
     * Clave primaria compuesta de la relación.
     *
     * Incluye:
     * - ID del usuario
     * - ID del rol
     */
    @EmbeddedId
    private UsuarioRolId id = new UsuarioRolId();

    /**
     * Usuario asociado a la relación.
     *
     * Se enlaza con la clave compuesta mediante {@link MapsId}.
     */
    @ManyToOne(optional = false)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * Rol asociado a la relación.
     *
     * Se enlaza con la clave compuesta mediante {@link MapsId}.
     */
    @ManyToOne(optional = false)
    @MapsId("rolId")
    @JoinColumn(name = "rol_id")
    private Rol rol;

    /**
     * Fecha de creación de la relación.
     *
     * Se establece automáticamente al persistir.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de borrado lógico de la relación.
     *
     * Permite desactivar la relación sin eliminar físicamente el registro.
     */
    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    /**
     * Constructor vacío requerido por JPA.
     */
    public UsuarioRol() {}

    /**
     * Constructor que crea la relación entre un usuario y un rol.
     *
     * @param usuario usuario asociado
     * @param rol rol asociado
     */
    public UsuarioRol(Usuario usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
    }

    /**
     * Método ejecutado automáticamente antes de persistir la entidad.
     *
     * Inicializa la fecha de creación de la relación.
     */
    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
}