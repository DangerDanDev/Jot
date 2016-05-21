package controller;

import Model.color.ColorPack;
import Model.color.NoteColors;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;

/**
 * Created by DanDan on 5/19/2016.
 */
public class ColorMenu extends ContextMenu {

    private final ColorMenuListener listener;

    public ColorMenu(ColorMenuListener listener) {

        //the keepOnTop checkmenuitem toggles whether or not the sticky note should
        //always be in front
        CheckMenuItem keepOnTop = new CheckMenuItem("Keep on Top");
        keepOnTop.setOnAction(event -> listener.setKeepOnTop(keepOnTop.isSelected()));
        getItems().addAll(keepOnTop);

        //Should cause the NotesListController to show all the notes
        MenuItem viewAllNotes = new MenuItem("View All Notes");
        getItems().add(viewAllNotes);

        getItems().add(new SeparatorMenuItem());

        for(ColorPack color : NoteColors.getColors())
            addColor(color.color, color.name);

        getItems().add(new SeparatorMenuItem());

        //Should cause the noteslistcontroller to exit all notes
        MenuItem exitAll = new MenuItem("Exit All Notes");
        getItems().add(exitAll);

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

        item.setOnAction(event -> {
            listener.setColor(color);
        });
    }

    public interface ColorMenuListener {
        void setColor(Color color);

        void exitAllNotes();

        void setKeepOnTop(boolean keepOnTop);

        void viewAllNotes();
    }
}
