package es.jmcenram.blockchain.model.entidademisora;

import es.jmcenram.blockchain.model.base.EntidadBase;
import es.jmcenram.blockchain.util.CryptoUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad de dominio que representa EntidadEmisora dentro del sistema.
 *
 * Almacena la informacion persistente necesaria para las reglas de negocio y la trazabilidad de la aplicacion.
 *
 * Forma parte del modelo JPA y se utiliza desde repositorios y servicios.
 *
 * @author Jcena
 * @version 1.0
 */
@Getter
@Setter
@Entity
@Table(name = "entidad_emisora")
public class EntidadEmisora extends EntidadBase {

    /**
     * Nombre de la entidad emisora.
     * Ejemplo: Universidad de León
     */
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    /**
     * Clave privada asociada a la wallet.
     *
     * Este campo debe almacenarse cifrado por seguridad.
     * Es único para evitar duplicidades, pero no es la clave primaria.
     */
    @Column(name = "private_key", nullable = false, unique = true, length = 255)
    private String privateKey;

    /**
     * Indica si la entidad emisora está activa.
     *
     * Permite habilitar o deshabilitar emisores sin eliminarlos físicamente.
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * Dirección pública de la entidad emisora en la red blockchain.
     *
     * Esta dirección (address) identifica de forma única la cuenta utilizada
     * para registrar y firmar transacciones en blockchain. Se trata de un dato
     * público derivado de la clave privada, por lo que no requiere cifrado.
     *
     * Se utiliza para verificar la autoría de los documentos registrados.
     */
    @Column(name = "address", nullable = false)
    private String address;

    /**
     * Obtiene la clave privada descifrada de la entidad emisora.
     *
     * Este método utiliza {@link CryptoUtil} para descifrar la clave privada
     * almacenada en la base de datos, la cual se guarda siempre cifrada
     * por motivos de seguridad.
     *
     * IMPORTANTE:
     * - La clave devuelta debe utilizarse únicamente en memoria.
     * - No debe ser logueada, persistida ni expuesta en la interfaz de usuario.
     * - Su uso debe limitarse exclusivamente a operaciones de firma en blockchain.
     *
     * @return clave privada en texto plano (formato hexadecimal)
     */
    public String getPrivateKeyDecrypted() {
        return CryptoUtil.decrypt(privateKey);
    }
}