package es.jmcenram.blockchain.service.apikey;

import es.jmcenram.blockchain.model.apikey.ApiKey;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import es.jmcenram.blockchain.service.base.BaseService;

public class ApiKeyService extends BaseService<ApiKey> {

    protected ApiKeyService(BaseRepository<ApiKey> repository) {
        super(repository);
    }
}
