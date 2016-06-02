package Model;

import Model.color.NoteColors;
import controller.NoteController;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by scyth on 4/22/2016.
 */
public class Note {

    /**
     * Each note is initially titled "untitled note 1" and so on. This number
     * is what the digit at the end will be
     */
    public static int untitled_note_num = 1;

    /**
     * The window manager who is managing me
     */
    private NoteController controller;

    /**
     * The title the user has given the note
     */
    private String title = "";

    /**
     * The text of the note
     */
    private String text = "";

    /**
     * Represents the last time that this note was saved
     */
    private Date dateSaved;

    /**
     * The class that listens for changes to my title
     */
    private NoteListener host;

    /**
     * List of my listeners
     */
    private ArrayList<NoteListener> noteListeners = new ArrayList<>();

    /**
     * My color!
     */
    private Color color = NoteColors.DEFAULT_COLOR.getColor();

    /**
     * Unique ID in the notes database
     */
    public final long id;

    /**
     * Creates a note with a given ID, a title of "untitled note x" where x is a number,
     * an empty Text property (""), and a lastSaved date of now (new Date());
     * @param id
     */
    public Note(long id) {
        this(id,"untitled note" + untitled_note_num++, "", new Date() );
    }

    /**
     * Creates a note that is loaded from existing data
     * @param id
     * @param title
     * @param text
     * @param dateSaved
     */
    public Note(long id, String title, String text, Date dateSaved) {
        this.id = id;

        initialize(title, text, dateSaved);
    }

    /**
     * Initializes the note with all of its basic values
     * @param title
     * @param text
     * @param dateSaved
     */
    private void initialize(String title, String text, Date dateSaved) {
        setTitle(title);
        setText(text);
        setDateSaved(dateSaved);
    }

    /**
     *
     * @param txt
     */
    public void setText(String txt) {
        this.text = txt;

        onNoteChanged();
    }

    /**
     *
     * @return
     */
    public String getText() {
        return this.text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;

        onNoteChanged();
    }

    private void onNoteChanged() {
        //if we have a host, notify it of the title change
        if(getHost() != null)
            getHost().noteChanged(this);

        for(NoteListener listener : noteListeners) {
            listener.noteChanged(this);
        }
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public NoteController getController() {
        return controller;
    }

    public void setController(NoteController controller) {
        this.controller = controller;
    }

    public Date getDateSaved() {
        return dateSaved;
    }

    public void setDateSaved(Date dateSaved) {
        this.dateSaved = dateSaved;
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;

        onNoteChanged();
    }

    public NoteListener getHost() {
        return host;
    }

    public void setHost(NoteListener host) {
        this.host = host;
    }

    public void addListener(NoteListener listener) {
        this.noteListeners.add(listener);
    }

    public void removeListener(NoteListener listener) {
        this.noteListeners.remove(listener);
    }

    @Override
    public boolean equals(Object obj) {
        //if our IDs are the same, we're the same note!
        if(obj instanceof Note) {
            Note note = (Note)obj;
            return this.id == note.id;
        } else {
            return super.equals(obj);
        }
    }

    /**
     * For any class that needs to listen to changes in my text or title
     */
    public interface NoteListener {
        void noteChanged(Note note);
    }
}
