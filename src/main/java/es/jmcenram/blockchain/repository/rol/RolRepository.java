package es.jmcenram.blockchain.repository.rol;

import es.jmcenram.blockchain.model.rol.Rol;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class RolRepository  extends BaseRepository<Rol> {

    public RolRepository() {
        super(Rol.class);
    }

    public Rol findByNombre(String nombre) {

        try {
            EntityManager em = getEntityManager();

            TypedQuery<Rol> query = em.createQuery(
                    "SELECT r FROM Rol r WHERE r.nombre = :nombre",
                    Rol.class
            );

            query.setParameter("nombre", nombre);

            return query.getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }
}