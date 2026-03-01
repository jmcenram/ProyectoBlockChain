package es.jmcenador.blockchain;

import es.jmcenador.blockchain.model.usuario.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

//TODO: Eliminar
//Clase de pruebas
public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("blockchainPU");

        EntityManager em = emf.createEntityManager();

        try {

            Long idBuscado = 1L; // Cambia si tu id es distinto

            Usuario usuario = em.find(Usuario.class, idBuscado);

            if (usuario != null) {
                System.out.println("Usuario encontrado:");
                System.out.println("ID: " + usuario.getId());
                System.out.println("Nombre: " + usuario.getNombre());
                System.out.println("Email: " + usuario.getEmail());
                System.out.println("Rol: " + usuario.getRol());
            } else {
                System.out.println("No se encontró usuario con ID " + idBuscado);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}
