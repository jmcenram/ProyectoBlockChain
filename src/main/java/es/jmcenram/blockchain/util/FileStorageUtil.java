package es.jmcenram.blockchain.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileStorageUtil {

    private static final String BASE_PATH = "storage/documentos/";

    public static String guardarArchivo(File file) throws IOException {

        File dir = new File(BASE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String nuevoNombre = System.currentTimeMillis() + "_" + file.getName();

        Path destino = Path.of(BASE_PATH + nuevoNombre);

        Files.copy(file.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

        return destino.toString();
    }
}