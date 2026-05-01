package es.jmcenram.blockchain.service.base;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.repository.base.BaseRepository;

import java.util.List;

/**
 * Servicio encargado de la logica de negocio de Base.
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
public abstract class BaseService<T extends EntidadBase> {

    /** Repositorio genérico que gestiona la persistencia de la entidad */
    protected final BaseRepository<T> repository;

    /**
     * Constructor protegido que inicializa el servicio con su repositorio.
     *
     * @param repository el repositorio genérico de la entidad
     */
    protected BaseService(BaseRepository<T> repository) {
        this.repository = repository;
    }

    /**
     * Guarda o actualiza una entidad en la base de datos a través del repositorio.
     *
     * @param entity la entidad a guardar o actualizar
     * @return la entidad guardada (con ID generado si era nuevo)
     */
    public T guardar(T entity) {
        return repository.save(entity);
    }

    /**
     * Obtiene una entidad por su ID primario.
     *
     * @param id el ID de la entidad buscada
     * @return la entidad encontrada
     */
    public T obtenerPorId(Long id) {
        return repository.findById(id);
    }

    /**
     * Obtiene todas las entidades activas (no borradas) de la base de datos.
     *
     * @return lista de todas las entidades activas
     */
    public List<T> obtenerTodos() {
        return repository.findAll();
    }

    /**
     * Marca una entidad como borrada (soft-delete) usando su ID.
     * La entidad no se elimina físicamente, solo se marca como inactiva.
     *
     * @param id el ID de la entidad a eliminar lógicamente
     */
    public void eliminar(Long id) {
        repository.softDelete(id);
    }
}