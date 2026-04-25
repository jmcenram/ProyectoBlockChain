package es.jmcenram.blockchain.controller.login;

import es.jmcenram.blockchain.controller.LayoutController;
import es.jmcenram.blockchain.controller.utils.AvisosUtil;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.rol.RolRepository;
import es.jmcenram.blockchain.repository.usuario.UsuarioRepository;
import es.jmcenram.blockchain.repository.usuariorol.UsuarioRolRepository;
import es.jmcenram.blockchain.service.usuario.UsuarioService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarError;
import static es.jmcenram.blockchain.controller.utils.AvisosUtil.mostrarInfo;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtPasswordNueva;

    private UsuarioService usuarioService;

    @FXML
    public void initialize() {
        UsuarioRepository usuarioRepo = new UsuarioRepository();
        RolRepository rolRepo = new RolRepository();
        UsuarioRolRepository usuarioRolRepo = new UsuarioRolRepository();
        usuarioService = new UsuarioService(usuarioRepo, rolRepo, usuarioRolRepo);

        // Permitir ENTER en contraseña
        txtPassword.setOnAction(e -> login());
    }

    @FXML
    private void login() {

        String username = txtUsuario.getText();
        String password = txtPassword.getText();

        // 🔒 Validación básica
        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {

            mostrarError("Introduce usuario y contraseña");
            return;
        }

        try {
            Usuario usuario = usuarioService.login(username, password);

            if (usuario.getActivo()) {

                inicioSesion(usuario);
            } else {
                String pass = txtPasswordNueva.getText();
                boolean visible = txtPasswordNueva.isVisible();
                txtPasswordNueva.setVisible(!visible);
                txtPasswordNueva.setManaged(!visible);

                if (visible && !"".equals(pass)){

                    if (!esPasswordSegura(pass)) {
                        mostrarError("❌ Contraseña insegura.\nDebe tener:\n- 8 caracteres\n- Mayúscula\n- Minúscula\n- Número\n- Símbolo");
                        return;
                    } else{
                        inicioSesion(usuarioService.activarUsuario(txtUsuario.getText(), pass));
                        mostrarInfo("Usuario activado con exito");
                    }
                }
            }
        } catch (Exception e) {
            mostrarError("Credenciales inválidas");
        }
    }

    private boolean inicioSesion(Usuario usuario) throws IOException {
        // 🔥 Guardar sesión
        Session.setUsuario(usuario);

        // 🔁 Cargar vista principal
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/main.fxml")
        );
        Parent mainView = loader.load();

        // 🔥 Obtener layout global desde la Scene
        LayoutController layout = (LayoutController)
                txtUsuario.getScene().getUserData();

        // 🛡️ Evitar NullPointer (si no se ha seteado en Main)
        if (layout == null) {
            mostrarError("Error interno de navegación");
            return true;
        }

        // 🔁 Cambiar SOLO el contenido
        layout.setContent(mainView);
        return false;
    }

    private boolean esPasswordSegura(String password) {

        if (password == null) return false;

        // Regex completa
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

        return password.matches(regex);
    }


}