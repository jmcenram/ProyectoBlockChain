package es.jmcenram.blockchain.repository.usuario;

import es.jmcenram.blockchain.config.JPAUtil;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;

public class UsuarioRepository extends BaseRepository<Usuario> {

    public UsuarioRepository() {
        super(Usuario.class);
    }

    public Usuario findByMail(String email) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT u FROM Usuario u WHERE u.email = :email AND u.fechaBorrado IS NULL",
                            Usuario.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    public boolean existsByMail(String email) {

        EntityManager em = getEntityManager();

        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM Usuario u WHERE LOWER(u.email) = LOWER(:email)",
                        Long.class
                )
                .setParameter("email", email)
                .getSingleResult();

        return count != null && count > 0;
    }
}
