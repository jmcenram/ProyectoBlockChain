package es.jmcenram.blockchain.repository.auditoria;


import es.jmcenram.blockchain.model.auditoria.Auditoria;
import es.jmcenram.blockchain.repository.base.BaseRepository;

public class AuditoriaRepository extends BaseRepository<Auditoria> {

    public AuditoriaRepository() {
        super(Auditoria.class);
    }
}