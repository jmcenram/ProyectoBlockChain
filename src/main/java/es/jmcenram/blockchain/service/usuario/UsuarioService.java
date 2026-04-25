package es.jmcenram.blockchain.service.usuario;

import es.jmcenram.blockchain.model.rol.Rol;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.model.usuariorol.UsuarioRol;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.base.BaseService;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioService extends BaseService<Usuario> {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          UsuarioRolRepository usuarioRolRepository) {

        super(usuarioRepository);
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    public Usuario login(String username, String password) {

        Usuario user = usuarioRepository.findByMail(username);

        if (user == null) {
            throw new RuntimeException();
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());


        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException();
        }

        return user;
    }
    // =========================
    // 👤 CREAR USER
    // =========================
    public Usuario crearUsuario(String nombre, String email, String password, boolean activo) {

        if (usuarioRepository.existsByMail(email)) {
            throw new RuntimeException("El usuario ya existe");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(hash);
        usuario.setActivo(activo);

        usuario = usuarioRepository.save(usuario);

        // 🔥 asignar rol USER
        Rol rolUser = rolRepository.findByNombre("USER");

        if (rolUser == null) {
            throw new RuntimeException("Rol USER no existe");
        }

        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(usuario);
        ur.setRol(rolUser);

        usuarioRolRepository.save(ur);

        return usuario;
    }

    // =========================
    // 👑 CREAR ADMIN
    // =========================
    public Usuario crearAdmin(String nombre, String email, String password) {

        if (usuarioRepository.existsByMail(email)) {
            throw new RuntimeException("El usuario ya existe");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(hash);
        usuario.setActivo(false);

        usuario = usuarioRepository.save(usuario);

        // 🔥 asignar rol ADMIN
        Rol rolAdmin = rolRepository.findByNombre("ADMIN");

        if (rolAdmin == null) {
            throw new RuntimeException("Rol ADMIN no existe");
        }

        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(usuario);
        ur.setRol(rolAdmin);

        usuarioRolRepository.save(ur);

        return usuario;
    }

    public Usuario activarUsuario(String email, String nuevaPassword) {

        Usuario usuario = usuarioRepository.findByMail(email);

        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // 🔒 validar contraseña (opcional pero recomendable)
        if (nuevaPassword == null || nuevaPassword.length() < 8) {
            throw new RuntimeException("Contraseña no válida");
        }

        // 🔐 hash
        String hash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt());

        // 🔥 actualizar
        usuario.setPassword(hash);
        usuario.setActivo(true);

        return usuarioRepository.update(usuario);
    }

    public boolean existsByMail(String email) {
        return usuarioRepository.existsByMail(email);
    }
}
