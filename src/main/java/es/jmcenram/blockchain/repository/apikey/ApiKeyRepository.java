package es.jmcenram.blockchain.repository.apikey;

import es.jmcenram.blockchain.model.apikey.ApiKey;
import es.jmcenram.blockchain.repository.base.BaseRepository;

public class ApiKeyRepository extends BaseRepository<ApiKey> {

    public ApiKeyRepository() {
        super(ApiKey.class);
    }
}