package es.jmcenram.blockchain.model.usuario;

import es.jmcenram.blockchain.model.apikey.ApiKey;
import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario extends EntidadBase {

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private Boolean activo = true;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioRol> roles = new HashSet<>();

    @OneToMany(mappedBy = "emisor")
    private List<Documento> documentos = new ArrayList<>();
}