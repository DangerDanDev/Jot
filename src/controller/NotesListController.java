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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
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
public class NotesListController {

    private Stage stage;

    private ArrayList<Note> notes = new ArrayList<>();

    @FXML
    private TableView<Note> table;

    @FXML
    private TableColumn titleColumn;

    @FXML
    private TableColumn lastEditedColumn;

    @FXML
    private Button bOpenNote;

    @FXML
    private Button bDeleteNote;

    @FXML
    private Button bAddNote;

    /**
     * The text field that determines the what string we will search for
     */
    @FXML
    private TextField tfQuery;

    /**
     * The object that handles showing of notes
     */
    private NoteControllerHost host;

    /**
     * Private constructor because this is a singleton class!
     */
/*    //private NotesListController() {
     //   setStage(createWindow());
//
 //       getStage().setOnCloseRequest(event -> onWindowClosed(event));
  //  }*/


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

            //get the table rady for notes
            initTable();

            //set the stage and scene
            setStage(new Stage());
            getStage().setScene(new Scene(root, 400, 300));

            //set my host so that they can listen to when I want to show a note
            setHost(host);
            setNotes(Database.getInstance().getNotes());

            //hook up the event listener for the user searching for notes
            tfQuery.textProperty().addListener(new QueryUpdater());
        } catch (IOException e) {
            System.out.println("There was an error loading the master notes list.");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prepares the table's CellFactories, and hooks up it's selection events
     */
    private void initTable() {
        titleColumn.setCellFactory((Callback<TableColumn, NoteTitleCell>) param ->  new NoteTitleCell());
        lastEditedColumn.setCellFactory((Callback<TableColumn, NoteLastEditedCell>) param -> new NoteLastEditedCell());

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            //we need to know if anything is selected
            boolean nothingSelected = table.getSelectionModel().getSelectedCells().size() == 0;

            //if things are selected, the open/delete buttons should be enabled. Otherwise
            //they should be enabled
            bDeleteNote.setDisable(nothingSelected);
            bOpenNote.setDisable(nothingSelected);
        });
    } //initTable()

    /**
     * Called when an OPEN note's name changes; this updates the table's listing
     */
    public void refresh(Note note) {
        table.refresh();
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

        //loop through all the notes given to us
        for(Note note : notes) {

            //if the note is already open, we want to add the open instance
            //not the instance from the database (or other source) to make sure the
            //title list on the table stays properly updated
            if(getHost().getOpenNotes().contains(note)) {
                this.notes.add(getHost().getOpenNotes().get(getHost().getOpenNotes().indexOf(note)));
            }
            //if the note was not already open, we add it from the database
            else {
                this.notes.add(note);
            }
        }

        //and update the table to reflect our changes
        table.getItems().clear();
        table.getItems().addAll(this.notes);
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

            //add the note to the table
            table.getItems().add(note);

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
    public void showNote() {
        if(table.getSelectionModel().getSelectedItem() != null)
            getHost().showNote(table.getSelectionModel().getSelectedItem());
    }

    /**
     * Removes a note from my tracked arraylist of notes, the notes table items list,
     * and from the database
     */
    @FXML
    public void deleteSelectedNote() {
        getHost().deleteNote(table.getSelectionModel().getSelectedItem());
    }

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
            getHost().showNote(table.getItems().get(index));
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
