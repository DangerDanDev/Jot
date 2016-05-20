package controller;

import Model.color.ColorPack;
import Model.color.NoteColors;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;

/**
 * Created by DanDan on 5/19/2016.
 */
public class ColorMenu extends ContextMenu {

    private ArrayList<Color> colors = new ArrayList<>();

    private final ColorMenuListener listener;

    public ColorMenu(ColorMenuListener listener) {
        for(ColorPack color : NoteColors.getColors())
            addColor(color.color, color.name);

        this.listener = listener;
    }

    public void addColor(Color color, String name) {
        MenuItem item = new MenuItem(name);

        Pane pane = new Pane();
        pane.setPrefSize(20,20);
        pane.setStyle("-fx-background-color: rgb(" +
                (255.0 * color.getRed()) + "," +
                (255.0 * color.getGreen()) + "," +
                (255.0 * color.getBlue()) + "," +
                "1" +
                ")");
        item.setGraphic(pane);
        getItems().add(item);
        colors.add(color);



        item.setOnAction(event -> {
            listener.setColor(color);
        });
    }

    public interface ColorMenuListener {
        void setColor(Color color);
    }
}
