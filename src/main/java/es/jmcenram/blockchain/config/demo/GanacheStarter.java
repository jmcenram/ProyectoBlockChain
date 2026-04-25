package es.jmcenram.blockchain.config.demo;

import java.io.IOException;

public class GanacheStarter {

    public static void startIfNotRunning() {
        if (isRunning()) {
            System.out.println("Ganache ya está activo");
            return;
        }

        try {
            new ProcessBuilder(
                    "ganache",
                    "--mnemonic", "test test test test test test test test test test test junk",
                    "--port", "8545"
            ).start();

            waitForGanache();

        } catch (Exception e) {
            throw new RuntimeException("Error iniciando Ganache", e);
        }
    }

    private static boolean isRunning() {
        try {
            new java.net.URL("http://127.0.0.1:8545").openConnection().connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void waitForGanache() {
        int retries = 10;
        while (retries-- > 0) {
            if (isRunning()) return;
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Ganache no responde");
    }
}