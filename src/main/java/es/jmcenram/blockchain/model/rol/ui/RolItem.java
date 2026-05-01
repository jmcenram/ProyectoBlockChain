package es.jmcenram.blockchain.model.rol.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Modelo de apoyo para representar roles en controles de interfaz.
 *
 * Permite mostrar un texto legible en combos o listas sin perder la referencia al rol real.
 *
 * Forma parte de los modelos auxiliares de presentacion.
 *
 * @author Jcena
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class RolItem {

    /**
     * Valor interno del rol.
     *
     * Se utiliza en la lógica de negocio y persistencia.
     */
    private final String value;

    /**
     * Texto visible del rol en la interfaz de usuario.
     *
     * Permite mostrar nombres amigables o traducidos.
     */
    private final String label;

    /**
     * Devuelve la representación en texto del objeto.
     *
     * Se utiliza automáticamente por componentes JavaFX
     * para mostrar el elemento en pantalla.
     *
     * @return etiqueta visible del rol
     */
    @Override
    public String toString() {
        return label;
    }

}