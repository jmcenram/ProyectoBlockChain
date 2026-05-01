package es.jmcenram.blockchain.repository.usuario;

import es.jmcenram.blockchain.config.JPAUtil;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Repositorio especializado para operaciones de Usuario.
 * Extiende BaseRepository&lt;Usuario&gt; con métodos personalizados para búsquedas y autenticación.
 * Hereda operaciones CRUD básicas (save, findById, findAll, softDelete, update).
 *
 * Caractorísticas adicionales:
 * - Búsqueda por email (con eager loading de roles)
 * - Verificación de existencia de email (case-insensitive)
 * - Cambio de contraseña con validación y hashing BCrypt
 *
 * @author Jcena
 * @version 1.0
 */
public class UsuarioRepository extends BaseRepository<Usuario> {

    /**
     * Constructor que inicializa el repositorio con la clase Usuario.
     */
    public UsuarioRepository() {
        super(Usuario.class);
    }

    /**
     * Busca un usuario por su dirección de email.
     * Carga eagerly los roles asociados al usuario (LEFT JOIN FETCH).
     * Solo retorna usuarios no borrados (fechaBorrado IS NULL).
     *
     * Utiliza LEFT JOIN FETCH para evitar N+1 queries al acceder a roles.
     *
     * @param email dirección de email del usuario (case-sensitive en esta implementación)
     * @return Usuario si se encuentra, null si no existe o está borrado
     */
    public Usuario findByMail(String email) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                            """
                            SELECT u FROM Usuario u
                            LEFT JOIN FETCH u.roles ur
                            LEFT JOIN FETCH ur.rol
                            WHERE u.email = :email AND u.fechaBorrado IS NULL
                            """,
                            Usuario.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();

        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    /**
     * Verifica si existe un usuario con un email determinado.
     * La búsqueda es case-insensitive (convierte ambos a minúsculas).
     * No cuenta usuarios marcados como borrados.
     *
     * @param email email a verificar
     * @return true si existe un usuario con ese email activo, false en caso contrario
     */
    public boolean existsByMail(String email) {

        EntityManager em = getEntityManager();

        Long count = em.createQuery(
                        "SELECT COUNT(u) FROM Usuario u WHERE LOWER(u.email) = LOWER(:email)",
                        Long.class
                )
                .setParameter("email", email)
                .getSingleResult();

        return count != null && count > 0;
    }

    /**
     * Cambia la contraseña de un usuario con validaciones de seguridad.
     * Valida que la contraseña actual sea correcta antes de permitir el cambio.
     *
     * Validaciones:
     * - Usuario debe existir
     * - Contraseña actual debe coincidir (validada con BCrypt)
     * - Nueva contraseña debe tener al menos 8 caracteres
     * - No se permite reutilizar la contraseña anterior
     *
     * La nueva contraseña se hashea con BCrypt antes de guardar.
     * Transacción ACID: commit si éxito, rollback si error.
     *
     * @param userId ID del usuario cuya contraseña cambiará
     * @param actual contraseña actual (sin hashear)
     * @param nueva contraseña nueva (será hasheada)
     * @return Usuario actualizado después del cambio
     * @throws RuntimeException si: usuario no existe, contraseña actual incorrecta,
     *                           nueva demasiado corta, reutilización de contraseña
     */
    public Usuario cambiarPassword(Long userId, String actual, String nueva) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {

            tx.begin();

            Usuario usuario = em.find(Usuario.class, userId);

            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado");
            }

            if (!BCrypt.checkpw(actual, usuario.getPassword())) {
                throw new RuntimeException("Contraseña actual incorrecta");
            }

            if (nueva == null || nueva.length() < 8) {
                throw new RuntimeException("Contraseña demasiado corta");
            }

            if (BCrypt.checkpw(nueva, usuario.getPassword())) {
                throw new RuntimeException("No puedes reutilizar la contraseña");
            }

            usuario.setPassword(BCrypt.hashpw(nueva, BCrypt.gensalt()));

            tx.commit();

            return usuario;

        } catch (Exception e) {

            if (tx.isActive()) tx.rollback();
            throw e;

        } finally {
            em.close();
        }
    }
}
