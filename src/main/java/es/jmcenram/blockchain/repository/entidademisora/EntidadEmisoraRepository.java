package es.jmcenram.blockchain.repository.entidademisora;

import es.jmcenram.blockchain.config.JPAUtil;
import es.jmcenram.blockchain.model.entidademisora.EntidadEmisora;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Repositorio especializado para operaciones de EntidadEmisora.
 * Extiende BaseRepository&lt;EntidadEmisora&gt; con métodos personalizados para búsquedas específicas.
 * Hereda operaciones CRUD básicas (save, findById, findAll, softDelete, update).
 *
 * Características adicionales:
 * - Búsqueda por nombre
 * - Búsqueda por address
 * - Búsqueda por private key
 * - Listado de entidades activas
 * - Verificación de existencia por nombre y address
 *
 * @author Jcena
 * @version 1.0
 */
public class EntidadEmisoraRepository extends BaseRepository<EntidadEmisora> {

    /**
     * Constructor que inicializa el repositorio con la clase EntidadEmisora.
     */
    public EntidadEmisoraRepository() {
        super(EntidadEmisora.class);
    }

    /**
     * Busca una entidad emisora por su nombre.
     * Solo retorna entidades no borradas (fechaBorrado IS NULL).
     * La búsqueda es case-insensitive.
     *
     * @param nombre nombre de la entidad a buscar
     * @return EntidadEmisora si se encuentra, null si no existe o está borrada
     */
    public EntidadEmisora findByNombre(String nombre) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT e FROM EntidadEmisora e " +
                            "WHERE LOWER(e.nombre) = LOWER(:nombre) AND e.fechaBorrado IS NULL",
                            EntidadEmisora.class
                    )
                    .setParameter("nombre", nombre)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Busca una entidad emisora por su dirección de wallet (address).
     * Solo retorna entidades no borradas (fechaBorrado IS NULL).
     * La búsqueda es case-sensitive ya que los addresses son valores exactos.
     *
     * @param address dirección pública de la wallet
     * @return EntidadEmisora si se encuentra, null si no existe o está borrada
     */
    public EntidadEmisora findByAddress(String address) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT e FROM EntidadEmisora e " +
                            "WHERE e.address = :address AND e.fechaBorrado IS NULL",
                            EntidadEmisora.class
                    )
                    .setParameter("address", address)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Busca una entidad emisora por su clave privada.
     * Solo retorna entidades no borradas (fechaBorrado IS NULL).
     * La búsqueda es case-sensitive ya que las claves privadas son valores exactos.
     *
     * @param privateKey clave privada de la wallet
     * @return EntidadEmisora si se encuentra, null si no existe o está borrada
     */
    public EntidadEmisora findByPrivateKey(String privateKey) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT e FROM EntidadEmisora e " +
                            "WHERE e.privateKey = :privateKey AND e.fechaBorrado IS NULL",
                            EntidadEmisora.class
                    )
                    .setParameter("privateKey", privateKey)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene todas las entidades emisoras activas (marcadas como activas y no borradas).
     * Useful para mostrar listados de emisores disponibles.
     *
     * @return lista de entidades emisoras activas; lista vacía si no hay registros
     */
    public List<EntidadEmisora> findAllActivas() {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT e FROM EntidadEmisora e " +
                    "WHERE e.activo = true AND e.fechaBorrado IS NULL " +
                    "ORDER BY e.nombre ASC",
                    EntidadEmisora.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    /**
     * Verifica si existe una entidad emisora con el nombre especificado.
     * La búsqueda es case-insensitive.
     * No cuenta entidades borradas.
     *
     * @param nombre nombre a verificar
     * @return true si existe una entidad con ese nombre actual, false en caso contrario
     */
    public boolean existsByNombre(String nombre) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            Long count = em.createQuery(
                            "SELECT COUNT(e) FROM EntidadEmisora e " +
                            "WHERE LOWER(e.nombre) = LOWER(:nombre) AND e.fechaBorrado IS NULL",
                            Long.class
                    )
                    .setParameter("nombre", nombre)
                    .getSingleResult();

            return count != null && count > 0;

        } finally {
            em.close();
        }
    }

    /**
     * Verifica si existe una entidad emisora con el address especificado.
     * La búsqueda es case-sensitive para addresses.
     * No cuenta entidades borradas.
     *
     * @param address address a verificar
     * @return true si existe una entidad con ese address, false en caso contrario
     */
    public boolean existsByAddress(String address) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            Long count = em.createQuery(
                            "SELECT COUNT(e) FROM EntidadEmisora e " +
                            "WHERE e.address = :address AND e.fechaBorrado IS NULL",
                            Long.class
                    )
                    .setParameter("address", address)
                    .getSingleResult();

            return count != null && count > 0;

        } finally {
            em.close();
        }
    }
}

