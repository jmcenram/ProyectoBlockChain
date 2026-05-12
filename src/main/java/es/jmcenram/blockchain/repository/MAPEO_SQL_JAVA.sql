/*
 * MAPEO DE TABLAS SQL A DAOS JAVA
 *
 * Este archivo documenta cómo cada tabla SQL se mapea a su correspondiente
 * entidad JPA y DAO en Java.
 *
 * Autor: Jcena
 * Versión: 1.0
 */

-- ========================================================================
-- TABLE: entidad_emisora
-- ========================================================================
-- Campos SQL                  → Atributo Java              → DAO
-- id                         → EntidadEmisora.id          → EntidadEmisoraRepository
-- nombre                     → EntidadEmisora.nombre      → findByNombre()
-- private_key                → EntidadEmisora.privateKey  → findByPrivateKey()
-- activo                     → EntidadEmisora.activo      → findAllActivas()
-- address                    → EntidadEmisora.address     → findByAddress()
-- fecha_creacion             → EntidadEmisora.fechaCreacion (heredada)
-- fecha_borrado              → EntidadEmisora.fechaBorrado (heredada)
--
-- Entidad Java: es.jmcenram.blockchain.model.entidademisora.EntidadEmisora
-- Repository: es.jmcenram.blockchain.repository.entidademisora.EntidadEmisoraRepository
-- Métodos principales:
--   • save() - Guardar nueva entidad
--   • findById(Long) - Buscar por ID
--   • findAll() - Listar todas (solo no borradas)
--   • findByNombre(String) - Buscar por nombre
--   • findByAddress(String) - Buscar por address blockchain
--   • findAllActivas() - Solo entidades activas
--   • existsByNombre(String) - Verificar existencia
--   • softDelete(Long) - Borrado lógico
--   • update(EntidadEmisora) - Actualizar


-- ========================================================================
-- TABLE: rol
-- ========================================================================
-- Campos SQL                  → Atributo Java              → DAO
-- id                         → Rol.id                     → RolRepository
-- nombre                     → Rol.nombre                 → findByNombre()
-- descripcion                → Rol.descripcion            → (búsqueda general)
-- fecha_creacion             → Rol.fechaCreacion (heredada)
-- fecha_borrado              → Rol.fechaBorrado (heredada)
--
-- Entidad Java: es.jmcenram.blockchain.model.rol.Rol
-- Repository: es.jmcenram.blockchain.repository.rol.RolRepository
-- Métodos principales:
--   • save(Rol)
--   • findById(Long)
--   • findAll() - Todos los roles activos
--   • findByNombre(String) - Rol por nombre único
--   • findByNombreContains(String) - Búsqueda parcial (LIKE)
--   • existsByNombre(String)
--   • softDelete(Long)


-- ========================================================================
-- TABLE: usuario
-- ========================================================================
-- Campos SQL                  → Atributo Java              → DAO
-- id                         → Usuario.id                 → UsuarioRepository
-- nombre                     → Usuario.nombre             → findByNombreContains()
-- email                      → Usuario.email              → findByMail()
-- password                   → Usuario.password           → cambiarPassword()
-- activo                     → Usuario.activo             → desactivarUsuario(), activarUsuario()
-- entidad_emisora_id         → Usuario.entidadEmisora     → findByEntidadEmisoraAndActivo()
-- fecha_creacion             → Usuario.fechaCreacion (heredada)
-- fecha_borrado              → Usuario.fechaBorrado (heredada)
--
-- Entidad Java: es.jmcenram.blockchain.model.usuario.Usuario
-- Repository: es.jmcenram.blockchain.repository.usuario.UsuarioRepository
-- Métodos principales:
--   • save(Usuario)
--   • findById(Long)
--   • findAll()
--   • findByMail(String) - Buscar por email único (carga roles eagerly)
--   • existsByMail(String)
--   • cambiarPassword(Long, String, String) - Con validación BCrypt
--   • desactivarUsuario(Long) - Sin eliminar
--   • activarUsuario(Long)
--   • findByNombreContains(String) - Búsqueda parcial
--   • findByEntidadEmisoraAndActivo(Long) - Usuarios de una entidad
--   • findByRol(Long) - Usuarios con un rol específico


