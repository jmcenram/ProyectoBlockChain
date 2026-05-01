package es.jmcenram.blockchain.model.usuario;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRol;
import es.jmcenram.blockchain.model.entidademisora.EntidadEmisora;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad de dominio que representa Usuario dentro del sistema.
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
@Table(name = "usuario")
public class Usuario extends EntidadBase {

    /**
     * Nombre del usuario.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Dirección de correo electrónico del usuario.
     *
     * Debe ser única en el sistema y se utiliza como identificador
     * principal para la autenticación.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Contraseña del usuario.
     *
     * Se almacena en formato cifrado (hash) por motivos de seguridad.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Indica si el usuario está activo en el sistema.
     *
     * true  → usuario habilitado
     * false → usuario deshabilitado
     */
    private boolean activo = true;

    /**
     * Entidad emisora asociada al usuario.
     *
     * - Relación ManyToOne: múltiples usuarios pueden pertenecer a una misma entidad emisora
     * - Puede ser null, permitiendo usuarios sin entidad asociada (por ejemplo, administradores)
     * - FetchType.LAZY para optimizar el rendimiento
     *
     * Esta relación permite vincular el usuario con una wallet blockchain
     * a través de la entidad emisora.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entidad_emisora_id", nullable = true)
    private EntidadEmisora entidadEmisora;

    /**
     * Relación con roles mediante entidad intermedia.
     *
     * Relación OneToMany:
     * - Un usuario puede tener múltiples roles
     * - CascadeType.ALL: propaga las operaciones de persistencia
     * - orphanRemoval: elimina las relaciones huérfanas
     *
     * Se utiliza {@link UsuarioRol} para modelar una relación muchos a muchos
     * con mayor flexibilidad.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioRol> roles = new HashSet<>();

    /**
     * Lista de documentos emitidos por el usuario.
     *
     * Relación OneToMany:
     * - Un usuario puede emitir múltiples documentos
     * - No se utiliza cascade para evitar eliminaciones en cascada no deseadas
     */
    @OneToMany(mappedBy = "emisor")
    private List<Documento> documentos = new ArrayList<>();
}