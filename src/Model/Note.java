package Model;

import Model.color.NoteColors;
import controller.NoteController;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by scyth on 4/22/2016.
 */
public class Note {

    /**
     * The executor that saves all the notes in a 2nd thread
     */
    private static final NoteSaveExecutor executor = new NoteSaveExecutor();

    /**
     * Each note is initially titled "untitled note 1" and so on. This number
     * is what the digit at the end will be
     */
    public static int untitled_note_num = 1;

    /**
     * Represents whether or not the note is fully instantiated. If this vairable is false,
     * then changing the text/title/etc should NOT trigger an automatic save
     */
    private boolean initialized;

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
     * Represents whether the note is currently open in a window
     */
    private boolean open;

    /**
     * Represents whether the note's most recent changes have been saved!
     */
    private boolean saved;

    /**
     * Represents the last time that this note was saved
     */
    private Date dateSaved;

    /**
     * The class that listens for changes to my title
     */
    private NoteListener host;

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

        initialized = true;
    }

    /**
     *
     * @param txt
     */
    public void setText(String txt) {
        this.text = txt;

        //we do not want to save if we are not fully initialized. This way, we do not save upon
        //loading a new note
        setSaved(false || !initialized);
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

        //if we have a host, notify it of the title change
        if(getHost() != null)
            getHost().noteTitleChanged(this);

        //we do not want to save if we are not fully initialized. This way, we do not save upon
        //loading a new note
        setSaved(false || !initialized);
    }

    @Override
    public String toString() {
        return getTitle();
    }

    /**
     * Sets whether or not the note is saved. If the note is not saved (ie: boolean saved is false!), then this method
     * automatically enqueues it in the NoteSaveExecutor's queue for saving in a second thread.
     * @param saved Whether or not the note is currently saved since its most recent edit. If false, it will be
     *              automatically enqueued in the NoteSaveExecutor's note saving queue
     */
    public void setSaved(boolean saved) {
        this.saved = saved;

        //if we just saved
        if(!this.isSaved())
            executor.queueNote(this);
    }

    /**
     * Sets whether or not the note is saved. If the saved boolean passed in is true,
     * sets the LastSaved property to the date that was passed in
     * @param saved
     * @param date
     */
    public void setSaved(boolean saved, Date date) {
        setSaved(saved);

        //update when I was last saved!
        if(saved)
            setDateSaved(date);
    }

    public boolean isSaved() {
        return this.saved;
    }

    public NoteController getController() {
        return controller;
    }

    public void setController(NoteController controller) {
        this.controller = controller;
    }

    public void setOpen(boolean open) {
        this.open = open;

        System.out.println("Set open: " + open);
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

        setSaved(false);
    }

    public NoteListener getHost() {
        return host;
    }

    public void setHost(NoteListener host) {
        this.host = host;
    }

    public void close() {
        setOpen(false);

        //if the note is somehow still not saved when it is closed (ie: User opened a note, and never typed ANYTHING into it),
        //then we save it!
        if(!isSaved())
            executor.queueNote(this);
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

    public boolean isOpen() {
        return this.open;
    }

    /**
     * For any class that needs to listen to changes in my text or title
     */
    public interface NoteListener {
        void noteTitleChanged(Note note);
    }
}
