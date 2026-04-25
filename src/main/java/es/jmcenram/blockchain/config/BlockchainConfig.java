package es.jmcenram.blockchain.config;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
@Getter
@Setter
public class BlockchainConfig {

    private String rpcUrl;
    private String privateKey;
    private String contractAddress;

    // opcional
    private BigInteger gasPrice;
    private BigInteger gasLimit;

}