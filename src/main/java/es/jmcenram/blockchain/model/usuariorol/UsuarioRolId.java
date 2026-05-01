package es.jmcenram.blockchain.model.usuariorol;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Identificador compuesto para la relacion entre usuario y rol.
 *
 * Agrupa las claves necesarias para persistir correctamente la tabla intermedia UsuarioRol.
 *
 * Forma parte del modelo de persistencia JPA.
 *
 * @author Jcena
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Getter
@Setter
public class UsuarioRolId implements Serializable {

    /**
     * Identificador del usuario.
     */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /**
     * Identificador del rol.
     */
    @Column(name = "rol_id")
    private Long rolId;

    /**
     * Compara dos objetos UsuarioRolId para determinar igualdad.
     *
     * Dos instancias son iguales si:
     * - Tienen el mismo usuarioId
     * - Tienen el mismo rolId
     *
     * @param o objeto a comparar
     * @return true si son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioRolId that)) return false;
        return Objects.equals(usuarioId, that.usuarioId)
                && Objects.equals(rolId, that.rolId);
    }

    /**
     * Genera el código hash basado en los campos de la clave compuesta.
     *
     * Es fundamental para el correcto funcionamiento en estructuras
     * de datos como HashSet o HashMap.
     *
     * @return código hash generado
     */
    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, rolId);
    }
}