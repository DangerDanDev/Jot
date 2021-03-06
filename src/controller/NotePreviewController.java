package controller;

import Model.Note;
import Model.color.ColorPack;
import View.ViewLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;

/**
 * Created by DanDan on 5/24/2016.
 */
public class NotePreviewController implements  Note.NoteListener {

    private Note note;

    /**
     * TextField that dislpays the note's title
     */
    @FXML
    private Label tfTitle;

    /**
     * Text field that displays the note's content
     */
    @FXML
    private Label tfText;

    /**
     * VBox that represents the background of the note preview
     */
    @FXML
    private Parent root;

    @FXML
    private VBox vbBackground;

    /**
     * Represents whether or not I'm selected on the NotesPreviewGrid
     */
    private boolean selected;

    private final NotePreviewListener listener;

    /**
     * Creates a new note controller and sets its values per a specific note
     * @param note
     */
    public NotePreviewController(Note note, NotePreviewListener listener) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource("NotePreview.fxml"));
            loader.setController(this);
            loader.load();

            root.setOnMouseClicked(event -> onClick(event));

            setNote(note);
        }
        catch (IOException e) {
            System.out.println("There was an error loading a note preview.");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            this.listener = listener;
        }
    }

    @FXML
    private void onClick(MouseEvent event) {

        //if we were just clicked once, toggle whether or not
        //I'm selected
        if(event.getClickCount() == 1) {
            toggleSelected();
        }
        //if I was double clicked, force the listener to show me in a full note window
        //and leave me selected
        else if(event.getClickCount() == 2) {
            listener.showNote(getNote());

            //we do not get selected after a double click-- only opened
            //setSelected(true);
        }
    }

    public void setNote(Note note) {
        if(this.note != null)
            this.note.removeListener(this);

        this.note = note;

        if(this.note != null)
            this.note.addListener(this);

        noteChanged(this.note);
    }

    public Note getNote() {
        return this.note;
    }

    /**
     * Returns the root view of this view
     * @return
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * When the title of my note changes
     * @param note
     */
    @Override
    public void noteChanged(Note note) {

        tfTitle.setText(note.getTitle());
        tfText.setText(note.getText());
        setColorStyle(note.getColor());
    }

    private void setColorStyle(Color color ) {
        vbBackground.setStyle("-fx-background-color: rgb(" +
                (255.0 * color.getRed()) + "," +
                (255.0 * color.getGreen()) + "," +
                (255.0 * color.getBlue()) + "," +
                "1" +
                ")");
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;

        //if we are not selected, the selection box will not be drawn
        float alpha = 0;

        //if we are selected, draw a green box around the note
        if(isSelected())
            alpha = 1.0f;

        root.setStyle("-fx-background-color: rgb(" +
                        (30 + "," +
                        (170) + "," +
                        (30) + "," +
                        alpha +
                        ")"));

        listener.setNoteSelected(this.getNote(), isSelected());
    }


    public void toggleSelected() {
        setSelected(!isSelected());
    }

    /**
     * Interface for anyone who wants to host me!
     */
    public interface NotePreviewListener {

        /**
         * Notifies the listener that my note should be shown
         * @param note
         */
        void showNote(Note note);

        /**
         * Notifies the listener when I'm selected or deselected
         * @param note
         * @param selected
         */
        void setNoteSelected(Note note, boolean selected);
    }
}