-- ========================================================================
-- TABLE: documento
-- ========================================================================
-- Campos SQL                  → Atributo Java              → DAO
-- id                         → Documento.id               → DocumentoRepository
-- nombre                     → Documento.nombre           → búsqueda general
-- descripcion                → Documento.descripcion      → búsqueda general
-- tipo                       → Documento.tipo             → búsqueda general
-- ruta_archivo               → Documento.rutaArchivo      → búsqueda general
-- hash                       → Documento.hash             → findByHash()
-- estado                     → Documento.estado           → findByEstado(), findPendientes()
-- fecha_registro_blockchain  → Documento.fechaRegistroBlockchain
-- transaction_hash           → Documento.transactionHash  → findRegistrados()
-- emisor_id                  → Documento.emisor (ManyToOne)
-- contenido                  → Documento.contenido        → búsqueda general
-- fecha_creacion             → Documento.fechaCreacion (heredada)
-- fecha_borrado              → Documento.fechaBorrado (heredada)
--
-- Entidad Java: es.jmcenram.blockchain.model.documento.Documento
-- Repository: es.jmcenram.blockchain.repository.documento.DocumentoRepository
-- Métodos principales:
--   • save(Documento)
--   • findById(Long)
--   • findAll()
--   • findByHash(String) - Búsqueda por SHA-256
--   • findByEstado(String) - Filtrar por estado
--   • findPendientes() - Estado = PENDIENTE
--   • findByEmisor(Long) - Documentos de un usuario
--   • findRegistrados() - Con transaction_hash válido
--   • obtenerTodosConRegistros() - Carga eager de registros blockchain


-- ========================================================================
-- TABLE: registro_blockchain
-- ========================================================================
-- Campos SQL                  → Atributo Java              → DAO
-- id                         → RegistroBlockchain.id      → RegistroBlockchainRepository
-- documento_id               → RegistroBlockchain.documento (ManyToOne)
-- hash_documento             → RegistroBlockchain.hashDocumento
-- direccion_contrato         → RegistroBlockchain.direccionContrato
-- transaction_hash           → RegistroBlockchain.transactionHash → findByTransactionHash()
-- bloque_number              → RegistroBlockchain.bloqueNumber
-- estado                     → RegistroBlockchain.estado  → findByEstado(), findPendientes()
-- fecha_creacion             → RegistroBlockchain.fechaCreacion (heredada)
-- fecha_borrado              → RegistroBlockchain.fechaBorrado (heredada)
--
-- Entidad Java: es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain
-- Repository: es.jmcenram.blockchain.repository.registroblockchain.RegistroBlockchainRepository
-- Métodos principales:
--   • save(RegistroBlockchain)
--   • findById(Long)
--   • findAll()
--   • findByEstado(EstadoBlockchain) - Estados: PENDIENTE, REGISTRADO, REVOCADO, ERROR
--   • findByTransactionHash(String) - Por hash de transacción
--   • findByDocumento(Long) - Registros de un documento
--   • findPendientes() - Estado = PENDIENTE


-- ========================================================================
-- TABLE: auditoria
-- ========================================================================
-- Campos SQL                  → Atributo Java              → DAO
-- id                         → Auditoria.id               → AuditoriaRepository
-- usuario_id                 → Auditoria.usuario (ManyToOne)
-- accion                     → Auditoria.accion           → findByAccion()
-- descripcion                → Auditoria.descripcion      → búsqueda general
-- fecha_creacion             → Auditoria.fechaCreacion (heredada)
-- fecha_borrado              → Auditoria.fechaBorrado (heredada)
--
-- Entidad Java: es.jmcenram.blockchain.model.auditoria.Auditoria
-- Repository: es.jmcenram.blockchain.repository.auditoria.AuditoriaRepository
-- Métodos principales:
--   • save(Auditoria)
--   • findById(Long)
--   • findAll()
--   • findByUsuario(Long) - Auditorías de un usuario
--   • findByAccion(String) - Auditorías de una acción
--   • findByUsuarioAndAccion(Long, String) - Combinación de filtros
--   • softDelete(Long)


