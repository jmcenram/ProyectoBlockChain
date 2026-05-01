package es.jmcenram.blockchain.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Utilidad encargada de inicializar y exponer la infraestructura JPA.
 *
 * Centraliza el EntityManagerFactory para que repositorios y servicios trabajen sobre una misma configuracion de persistencia.
 *
 * Forma parte de la capa de infraestructura de base de datos.
 *
 * @author Jcena
 * @version 1.0
 */
public class JPAUtil {

    /** Factoría única de EntityManager, instanciada una sola vez al cargar la clase */
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory(AppConstants.PERSISTENCE_UNIT);

    /**
     * Constructor privado para evitar instanciación de la clase utilitaria.
     */
    private JPAUtil() {
        // Evita instanciación
    }

    /**
     * Obtiene un EntityManager del pool de conexiones de la factoría.
     * Cada invocación retorna una nueva instancia que debe cerrarse después de usar.
     * Es responsabilidad del llamador cerrar el EntityManager cuando termine.
     *
     * @return EntityManager activo listo para operaciones JPA
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Cierra la factoría de EntityManager y libera recursos.
     * Debe invocarse al finalizar la aplicación para liberar conexiones de base de datos.
     * Después de esta invocación, no se pueden obtener nuevos EntityManagers.
     */
    public static void close() {
        emf.close();
    }
}