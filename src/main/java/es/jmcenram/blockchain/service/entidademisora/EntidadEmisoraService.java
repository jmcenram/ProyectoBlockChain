package es.jmcenram.blockchain.service.entidademisora;

import es.jmcenram.blockchain.model.entidademisora.EntidadEmisora;
import es.jmcenram.blockchain.repository.entidademisora.EntidadEmisoraRepository;
import es.jmcenram.blockchain.service.base.BaseService;

import java.util.List;

/**
 * Servicio encargado de la logica de negocio de EntidadEmisora.
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
public class EntidadEmisoraService extends BaseService<EntidadEmisora> {

    // =========================
    // CONSTANTES
    // =========================
    /** Longitud mínima requerida para nombre de entidad */
    private static final int MIN_NOMBRE_LENGTH = 3;

    /** Longitud mínima requerida para address */
    private static final int MIN_ADDRESS_LENGTH = 20;

    /** Longitud mínima requerida para private key */
    private static final int MIN_PRIVATE_KEY_LENGTH = 32;

    /** Repositorio para operaciones de EntidadEmisora */
    private final EntidadEmisoraRepository repository;

    /**
     * Constructor que inicializa el servicio con su repositorio.
     * Si no se proporciona repositorio, se crea una instancia por defecto.
     *
     * @param repository repositorio para EntidadEmisora
     */
    public EntidadEmisoraService(EntidadEmisoraRepository repository) {
        super(repository);
        this.repository = repository;
    }

    /**
     * Constructor por defecto que instancia automáticamente el repositorio.
     * Utilizado por el controlador para simplificar la creación del servicio.
     */
    public EntidadEmisoraService() {
        this(new EntidadEmisoraRepository());
    }

    // =========================
    // MÉTODOS DE ACCESO DIRECTO (usados por el controlador)
    // =========================

    /**
     * Guarda una entidad emisora (crea o actualiza).
     * Wrapper sobre el método guardar() de BaseService.
     *
     * @param entity entidad a guardar
     * @return EntidadEmisora guardada
     */
    public EntidadEmisora save(EntidadEmisora entity) {
        return super.guardar(entity);
    }

    /**
     * Elimina una entidad de forma lógica (soft delete).
     * Wrapper sobre el método eliminar() de BaseService.
     *
     * @param entity entidad a eliminar
     */
    public void delete(EntidadEmisora entity) {
        if (entity != null && entity.getId() != null) {
            super.eliminar(entity.getId());
        }
    }

    /**
     * Obtiene todas las entidades emisoras activas.
     * Wrapper sobre el método del repositorio para acceso directo.
     *
     * @return lista de EntidadEmisora activas
     */
    public List<EntidadEmisora> findAllActivas() {
        return repository.findAllActivas();
    }

    /**
     * Obtiene todas las entidades emisoras (activas e inactivas).
     * Wrapper sobre el método obtenerTodos() de BaseService.
     *
     * @return lista de todas las EntidadEmisora
     */
    public List<EntidadEmisora> findAll() {
        return super.obtenerTodos();
    }

    // =========================
    // MÉTODOS DE LÓGICA DE NEGOCIO
    // =========================

    /**
     * Crea una nueva entidad emisora con validaciones de seguridad.
     * Verifica que nombre y address sean únicos.
     * Inicializa la entidad como activa.
     *
     * @param nombre nombre de la entidad emisora (min. {@value MIN_NOMBRE_LENGTH} caracteres)
     * @param address dirección pública de wallet (min. {@value MIN_ADDRESS_LENGTH} caracteres)
     * @param privateKey clave privada de wallet (min. {@value MIN_PRIVATE_KEY_LENGTH} caracteres)
     * @return EntidadEmisora creada y persistida
     * @throws RuntimeException si datos inválidos, nombre/address ya existen, o error de persistencia
     */
    public EntidadEmisora crearEntidad(String nombre, String address, String privateKey) {

        validarDatosCreacion(nombre, address, privateKey);
        validarUnicidadDatos(nombre, address);

        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setNombre(nombre.trim());
        entidad.setPrivateKey(privateKey.trim());
        entidad.setActivo(true);

        return repository.save(entidad);
    }

    /**
     * Busca una entidad emisora por su nombre.
     *
     * @param nombre nombre de la entidad a buscar
     * @return EntidadEmisora si se encuentra
     * @throws RuntimeException si nombre no existe o es borrada
     */
    public EntidadEmisora obtenerPorNombre(String nombre) {

        validarNombre(nombre);

        EntidadEmisora entidad = repository.findByNombre(nombre);

        if (entidad == null) {
            throw new RuntimeException("Entidad emisora no encontrada con nombre: " + nombre);
        }

        return entidad;
    }

    /**
     * Busca una entidad emisora por su address (dirección de wallet).
     *
     * @param address dirección pública de wallet
     * @return EntidadEmisora si se encuentra
     * @throws RuntimeException si address no existe o es borrada
     */
    public EntidadEmisora obtenerPorAddress(String address) {

        validarAddress(address);

        EntidadEmisora entidad = repository.findByAddress(address);

        if (entidad == null) {
            throw new RuntimeException("Entidad emisora no encontrada con address: " + address);
        }

        return entidad;
    }

    /**
     * Busca una entidad emisora por su clave privada.
     * Operación sensible desde el punto de vista de seguridad.
     *
     * @param privateKey clave privada de wallet
     * @return EntidadEmisora si se encuentra
     * @throws RuntimeException si privateKey no existe o es borrada
     */
    public EntidadEmisora obtenerPorPrivateKey(String privateKey) {

        validarPrivateKey(privateKey);

        EntidadEmisora entidad = repository.findByPrivateKey(privateKey);

        if (entidad == null) {
            throw new RuntimeException("Entidad emisora no encontrada con esa clave privada");
        }

        return entidad;
    }

    /**
     * Obtiene todas las entidades emisoras activas.
     * Útil para mostrar listados de emisores disponibles en la UI.
     *
     * @return lista de EntidadEmisora activas, ordenadas por nombre
     */
    public List<EntidadEmisora> obtenerTodasActivas() {
        return repository.findAllActivas();
    }

    /**
     * Activa una entidad emisora por su ID.
     * Cambia el campo activo a true.
     *
     * @param id ID de la entidad a activar
     * @return EntidadEmisora actualizada
     * @throws RuntimeException si ID inválido o entidad no existe
     */
    public EntidadEmisora activar(Long id) {

        validarId(id);

        EntidadEmisora entidad = repository.findById(id);

        if (entidad == null) {
            throw new RuntimeException("Entidad emisora no encontrada con ID: " + id);
        }

        entidad.setActivo(true);

        return repository.update(entidad);
    }

    /**
     * Desactiva una entidad emisora por su ID.
     * Cambia el campo activo a false sin eliminar el registro.
     *
     * @param id ID de la entidad a desactivar
     * @return EntidadEmisora actualizada
     * @throws RuntimeException si ID inválido o entidad no existe
     */
    public EntidadEmisora desactivar(Long id) {

        validarId(id);

        EntidadEmisora entidad = repository.findById(id);

        if (entidad == null) {
            throw new RuntimeException("Entidad emisora no encontrada con ID: " + id);
        }

        entidad.setActivo(false);

        return repository.update(entidad);
    }

    /**
     * Actualiza los datos de una entidad emisora existente.
     * Valida que nombre y address sean únicos (excepto el actual si no cambia).
     *
     * @param id ID de la entidad a actualizar
     * @param nombre nuevo nombre
     * @param address nuevo address
     * @param privateKey nueva clave privada
     * @param activo nuevo estado de activacion
     * @return EntidadEmisora actualizada
     * @throws RuntimeException si datos inválidos o entidad no existe
     */
    public EntidadEmisora actualizar(Long id, String nombre, String address, String privateKey, Boolean activo) {

        validarId(id);
        validarDatosCreacion(nombre, address, privateKey);

        EntidadEmisora entidad = repository.findById(id);

        if (entidad == null) {
            throw new RuntimeException("Entidad emisora no encontrada con ID: " + id);
        }

        // Validar unicidad: si cambió el nombre, verificar que el nuevo no exista
        if (!nombre.equalsIgnoreCase(entidad.getNombre())) {
            if (repository.existsByNombre(nombre)) {
                throw new RuntimeException("Ya existe una entidad emisora con el nombre: " + nombre);
            }
        }


        entidad.setNombre(nombre.trim());
        entidad.setPrivateKey(privateKey.trim());
        entidad.setActivo(activo);

        return repository.update(entidad);
    }

    /**
     * Verifica si existe una entidad emisora con el nombre especificado.
     *
     * @param nombre nombre a verificar
     * @return true si existe entidad activa con ese nombre, false en caso contrario
     */
    public boolean existeNombre(String nombre) {
        validarNombre(nombre);
        return repository.existsByNombre(nombre);
    }

    /**
     * Verifica si existe una entidad emisora con el address especificado.
     *
     * @param address address a verificar
     * @return true si existe entidad activa con ese address, false en caso contrario
     */
    public boolean existeAddress(String address) {
        validarAddress(address);
        return repository.existsByAddress(address);
    }

    // =========================
    // MÉTODOS DE VALIDACIÓN PRIVADOS
    // =========================

    /**
     * Valida que el nombre sea válido.
     *
     * @param nombre nombre a validar
     * @throws RuntimeException si nombre inválido
     */
    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("Nombre no puede estar vacío");
        }
        if (nombre.trim().length() < MIN_NOMBRE_LENGTH) {
            throw new RuntimeException("Nombre debe tener al menos " + MIN_NOMBRE_LENGTH + " caracteres");
        }
        if (nombre.length() > 150) {
            throw new RuntimeException("Nombre no puede exceder 150 caracteres");
        }
    }

    /**
     * Valida que el address (dirección de wallet) sea válido.
     *
     * @param address address a validar
     * @throws RuntimeException si address inválido
     */
    private void validarAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("Address no puede estar vacío");
        }
        if (address.trim().length() < MIN_ADDRESS_LENGTH) {
            throw new RuntimeException("Address debe tener al menos " + MIN_ADDRESS_LENGTH + " caracteres");
        }
        if (address.length() > 100) {
            throw new RuntimeException("Address no puede exceder 100 caracteres");
        }
    }

    /**
     * Valida que la clave privada sea válida.
     *
     * @param privateKey clave privada a validar
     * @throws RuntimeException si privateKey inválida
     */
    private void validarPrivateKey(String privateKey) {
        if (privateKey == null || privateKey.trim().isEmpty()) {
            throw new RuntimeException("Clave privada no puede estar vacía");
        }
        if (privateKey.trim().length() < MIN_PRIVATE_KEY_LENGTH) {
            throw new RuntimeException("Clave privada debe tener al menos " + MIN_PRIVATE_KEY_LENGTH + " caracteres");
        }
        if (privateKey.length() > 255) {
            throw new RuntimeException("Clave privada no puede exceder 255 caracteres");
        }
    }

    /**
     * Valida que el ID sea válido (positivo y no nulo).
     *
     * @param id ID a validar
     * @throws RuntimeException si ID inválido
     */
    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID inválido: " + id);
        }
    }

    /**
     * Valida todos los datos requeridos para creación/actualización.
     *
     * @param nombre nombre a validar
     * @param address address a validar
     * @param privateKey clave privada a validar
     * @throws RuntimeException si algún dato inválido
     */
    private void validarDatosCreacion(String nombre, String address, String privateKey) {
        validarNombre(nombre);
        validarAddress(address);
        validarPrivateKey(privateKey);
    }

    /**
     * Valida que no existan registros duplicados para nombre y address.
     *
     * @param nombre nombre a verificar
     * @param address address a verificar
     * @throws RuntimeException si nombre o address ya existen
     */
    private void validarUnicidadDatos(String nombre, String address) {
        if (repository.existsByNombre(nombre)) {
            throw new RuntimeException("Ya existe una entidad emisora con el nombre: " + nombre);
        }
        if (repository.existsByAddress(address)) {
            throw new RuntimeException("Ya existe una entidad emisora con el address: " + address);
        }
    }
}

