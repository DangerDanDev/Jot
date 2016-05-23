package controller;

import Model.*;
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
public class NotesListController implements Initializable {

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


    public NotesListController(NoteControllerHost host) {
        try {

            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource("NotesList.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            setStage(new Stage());
            getStage().setScene(new Scene(root, 400, 300));

            setNotes(Database.getInstance().getNotes());
            setHost(host);
        } catch (IOException e) {
            System.out.println("There was an error loading the master notes list.");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
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
                    host.showNote(table.getItems().get(cell.getIndex()));
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
    } //initTable()

    /**
     * Called when a note's name changes; this updates the table's listing
     */
    public void refresh() {
        table.refresh();
    }

    /**
     * Called when the notes database has changed. Forces a re-query of the database
     */
    public void reload() {
        setNotes(Database.getInstance().getNotes());
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

    /**
     * Instructs my host to instantiate and create a new note
     */
    @FXML
    public void addNote() {
        getHost().createNote();
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
