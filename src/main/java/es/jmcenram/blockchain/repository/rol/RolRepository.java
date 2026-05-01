package es.jmcenram.blockchain.repository.rol;

import es.jmcenram.blockchain.model.rol.Rol;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * Repositorio encargado del acceso a datos de Rol.
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
public class RolRepository  extends BaseRepository<Rol> {

    /**
     * Constructor que inicializa el repositorio con la clase Rol.
     */
    public RolRepository() {
        super(Rol.class);
    }

    /**
     * Busca un rol por su nombre único.
     * El nombre es el identificador lógico del rol (ej: "ADMIN", "USER").
     *
     * @param nombre nombre del rol a buscar (case-sensitive)
     * @return Rol si se encuentra, null si no existe o hay error
     */
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