package es.jmcenram.blockchain.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Clase utilitaria para la gestión de internacionalización (i18n).
 *
 * Permite:
 * - Establecer el idioma de la aplicación dinámicamente
 * - Obtener el ResourceBundle activo
 * - Recuperar textos traducidos mediante claves
 *
 * Utiliza archivos de propiedades (messages_*.properties).
 *
 * @author Jcena
 * @version 1.0
 */
public class Messages {

    /** Locale actual de la aplicación */
    private static Locale locale = new Locale("es");

    /** ResourceBundle activo según el locale */
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);

    /**
     * Cambia el idioma de la aplicación.
     * Recarga automáticamente el ResourceBundle asociado.
     *
     * @param newLocale nuevo locale a aplicar
     */
    public static void setLocale(Locale newLocale) {
        locale = newLocale;
        bundle = ResourceBundle.getBundle("messages", locale);
    }

    /**
     * Obtiene el ResourceBundle actual.
     *
     * @return bundle activo
     */
    public static ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Obtiene un texto traducido a partir de una clave.
     *
     * Si la clave no existe, devuelve la clave rodeada de signos de exclamación.
     *
     * @param key clave del mensaje
     * @return texto traducido o indicador de clave no encontrada
     */
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    /**
     * Obtiene el locale actual de la aplicación.
     *
     * @return locale activo
     */
    public static Locale getLocale() {
        return locale;
    }
}