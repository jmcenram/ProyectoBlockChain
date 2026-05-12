package es.jmcenram.blockchain.repository;

/**
 * EJEMPLOS DE USO DE LOS DAOs DEL SISTEMA BLOCKCHAIN
 *
 * Este archivo contiene ejemplos prácticos sobre cómo utilizar
 * cada uno de los repositorios del sistema.
 *
 * @author Jcena
 * @version 1.0
 */
public class EjemplosUsoDAOs {

    /**
     * EJEMPLOS DE ENTIDAD EMISORA
     */
    public static void ejemplosEntidadEmisora() {
        /*
        // Instanciar repositorio
        EntidadEmisoraRepository repo = new EntidadEmisoraRepository();

        // 1. CREAR
        EntidadEmisora emisora = new EntidadEmisora();
        emisora.setNombre("Universidad de León");
        emisora.setAddress("0x1A2B3C4D5E6F7G8H9I0J");
        emisora.setPrivateKey("0xPrivateKeyEncriptada");
        emisora.setActivo(1);

        EntidadEmisora guardada = repo.save(emisora);
        System.out.println("Emisora guardada con ID: " + guardada.getId());

        // 2. BUSCAR POR NOMBRE
        EntidadEmisora encontrada = repo.findByNombre("Universidad de León");
        if (encontrada != null) {
            System.out.println("Emisora encontrada: " + encontrada.getNombre());
        }

        // 3. BUSCAR POR ADDRESS
        EntidadEmisora porAddress = repo.findByAddress("0x1A2B3C4D5E6F7G8H9I0J");

        // 4. LISTAR TODAS LAS ACTIVAS
        List<EntidadEmisora> activas = repo.findAllActivas();
        activas.forEach(e -> System.out.println(e.getNombre()));

        // 5. VERIFICAR EXISTENCIA
        boolean existe = repo.existsByNombre("Universidad de León");

        // 6. ACTUALIZAR
        encontrada.setNombre("Universidad de León - Campus Central");
        repo.update(encontrada);

        // 7. SOFT DELETE (borrado lógico)
        repo.softDelete(encontrada.getId());
        // Después de esto, findAll() no lo mostrará
        */
    }

    /**
     * EJEMPLOS DE USUARIO
     */
    public static void ejemplosUsuario() {
        /*
        UsuarioRepository repo = new UsuarioRepository();

        // 1. CREAR USUARIO
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan Cena");
        usuario.setEmail("juan@example.com");
        usuario.setPassword(BCrypt.hashpw("MiPassword123", BCrypt.gensalt()));
        usuario.setActivo(true);

        Usuario guardado = repo.save(usuario);

        // 2. BUSCAR POR EMAIL
        Usuario por = repo.findByMail("juan@example.com");

        // 3. VERIFICAR SI EXISTE EMAIL
        boolean existeEmail = repo.existsByMail("juan@example.com");

        // 4. CAMBIAR CONTRASEÑA
        try {
            Usuario actualizado = repo.cambiarPassword(
                guardado.getId(),
                "MiPassword123",
                "NuevaPassword456"
            );
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
        }

        // 5. DESACTIVAR USUARIO (sin eliminarlo)
        repo.desactivarUsuario(guardado.getId());

        // 6. REACTIVAR USUARIO
        repo.activarUsuario(guardado.getId());

        // 7. BUSCAR POR NOMBRE PARCIAL
        List<Usuario> usuarios = repo.findByNombreContains("Juan");

        // 8. BUSCAR USUARIOS DE UNA ENTIDAD EMISORA
        List<Usuario> usuariosEntidad = repo.findByEntidadEmisoraAndActivo(1L);

        // 9. BUSCAR USUARIOS CON UN ROL ESPECÍFICO
        List<Usuario> admins = repo.findByRol(1L); // ID del rol ADMIN
        */
    }

