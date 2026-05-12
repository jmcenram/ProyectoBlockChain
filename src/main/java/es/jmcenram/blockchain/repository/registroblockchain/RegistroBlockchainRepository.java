package es.jmcenram.blockchain.repository.registroblockchain;


import es.jmcenram.blockchain.model.registroblockchain.EstadoBlockchain;
import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Repositorio encargado del acceso a datos de RegistroBlockchain.
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
public class RegistroBlockchainRepository extends BaseRepository<RegistroBlockchain> {

    /**
     * Constructor que inicializa el repositorio con la clase RegistroBlockchain.
     */
    public RegistroBlockchainRepository() {
        super(RegistroBlockchain.class);
    }

    /**
     * Busca todos los registros blockchain con un estado determinado.
     * Solo retorna registros no borrados (fechaBorrado IS NULL).
     *
     * Estados posibles: PENDIENTE, REGISTRADO, REVOCADO, ERROR
     *
     * @param estado el estado blockchain a filtrar
     * @return lista de registros con el estado especificado
     */
    public List<RegistroBlockchain> findByEstado(EstadoBlockchain estado) {

        EntityManager em = getEntityManager();

        try {
            return em.createQuery(
                            "FROM RegistroBlockchain r WHERE r.estado = :estado AND r.fechaBorrado IS NULL",
                            RegistroBlockchain.class
                    )
                    .setParameter("estado", estado)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    /**
     * Busca un registro blockchain por su hash de transacción.
     *
     * @param transactionHash hash de la transacción en blockchain
     * @return RegistroBlockchain si se encuentra, null en caso contrario
     */
    public RegistroBlockchain findByTransactionHash(String transactionHash) {

        EntityManager em = getEntityManager();

        try {
            return em.createQuery(
                            "SELECT r FROM RegistroBlockchain r WHERE r.transactionHash = :txHash AND r.fechaBorrado IS NULL",
                            RegistroBlockchain.class
                    )
                    .setParameter("txHash", transactionHash)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            em.close();
        }
    }

    /**
     * Busca todos los registros de un documento específico.
     *
     * @param documentoId ID del documento
     * @return lista de registros blockchain del documento
     */
    public List<RegistroBlockchain> findByDocumento(Long documentoId) {

        EntityManager em = getEntityManager();

        try {
            return em.createQuery(
                            "SELECT r FROM RegistroBlockchain r WHERE r.documento.id = :docId AND r.fechaBorrado IS NULL",
                            RegistroBlockchain.class
                    )
                    .setParameter("docId", documentoId)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    /**
     * Busca registros pendientes de confirmación en blockchain.
     * (estado = PENDIENTE)
     *
     * @return lista de registros pendientes
     */
    public List<RegistroBlockchain> findPendientes() {

        EntityManager em = getEntityManager();

        try {
            return em.createQuery(
                            "SELECT r FROM RegistroBlockchain r WHERE r.estado = es.jmcenram.blockchain.model.registroblockchain.EstadoBlockchain.PENDIENTE AND r.fechaBorrado IS NULL",
                            RegistroBlockchain.class
                    )
                    .getResultList();

        } finally {
            em.close();
        }
    }
}