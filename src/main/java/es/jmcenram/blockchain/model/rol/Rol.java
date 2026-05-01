package es.jmcenram.blockchain.model.rol;

import java.util.HashSet;
import java.util.Set;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRol;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

/**
 * Entidad de dominio que representa Rol dentro del sistema.
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
@Table(name = "rol")
public class Rol extends EntidadBase {

    /**
     * Nombre único del rol.
     *
     * Se utiliza como identificador lógico del rol
     * (ej: "ADMIN", "USER").
     */
    @Column(unique = true, nullable = false)
    private String nombre;

    /**
     * Descripción del rol.
     *
     * Proporciona información adicional sobre sus permisos
     * o finalidad dentro del sistema.
     */
    private String descripcion;

    /**
     * Relación con usuarios mediante entidad intermedia.
     *
     * Relación OneToMany:
     * - Un rol puede estar asociado a múltiples usuarios
     * - Cascade ALL: propaga operaciones a la relación
     * - orphanRemoval: elimina relaciones huérfanas
     *
     * Se gestiona a través de {@link UsuarioRol}
     * para permitir una relación muchos a muchos enriquecida.
     */
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioRol> usuarioRoles = new HashSet<>();
}