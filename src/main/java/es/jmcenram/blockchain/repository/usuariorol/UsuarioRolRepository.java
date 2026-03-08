package es.jmcenram.blockchain.repository.usuariorol;

import es.jmcenram.blockchain.config.JPAUtil;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRol;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRolId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class UsuarioRolRepository {

    public UsuarioRol save(UsuarioRol entity) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            entity = em.merge(entity); // con clave compuesta usamos merge
            tx.commit();
            return entity;

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public UsuarioRol findById(UsuarioRolId id) {

        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(UsuarioRol.class, id);
        } finally {
            em.close();
        }
    }

    public List<UsuarioRol> findAll() {

        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT ur FROM UsuarioRol ur",
                    UsuarioRol.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(UsuarioRolId id) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            UsuarioRol entity = em.find(UsuarioRol.class, id);
            if (entity != null) {
                em.remove(entity);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}