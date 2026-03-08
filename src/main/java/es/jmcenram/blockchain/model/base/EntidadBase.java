package es.jmcenram.blockchain.model.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class EntidadBase implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    public boolean estaBorrado() {
        return fechaBorrado != null;
    }

    public void marcarComoBorrado() {
        this.fechaBorrado = LocalDateTime.now();
    }

}
