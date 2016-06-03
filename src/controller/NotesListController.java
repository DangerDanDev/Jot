package controller;

import Model.*;
import View.ViewLoader;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * This class manages a list of notes displayed in a table,
 * tells its host (the NoteManager) when to open a note and what notes to open, and is
 * responsible for creating new notes from the database and passing them along to the NotesManager
 * when they need to be opened.
 */
public class NotesListController implements NotePreviewController.NotePreviewListener{

    private Stage stage;

    private ArrayList<Note> notes = new ArrayList<>();

    @FXML
    private Button bOpenNote;

    @FXML
    private Button bDeleteNote;

    @FXML
    private Button bAddNote;

    @FXML
    private FlowPane fpNotePreviews;

    /**
     * The text field that determines the what string we will search for
     */
    @FXML
    private TextField tfQuery;

    private ArrayList<Note> selectedNotes = new ArrayList<>();

    /**
     * The object that handles showing of notes
     */
    private NoteControllerHost host;

    /**
     * Loads the FXML file for the notelist controller, initializes the notes table,
     * shows the stage, and adds a textproperty listener ot the search text field
     * @param host
     */
    public NotesListController(NoteControllerHost host) {
        try {

            //load our FXML file
            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource("NotesList.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            //set the stage and scene
            setStage(new Stage());
            getStage().setScene(new Scene(root, 465, 240));
            getStage().getIcons().add(new Image("Content/icon.png"));

            //set my host so that they can listen to when I want to show a note
            setHost(host);
            setNotes(Database.getInstance().getNotes());

            //hook up the event listener for the user searching for notes
            tfQuery.textProperty().addListener(new QueryUpdater());
        } //try {}

        catch (IOException e) {
            System.out.println("There was an error loading the master notes list.");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } //catch
    } //NotesListController();

    /**
     * Called when an OPEN note's name changes; this updates the table's listing
     */
    public void refresh(Note note) {
        //TODO: Make sure the note previews refresh accurately
    }

    /**
     * Called when the notes database has changed. Forces a re-query of the database
     */
    public void reload() {
        setNotes(Database.getInstance().getNotes());
    }

    /**
     * Class that listens for changes to the text of the query search box
     * and triggers a re-query of the database
     */
    private class QueryUpdater implements ChangeListener<String> {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            setNotes(Database.getInstance().getNotes(tfQuery.getText()));
        }
    }
    /**
     * Kills all notes already contained in my list.
     * Then loops through all the new notes that were passed in, and
     * checks to see if they are already open. If a note is already open, we add the ALREADY OPEN REFERENCE
     * from the WINDOW MANAGER to the table instead of the reference from the database.
     * This prevents the notes list not updating titles properly after a re-query
     * @param notes
     */
    public void setNotes(ArrayList<Note> notes) {
        this.notes.clear();

        //loop through all the notes given to us so we can filter the ones from the database out
        //that we already have open. Existing instance takes priority over database instance.
        for(Note note : notes) {

            //if the note is already open, we want to add the open instance
            //not the instance from the database (or other source) to make sure the
            //title list on the table stays properly updated
            if(getHost().getOpenNotes().contains(note)) {
                this.notes.add(getHost().getOpenNotes().get(getHost().getOpenNotes().indexOf(note)));
            }
            //if the note was not already open or already in the previews list, we add it from the database
            else {
                this.notes.add(note);
            }
        }

        //add in all the notes that were passed in that do not already
        //have previews open
        //clear all the note previews
        fpNotePreviews.getChildren().clear();

        //and add in only the notes we need to show (ie: the ones passed in)
        for(int i = 0; i < this.notes.size(); i++) {
            NotePreviewController controller1 = new NotePreviewController(this.notes.get(i), this);
            fpNotePreviews.getChildren().add(controller1.getRoot());
        }

        selectedNotes.clear();

        System.out.println("Selected notes: " + selectedNotes.size());
    }

    /**
     * Instantiates a new note from the database, and instructs the host
     * window manager to show it
     */
    @FXML
    public void addNote() {
        try {
            //get a new note from the database
            Note note = Database.getInstance().newNote();

            //track this note and add it to the table
            notes.add(note);

            if(note.getTitle().contains(tfQuery.getText())) {
                NotePreviewController previewController = new NotePreviewController(note, this);
                fpNotePreviews.getChildren().add(previewController.getRoot());
            }

            //have the host window manager
            getHost().showNote(note);

        } catch (SQLException e) {
            System.out.println("There was an error adding a new note");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Instructs my host to show the currently selected note from the table
     */
    @FXML
    public void showNote(Note note) {
        getHost().showNote(note);
    }

    @FXML
    private void showSelectedNotes() {
        for(Note note : selectedNotes) {
            getHost().showNote(note);
        }
    }

    /**
     * Called when a Note notifies me of selection or deselection. I DO NOT MANAGE THE NOTE'S INTERNAL
     * FLAG. Only an external list
     * Adds or removes a note from the selected notes list
     * @param note
     * @param selected
     */
    @Override
    public void setNoteSelected(Note note, boolean selected) {
        if(selected && !selectedNotes.contains(note)) {
            selectedNotes.add(note);
            bDeleteNote.setDisable(false);
            bOpenNote.setDisable(false);
        }
        else {
            selectedNotes.remove(note);

            if(selectedNotes.size() == 0) {
                bDeleteNote.setDisable(true);
                bOpenNote.setDisable(true);
            }
        }
    }

    /**
     * Removes a note from my tracked arraylist of notes, the notes table items list,
     * and from the database
     */
    @FXML
    public void deleteSelectedNotes() {
         //Let my host know to delete everything I've got selected
        getHost().deleteAllNotes(selectedNotes);

        selectedNotes.clear();
    }

    /**
     * Defunct class to dsiaply info in a table cell
     */
    public class NoteTitleCell extends TableCell<Note, String> {

        public NoteTitleCell() {
            this.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                //If this cell contains a note,
                //show a note when double clicked
                if(!this.isEmpty() && event.getClickCount() == 2)
                    showNoteAtIndex(getIndex());
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if(empty) {
               //if we're empty, we don't show anything
                setText(null);
                setGraphic(null);
            }
            //if we're NOT empty
            else {
                setText(notes.get(getIndex()).getTitle());
            }
        }
    }

    /**
     * Now Defunct class to display info in a table cell
     */
    public class NoteLastEditedCell extends TableCell<Note, String> {

        public NoteLastEditedCell() {
            this.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                //If this cell contains a note,
                //show a note when double clicked
                if(!this.isEmpty() && event.getClickCount() == 2)
                    showNoteAtIndex(getIndex());
            });
        }

        /**
         *
         * @param item
         * @param empty
         */
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if(empty) {
                setText(null);
                setGraphic(null);
            } else {
                DateFormat date = DateFormat.getDateInstance(DateFormat.DATE_FIELD);
                DateFormat time = DateFormat.getTimeInstance();
                setText(date.format(notes.get(getIndex()).getDateSaved()) + " at " +  time.format(notes.get(getIndex()).getDateSaved()));
                setGraphic(null);
            }
        }
    }

    /**
     * Instructs the host to show the note that was clicked on, if it
     * is not null.
     * @param index The index of the table that was clicked
     */
    private void showNoteAtIndex(int index) {
            getHost().showNote(notes.get(index));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return this.stage;
    }

    public NoteControllerHost getHost() {
        return host;
    }

    public void setHost(NoteControllerHost host) {
        this.host = host;
    }

}
