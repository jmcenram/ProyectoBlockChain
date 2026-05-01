package es.jmcenram.blockchain.service.usuario;

import es.jmcenram.blockchain.controller.login.Session;
import es.jmcenram.blockchain.model.entidademisora.EntidadEmisora;
import es.jmcenram.blockchain.model.rol.Rol;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRol;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.base.BaseService;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Servicio encargado de la logica de negocio de Usuario.
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
public class UsuarioService extends BaseService<Usuario> {

    // =========================
    // CONSTANTES
    // =========================
    /** Nombre del rol de usuario regular */
    private static final String ROL_USER = "USER";

    /** Nombre del rol de usuario administrador */
    private static final String ROL_ADMIN = "ADMIN";

    /** Nombre del rol maestro con permisos de infraestructura */
    private static final String ROL_MASTER = "MASTER";

    /** Longitud mínima requerida para contraseña */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /** Repositorio para operaciones de Usuario */
    private final UsuarioRepository usuarioRepository;

    /** Repositorio para operaciones de Rol */
    private final RolRepository rolRepository;

    /** Repositorio para operaciones de UsuarioRol (relación M:M) */
    private final UsuarioRolRepository usuarioRolRepository;

    /**
     * Constructor que inicializa el servicio con sus repositorios especializados.
     *
     * @param usuarioRepository repositorio para Usuario
     * @param rolRepository repositorio para Rol
     * @param usuarioRolRepository repositorio para relación UsuarioRol
     */
    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          UsuarioRolRepository usuarioRolRepository) {

        super(usuarioRepository);
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    /**
     * Realiza autenticación de usuario con email y contraseña.
     * Valida el formato de email y contraseña.
     * Utiliza BCrypt para verificar la contraseña guardada.
     *
     * @param email email del usuario (case-insensitive en búsqueda)
     * @param password contraseña sin hashear (será hasheada para comparación)
     * @return Usuario autenticado si credenciales son válidas
     * @throws RuntimeException si email no existe o contraseña es incorrecta
     */
    public Usuario login(String email, String password) {

        validarEmail(email);
        validarPassword(password);

        Usuario user = usuarioRepository.findByMail(email);

        if (user == null) {
            throw new RuntimeException("Usuario no encontrado con el email: " + email);
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return user;
    }

    /**
     * Crea un nuevo usuario con rol asignado.
     * Valida datos antes de crear.
     * Asigna automáticamente rol USER o ADMIN según parámetro.
     * Contraseña se hashea automáticamente.
     *
     * Datos del usuario:
     * - Nombre: se trimea
     * - Email: se convierte a minúsculas y se trimea
     * - Password: se hashea con BCrypt
     * - Activo: inicia como false (debe activar contraseña después)
     *
     * @param nombre nombre completo del usuario (mín. 2 caracteres)
     * @param email email único (debe ser válido y no existir)
     * @param password contraseña (mín. 8 caracteres)
     * @param esAdmin true para crear ADMIN, false para USER
     * @return Usuario creado con rol asignado
     * @throws RuntimeException si datos inválidos o email ya existe
     */
    public Usuario crearUsuario(String nombre, String email, String password, boolean esAdmin) {

        return crearUsuario(nombre, email, password, esAdmin ? ROL_ADMIN : ROL_USER, null);
    }

    /**
     * Crea un nuevo usuario con rol asignado y entidad emisora asociada.
     * Similar a crearUsuario(String, String, String, boolean) pero permite asignar
     * una entidad emisora al usuario (típicamente para usuarios ADMIN).
     *
     * Valida datos antes de crear.
     * Asigna automáticamente rol USER o ADMIN según parámetro.
     * Contraseña se hashea automáticamente.
     *
     * Datos del usuario:
     * - Nombre: se trimea
     * - Email: se convierte a minúsculas y se trimea
     * - Password: se hashea con BCrypt
     * - Activo: inicia como false (debe activar contraseña después)
     * - EntidadEmisora: asociación opcional
     *
     * @param nombre nombre completo del usuario (mín. 2 caracteres)
     * @param email email único (debe ser válido y no existir)
     * @param password contraseña (mín. 8 caracteres)
     * @param esAdmin true para crear ADMIN, false para USER
     * @param entidadEmisora entidad emisora a asociar (puede ser null)
     * @return Usuario creado con rol asignado y entidad emisora
     * @throws RuntimeException si datos inválidos o email ya existe
     */
    public Usuario crearUsuario(String nombre, String email, String password,
                                boolean esAdmin, EntidadEmisora entidadEmisora) {

        return crearUsuario(nombre, email, password, esAdmin ? ROL_ADMIN : ROL_USER, entidadEmisora);
    }

    /**
     * Crea un nuevo usuario asignando explicitamente el rol indicado.
     *
     * Permite registrar usuarios USER, ADMIN o MASTER sin convertir la capa de
     * presentacion en responsable de persistir relaciones usuario-rol.
     *
     * @param nombre nombre completo del usuario
     * @param email email unico del usuario
     * @param password contrasena temporal que se enviara al usuario
     * @param nombreRol rol que se asignara al usuario
     * @param entidadEmisora entidad emisora opcional asociada al usuario
     * @return usuario creado con su rol y entidad asociada cuando corresponde
     */
    public Usuario crearUsuario(String nombre, String email, String password,
                                String nombreRol, EntidadEmisora entidadEmisora) {

        validarDatosCreacion(nombre, email, password);
        validarRolCreacion(nombreRol);
        validarUsuarioNoExiste(email);

        Usuario usuario = crearUsuarioBase(nombre, email, password);
        Rol rol = encontrarRol(nombreRol);

        guardarRelacionUsuarioRol(usuario, rol);

        if (entidadEmisora != null) {
            usuario.setEntidadEmisora(entidadEmisora);
            usuarioRepository.update(usuario);
        }

        return usuario;
    }

    /**
     * Guarda la relación usuario-rol en la BD.
     * Utiliza @MapsId para clave compuesta automática.
     *
     * @param usuario usuario al que asignar rol
     * @param rol rol a asignar
     */
    private void guardarRelacionUsuarioRol(Usuario usuario, Rol rol) {

        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(usuario);
        ur.setRol(rol);

        // NO setId()
        // Hibernate lo hace automáticamente con @MapsId

        usuarioRolRepository.save(ur);
    }

    /**
     * Activa un usuario nuevo estableciendo contraseña y marcando como activo.
     * Típicamente se invoca tras primer login cuando usuario =está inactivo.
     *
     * @param email email del usuario a activar
     * @param nuevaPassword contraseña nueva (será hasheada)
     * @return Usuario actualizado y activado
     * @throws RuntimeException si email no existe o contraseña inválida
     */
    public Usuario activarUsuario(String email, String nuevaPassword) {

        validarEmail(email);
        validarNuevaPassword(nuevaPassword);

        Usuario usuario = usuarioRepository.findByMail(email);

        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado con el email: " + email);
        }

        String hash = hashearPassword(nuevaPassword);
        usuario.setPassword(hash);
        usuario.setActivo(true);

        return usuarioRepository.update(usuario);
    }

    /**
     * Verifica si existe un usuario con el email especificado.
     *
     * @param email email a verificar
     * @return true si existe usuario activo con ese email, false en caso contrario
     * @throws RuntimeException si email tiene formato inválido
     */
    public boolean existsByMail(String email) {
        validarEmail(email);
        return usuarioRepository.existsByMail(email);
    }

    /**
     * Cambia la contraseña de un usuario validando contraseña actual.
     * Impide reutilizar la misma contraseña.
     * Actualiza la sesión con el usuario changed.
     *
     * @param userId ID del usuario
     * @param actual contraseña actual sin hashear
     * @param nueva contraseña nueva sin hashear (mín. 8 caracteres)
     * @return Usuario actualizado
     * @throws RuntimeException si usuario no existe, contraseña actual incorrecta, o nueva inválida
     */
    public Usuario cambiarPassword(Long userId, String actual, String nueva) {

        validarNuevaPassword(nueva);

        // evitar reutilizar la misma
        if (nueva.equals(actual)) {
            throw new RuntimeException("No puedes usar la misma contraseña");
        }

        Usuario usuario = usuarioRepository.findById(userId);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        // verificar contraseña actual
        if (!BCrypt.checkpw(actual, usuario.getPassword())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        Usuario actualizado = usuarioRepository.cambiarPassword(userId, actual, nueva);
        Session.setUsuario(actualizado);

        return actualizado;
    }

    /**
     * Actualiza el nombre del usuario.
     *
     * @param userId ID del usuario
     * @param nombre nuevo nombre
     * @return Usuario actualizado
     * @throws RuntimeException si usuario no existe o nombre inválido
     */
    public Usuario actualizarNombre(Long userId, String nombre) {

        validarNombre(nombre);

        Usuario usuario = usuarioRepository.findById(userId);

        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        usuario.setNombre(nombre);

        return usuarioRepository.update(usuario);
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws RuntimeException si ID inválido o usuario no existe
     */
    public Usuario findById(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID de usuario inválido: " + id);
        }
        return usuarioRepository.findById(id);
    }

    /**
     * Actualiza un usuario existente.
     *
     * @param usuario usuario con cambios
     * @return Usuario actualizado
     * @throws RuntimeException si usuario null o sin ID
     */
    public Usuario update(Usuario usuario) {

        if (usuario == null) {
            throw new RuntimeException("Usuario no puede ser null");
        }

        if (usuario.getId() == null) {
            throw new RuntimeException("ID de usuario no puede ser null");
        }

        return usuarioRepository.update(usuario);
    }

    /**
     * Valida que el email tenga formato correcto.
     *
     * @param email email a validar
     * @throws RuntimeException si vacío o con formato inválido
     */
    private void validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email no puede estar vacío");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("Formato de email inválido");
        }
    }

    /**
     * Valida que la contraseña no esté vacía.
     *
     * @param password contraseña a validar
     * @throws RuntimeException si vacía
     */
    private void validarPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Contraseña no puede estar vacía");
        }
    }

    /**
     * Valida que la nueva contraseña cumpla requisitos de seguridad.
     *
     * @param password contraseña a validar (mín. MIN_PASSWORD_LENGTH caracteres)
     * @throws RuntimeException si no cumple requisitos
     */
    private void validarNuevaPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
    }

    /**
     * Valida que el nombre sea válido (no vacío, mín. 2 caracteres).
     *
     * @param nombre nombre a validar
     * @throws RuntimeException si inválido
     */
    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("Nombre no puede estar vacío");
        }
        if (nombre.length() < 2) {
            throw new RuntimeException("Nombre debe tener al menos 2 caracteres");
        }
    }

    /**
     * Valida todos los datos para creación de usuario.
     *
     * @param nombre nombre a validar
     * @param email email a validar
     * @param password contraseña a validar
     * @throws RuntimeException si algún dato inválido
     */
    private void validarDatosCreacion(String nombre, String email, String password) {
        validarNombre(nombre);
        validarEmail(email);
        validarNuevaPassword(password);
    }

    /**
     * Verifica que no exista usuario con el email especificado.
     *
     * @param email email a verificar
     * @throws RuntimeException si ya existe usuario con ese email
     */
    private void validarUsuarioNoExiste(String email) {
        if (usuarioRepository.existsByMail(email)) {
            throw new RuntimeException("Ya existe un usuario con el email: " + email);
        }
    }

    /**
     * Crea instancia base de Usuario con datos iniciales.
     * Email se convierte a minúsculas, contraseña se hashea, activo=false.
     *
     * @param nombre nombre del usuario
     * @param email email del usuario
     * @param password contraseña a hashear
     * @return Usuario creado (no persistido aún)
     */
    private Usuario crearUsuarioBase(String nombre, String email, String password) {
        String hash = hashearPassword(password);

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setEmail(email.toLowerCase().trim());
        usuario.setPassword(hash);
        usuario.setActivo(false);

        return usuarioRepository.save(usuario);
    }

    /**
     * Hashea una contraseña en texto plano usando BCrypt.
     * Genera salt automáticamente.
     *
     * @param password contraseña en texto plano
     * @return hash seguro BCrypt
     */
    private String hashearPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Busca un rol por su nombre.
     *
     * @param nombreRol nombre del rol a encontrar (ej: "USER", "ADMIN")
     * @return Rol encontrado
     * @throws RuntimeException si rol no existe en el sistema
     */
    private Rol encontrarRol(String nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol);
        if (rol == null) {
            throw new RuntimeException("Rol '" + nombreRol + "' no existe en el sistema");
        }
        return rol;
    }

    /**
     * Valida que el rol solicitado sea uno de los roles soportados por la aplicacion.
     *
     * Mantiene la entrada de registro limitada a USER, ADMIN y MASTER aunque en
     * base de datos existan roles historicos o de otros flujos.
     *
     * @param nombreRol rol solicitado desde la interfaz
     */
    private void validarRolCreacion(String nombreRol) {
        if (!ROL_USER.equals(nombreRol) &&
                !ROL_ADMIN.equals(nombreRol) &&
                !ROL_MASTER.equals(nombreRol)) {
            throw new RuntimeException("Rol no permitido para creacion de usuario: " + nombreRol);
        }
    }

    /**
     * Guarda un usuario directamente en base de datos.
     *
     * Este método se utiliza principalmente en flujos externos como:
     * - Registro desde UI
     * - Importaciones
     *
     * No asigna roles automáticamente ni valida duplicados,
     * por lo que debe usarse con precaución.
     *
     * @param usuario usuario a persistir
     * @return usuario guardado
     */
    public Usuario save(Usuario usuario) {

        if (usuario == null) {
            throw new RuntimeException("Usuario no puede ser null");
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new RuntimeException("Email obligatorio");
        }

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new RuntimeException("Password obligatorio");
        }

        // Normalizar email
        usuario.setEmail(usuario.getEmail().toLowerCase().trim());

        // Hashear si no está hasheada
        if (!usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(hashearPassword(usuario.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Alias de compatibilidad para existsByMail.
     *
     * Se utiliza para mantener coherencia con naming en controllers
     * donde se usa "email" en lugar de "mail".
     *
     * @param email email a verificar
     * @return true si existe usuario, false en caso contrario
     */
    public boolean existsByEmail(String email) {
        return existsByMail(email);
    }
}
