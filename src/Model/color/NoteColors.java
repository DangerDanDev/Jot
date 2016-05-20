package Model.color;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DanDan on 5/20/2016.
 */
public class NoteColors {

    /**
     * All of the following are a series of RGB colors from 0-255 ( because i find that way easier to think) converted into Java color's
     * 0.0-1.0 model
     */
    private static final Color GREEN = new Color(195 / 255.0,244 / 255.0,190 / 255.0,1);
    private static final Color BLUE = new Color(200 / 255.0,230 / 255.0,247 / 255.0,1);
    private static final Color YELLOW = new Color(252 / 255.0,250 / 255.0,171 / 255.0,1);
    private static final Color PINK = new Color(241.0 / 255, 194.0 / 255, 241.0 / 255, 1);
    private static final Color PURPLE = new Color(209.0 / 255, 200.0 / 255, 254.0 / 255, 1);
    private static final Color WHITE = new Color(244.0 / 255, 244.0 / 255, 244.0 / 255, 1);

    public static final ColorPack DEFAULT_COLOR = new ColorPack("Green", GREEN);

    /**
     * List of all the colors, searchable by the name (ie: "blue")
     */
    private static final ArrayList<ColorPack> colors = new ArrayList<>();

    public static void initialize() {
        putColor("Yellow", YELLOW);
        putColor("Green", GREEN);
        putColor("Blue", BLUE);
        putColor("Pink", PINK);
        putColor("Purple", PURPLE);
        putColor("White", WHITE);
    }

    public static void putColor(String name, Color color) {
        colors.add(new ColorPack(name, color));
    }

    public static ArrayList<ColorPack> getColors() {
        if(colors.isEmpty())
            initialize();

        return colors;
    }


}
