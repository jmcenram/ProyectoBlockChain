package es.jmcenram.blockchain.controller.login;

import es.jmcenram.blockchain.model.usuario.Usuario;

/**
 * Clase encargada de mantener la sesion del usuario autenticado.
 *
 * Permite:
 * - Guardar el usuario activo
 * - Consultar permisos desde otros controladores
 * - Limpiar la sesion al cerrar sesion
 *
 * Actua como estado compartido de autenticacion en la capa de presentacion.
 *
 * @author Jcena
 * @version 1.0
 */
public class Session {

    /** Usuario autenticado actual (null si no hay sesión activa) */
    private static Usuario usuarioActual;

    /**
     * Establece el usuario actual de la sesión.
     * Típicamente se invoca tras login exitoso.
     *
     * @param usuario el usuario autenticado
     */
    public static void setUsuario(Usuario usuario) {
        usuarioActual = usuario;
    }

    /**
     * Obtiene el usuario autenticado actual.
     *
     * @return usuario actual o null si no hay sesión activa
     */
    public static Usuario getUsuario() {
        return usuarioActual;
    }

    /**
     * Cierra la sesión actual anulando la referencia del usuario.
     * Típicamente se invoca al hacer logout.
     */
    public static void logout() {
        usuarioActual = null;
    }
}