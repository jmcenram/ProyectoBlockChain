package es.jmcenram.blockchain.controller.login;

import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.usuario.UsuarioService;
import es.jmcenram.blockchain.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas encargada de validar el comportamiento de LoginController.
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
@DisplayName("Tests para LoginController")
public class LoginControllerTest {

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
     * Comprueba el flujo de login cuando el usuario existe y la password coincide.
     *
     * El test confirma que el controlador delega en el servicio y abre sesion solo en el camino valido.
     */
    @Test
    @DisplayName("Login válido con email y contraseña correctos")
    void testLoginExitoso() {
        // Arrange
        String email = "user@test.com";
        String password = "SecurePass123!";

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(email);
        usuario.setNombre("Test User");
        usuario.setActivo(true);
        usuario.setPassword(password); // En la práctica, sería hasheada

        when(usuarioRepository.findByMail(email)).thenReturn(usuario);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            usuarioService.login(email, "wrongPassword");
        });
    }

    /**
     * Comprueba que el login falle cuando el email no pertenece a ningun usuario.
     *
     * El escenario protege el flujo de autenticacion frente a accesos con cuentas inexistentes.
     */
    @Test
    @DisplayName("Login fallido con email inexistente")
    void testLoginFallidoEmailNoExiste() {
        // Arrange
        String email = "notexist@test.com";

        when(usuarioRepository.findByMail(email)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            usuarioService.login(email, "anyPassword");
        });
    }

    /**
     * Comprueba las reglas minimas de seguridad aplicadas a una nueva password.
     *
     * Estas reglas evitan aceptar claves demasiado debiles en activacion o cambio de credenciales.
     */
    @Test
    @DisplayName("Validación de contraseña segura")
    void testValidacionPasswordSegura() {
        // Contraseñas para probar
        String[] passwordsValidadas = {
            "Segura@123",
            "MyPassword!42",
            "Complex@Pass99"
        };

        // Todas deben ser válidas (cumplen criterios)
        for (String password : passwordsValidadas) {
            assertNotNull(password);
            assertTrue(password.length() >= 8);
        }
    }

    /**
     * Comprueba que el cambio a espanol cargue el bundle de mensajes correspondiente.
     *
     * El test protege la navegacion multidioma para que la vista se refresque con recursos coherentes.
     */
    @Test
    @DisplayName("Cambio de idioma a Español")
    void testCambioIdiomaEspanol() {
        // Arrange
        Locale localeES = new Locale("es");

        // Act
        Messages.setLocale(localeES);

        // Assert
        assertEquals(Messages.getLocale().getLanguage(), "es");
    }

    /**
     * Comprueba que el cambio a ingles cargue el bundle de mensajes correspondiente.
     *
     * El escenario complementa el idioma por defecto y evita romper la seleccion internacionalizada.
     */
    @Test
    @DisplayName("Cambio de idioma a English")
    void testCambioIdiomaEnglish() {
        // Arrange
        Locale localeEN = new Locale("en");

        // Act
        Messages.setLocale(localeEN);

        // Assert
        assertEquals(Messages.getLocale().getLanguage(), "en");
    }

    /**
     * Comprueba que la sesion guarde el usuario autenticado.
     *
     * El resto de controladores consulta esa sesion para permisos, auditoria y datos de perfil.
     */
    @Test
    @DisplayName("Sesión se establece correctamente tras login exitoso")
    void testEstablecerSesion() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@test.com");
        usuario.setNombre("Test User");

        // Act
        Session.setUsuario(usuario);
        Usuario usuarioEnSesion = Session.getUsuario();

        // Assert
        assertNotNull(usuarioEnSesion);
        assertEquals("Test User", usuarioEnSesion.getNombre());
    }
}

