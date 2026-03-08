package es.jmcenram.blockchain.model.usuariorol;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.rol.Rol;
import es.jmcenram.blockchain.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "usuario_rol")
public class UsuarioRol {

    @EmbeddedId
    private UsuarioRolId id;

    @ManyToOne(optional = false)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @MapsId("rolId")
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    // 👇 OBLIGATORIO PARA JPA
    protected UsuarioRol() {
    }

    public UsuarioRol(Usuario usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
        this.id = new UsuarioRolId(usuario.getId(), rol.getId());
        this.fechaCreacion = LocalDateTime.now();
    }
}