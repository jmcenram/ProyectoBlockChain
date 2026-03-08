package es.jmcenram.blockchain.repository.usuario;

import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.base.BaseRepository;

public class UsuarioRepository extends BaseRepository<Usuario> {

    public UsuarioRepository() {
        super(Usuario.class);
    }
}
