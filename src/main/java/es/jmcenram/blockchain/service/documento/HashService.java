package es.jmcenram.blockchain.service.documento;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;

/**
 * Servicio encargado de la logica de negocio de Hash.
 *
 * Permite:
 * - Validar reglas antes de persistir cambios
 * - Coordinar repositorios relacionados
 * - Exponer operaciones usadas por controladores u otros servicios
 *
 * Forma parte de la capa de servicio y mantiene la logica fuera de la interfaz.
 *
 * @author Jcena
 * @version 1.0
 */
public class HashService {

    /**
     * Genera un hash SHA-256 a partir de un arreglo de bytes.
     * El resultado es una cadena hexadecimal de 64 caracteres.
     *
     * Ejemplo:
     * {@code
     * HashService hashService = new HashService();
     * String hash = hashService.generarHash("documento.pdf".getBytes());
     * // resultado: "c0a0f5d3..." (64 caracteres hexadecimales)
     * }
     *
     * @param data el arreglo de bytes a hashear (típicamente contenido del documento)
     * @return hash SHA-256 en formato hexadecimal (64 caracteres)
     * @throws RuntimeException si hay error al generar el hash (ej: algoritmo no disponible)
     */
    public String generarHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error generando hash", e);
        }
    }

    /**
     * Calcula el hash SHA-256 de un archivo leyendo su contenido completo.
     *
     * El resultado se usa como huella inmutable del documento antes de guardarlo o consultarlo en blockchain.
     *
     * @param file archivo cuyo contenido se leera para calcular el hash
     * @return hash SHA-256 en hexadecimal del contenido recibido
     */
    public String generarHash(File file) {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            return generarHash(data);
        } catch (Exception e) {
            throw new RuntimeException("Error generando hash del archivo", e);
        }
    }
}