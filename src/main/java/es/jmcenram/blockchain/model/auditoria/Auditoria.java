package es.jmcenram.blockchain.model.auditoria;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "auditoria")
public class Auditoria extends EntidadBase {

    @Column(nullable = false)
    private String accion;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}