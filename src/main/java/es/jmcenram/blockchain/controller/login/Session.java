package es.jmcenram.blockchain.controller.login;

import es.jmcenram.blockchain.model.usuario.Usuario;

public class Session {

    private static Usuario usuarioActual;

    public static void setUsuario(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuario() {
        return usuarioActual;
    }

    public static void logout() {
        usuarioActual = null;
    }
}