package es.jmcenram.blockchain.repository.base;

import es.jmcenram.blockchain.config.JPAUtil;
import es.jmcenram.blockchain.model.base.EntidadBase;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

/**
 * Repositorio encargado del acceso a datos de Base.
 *
 * Permite:
 * - Consultar registros persistidos
 * - Guardar cambios de la entidad
 * - Encapsular consultas especificas del dominio
 *
 * Aisla la logica JPA para que los servicios no dependan de consultas ni EntityManager directamente.
 *
 * Forma parte de la capa de persistencia.
 *
 * @author Jcena
 * @version 1.0
 */
public abstract class BaseRepository<T extends EntidadBase> {

    /** Clase de tipo parametrizado para operaciones JPA dinámicas */
    private final Class<T> clazz;

    /**
     * Constructor protegido que inicializa el repositorio con la clase de entidad.
     *
     * @param clazz la clase de la entidad a gestionar
     */
    public BaseRepository(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Obtiene una instancia de EntityManager desde el pool de conexiones JPA.
     * Esta instancia debe cerrarse después de su uso.
     *
     * @return EntityManager activo
     */
    protected EntityManager getEntityManager() {
        return JPAUtil.getEntityManager();
    }

    /**
     * Extrae el nombre de la entidad JPA usando anotación @Entity.
     * Si la anotación especifica un nombre personalizado, lo usa; en caso contrario, usa el nombre simple de la clase.
     *
     * @return nombre de la entidad JPA
     */
    private String getEntityName() {
        Entity entity = clazz.getAnnotation(Entity.class);
        if (entity != null && entity.name() != null && !entity.name().isBlank()) {
            return entity.name();
        }
        return clazz.getSimpleName();
    }

    /**
     * Guarda o actualiza una entidad en la base de datos.
     * Si el ID es null, realiza INSERT (persist); si tiene ID, realiza UPDATE (merge).
     *
     * Manejo de transacciones:
     * - Abre transacción al inicio
     * - Realiza commit si todo va bien
     * - Realiza rollback automático si hay excepción
     *
     * @param entity la entidad a guardar o actualizar
     * @return la entidad guardada (con ID generado si era nuevo)
     * @throws RuntimeException si hay errores en la transacción
     */
    public T save(T entity) {

        EntityManager em = getEntityManager();
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

    /**
     * Busca una entidad por su ID primario.
     * Solo retorna entidades no borradas (fechaBorrado IS NULL).
     * Si no encuentra la entidad activa, lanza NoResultException.
     *
     * @param id el ID primario de la entidad
     * @return la entidad encontrada
     * @throws jakarta.persistence.NoResultException si no existe entidad activa con ese ID
     */
    public T findById(Long id) {

        EntityManager em = getEntityManager();

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

    /**
     * Obtiene todas las entidades no borradas de la base de datos.
     * Excluye automáticamente entidades marcadas como borradas (fechaBorrado IS NOT NULL).
     *
     * @return lista de todas las entidades activas; lista vacía si no hay registros
     */
    public List<T> findAll() {

        EntityManager em = getEntityManager();

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

    /**
     * Marca una entidad como borrada sin eliminarla físicamente (soft-delete).
     * Busca la entidad por ID y establece su fecha de borrado a la fecha-hora actual.
     *
     * Manejo de transacciones:
     * - Abre transacción
     * - Realiza commit si tiene éxito
     * - Realiza rollback si hay errores
     *
     * @param id el ID de la entidad a marcar como borrada
     * @throws RuntimeException si hay errores en la transacción
     */
    public void softDelete(Long id) {

        EntityManager em = getEntityManager();
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

    /**
     * Actualiza una entidad existente en la base de datos.
     * Realiza merge de la entidad y flush para asegurar cambios inmediatos.
     *
     * Manejo de transacciones:
     * - Abre transacción
     * - Realiza flush de cambios pendientes
     * - Realiza commit si todo va bien
     * - Realiza rollback si hay errores
     *
     * @param entity la entidad con cambios a actualizar
     * @return la entidad actualizada desde la base de datos
     * @throws RuntimeException si hay errores en la transacción
     */
    public T update(T entity) {

        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            T merged = em.merge(entity);

            em.flush();

            tx.commit();

            return merged;

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