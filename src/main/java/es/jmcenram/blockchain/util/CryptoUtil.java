package es.jmcenram.blockchain.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Utilidad encargada del cifrado y descifrado de datos sensibles.
 *
 * Permite proteger private keys antes de guardarlas y recuperarlas solo cuando una operacion necesita firmar.
 *
 * Centraliza la logica criptografica para evitar manejar claves en claro en varias partes del codigo.
 *
 * @author Jcena
 * @version 1.0
 */
public class CryptoUtil {

    /**
     * Clave secreta utilizada para derivar la clave AES.
     * Se recomienda externalizar este valor en producción.
     */
    private static final String SECRET = "clave-super-secreta";

    /**
     * Algoritmo de cifrado utilizado.
     */
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * Genera una clave AES a partir de la constante SECRET.
     *
     * @return clave secreta AES de 128 bits
     * @throws Exception si ocurre un error durante la generación de la clave
     */
    private static SecretKey getKey() throws Exception {
        byte[] key = SECRET.getBytes("UTF-8");

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);

        key = Arrays.copyOf(key, 16);

        return new SecretKeySpec(key, "AES");
    }

    /**
     * Cifra una cadena de texto utilizando AES.
     *
     * <p>El método genera un IV aleatorio de 16 bytes, cifra los datos
     * y concatena el IV con el resultado cifrado. El resultado final se codifica en Base64.</p>
     *
     * @param data texto plano a cifrar
     * @return cadena cifrada codificada en Base64
     * @throws RuntimeException si ocurre un error durante el cifrado
     */
    public static String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            SecretKey key = getKey();

            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Error cifrando datos", e);
        }
    }

    /**
     * Descifra una cadena previamente cifrada con el método encrypt.
     *
     * <p>El método decodifica la cadena Base64, separa el IV de los datos cifrados
     * y reconstruye el contenido original.</p>
     *
     * @param encryptedData cadena cifrada en Base64
     * @return texto original descifrado
     * @throws RuntimeException si ocurre un error durante el descifrado
     */
    public static String decrypt(String encryptedData) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            byte[] iv = Arrays.copyOfRange(combined, 0, 16);
            byte[] encrypted = Arrays.copyOfRange(combined, 16, combined.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            SecretKey key = getKey();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            byte[] original = cipher.doFinal(encrypted);

            return new String(original, "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("Error descifrando datos", e);
        }
    }
}