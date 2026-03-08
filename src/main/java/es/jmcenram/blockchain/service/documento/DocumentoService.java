package es.jmcenram.blockchain.service.documento;

import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import es.jmcenram.blockchain.service.base.BaseService;

public class DocumentoService extends BaseService<Documento> {

    protected DocumentoService(BaseRepository<Documento> repository) {
        super(repository);
    }
}
