package es.jmcenram.blockchain.model.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Clase base para entidades persistentes con campos comunes.
 *
 * Define identificador y datos compartidos que heredan los modelos de dominio.
 *
 * Permite mantener coherencia en la persistencia JPA y reducir duplicacion entre entidades.
 *
 * @author Jcena
 * @version 1.0
 */
@Getter
@Setter
@MappedSuperclass
public abstract class EntidadBase implements Serializable{

    /** ID único de la entidad, generado automáticamente por la base de datos */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha de creación de la entidad, establecida automáticamente al instanciar */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de borrado lógico; null si la entidad está activa */
    @Column(name = "fecha_borrado")
    private LocalDateTime fechaBorrado;

    /**
     * Verifica si la entidad ha sido marcada como borrada (soft-delete).
     *
     * @return true si ha sido borrada lógicamente (fechaBorrado != null), false en caso contrario
     */
    public boolean estaBorrado() {
        return fechaBorrado != null;
    }

    /**
     * Marca la entidad como borrada estableciendo la fecha de borrado actual.
     * Esta operación implementa soft-delete, no elimina el registro de la base de datos.
     * La fecha se establece al momento de la invocación.
     */
    public void marcarComoBorrado() {
        this.fechaBorrado = LocalDateTime.now();
    }

}