    /**
     * EJEMPLOS DE ROL
     */
    public static void ejemplosRol() {
        /*
        RolRepository repo = new RolRepository();

        // 1. CREAR ROL
        Rol admin = new Rol();
        admin.setNombre("ADMIN");
        admin.setDescripcion("Administrador del sistema");
        Rol guardado = repo.save(admin);

        // 2. BUSCAR POR NOMBRE
        Rol encontrado = repo.findByNombre("ADMIN");

        // 3. BÚSQUEDA PARCIAL
        List<Rol> rolesConAdmin = repo.findByNombreContains("ADMIN");

        // 4. VERIFICAR EXISTENCIA
        boolean existe = repo.existsByNombre("ADMIN");

        // 5. LISTAR TODOS
        List<Rol> todosLosRoles = repo.findAll();
        */
    }

    /**
     * EJEMPLOS DE DOCUMENTO
     */
    public static void ejemplosDocumento() {
        /*
        DocumentoRepository repo = new DocumentoRepository();

        // 1. CREAR DOCUMENTO
        Documento doc = new Documento();
        doc.setNombre("Certificado de Nacimiento");
        doc.setTipo("PDF");
        doc.setRutaArchivo("/documentos/cert_123.pdf");
        doc.setHash("abc123def456..."); // SHA-256
        doc.setEstado(EstadoDocumento.PENDIENTE);

        Usuario emisor = new Usuario(); // Previamente cargado
        doc.setEmisor(emisor);

        Documento guardado = repo.save(doc);

        // 2. BUSCAR POR HASH
        Documento porHash = repo.findByHash("abc123def456...");

        // 3. LISTAR DOCUMENTOS PENDIENTES
        List<Documento> pendientes = repo.findPendientes();

        // 4. FILTRAR POR ESTADO
        List<Documento> registrados = repo.findByEstado("REGISTRADO");

        // 5. DOCUMENTOS DE UN USUARIO
        List<Documento> misDocumentos = repo.findByEmisor(usuarioId);

        // 6. DOCUMENTOS YA EN BLOCKCHAIN
        List<Documento> enBlockchain = repo.findRegistrados();

        // 7. CARGAR CON REGISTROS BLOCKCHAIN
        List<Documento> conRegistros = repo.obtenerTodosConRegistros();
        conRegistros.forEach(d -> {
            System.out.println(d.getNombre());
            d.getRegistros().forEach(r -> System.out.println("  - " + r.getTransactionHash()));
        });
        */
    }

    /**
     * EJEMPLOS DE REGISTRO BLOCKCHAIN
     */
    public static void ejemplosRegistroBlockchain() {
        /*
        RegistroBlockchainRepository repo = new RegistroBlockchainRepository();

        // 1. CREAR REGISTRO
        RegistroBlockchain registro = new RegistroBlockchain();
        registro.setDocumento(documento); // Documento ya cargado
        registro.setHashDocumento("abc123...");
        registro.setDireccionContrato("0xContractAddress");
        registro.setTransactionHash("0xTransactionHash");
        registro.setBloqueNumber(12345L);
        registro.setEstado(EstadoBlockchain.REGISTRADO);

        RegistroBlockchain guardado = repo.save(registro);

        // 2. BUSCAR POR ESTADO
        List<RegistroBlockchain> pendientes = repo.findByEstado(EstadoBlockchain.PENDIENTE);

        // 3. BUSCAR POR HASH DE TRANSACCIÓN
        RegistroBlockchain porTx = repo.findByTransactionHash("0xTransactionHash");

        // 4. REGISTROS DE UN DOCUMENTO
        List<RegistroBlockchain> registrosDoc = repo.findByDocumento(documentoId);

        // 5. LISTAR PENDIENTES
        List<RegistroBlockchain> porConfirmar = repo.findPendientes();
        */
    }

