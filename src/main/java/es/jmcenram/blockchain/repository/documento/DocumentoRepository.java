package es.jmcenram.blockchain.repository.documento;


import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.repository.base.BaseRepository;

public class DocumentoRepository extends BaseRepository<Documento> {

    public DocumentoRepository() {
        super(Documento.class);
    }
}