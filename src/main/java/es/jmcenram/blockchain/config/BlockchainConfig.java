package es.jmcenram.blockchain.config;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

/**
 * Modelo de configuracion necesario para conectar la aplicacion con blockchain.
 *
 * Permite transportar los datos activos de conexion:
 * - URL RPC
 * - Direccion del contrato
 * - Parametros opcionales de gas
 *
 * Se carga y persiste mediante ConfigManager antes de inicializar BlockchainService.
 *
 * @author Jcena
 * @version 1.0
 */
@Getter
@Setter
public class BlockchainConfig {

    /** URL del proveedor JSON-RPC para conectar con la blockchain (ej: http://localhost:8545) */
    private String rpcUrl;

    /** Dirección del contrato inteligente desplegado (con prefijo 0x) */
    private String contractAddress;

    /** Precio del gas opcional en Wei; null si usa el precio actual de la red */
    private BigInteger gasPrice;

    /** Límite máximo de gas opcional para transacciones; null si calcula automáticamente */
    private BigInteger gasLimit;

}
