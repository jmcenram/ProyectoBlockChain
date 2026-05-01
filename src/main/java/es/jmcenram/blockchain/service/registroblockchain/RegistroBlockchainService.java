package es.jmcenram.blockchain.service.registroblockchain;

import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import es.jmcenram.blockchain.service.base.BaseService;

/**
 * Servicio encargado de la logica de negocio de RegistroBlockchain.
 *
 * Permite:
 * - Validar reglas antes de persistir cambios
 * - Coordinar repositorios relacionados
 * - Exponer operaciones usadas por controladores u otros servicios
 *
 * Forma parte de la capa de servicio y mantiene la logica fuera de la interfaz.
 *
 * @author Jcena
 * @version 1.0
 */
public class RegistroBlockchainService extends BaseService<RegistroBlockchain> {

    /**
     * Constructor protegido que inicializa el servicio con su repositorio.
     *
     * @param repository el repositorio que gestiona la persistencia de RegistroBlockchain
     */
    protected RegistroBlockchainService(BaseRepository<RegistroBlockchain> repository) {
        super(repository);
    }
}
