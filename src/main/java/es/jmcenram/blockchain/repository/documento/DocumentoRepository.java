package es.jmcenram.blockchain.repository.documento;


import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

import static es.jmcenram.blockchain.config.JPAUtil.getEntityManager;

public class DocumentoRepository extends BaseRepository<Documento> {

    public DocumentoRepository() {
        super(Documento.class);
    }

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
}