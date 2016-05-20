package controller;

import Model.Note;
import Model.NoteListListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class NoteController implements Initializable, ColorMenu.ColorMenuListener{

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
     * The NoteManager object whom I notify when the user clicks the "new note" button
     */
    private NoteListListener listener;

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
     * Checkbox that says whether or not we should keep this window on top
     */
    @FXML
    private CheckBox cbKeepOnTop;

    private ColorMenu colorMenu = new ColorMenu(this);

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
        getListener().noteClosed(this);
        getStage().close();
    }

    /**
     * The mouse coordinates relative to the stage
     */
    private Delta dragStageDelta = new Delta();

    /**
     * Used to drag the stage around the screen
     * @param event
     */
    @FXML
    private void dragStage(MouseEvent event) {
        //drag the stage, taking into account the mouse's original relative position
        getStage().setX(event.getScreenX() + dragStageDelta.X);
        getStage().setY(event.getScreenY() + dragStageDelta.Y);

        event.consume();
    }

    @FXML
    private void toggleKeepOnTop(ActionEvent e) {
        getStage().setAlwaysOnTop(cbKeepOnTop.isSelected());
    }

    /**
     * Prior to dragging the stage around, we need to know the mouse co-ordinates relative
     * to the stage. This event handler does just that. This is called once-- the first time the mouse is pressed-- per drag gesture
     * @param event
     */
    @FXML
    private void startDrag(MouseEvent event) {
        //get the mouse position relative to the scene
        dragStageDelta.X = -event.getSceneX();
        dragStageDelta.Y = -event.getSceneY();
    }

    /**
     * When dragging on the bottom-end toolbar, this variable is used to determine whether hte mouse is clsoe enough to cause the
     * window to be resized horizontally
     */
    boolean horizontalDrag = false;

    /**
     * When dragging on the bottom-end toolbar, this variable is used to determine whether hte mouse is clsoe enough to cause the
     * window to be resized horizontally
     */
    boolean verticalDrag = false;

    /**
     * The distance the mouse must be from the border of a window
     * to allow resizing
     */
    final int RESIZE_BUFFER = 10;

    /**
     * Resizes the stage based on the mouses's screen location (during a drag) relative to the scene's
     * screen location.
     * @param event
     */
    @FXML
    private void resize(MouseEvent event) {
        //if we are horizontal dragging, set the stage size to which ever is GREATER:
        //the minimum width or the dragged width
        if(horizontalDrag)
            getStage().setWidth(Math.max(MINIMUM_WIDTH, event.getScreenX() - getStage().getX()));

        //if we are vertical dragging, set the stage size to which ever is GREATER:
        //the minimum height or the dragged height
        if(verticalDrag)
            getStage().setHeight(Math.max(MINIMUM_HEIGHT, event.getScreenY() - getStage().getY()));
    }

    /**
     * Sets the horizontalDrag and verticalDrag variables, to determine in which
     * way(s) the window will be resized
     * @param event
     */
    @FXML
    private void startResize(MouseEvent event) {
        horizontalDrag = getStage().getWidth() - event.getSceneX() <= RESIZE_BUFFER;
        verticalDrag = (getStage().getHeight() - event.getSceneY() <= RESIZE_BUFFER);
    }

    /**
     * Calls my listener (typically a NotesManager object) and tells it
     * to add a new note.
     */
    @FXML
    private void addNewNote() {
        listener.addNewNote();
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

        stage.setOnCloseRequest(event -> {
            getListener().noteClosed(this);
        });
    }

    /**
     * Shows the master note window, if it is not currently showing. Brings to front if otherwise.
     */
    @FXML
    private void showNotesList() {
        getListener().showNotesList();
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        taNoteContent.textProperty().removeListener(noteContentListener);
        tfNoteTitle.textProperty().removeListener(noteTitleListener);

        this.note = note;
        this.note.setOpen(true);
        this.note.setController(this);

        setColorStyle(note.getColor());

        taNoteContent.setText(note.getText());
        tfNoteTitle.setText(note.getTitle());

        taNoteContent.textProperty().addListener(noteContentListener);
        tfNoteTitle.textProperty().addListener(noteTitleListener);
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
            getListener().noteChanged(note);
        }
    }

    private NoteContentListener noteContentListener = new NoteContentListener();
    private class NoteContentListener implements ChangeListener<String>{

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            note.setText(taNoteContent.getText());
        }
    }

    public NoteListListener getListener() {
        return listener;
    }

    public void setListener(NoteListListener listener) {
        this.listener = listener;
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
