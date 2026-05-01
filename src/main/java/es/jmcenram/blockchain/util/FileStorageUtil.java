package es.jmcenram.blockchain.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Clase utilitaria para la gestión de almacenamiento de archivos en el sistema.
 *
 * Permite guardar archivos en una ruta base definida,
 * generando un nombre único para evitar colisiones.
 *
 * Los archivos se almacenan en el directorio:
 * storage/documentos/
 *
 * @author Jcena
 * @version 1.0
 */
public class FileStorageUtil {

    /** Ruta base donde se almacenan los documentos */
    private static final String BASE_PATH = "storage/documentos/";

    /**
     * Guarda un archivo en el sistema de almacenamiento local.
     *
     * - Crea el directorio si no existe
     * - Genera un nombre único basado en timestamp
     * - Copia el archivo al destino
     *
     * @param file archivo a guardar
     * @return ruta completa donde se ha almacenado el archivo
     * @throws IOException si ocurre un error durante la copia
     */
    public static String guardarArchivo(File file) throws IOException {

        File dir = new File(BASE_PATH);

        // Crear directorio si no existe
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generar nombre único
        String nuevoNombre = System.currentTimeMillis() + "_" + file.getName();

        Path destino = Path.of(BASE_PATH + nuevoNombre);

        // Copiar archivo
        Files.copy(file.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

        return destino.toString();
    }
}