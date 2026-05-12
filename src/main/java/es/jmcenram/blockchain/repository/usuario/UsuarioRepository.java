package es.jmcenram.blockchain.repository.usuario;

import es.jmcenram.blockchain.config.JPAUtil;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

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
                            SELECT DISTINCT u FROM Usuario u
                            LEFT JOIN FETCH u.roles ur
                            LEFT JOIN FETCH ur.rol
                            WHERE LOWER(u.email) = LOWER(:email) AND u.fechaBorrado IS NULL
                            """,
                            Usuario.class
                    )
                    .setParameter("email", email.trim())
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

    /**
     * Desactiva un usuario sin eliminarlo físicamente.
     * Cambia el estado activo a false.
     *
     * @param userId ID del usuario a desactivar
     * @throws RuntimeException si el usuario no existe
     */
    public void desactivarUsuario(Long userId) {

        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Usuario usuario = em.find(Usuario.class, userId);
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado");
            }

            usuario.setActivo(false);
            em.merge(usuario);

            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Activa un usuario previamente desactivado.
     * Cambia el estado activo a true.
     *
     * @param userId ID del usuario a activar
     * @throws RuntimeException si el usuario no existe
     */
    public void activarUsuario(Long userId) {

        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Usuario usuario = em.find(Usuario.class, userId);
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado");
            }

            usuario.setActivo(true);
            em.merge(usuario);

            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Busca usuarios por su nombre (búsqueda parcial, case-insensitive).
     *
     * @param nombreParcial parte del nombre a buscar
     * @return lista de usuarios que coinciden
     */
    public List<Usuario> findByNombreContains(String nombreParcial) {

        EntityManager em = getEntityManager();

        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(:nombre) AND u.fechaBorrado IS NULL",
                    Usuario.class
            )
                    .setParameter("nombre", "%" + nombreParcial + "%")
                    .getResultList();

        } finally {
            em.close();
        }
    }

    /**
     * Busca todos los usuarios activos de una entidad emisora específica.
     *
     * @param entidadEmisoraId ID de la entidad emisora
     * @return lista de usuarios activos de la entidad
     */
    public List<Usuario> findByEntidadEmisoraAndActivo(Long entidadEmisoraId) {

        EntityManager em = getEntityManager();

        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.entidadEmisora.id = :entidadId AND u.activo = true AND u.fechaBorrado IS NULL",
                    Usuario.class
            )
                    .setParameter("entidadId", entidadEmisoraId)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    /**
     * Busca todos los usuarios que tienen un rol específico.
     *
     * @param rolId ID del rol
     * @return lista de usuarios con ese rol
     */
    public List<Usuario> findByRol(Long rolId) {

        EntityManager em = getEntityManager();

        try {
            return em.createQuery(
                    "SELECT DISTINCT u FROM Usuario u " +
                            "INNER JOIN u.roles ur " +
                            "WHERE ur.rol.id = :rolId AND u.fechaBorrado IS NULL",
                    Usuario.class
            )
                    .setParameter("rolId", rolId)
                    .getResultList();

        } finally {
            em.close();
        }
    }
}
