package es.jmcenram.blockchain.model.mensaje;

import es.jmcenram.blockchain.model.documento.Documento;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultadoDocumento {

    private Documento documento;
    private String mensaje;
    private boolean duplicado;

}