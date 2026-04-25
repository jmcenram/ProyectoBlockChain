package es.jmcenram.blockchain.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ConfigManager {

    public static void save(BlockchainConfig config, File file) {
        try {
            Properties props = new Properties();

            props.setProperty("rpcUrl", config.getRpcUrl());
            props.setProperty("privateKey", config.getPrivateKey());
            props.setProperty("contractAddress", config.getContractAddress());

            FileOutputStream fos = new FileOutputStream(file);
            props.store(fos, "Blockchain Config");
            fos.close();

        } catch (Exception e) {
            throw new RuntimeException("Error guardando configuración", e);
        }
    }

    public static BlockchainConfig load(File file) {
        try {
            Properties props = new Properties();

            FileInputStream fis = new FileInputStream(file);
            props.load(fis);
            fis.close();

            BlockchainConfig config = new BlockchainConfig();
            config.setRpcUrl(props.getProperty("rpcUrl"));
            config.setPrivateKey(props.getProperty("privateKey"));
            config.setContractAddress(props.getProperty("contractAddress"));

            return config;

        } catch (Exception e) {
            throw new RuntimeException("Error cargando configuración", e);
        }
    }
}