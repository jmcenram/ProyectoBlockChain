package es.jmcenram.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO encargado de transportar informacion de un documento consultado en blockchain.
 *
 * Contiene los datos normalizados que devuelven los servicios: hash, fecha, emisor y estado.
 *
 * Evita exponer tuplas o tipos propios de Web3j fuera de la capa blockchain.
 *
 * @author Jcena
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String hash;
    private String fecha;
    private String emisor;
    private String estado;
}