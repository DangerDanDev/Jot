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
        getItems().add(keepOnTop);

        //Should cause the NotesListController to show all the notes
        MenuItem viewAllNotes = new MenuItem("View All Notes");
        viewAllNotes.setOnAction(event -> listener.viewAllNotes());
        getItems().add(viewAllNotes);

        //the Keep on Top and View All Notes buttons are
        //separate from the colors section of the menu
        getItems().add(new SeparatorMenuItem());

        //add all the colors available to the menu
        for(ColorPack color : NoteColors.getColors())
            addColor(color.color, color.name);

        //the colors are separate from the exit button
        getItems().add(new SeparatorMenuItem());

        //Should cause the noteslistcontroller to exit all notes
        MenuItem exitAll = new MenuItem("Exit All Notes");
        exitAll.setOnAction(event -> listener.exitAllNotes());
        getItems().add(exitAll);

        this.listener = listener;
    }

    /**
     * Adds a selectable color to the menu
     * @param color The color to be added
     * @param name The text that the menu will show next to the color
     */
    public void addColor(Color color, String name) {
        //create a menu button with the name of the color
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
        /**
         * Instructs the listener to set the color
         * of its note
         * @param color
         */
        void setColor(Color color);

        /**
         * Instructs the listener to close all notes without changing the
         * isOpen() field
         */
        void exitAllNotes();

        /**
         * Instructs the listener to toggle whether or not a window is kept on top
         * @param keepOnTop
         */
        void setKeepOnTop(boolean keepOnTop);

        /**
         * Instructs the listener to show the notes list window
         */
        void viewAllNotes();
    }
}
