package controller;

import Model.Database;
import Model.Note;
import Model.NoteListListener;
import View.ViewLoader;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
 * Created by scyth on 4/23/2016.
 */
public class NotesListController implements Initializable, NoteListListener {

    private static NotesListController instance = new NotesListController();
    public static NotesListController getInstance() { return instance; }

    private Stage stage;

    private ArrayList<NoteController> controllers = new ArrayList<>();

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
     * Private constructor because this is a singleton class!
     */
    private NotesListController() {
        setStage(createWindow());

        getStage().setOnCloseRequest(event -> onWindowClosed(event));
    }


    /**
     * When the window is closed, we set the stage to null
     * and check to see if all other windows are closed as well.
     * if all other windows are closed, we close the database.
     * @param event
     */
    public void onWindowClosed(WindowEvent event) {
        if(controllers.size() == 0) {
            setStage(null);
            Database.getInstance().close();
            System.exit(0);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //get all the notes from the database!
        notes = Database.getInstance().getNotes();

        //prepare the table to display all the initial notes
        initTable();

        //
        setNotes(notes);
    }

    /**
     * Prepares the table's CellFactories, and hooks up it's selection events
     */
    private void initTable() {
        titleColumn.setCellFactory((Callback<TableColumn, NoteTitleCell>) param ->  {
            NoteTitleCell cell = new NoteTitleCell();

            //event listener for double clicking on an item in the table
            cell.addEventFilter(MouseEvent.MOUSE_CLICKED,event -> {
                if(event.getClickCount() == 2) {
                    //if the cell was double clicked, blegh a blegh, and the note
                    //is NOT already open, we open it.
                    if(!table.getSelectionModel().getSelectedItem().isOpen())
                        showNote(table.getSelectionModel().getSelectedItem());
                    //otherwise find the controller who manages the note we want
                    else
                        for(NoteController noteCont : controllers)
                            //and we bring that note to the front
                            if(noteCont.getNote() == table.getSelectionModel().getSelectedItem())
                                noteCont.getStage().toFront();
                }
            });

            return cell;
        } );
        lastEditedColumn.setCellFactory((Callback<TableColumn, NoteLastEditedCell>) param -> new NoteLastEditedCell());

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            //we need to know if anything is selected
            boolean nothingSelected = table.getSelectionModel().getSelectedCells().size() == 0;

            //if things are selected, the open/delete buttons should be enabled. Otherwise
            //they should be enabled
            bDeleteNote.setDisable(nothingSelected);
            bOpenNote.setDisable(nothingSelected);
        });
    }

    /**
     * Sets my reference to the list of all notes in the database.
     * @param notes
     */
    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;

        table.getItems().clear();
        table.getItems().addAll(notes);
    }

    public void addNote(Note note) {
        this.notes.add(note);

        table.getItems().add(note);
    }

    /**
     * Removes a note from my tracked arraylist of notes, the notes table items list,
     * and from the database
     * @param note
     */
    public void removeNote(Note note) {
        this.notes.remove(note);

        table.getItems().remove(note);
        Database.getInstance().deleteNote(note);
    }

    public class NoteTitleCell extends TableCell<Note, String> {
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
     * Creates a window for a new note, and sets the window controller's note
     * to be the note passed in as a parameter
     * @param note The note that the window will show
     * @return
     */
    public void showNote(Note note) {

        //Creates a new note window
        NoteController controller = createNoteWindow();

        //Instantiate the controller's note
        controller.setNote(note);

        note.setOpen(true);

    } //showNote(Note note);

    /**
     * Adds a new blank note window
     */
    public NoteController createNoteWindow() {

        try {
            //instantiate our stage
            Stage stage = new Stage();

            //Load the actual scene. We don't know what control/container will be the root (since that may change),
            //so we load it as a Parent object
            FXMLLoader loader = new FXMLLoader(new ViewLoader().getClass().getResource("Note.fxml"));
            VBox root = loader.load();

            //get the controller, let it know what window it's managing,
            //add me as it's listener for when the "new note button" is clicked,
            //and add it to my list of tracked controllers
            NoteController controller = loader.getController();
            controller.setStage(stage);
            addController(controller);

            //finally create the scene/window and set it's title
            stage.getIcons().add(new Image("/Content/icon.png"));
            stage.setScene(new Scene(root, 350, 225));
            stage.setTitle("");
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();

            return controller;

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Could not open FXML File: " + "Note.fxml.");
            return null;
        }
    } //createNoteWindow

    /**
     * OVERRIDES NoteController.NoteControllerListener.noteClosed();
     * @param c
     */
    @Override
    public void noteClosed(NoteController c) {
        removeController(c);

        System.out.println("Open notes: " + controllers.size());

        //If the master notes window and all child note windows
        //are closed, the program is exiting. Close the database.
        if(controllers.size() == 0 && !getStage().isShowing()) {
            Database.getInstance().close();
        }

        c.getNote().close();
    }

    /**
     * Calls Database.getInstance().newNote() to create a new note
     * in the database, and passes the new note into the showNote(Note note) method
     * to have it create a window/controller.
     * OVERRIDES NoteController.NoteControllerListener.addNewNote();
     */
    @Override
    public void addNewNote() {
        try {
            Note note = Database.getInstance().newNote();

            showNote(note);

            addNote(note);
            //setNotes(Database.getInstance().getNotes());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteNote() {
        Note note = table.getSelectionModel().getSelectedItem();

        if(note != null) {
            System.out.println("Note is open: " + note.isOpen());

            //If the note is open, close that baby!
            if(note.isOpen())
                note.getController().closeStage();

            //Delete the note!
            removeNote(note);
        }
    }

    /**
     * From interface NoteListListener.
     * Shows my stage, and brings it to the front
     */
    @Override
    public void showNotesList() {
        getStage().show();
        getStage().toFront();
    }

    @Override
    public void noteChanged(Note note) {
        table.refresh();
    }

    /**
     * Opens whatever note is currently selected on the notes table
     */
    @FXML
    private void openSelectedNote() {
        Note note = table.getSelectionModel().getSelectedItem();

        //Only open the note if it is not already open
        if(!note.isOpen())
            showNote(table.getSelectionModel().getSelectedItem());
    }

    public void addController(NoteController controller) {
        controllers.add(controller);
        controller.setListener(this);
    }

    public void removeController(NoteController controller) {
        controllers.remove(controller);
        controller.setListener(null);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return this.stage;
    }

    /**
     * Loads and shows the window that contains all previously edited notes
     */
    public Stage createWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource("NotesList.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 300, 300));
            setStage(stage);
            stage.show();

            return stage;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



}
