package es.jmcenram.blockchain.service.usuario;

import es.jmcenram.blockchain.model.rol.Rol;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas encargada de validar el comportamiento de UsuarioService.
 *
 * Verifica:
 * - Casos correctos esperados
 * - Entradas invalidas o errores controlados
 * - Colaboracion con dependencias mockeadas cuando aplica
 *
 * Forma parte de la suite de pruebas automatizadas del proyecto.
 *
 * @author Jcena
 * @version 1.0
 */
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private UsuarioRolRepository usuarioRolRepository;

    private UsuarioService usuarioService;

    /**
     * Prepara el entorno necesario para la prueba.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuarioService = new UsuarioService(usuarioRepository, rolRepository, usuarioRolRepository);
    }

    /**
     * Comprueba que la consulta de existencia delegue en el repositorio para un email valido.
     *
     * El servicio usa este resultado para impedir duplicados antes de crear usuarios.
     */
    @Test
    void testExistsByMail_ValidEmail() {
        // Given
        String email = "test@example.com";
        when(usuarioRepository.existsByMail(email)).thenReturn(true);

        // When
        boolean result = usuarioService.existsByMail(email);

        // Then
        assertTrue(result);
        verify(usuarioRepository).existsByMail(email);
    }

    /**
     * Comprueba que un email nulo no llegue al repositorio como consulta valida.
     *
     * El servicio debe cortar entradas obligatorias ausentes antes de acceder a persistencia.
     */
    @Test
    void testExistsByMail_InvalidEmail() {
        // Given
        String email = null;

        // When & Then
        assertThrows(RuntimeException.class, () -> usuarioService.existsByMail(email));
    }

    /**
     * Comprueba que el login devuelva el usuario cuando email y password son correctos.
     *
     * El caso fija la colaboracion entre busqueda por email y validacion BCrypt.
     */
    @Test
    void testLogin_Success() {
        // Given
        String email = "test@example.com";
        String password = "password";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword("$2a$10$abcdefghijklmnopqrstuvABCDEFGHI"); // Mock hash
        when(usuarioRepository.findByMail(email)).thenReturn(usuario);

        // Mock BCrypt.checkpw
        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.checkpw(password, usuario.getPassword())).thenReturn(true);

            // When
            Usuario result = usuarioService.login(email, password);

            // Then
            assertEquals(usuario, result);
            verify(usuarioRepository).findByMail(email);
        }
    }

    /**
     * Comprueba que el login falle cuando el repositorio no encuentra el usuario.
     *
     * La autenticacion no debe continuar con passwords si no existe una cuenta asociada al email.
     */
    @Test
    void testLogin_UserNotFound() {
        // Given
        String email = "notfound@example.com";
        String password = "password";
        when(usuarioRepository.findByMail(email)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> usuarioService.login(email, password));
    }

    /**
     * Comprueba que una password incorrecta rechace el inicio de sesion.
     *
     * El test protege la separacion entre usuario existente y credenciales validas.
     */
    @Test
    void testLogin_WrongPassword() {
        // Given
        String email = "test@example.com";
        String password = "wrong";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword("$2a$10$correcthash");
        when(usuarioRepository.findByMail(email)).thenReturn(usuario);

        // When & Then
        assertThrows(RuntimeException.class, () -> usuarioService.login(email, password));
    }

    /**
     * Comprueba que la creacion de usuario persista usuario, rol y relacion usuario-rol.
     *
     * El registro necesita completar esas tres piezas para que permisos y login funcionen despues.
     */
    @Test
    void testCrearUsuario_Success() {
        // Given
        String nombre = "Test User";
        String email = "test@example.com";
        String password = "password123";
        boolean esAdmin = false;
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("USER");
        when(usuarioRepository.existsByMail(email)).thenReturn(false);
        when(rolRepository.findByNombre("USER")).thenReturn(rol);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // When
        Usuario result = usuarioService.crearUsuario(nombre, email, password, esAdmin);

        // Then
        assertNotNull(result);
        verify(usuarioRepository).existsByMail(email);
        verify(rolRepository).findByNombre("USER");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(usuarioRolRepository).save(any());
    }

    /**
     * Comprueba que no se cree un usuario cuando el email ya esta registrado.
     *
     * El caso protege la unicidad de cuentas y evita relaciones de rol duplicadas.
     */
    @Test
    void testCrearUsuario_UserAlreadyExists() {
        // Given
        String nombre = "Test User";
        String email = "existing@example.com";
        String password = "password123";
        boolean esAdmin = false;
        when(usuarioRepository.existsByMail(email)).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> usuarioService.crearUsuario(nombre, email, password, esAdmin));
    }

    /**
     * Comprueba que activar un usuario marque la cuenta como activa y actualice la password.
     *
     * El flujo representa la primera configuracion de credenciales tras crear una cuenta pendiente.
     */
    @Test
    void testActivarUsuario_Success() {
        // Given
        String email = "test@example.com";
        String nuevaPassword = "newpassword123";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword("oldhash");
        when(usuarioRepository.findByMail(email)).thenReturn(usuario);
        when(usuarioRepository.update(any(Usuario.class))).thenReturn(usuario);

        // When
        Usuario result = usuarioService.activarUsuario(email, nuevaPassword);

        // Then
        assertNotNull(result);
        assertTrue(result.isActivo());
        verify(usuarioRepository).findByMail(email);
        verify(usuarioRepository).update(any(Usuario.class));
    }

    /**
     * Comprueba que el cambio de password valide primero la clave actual.
     *
     * El servicio solo debe persistir la nueva password cuando BCrypt confirma la credencial anterior.
     */
    @Test
    void testCambiarPassword_Success() {
        // Given
        Long userId = 1L;
        String actual = "oldpassword";
        String nueva = "newpassword123";
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        usuario.setPassword("$2a$10$oldhash");
        when(usuarioRepository.findById(userId)).thenReturn(usuario);
        when(usuarioRepository.cambiarPassword(userId, actual, nueva)).thenReturn(usuario);

        // Mock BCrypt.checkpw
        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.checkpw(actual, usuario.getPassword())).thenReturn(true);

            // When
            Usuario result = usuarioService.cambiarPassword(userId, actual, nueva);

            // Then
            assertEquals(usuario, result);
            verify(usuarioRepository).findById(userId);
            verify(usuarioRepository).cambiarPassword(userId, actual, nueva);
        }
    }

    /**
     * Comprueba que actualizar el nombre modifique el usuario existente y lo persista.
     *
     * El perfil depende de este flujo para guardar cambios sin alterar el resto de datos de cuenta.
     */
    @Test
    void testActualizarNombre_Success() {
        // Given
        Long userId = 1L;
        String nombre = "New Name";
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        usuario.setNombre("Old Name");
        when(usuarioRepository.findById(userId)).thenReturn(usuario);
        when(usuarioRepository.update(any(Usuario.class))).thenReturn(usuario);

        // When
        Usuario result = usuarioService.actualizarNombre(userId, nombre);

        // Then
        assertEquals(nombre, result.getNombre());
        verify(usuarioRepository).findById(userId);
        verify(usuarioRepository).update(any(Usuario.class));
    }

    /**
     * Comprueba que una busqueda por id valido devuelva el usuario del repositorio.
     *
     * El servicio conserva esta operacion como acceso controlado a datos de perfil y permisos.
     */
    @Test
    void testFindById_ValidId() {
        // Given
        Long id = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(id);
        when(usuarioRepository.findById(id)).thenReturn(usuario);

        // When
        Usuario result = usuarioService.findById(id);

        // Then
        assertEquals(usuario, result);
        verify(usuarioRepository).findById(id);
    }

    /**
     * Comprueba que un id nulo sea rechazado antes de consultar el repositorio.
     *
     * La validacion evita llamadas de persistencia con identificadores obligatorios ausentes.
     */
    @Test
    void testFindById_InvalidId() {
        // Given
        Long id = null;

        // When & Then
        assertThrows(RuntimeException.class, () -> usuarioService.findById(id));
    }
}

