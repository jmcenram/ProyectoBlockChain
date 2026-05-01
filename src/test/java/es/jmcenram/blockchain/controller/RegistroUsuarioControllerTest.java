package es.jmcenram.blockchain.controller;

import es.jmcenram.blockchain.model.rol.ui.RolItem;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.usuario.UsuarioService;
import es.jmcenram.blockchain.util.Messages;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas encargada de validar el comportamiento de RegistroUsuarioController.
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
class RegistroUsuarioControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private UsuarioRolRepository usuarioRolRepository;
    @Mock
    private UsuarioService usuarioService;

    private RegistroUsuarioController controller;

    /**
     * Prepara el entorno necesario para la prueba.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new RegistroUsuarioController();
        // No mockeamos JavaFX components porque no se pueden mockear fácilmente
    }

    /**
     * Comprueba que la password temporal generada tenga longitud y caracteres permitidos.
     *
     * El registro de usuarios depende de este valor para crear credenciales iniciales seguras y comunicables por correo.
     */
    @Test
    void testGenerarPassword() {
        // Given
        RegistroUsuarioController controller = new RegistroUsuarioController();

        // When
        String password = controller.generarPassword();

        // Then
        assertNotNull(password);
        assertEquals(10, password.length());
        // Verificar que contiene caracteres válidos
        assertTrue(password.matches("[A-Za-z0-9!@#]*"));
    }

    /**
     * Comprueba que un correo con estructura normal sea aceptado por la validacion del registro.
     *
     * Este caso cubre el camino correcto antes de permitir crear un usuario.
     */
    @Test
    void testEsEmailValido_ValidEmail() {
        // Given
        RegistroUsuarioController controller = new RegistroUsuarioController();
        String email = "test@example.com";

        // When
        boolean result = controller.esEmailValido(email);

        // Then
        assertTrue(result);
    }

    /**
     * Comprueba que una cadena sin formato de correo sea rechazada.
     *
     * Evita que el registro guarde identificadores que no podrian usarse despues para login o notificaciones.
     */
    @Test
    void testEsEmailValido_InvalidEmail() {
        // Given
        RegistroUsuarioController controller = new RegistroUsuarioController();
        String email = "invalid-email";

        // When
        boolean result = controller.esEmailValido(email);

        // Then
        assertFalse(result);
    }

    /**
     * Comprueba que un correo nulo se considere invalido.
     *
     * El caso evita errores por null y fuerza a que el formulario trate el campo como obligatorio.
     */
    @Test
    void testEsEmailValido_NullEmail() {
        // Given
        RegistroUsuarioController controller = new RegistroUsuarioController();
        String email = null;

        // When
        boolean result = controller.esEmailValido(email);

        // Then
        assertFalse(result);
    }

    // Nota: Los tests de validarCampos requieren mocking de JavaFX, lo cual es problemático.
    // Para tests unitarios completos, se recomienda refactorizar el controlador para separar la lógica de la UI.
}
