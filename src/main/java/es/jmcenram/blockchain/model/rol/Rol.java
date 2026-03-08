package es.jmcenram.blockchain.model.rol;

import java.util.HashSet;
import java.util.Set;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRol;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "rol")
public class Rol extends EntidadBase {

    @Column(unique = true, nullable = false)
    private String nombre;

    private String descripcion;

    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioRol> usuarioRoles = new HashSet<>();
}