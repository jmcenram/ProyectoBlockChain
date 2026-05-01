package es.jmcenram.blockchain.repository.auditoria;


import es.jmcenram.blockchain.model.auditoria.Auditoria;
import es.jmcenram.blockchain.repository.base.BaseRepository;

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
}