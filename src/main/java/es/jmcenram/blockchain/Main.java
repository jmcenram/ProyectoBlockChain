package es.jmcenram.blockchain;
import es.jmcenram.blockchain.model.apikey.ApiKey;
import es.jmcenram.blockchain.model.auditoria.Auditoria;
import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.model.documento.EstadoDocumento;
import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.model.rol.Rol;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRol;
import es.jmcenram.blockchain.repository.apikey.ApiKeyRepository;
import es.jmcenram.blockchain.repository.auditoria.AuditoriaRepository;
import es.jmcenram.blockchain.repository.documento.DocumentoRepository;
import es.jmcenram.blockchain.repository.registroblockchain.RegistroBlockchainRepository;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {

        System.out.println("=== INICIO TEST PROYECTO BLOCKCHAIN CHEMA ===");

        // Repositories
        UsuarioRepository usuarioRepo = new UsuarioRepository();
        RolRepository rolRepo = new RolRepository();
        UsuarioRolRepository usuarioRolRepo = new UsuarioRolRepository();
        DocumentoRepository documentoRepo = new DocumentoRepository();
        RegistroBlockchainRepository registroRepo = new RegistroBlockchainRepository();
        AuditoriaRepository auditoriaRepo = new AuditoriaRepository();
        ApiKeyRepository apiKeyRepo = new ApiKeyRepository();

        // CREAR ROLES
        Rol admin = new Rol();
        admin.setNombre("ADMIN");

        Rol user = new Rol();
        user.setNombre("USER");

        rolRepo.save(admin);
        rolRepo.save(user);

        System.out.println("Roles creados.");

        // CREAR USUARIO
        Usuario usuario = new Usuario();
        usuario.setNombre("chema");
        usuario.setPassword("1234");
        usuario.setEmail("chema@blockchain.com");

        usuarioRepo.save(usuario);

        System.out.println("Usuario creado.");

        // ASIGNAR ROL A USUARIO
        UsuarioRol usuarioRol = new UsuarioRol(usuario, admin);
        usuarioRolRepo.save(usuarioRol);
        usuarioRolRepo.save(usuarioRol);

        System.out.println("Rol asignado al usuario.");

        // CREAR DOCUMENTO
        Documento documento = new Documento();
        documento.setNombre("Contrato_Prueba.pdf");
        documento.setHash("HASH_DOCUMENTO_123456");
        documento.setRutaArchivo("ruta");
        documento.setEstado(EstadoDocumento.BORRADOR);
        documento.setEmisor(usuario);

        documentoRepo.save(documento);

        System.out.println("Documento creado.");

        // CREAR REGISTRO BLOCKCHAIN
        RegistroBlockchain registro = new RegistroBlockchain();
        registro.setDocumento(documento);
        registro.setHashDocumento("HASH_DOC_123");
        registro.setDireccionContrato("0xABCDEF123456");
        registro.setTransactionHash("0xTRANSACTIONHASH");
        registro.setBloqueNumber(123456L);

        registroRepo.save(registro);

        System.out.println("Registro blockchain creado.");

        // CREAR AUDITORÍA
        Auditoria auditoria = new Auditoria();
        auditoria.setAccion("CREACION_DOCUMENTO");
        auditoria.setUsuario(usuario);
        auditoria.setFechaCreacion(LocalDateTime.now());

        auditoriaRepo.save(auditoria);

        System.out.println("Auditoría registrada.");

        // CREAR API KEY
        ApiKey apiKey = new ApiKey();
        apiKey.setClave(UUID.randomUUID().toString());

        apiKeyRepo.save(apiKey);

        System.out.println("API Key generada.");

        // PROBAR SOFT DELETE
        usuarioRepo.softDelete(usuario.getId());

        System.out.println("Usuario soft deleted.");

        System.out.println("=== FIN TEST COMPLETADO CORRECTAMENTE ===");
    }
}