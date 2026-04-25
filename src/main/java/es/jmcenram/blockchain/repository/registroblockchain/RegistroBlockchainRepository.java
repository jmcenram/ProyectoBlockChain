package es.jmcenram.blockchain.repository.registroblockchain;


import es.jmcenram.blockchain.model.registroblockchain.EstadoBlockchain;
import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class RegistroBlockchainRepository extends BaseRepository<RegistroBlockchain> {

    public RegistroBlockchainRepository() {
        super(RegistroBlockchain.class);
    }


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
}