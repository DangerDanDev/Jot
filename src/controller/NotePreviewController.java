package controller;

import Model.Note;
import View.ViewLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by DanDan on 5/24/2016.
 */
public class NotePreviewController {

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
    private VBox root;

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

            root.setOnMouseClicked(event -> {
                listener.notePreviewClicked(getNote(), event);
            });

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

    public void setNote(Note note) {
        this.note = note;

        tfTitle.setText(note.getTitle());
        tfText.setText(note.getText());
    }

    public Note getNote() {
        return this.note;
    }

    /**
     * Returns the root view of this view
     * @return
     */
    public VBox getRoot() {
        return root;
    }

    /**
     * Interface for anyone who wants to host me!
     */
    public interface NotePreviewListener {
        void notePreviewClicked(Note note, MouseEvent event);
    }
}
