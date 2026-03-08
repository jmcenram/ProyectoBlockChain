package es.jmcenram.blockchain.repository.registroblockchain;


import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.repository.base.BaseRepository;

public class RegistroBlockchainRepository  extends BaseRepository<RegistroBlockchain> {

    public RegistroBlockchainRepository() {
        super(RegistroBlockchain.class);
    }
}