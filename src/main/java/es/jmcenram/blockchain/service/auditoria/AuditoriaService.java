package es.jmcenram.blockchain.service.auditoria;

import es.jmcenram.blockchain.model.auditoria.Auditoria;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import es.jmcenram.blockchain.service.base.BaseService;

public class AuditoriaService extends BaseService<Auditoria> {

    protected AuditoriaService(BaseRepository<Auditoria> repository) {
        super(repository);
    }
}
