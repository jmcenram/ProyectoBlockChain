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

/**
 * Servicio encargado de la logica de negocio de BlockchainUpdate.
 *
 * Permite:
 * - Validar reglas antes de persistir cambios
 * - Coordinar repositorios relacionados
 * - Exponer operaciones usadas por controladores u otros servicios
 *
 * Forma parte de la capa de servicio y mantiene la logica fuera de la interfaz.
 *
 * @author Jcena
 * @version 1.0
 */
public class BlockchainUpdateService {

    /**
     * Actualiza el registro blockchain y el documento después de registrar un hash en la blockchain.
     * Establece el estado a REGISTRADO, guarda el txHash y la fecha de registro.
     *
     * Operación transaccional: todos los cambios se hacen en una misma transacción:
     * - Actualiza RegistroBlockchain con txHash y estado REGISTRADO
     * - Actualiza Documento con fechaRegistroBlockchain
     * - Crea registro de auditoría con mensaje "REGISTRO_BLOCKCHAIN"
     *
     * Si el registro o documento no existen, hace rollback silenciosamente.
     *
     * @param registroId ID del RegistroBlockchain a actualizar
     * @param documentoId ID del Documento correspondiente
     * @param usuario usuario que realizó la operación (para auditoría)
     * @param txHash hash de transacción de blockchain (para traceabilidad)
     */
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

            System.out.println("REGISTRO ACTUALIZADO EN DB");

        } catch (Exception e) {

            if (tx.isActive()) tx.rollback();
            e.printStackTrace();

        } finally {
            em.close();
        }
    }

    /**
     * Actualiza el registro blockchain después de revocar un documento en la blockchain.
     * Establece el estado a REVOCADO y guarda el txHash de la revocación.
     *
     * Operación transaccional: todos los cambios en una misma transacción:
     * - Actualiza RegistroBlockchain con txHash y estado REVOCADO
     * - Crea registro de auditoría con mensaje "REVOCACION_BLOCKCHAIN"
     *
     * Si el registro no existe, hace rollback silenciosamente.
     *
     * @param registroId ID del RegistroBlockchain a revocación
     * @param usuario usuario que realizó la operación (para auditoría)
     * @param txHash hash de transacción de revocación en blockchain
     */
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

            System.out.println("REVOCACION ACTUALIZADA EN DB");

        } catch (Exception e) {

            if (tx.isActive()) tx.rollback();
            e.printStackTrace();

        } finally {
            em.close();
        }
    }

    /**
     * Marca un registro blockchain en estado ERROR cuando falla la operación en blockchain.
     * Se invoca desde DocumentoService si la transacción async rechaza o falla.
     *
     * Operación transaccional simple que solo actualiza el estado a ERROR.
     *
     * @param registroId ID del RegistroBlockchain que falló
     */
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

            System.out.println("ERROR GUARDADO EN DB");

        } catch (Exception e) {

            if (tx.isActive()) tx.rollback();
            e.printStackTrace();

        } finally {
            em.close();
        }
    }
}
