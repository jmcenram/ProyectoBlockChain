package es.jmcenram.blockchain.service.registroblockchain;

import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import es.jmcenram.blockchain.service.base.BaseService;

public class RegistroBlockchainService extends BaseService<RegistroBlockchain> {

    protected RegistroBlockchainService(BaseRepository<RegistroBlockchain> repository) {
        super(repository);
    }
}
