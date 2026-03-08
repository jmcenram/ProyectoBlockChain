package es.jmcenram.blockchain.repository.base;

import es.jmcenram.blockchain.config.JPAUtil;
import es.jmcenram.blockchain.model.base.EntidadBase;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public abstract class BaseRepository<T extends EntidadBase> {

    private final Class<T> clazz;

    public BaseRepository(Class<T> clazz) {
        this.clazz = clazz;
    }

    private String getEntityName() {
        Entity entity = clazz.getAnnotation(Entity.class);
        if (entity != null && entity.name() != null && !entity.name().isBlank()) {
            return entity.name();
        }
        return clazz.getSimpleName();
    }

    public T save(T entity) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            if (entity.getId() == null) {
                em.persist(entity);
            } else {
                entity = em.merge(entity);
            }

            tx.commit();
            return entity;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public T findById(Long id) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            return em.createQuery(
                            "SELECT e FROM " + getEntityName() +
                                    " e WHERE e.id = :id AND e.fechaBorrado IS NULL",
                            clazz
                    )
                    .setParameter("id", id)
                    .getSingleResult();

        } finally {
            em.close();
        }
    }

    public List<T> findAll() {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            return em.createQuery(
                    "SELECT e FROM " + getEntityName() +
                            " e WHERE e.fechaBorrado IS NULL",
                    clazz
            ).getResultList();

        } finally {
            em.close();
        }
    }

    public void softDelete(Long id) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            T entity = em.find(clazz, id);

            if (entity != null && entity.getFechaBorrado() == null) {
                entity.marcarComoBorrado();
                em.merge(entity);
            }

            tx.commit();

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }
}