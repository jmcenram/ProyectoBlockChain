package es.jmcenram.blockchain.service.base;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.repository.base.BaseRepository;

import java.util.List;

public abstract class BaseService<T extends EntidadBase> {

    protected final BaseRepository<T> repository;

    protected BaseService(BaseRepository<T> repository) {
        this.repository = repository;
    }

    public T guardar(T entity) {
        return repository.save(entity);
    }

    public T obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public List<T> obtenerTodos() {
        return repository.findAll();
    }

    public void eliminar(Long id) {
        repository.softDelete(id);
    }
}