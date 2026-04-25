package es.jmcenram.blockchain.service.blockchain;

import es.jmcenram.blockchain.config.JPAUtil;
import es.jmcenram.blockchain.model.auditoria.Auditoria;
import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.model.registroblockchain.EstadoBlockchain;
import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.model.usuario.Usuario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDateTime;

public class BlockchainUpdateService {

    // =========================
    // REGISTRO
    // =========================
    public void actualizarRegistro(Long registroId, Long documentoId, Usuario usuario, String txHash) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            RegistroBlockchain registro = em.find(RegistroBlockchain.class, registroId);
            Documento documento = em.find(Documento.class, documentoId);

            if (registro == null || documento == null) {
                tx.rollback();
                return;
            }

            registro.setTransactionHash(txHash);
            registro.setEstado(EstadoBlockchain.REGISTRADO);

            documento.setFechaRegistroBlockchain(LocalDateTime.now());

            em.merge(registro);
            em.merge(documento);

            Auditoria auditoria = new Auditoria();
            auditoria.setAccion("REGISTRO_BLOCKCHAIN");
            auditoria.setUsuario(usuario);
            auditoria.setFechaCreacion(LocalDateTime.now());

            em.persist(auditoria);

            tx.commit();

            System.out.println("🟢 REGISTRO ACTUALIZADO EN DB");

        } catch (Exception e) {

            if (tx.isActive()) tx.rollback();
            e.printStackTrace();

        } finally {
            em.close();
        }
    }

    // =========================
    // REVOCACIÓN
    // =========================
    public void actualizarRevocacion(Long registroId, Usuario usuario, String txHash) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            RegistroBlockchain registro = em.find(RegistroBlockchain.class, registroId);

            if (registro == null) {
                tx.rollback();
                return;
            }

            registro.setTransactionHash(txHash);
            registro.setEstado(EstadoBlockchain.REVOCADO);

            em.merge(registro);

            Auditoria auditoria = new Auditoria();
            auditoria.setAccion("REVOCACION_BLOCKCHAIN");
            auditoria.setUsuario(usuario);
            auditoria.setFechaCreacion(LocalDateTime.now());

            em.persist(auditoria);

            tx.commit();

            System.out.println("🟠 REVOCACIÓN ACTUALIZADA EN DB");

        } catch (Exception e) {

            if (tx.isActive()) tx.rollback();
            e.printStackTrace();

        } finally {
            em.close();
        }
    }

    // =========================
    // ERROR
    // =========================
    public void marcarError(Long registroId) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            RegistroBlockchain registro = em.find(RegistroBlockchain.class, registroId);

            if (registro != null) {
                registro.setEstado(EstadoBlockchain.ERROR);
                em.merge(registro);
            }

            tx.commit();

            System.out.println("🔴 ERROR GUARDADO EN DB");

        } catch (Exception e) {

            if (tx.isActive()) tx.rollback();
            e.printStackTrace();

        } finally {
            em.close();
        }
    }
}