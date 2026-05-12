package es.jmcenram.blockchain.repository.usuariorol;

import es.jmcenram.blockchain.config.JPAUtil;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRol;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRolId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

/**
 * Repositorio encargado del acceso a datos de UsuarioRol.
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
public class UsuarioRolRepository {

    /**
     * Guarda o actualiza una relación usuario-rol en la base de datos.
     * Utiliza merge porque la clave es compuesta y puede o no existir.
     *
     * Manejo de transacciones:
     * - Abre transacción al inicio
     * - Realiza commit si todo va bien
     * - Realiza rollback automático si hay excepción
     *
     * @param entity la relación usuario-rol a guardar
     * @return la relación guardada/actualizada
     * @throws RuntimeException si hay error en la transacción
     */
    public UsuarioRol save(UsuarioRol entity) {

        if (entity == null || entity.getUsuario() == null || entity.getRol() == null) {
            throw new IllegalArgumentException("La relacion usuario-rol debe tener usuario y rol");
        }

        if (entity.getUsuario().getId() == null || entity.getRol().getId() == null) {
            throw new IllegalArgumentException("Usuario y rol deben estar persistidos antes de asignar la relacion");
        }

        entity.setId(new UsuarioRolId(entity.getUsuario().getId(), entity.getRol().getId()));

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

    /**
     * Busca una relación usuario-rol por su clave compuesta.
     *
     * @param id la clave compuesta (usuarioId, rolId)
     * @return UsuarioRol si existe, null en caso contrario
     */
    public UsuarioRol findById(UsuarioRolId id) {

        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(UsuarioRol.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene todas las relaciones usuario-rol de la base de datos.
     *
     * @return lista de todas las asignaciones de roles a usuarios
     */
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

    /**
     * Elimina una relación usuario-rol de la base de datos.
     * Busca por clave compuesta y la elimina si existe.
     *
     * Manejo de transacciones:
     * - Abre transacción al inicio
     * - Realiza commit si todo va bien
     * - Realiza rollback automático si hay excepción
     *
     * @param id la clave compuesta de la relación a eliminar
     * @throws RuntimeException si hay error en la transacción
     */
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
