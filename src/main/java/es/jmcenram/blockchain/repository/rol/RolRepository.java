package es.jmcenram.blockchain.repository.rol;

import es.jmcenram.blockchain.model.rol.Rol;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.base.BaseRepository;

public class RolRepository  extends BaseRepository<Rol> {

    public RolRepository() {
        super(Rol.class);
    }
}