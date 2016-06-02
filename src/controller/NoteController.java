package controller;

import Model.Note;
import Model.NoteControllerHost;
import Model.NoteSaveListener;
import Model.WindowManager;
import View.ViewLoader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NoteController implements Initializable, ColorMenu.ColorMenuListener, WindowManager.Window{

    public static final int MINIMUM_WIDTH = 255;
    public static final int MINIMUM_HEIGHT = 125;

    /**
     * The window I'm representing
     */
    private Stage stage;

    /**
     * The note that I'm responsible for
     */
    private Note note;

    /**
     * The root view of the note window
     */
    @FXML
    private VBox rootView;

    /**
     * The main body of the note
     */
    @FXML
    private TextArea taNoteContent;

    /**
     * What the note is called
     */
    @FXML
    private TextField tfNoteTitle;

    /**
     * The button that we can click and drag on to move the window
     */
    @FXML
    private Button bDrag;

    /**
     * The class that will manage me, and when requested, create new notes for me
     */
    private NoteControllerHost host;

    /**
     * Object that listens for changes in the note and saves them
     */
    private NoteSaveListener noteSaveListener;

    /**
     * The context menu that shows
     */
    private ColorMenu colorMenu = new ColorMenu(this);

    public NoteController(Note note, NoteControllerHost host) {
        try {
            this.setHost(host);

            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource("Note.fxml"));
            loader.setController(this);
            rootView = loader.load();

            setStage(new Stage(StageStyle.TRANSPARENT));
            getStage().setScene(new Scene(rootView, 400,300));
            getStage().getIcons().add(new Image("Content/icon.png"));
            setNote(note);
        }
        catch (IOException e) {
            System.out.println("Error instantiating Note Controller: " );
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        taNoteContent.setContextMenu(colorMenu);
    }

    /**
     * Sets the color of both the window and the note that I own
     * @param color
     */
    @Override
    public void setColor(Color color) {
        setColorStyle(color);
        note.setColor(color);
    }

    @Override
    public void exitAllNotes() {
        getHost().exitAllNotes();
    }

    /**
     * Toggles whether this view should be kept on top of all other windows
     * @param keepOnTop
     */
    @Override
    public void setKeepOnTop(boolean keepOnTop) {
        this.getStage().setAlwaysOnTop(keepOnTop);
    }

    /**
     * Displays the master notes window
     */
    @Override
    public void viewAllNotes() {
        showNotesList();
    }

    /**
     * Sets the color of ONLY the window, not the note that I own. This is for when we load a note, we want
     * to set my color to match the note, but without also setting the note's color as would be done by the setColor() method
     * @param color
     */
    private void setColorStyle(Color color) {
        getStage().getScene().getRoot().setStyle("-fx-background-color: rgb(" +
                (255.0 * color.getRed()) + "," +
                (255.0 * color.getGreen()) + "," +
                (255.0 * color.getBlue()) + "," +
                "1" +
                ")");
    }

    /**
     * Exits the stage holding the note
     */
    public void closeStage() {
        getStage().close();
    }

    /**
     * The distance the mouse must be from the border of a window
     * to allow resizing
     */
    final int RESIZE_BUFFER = 10;

    /**
     * Whether or not the mouse is close enough to trigger a horizontal resize
     */
    private boolean horizontalEdge = false;

    /**
     * Whether or not the mouse is close enough to trigger a horizontal resize
     */
    private boolean verticalEdge = false;

    /**
     * Called for mousemovements on the scene-- root VBox, top HBox, and exit button
     * Detects whether the mouse is close enough to the horizontal vertical edges to
     * show the resize cursor
     */
    @FXML
    private void onMouseMoved(MouseEvent event) {

        //only a mouse move over of the root view (ie: not text box, buttons, etc)
        //will be used for a resize or window drag. Everything else is ignored
        if(event.getSource().equals(rootView)) {
            //compute if we are within resizing distance of the right side and bottom of the
            //window
            horizontalEdge = getStage().getWidth() - event.getSceneX() <= RESIZE_BUFFER;
            verticalEdge = getStage().getHeight() - event.getSceneY() <= RESIZE_BUFFER;

            //change the cursor based on if we are close enough to resize horizontally/vertically
            if (horizontalEdge && verticalEdge)
                getStage().getScene().setCursor(Cursor.SE_RESIZE);
            else if (horizontalEdge)
                getStage().getScene().setCursor(Cursor.E_RESIZE);
            else if (verticalEdge)
                getStage().getScene().setCursor(Cursor.S_RESIZE);
        }
        //else the mouse move was over a component other than the root VBox
        else {
            horizontalEdge = false;
            verticalEdge = false;
            getStage().getScene().setCursor(Cursor.DEFAULT);
        }
    }

    /**
     * Called when the mouse exit's the scenes root VBox, top HBox, and exit button
     * Detects when the mouse leaves the window
     * @param event
     */
    @FXML
    private void onMouseExit(MouseEvent event) {
        //horizontalEdge = false;
        //verticalEdge = false;

        //getStage().getScene().setCursor(Cursor.DEFAULT);
    }

    private Delta dragDelta = new Delta();

    /**
     * Called when a mouse dragged starts (onMousePressed)
     * Sets the drag delta's initial ever
     * @param event
     */
    @FXML
    private void onMouseDragStarted(MouseEvent event) {
        dragDelta.X = event.getSceneX();
        dragDelta.Y = event.getSceneY();
    }

    private long mouseMoves = 0;

    @FXML
    private void onMouseDragged(MouseEvent event) {

        //if we are reszing X & Y coordinates, the size should be equal to the difference
        //between the window's position and the mouse's position
        if(horizontalEdge && verticalEdge) {
            getStage().setWidth(Math.max(event.getSceneX() - getStage().getScene().getX(), MINIMUM_WIDTH));
            getStage().setHeight(Math.max(event.getSceneY() - getStage().getScene().getY(), MINIMUM_HEIGHT));
        }
        //for only resizing width-wise
        else if(horizontalEdge) {
            getStage().setWidth(Math.max(event.getSceneX() - getStage().getScene().getX(), MINIMUM_WIDTH));
        }
        //only resizing height
        else if(verticalEdge) {
            getStage().setHeight(Math.max(event.getSceneY() - getStage().getScene().getY(), MINIMUM_HEIGHT));
        }
        //if we aren't resizing it means the window is being dragged
        else {
            getStage().setX(event.getScreenX() - dragDelta.X);
            getStage().setY(event.getScreenY() - dragDelta.Y);
        }
    }

    /**
     * Calls my listener (typically a NotesManager object) and tells it
     * to add a new note.
     */
    @FXML
    private void addNewNote() {
        getHost().createNote();
    }

    @FXML
    private void changeName() {
        getNote().setTitle(tfNoteTitle.getText());
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public NoteControllerHost getHost() {
        return host;
    }

    public void setHost(NoteControllerHost host) {
        this.host = host;
    }

    /**
     * Shows the master note window, if it is not currently showing. Brings to front if otherwise.
     */
    @FXML
    private void showNotesList() {
        host.showNotesList();
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        taNoteContent.textProperty().removeListener(noteContentListener);
        tfNoteTitle.textProperty().removeListener(noteTitleListener);

        this.note = note;
        this.note.setController(this);

        setColorStyle(note.getColor());

        taNoteContent.setText(note.getText());
        tfNoteTitle.setText(note.getTitle());

        taNoteContent.textProperty().addListener(noteContentListener);
        tfNoteTitle.textProperty().addListener(noteTitleListener);

        noteSaveListener = new NoteSaveListener(this.note);
    }

    private NoteTitleListener noteTitleListener = new NoteTitleListener();

    /**
     * Listens for any changes in the text field for the note's title. When changes happen,
     * we update the note's title, and let my listener (the master note window) know as well
     */
    private class NoteTitleListener implements ChangeListener<String>{
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            note.setTitle(tfNoteTitle.getText());
        }
    }

    /**
     * Listens to changes in the text area on the form, and propogates changes to the note object
     */
    private NoteContentListener noteContentListener = new NoteContentListener();

    /**
     * Listens for changes in the note's text, and propogates them to the note object
     */
    private class NoteContentListener implements ChangeListener<String>{

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            note.setText(taNoteContent.getText());
        }
    }

    /************************************************
     * Interfaces and Inner Classes
     ************************************************/

    /**
     * Keeps track of difference in two points. Used to track mouse position relative to the window.
     */
    public class Delta {
        public double X;
        public double Y;
    }
}