    /**
     * EJEMPLOS DE AUDITORÍA
     */
    public static void ejemplosAuditoria() {
        /*
        AuditoriaRepository repo = new AuditoriaRepository();

        // 1. REGISTRAR ACCIÓN
        Auditoria audit = new Auditoria();
        audit.setUsuario(usuario); // Usuario actual
        audit.setAccion("CREAR_DOCUMENTO");
        audit.setDescripcion("Documento de certificación creado");

        Auditoria guardada = repo.save(audit);

        // 2. AUDITORÍAS DE UN USUARIO
        List<Auditoria> porUsuario = repo.findByUsuario(usuarioId);

        // 3. AUDITORÍAS DE UNA ACCIÓN
        List<Auditoria> crearDocs = repo.findByAccion("CREAR_DOCUMENTO");

        // 4. COMBINACIÓN DE FILTROS
        List<Auditoria> usuarioAccion = repo.findByUsuarioAndAccion(
            usuarioId,
            "CREAR_DOCUMENTO"
        );
        */
    }

    /**
     * EJEMPLOS DE USUARIO-ROL
     */
    public static void ejemplosUsuarioRol() {
        /*
        UsuarioRolRepository repo = new UsuarioRolRepository();

        // 1. ASIGNAR ROL A USUARIO
        UsuarioRol ur = new UsuarioRol(usuario, rol);
        UsuarioRol guardado = repo.save(ur);

        // 2. RECUPERAR RELACIÓN
        UsuarioRolId id = new UsuarioRolId(usuarioId, rolId);
        UsuarioRol relacion = repo.findById(id);

        // 3. LISTAR TODAS LAS RELACIONES
        List<UsuarioRol> todas = repo.findAll();

        // 4. REMOVER ROL DE USUARIO
        repo.delete(id);
        */
    }

    /**
     * EJEMPLO COMPLETO: FLUJO DE REGISTRO DE DOCUMENTO EN BLOCKCHAIN
     */
    public static void ejemploFlujoCompleto() {
        /*
        // PASO 1: Obtener usuario y entidad emisora
        UsuarioRepository userRepo = new UsuarioRepository();
        Usuario usuario = userRepo.findByMail("juan@example.com");

        EntidadEmisoraRepository emisoraRepo = new EntidadEmisoraRepository();
        EntidadEmisora emisora = emisoraRepo.findByNombre("Universidad de León");

        // PASO 2: Crear documento
        DocumentoRepository docRepo = new DocumentoRepository();
        Documento documento = new Documento();
        documento.setNombre("Diploma de Grado");
        documento.setTipo("PDF");
        documento.setRutaArchivo("/documentos/diploma_123.pdf");
        documento.setHash("hash_sha256_del_doc");
        documento.setEstado("PENDIENTE");
        documento.setEmisor(usuario);

        Documento docGuardado = docRepo.save(documento);

        // PASO 3: Registrar en blockchain
        RegistroBlockchainRepository regRepo = new RegistroBlockchainRepository();
        RegistroBlockchain registro = new RegistroBlockchain();
        registro.setDocumento(docGuardado);
        registro.setHashDocumento(docGuardado.getHash());
        registro.setDireccionContrato("0xContractAddress");
        // ... después de interactuar con blockchain ...
        registro.setTransactionHash("0xTxHash");
        registro.setBloqueNumber(12345L);
        registro.setEstado("REGISTRADO");

        RegistroBlockchain regGuardado = regRepo.save(registro);

        // PASO 4: Registrar auditoría
        AuditoriaRepository auditRepo = new AuditoriaRepository();
        Auditoria audit = new Auditoria();
        audit.setUsuario(usuario);
        audit.setAccion("REGISTRAR_BLOCKCHAIN");
        audit.setDescripcion("Diploma registrado en blockchain - TX: " + regGuardado.getTransactionHash());

        auditRepo.save(audit);

        // PASO 5: Actualizar estado del documento
        docGuardado.setEstado("REGISTRADO");
        docGuardado.setTransactionHash(regGuardado.getTransactionHash());
        docGuardado.setFechaRegistroBlockchain(LocalDateTime.now());

        docRepo.update(docGuardado);
        */
    }
}