-- ========================================================================
-- TABLE: usuario_rol (Relación Muchos-a-Muchos)
-- ========================================================================
-- Campos SQL                  → Atributo Java              → DAO
-- usuario_id                 → UsuarioRol.usuario (ManyToOne)
-- rol_id                     → UsuarioRol.rol (ManyToOne)
-- fecha_creacion             → UsuarioRol.fechaCreacion
-- fecha_borrado              → UsuarioRol.fechaBorrado
--
-- Clave Primaria Compuesta: (usuario_id, rol_id)
-- Entidad Java: es.jmcenram.blockchain.model.usuariorol.UsuarioRol
-- Entidad ID: es.jmcenram.blockchain.model.usuariorol.UsuarioRolId
-- Repository: es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository
-- Métodos principales:
--   • save(UsuarioRol) - Asignar rol a usuario
--   • findById(UsuarioRolId) - Buscar relación específica
--   • findAll() - Todas las asignaciones
--   • delete(UsuarioRolId) - Remover rol


-- ========================================================================
-- RESUMEN DE OPERACIONES HEREDADAS (BaseRepository)
-- ========================================================================

-- Estas operaciones están disponibles en TODOS los repositorios:

-- 1. INSERT/UPDATE
--    public T save(T entity)
--    - Si entity.id == null: INSERT con AUTOINCREMENT
--    - Si entity.id != null: UPDATE (merge)
--    - Retorna la entidad con ID generado

-- 2. SELECT por ID
--    public T findById(Long id)
--    - Retorna null si no existe
--    - Excluye registros borrados (fechaBorrado IS NULL)

-- 3. SELECT todos
--    public List<T> findAll()
--    - Retorna lista vacía si no hay
--    - Siempre excluye borrados

-- 4. SOFT DELETE (Borrado Lógico)
--    public void softDelete(Long id)
--    - Marca fechaBorrado = NOW()
--    - No elimina físicamente
--    - Transacción ACID con rollback en caso de error

-- 5. UPDATE
--    public T update(T entity)
--    - Realiza merge y flush
--    - Transacción ACID


-- ========================================================================
-- FECHAS AUTOMÁTICAS
-- ========================================================================

-- Todas las entidades heredan:
-- • fecha_creacion (LocalDateTime) - Se asigna automáticamente con LocalDateTime.now()
-- • fecha_borrado (LocalDateTime) - NULL hasta que se elimine lógicamente

-- Esto permite:
-- - Trazabilidad completa de cada operación
-- - Auditoría de cambios
-- - Impedir pérdida de datos


-- ========================================================================
-- TRANSACCIONES Y MANEJO DE ERRORES
-- ========================================================================

-- Todos los repositorios:
-- Manejan transacciones automáticamente
-- Realiza commit en caso de éxito
-- Realiza rollback automático en caso de excepción
-- Cierra EntityManager en el bloque finally
-- Lanza RuntimeException si hay errores


-- ========================================================================
-- VALIDACIONES DE NEGOCIO
-- ========================================================================

-- UsuarioRepository:
--   • cambiarPassword(): Valida que contraseña sea >8 caracteres
--   • cambiarPassword(): Valida BCrypt con contraseña actual
--   • cambiarPassword(): No permite reutilizar contraseña anterior

-- EntidadEmisoraRepository:
--   • findByNombre(): case-insensitive (LOWER)
--   • existsByNombre(): Búsqueda case-insensitive

-- UsuarioRepository:
--   • findByMail(): Carga roles eagerly para evitar N+1 queries
--   • findByMail(): case-sensitive (según implementación actual)


-- ========================================================================
-- RECOMENDACIONES
-- ========================================================================

1. Siempre usar soft-delete en lugar de delete() para mantener auditoría
2. Las búsquedas nunca retornan registros con fechaBorrado != NULL
3. Usar queries específicas en lugar de findAll() para grandes volúmenes
4. Cerrar EntityManager automáticamente (ya lo hacen los repositorios)
5. Las contraseñas siempre se almacenan hasheadas (BCrypt)
6. Las claves privadas se almacenan cifradas

