package es.jmcenram.blockchain.repository.documento;


import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

import static es.jmcenram.blockchain.config.JPAUtil.getEntityManager;

/**
 * Repositorio encargado del acceso a datos de Documento.
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
public class DocumentoRepository extends BaseRepository<Documento> {

    /**
     * Constructor que inicializa el repositorio con la clase Documento.
     */
    public DocumentoRepository() {
        super(Documento.class);
    }

    /**
     * Obtiene todos los documentos activos con sus registros blockchain cargados eagerly.
     * Utiliza LEFT JOIN FETCH para cargar la colección registros en una sola query.
     * DISTINCT evita duplicados cuando un documento tiene múltiples registros.
     *
     * Esta query optimiza el acceso porque carga todo lo necesario de una vez,
     * evitando N+1 queries cuando se accede a getRegistros() en los documentos.
     *
     * @return lista de todos los documentos no borrados con sus registros blockchain eagerly loaded
     */
    public List<Documento> obtenerTodosConRegistros() {

        EntityManager em = getEntityManager();

        try {
            TypedQuery<Documento> query = em.createQuery(
                    "SELECT DISTINCT d FROM Documento d " +
                            "LEFT JOIN FETCH d.registros " +
                            "WHERE d.fechaBorrado IS NULL",
                    Documento.class
            );

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    /**
     * Busca un documento activo por su hash SHA-256.
     *
     * Este método permite localizar un documento a partir de su identificador único
     * generado mediante hashing del contenido. Es clave para la verificación de
     * documentos externos en el sistema.
     *
     * Utiliza una query JPQL simple filtrando por:
     * - hash del documento (campo único lógico)
     * - fechaBorrado IS NULL para excluir soft deletes
     *
     * getResultStream().findFirst() evita excepciones si no hay resultados,
     * devolviendo null en lugar de lanzar NoResultException.
     *
     * @param hash hash SHA-256 del documento a buscar
     * @return Documento encontrado o null si no existe en el sistema
     */
    public Documento findByHash(String hash) {

        EntityManager em = getEntityManager();

        try {
            TypedQuery<Documento> query = em.createQuery(
                    "SELECT d FROM Documento d " +
                            "WHERE d.hash = :hash " +
                            "AND d.fechaBorrado IS NULL",
                    Documento.class
            );

            return query.setParameter("hash", hash)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            em.close();
        }
    }
}