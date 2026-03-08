package es.jmcenram.blockchain.model.apikey;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "api_key")
public class ApiKey extends EntidadBase {

    @Column(unique = true, nullable = false)
    private String clave;

    private Boolean activa = true;

    private String descripcion;

}