package es.jmcenram.blockchain.service.usuario;

import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import es.jmcenram.blockchain.service.base.BaseService;

public class UsuarioService extends BaseService<Usuario> {

    protected UsuarioService(BaseRepository<Usuario> repository) {
        super(repository);
    }
}
