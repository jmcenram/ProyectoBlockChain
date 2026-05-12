package es.jmcenram.blockchain.repository.auditoria;


import es.jmcenram.blockchain.model.auditoria.Auditoria;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Repositorio encargado del acceso a datos de Auditoria.
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
public class AuditoriaRepository extends BaseRepository<Auditoria> {

    /**
     * Constructor que inicializa el repositorio con la clase Auditoria.
     */
    public AuditoriaRepository() {
        super(Auditoria.class);
    }

    /**
     * Busca todos los registros de auditoría de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return lista de auditorías del usuario
     */
    public List<Auditoria> findByUsuario(Long usuarioId) {

        EntityManager em = getEntityManager();

        try {
            return em.createQuery(
                    "SELECT a FROM Auditoria a WHERE a.usuario.id = :usuarioId",
                    Auditoria.class
            )
                    .setParameter("usuarioId", usuarioId)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    /**
     * Busca todos los registros de auditoría de una acción específica.
     *
     * Ejemplos de acciones: CREAR_DOCUMENTO, VALIDAR_DOCUMENTO, REGISTRAR_BLOCKCHAIN
     *
     * @param accion nombre de la acción a buscar
     * @return lista de auditorías de la acción
     */
    public List<Auditoria> findByAccion(String accion) {

        EntityManager em = getEntityManager();

        try {
            return em.createQuery(
                    "SELECT a FROM Auditoria a WHERE a.accion = :accion",
                    Auditoria.class
            )
                    .setParameter("accion", accion)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    /**
     * Busca auditorías combinando usuario y acción.
     *
     * @param usuarioId ID del usuario
     * @param accion nombre de la acción
     * @return lista de auditorías que coinciden con ambos criterios
     */
    public List<Auditoria> findByUsuarioAndAccion(Long usuarioId, String accion) {

        EntityManager em = getEntityManager();

        try {
            return em.createQuery(
                    "SELECT a FROM Auditoria a WHERE a.usuario.id = :usuarioId AND a.accion = :accion",
                    Auditoria.class
            )
                    .setParameter("usuarioId", usuarioId)
                    .setParameter("accion", accion)
                    .getResultList();

        } finally {
            em.close();
        }
    }
}