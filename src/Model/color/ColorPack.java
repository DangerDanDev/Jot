package Model.color;

import javafx.scene.paint.Color;

/**
 * A simple class that holds a color and a name for it.
 * These objects are used in a list to populate a context menu for color selection
 */
public class ColorPack {

    public final Color color;
    public final String name;

    public ColorPack(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    /**
     * Returns the color that I hold
     * @return
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Returns the name of the color I represent
     * @return
     */
    public String getName() {
        return this.name;
    }
}
