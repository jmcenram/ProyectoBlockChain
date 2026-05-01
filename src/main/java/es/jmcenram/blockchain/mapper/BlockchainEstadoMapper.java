package es.jmcenram.blockchain.mapper;

import es.jmcenram.blockchain.model.registroblockchain.EstadoBlockchain;

import java.math.BigInteger;

/**
 * Mapper encargado de traducir estados del smart contract al dominio Java.
 *
 * Permite convertir valores numericos devueltos por Solidity en EstadoBlockchain.
 *
 * Centraliza esta conversion para que servicios y controladores no dependan de codigos magicos.
 *
 * @author Jcena
 * @version 1.0
 */
public class BlockchainEstadoMapper {

    /**
     * Traduce el codigo numerico del smart contract al enum usado por el dominio Java.
     *
     * Centralizar esta conversion evita repartir literales de Solidity por servicios y controladores.
     *
     * @param estado valor numerico devuelto por el contrato inteligente
     * @return estado de dominio equivalente al codigo numerico del contrato
     */
    public static EstadoBlockchain fromSmartContract(BigInteger estado) {

        if (estado == null) {
            return EstadoBlockchain.ERROR;
        }

        return switch (estado.intValue()) {

            case 1 -> EstadoBlockchain.REGISTRADO;

            case 2 -> EstadoBlockchain.REVOCADO;

            case 0 -> EstadoBlockchain.ERROR;

            default -> EstadoBlockchain.ERROR;
        };
    }

    /**
     *  Método auxiliar para diferenciar NO_EXISTE
     */
    public static boolean esNoExiste(BigInteger estado) {
        return estado != null && estado.intValue() == 0;
    }
}