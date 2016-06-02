package Model;

import controller.NoteController;
import controller.NotePreviewController;
import controller.NotesListController;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by DanDan on 5/22/2016.
 */
public class WindowManager implements NoteControllerHost, Note.NoteListener {

    /**
     * A list of all notes currently showing in their own window
     */
    private ArrayList<Note> openNotes = new ArrayList<>();

    /**
     * A list of all the currently open note windows
     */
    private ArrayList<Window> windows = new ArrayList<>();

    /**
     * The controller for the notes list/notes preview grid
     */
    private NotesListController notesListController;

    /**
     * Loads all notes from the database, and shows them all at once
     */
    public WindowManager() {


        ArrayList<Note> previouslyOpen = Database.getInstance().getOpenNotes();
        System.out.println("Open notes: " + previouslyOpen.size());
        for(Note note : previouslyOpen)
            showNote(note);

        notesListController = new NotesListController(this);
        notesListController.getStage().setOnHidden(event -> {
            System.out.println("Notes List hidden");

            if(allWindowsClosed()) {
                exit();
            }
        });

        if(openNotes.size() == 0)
            notesListController.getStage().show();

    }

    /**
     *
     * @return Whether or not the master note list stage is showing
     */
    public boolean isNotesListShowing() {
        return notesListController.getStage().isShowing();
    }

    /**
     * Shows a note. This is accomplished by instantiating a NoteController, adding the note to our tracking list, and the
     * notesListController to our tracking list as well.
     * @param note
     */
    public void showNote(Note note) {
        //we only want to show the note in a new window if it is
        //not already open.
        if(!openNotes.contains(note)) {

            NoteController noteController = new NoteController(note, this);
            noteController.getStage().show();

            trackNote(note, noteController);

            //hook up the listener that tracks when notes are closed
            noteController.getStage().setOnHidden(new CloseNoteListener(note, noteController));
        } //if !openNotes.contains(note)

        //otherwise we want to bring it to the front, as it is already open
        else {
            windows.get(openNotes.indexOf(note)).getStage().toFront();
        }
    }

    /**
     * Creates and shows a brand new note, instantiated directly from the database
     * and calls onNotesDatabaseChanged()
     */
    @Override
    public void createNote() {
        try {
            showNote(Database.getInstance().newNote());
            onNotesDatabaseChanged();
        } catch(SQLException ex) {
            System.out.println("Error creating a new note");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Deletes a note from the database and calls
     * onNotesDatabaseChanged. Used for single note deletion
     * @param note
     */
    @Override
    public void deleteNote(Note note) {
        deleteNoteNoReload(note);

        onNotesDatabaseChanged();
    }

    /**
     * Deletes a note from the database but does NOT call onNotesDatabaseChanged() to notify the
     * listener that changes have been made. Use this method for batch deleting, where the
     * onDatabaseChanged() method should be called at the end.
     * @param note
     */
    public void deleteNoteNoReload(Note note) {
        Database.getInstance().deleteNote(note);

        //if the note is open, we need to close its window
        //because it's being deleted
        if(openNotes.contains(note)) {

            //so loop through every open window until we find the one
            //that is showing the note we are deleting
            for (int i = 0; i < windows.size(); i++) {

                //if we've found the window showing this note,
                //close it and then stop tracking the note/window
                if(windows.get(i).getNote().equals(note)) {
                    windows.get(i).getStage().close();
                }
            }
        }
    }

    /**
     * Deletes all the notes passed in in the parameter, and at the end calls
     * onNotesDatabaseChanged
     * @param notes All the notes requiring deletion
     */
    @Override
    public void deleteAllNotes(ArrayList<Note> notes) {

        //Delete all the notes without calling
        //onDatabaseChanged
        for(Note note : notes) {
            deleteNoteNoReload(note);
        }

        //At the end, after all changes are made, call
        //onNotesDatabaseChanged();
        onNotesDatabaseChanged();
    }
/**
     * Shows the notes list window, if it is not already showing.
     * Otherwise brings it to front
     */
    @Override
    public void showNotesList() {
        //If the notes list is not already showing,s how it
        if(!notesListController.getStage().isShowing())
            notesListController.getStage().show();
        //if it was showing, bring it to front
        else
            notesListController.getStage().toFront();
    }

    /**
     * Adds a note and its associated controller to our tracking list
     * @param note
     * @param noteController
     */
    private void trackNote(Note note, NoteController noteController) {
        openNotes.add(note);
        note.setHost(this);

        windows.add(noteController);
    }

    @Override
    public void exitAllNotes() {

        //if the notes list window is not showing, that means we're doing a global close.
        //Tell the NoteControllers this!
        if(!notesListController.getStage().isShowing()) {
            NoteController.enableGlobalClose();
        }

        for(int i = windows.size() - 1; i >= 0; i--)
            windows.get(i).getStage().hide();
    }

    /**
     * Notifies the notes list window that the database has changed, so it can re-query the database
     */
    private void onNotesDatabaseChanged() {
        notesListController.reload();
    }

    /**
     * Notify the notes list window that a note it may be holding has been updated.
     * @param note
     */
    @Override
    public void noteChanged(Note note) {
        notesListController.refresh(note);
    }

    /**
     * Tells the the note window that it needs to update a note if it is being
     * shown on the table
     * @param note
     */
    private void onOpenNoteChanged(Note note) {
        notesListController.refresh(note);
    }

    /**
     *
     * @return True if all notes are closed and the noteListController's window is not visible; ie: No windows are open
     */
    private boolean allWindowsClosed() {
        return openNotes.size() == 0 && !notesListController.getStage().isShowing();
    }

    private void exit() {
        Database.getInstance().close();
        System.exit(0);
    }

    /**
     * Class that listens for the closing of a note window. If the last note is closed, the program exits
     */
    private class CloseNoteListener implements EventHandler<WindowEvent>{

        /**
         * The note whose window closing I listen for
         */
        private final Note note;

        /**
         * The NoteController whose window closing I listen for
         */
        private final NoteController noteController;

        /**
         * Sets the note and NoteController whose closing I listen for
         * @param note
         * @param noteCont
         */
        public CloseNoteListener(Note note, NoteController noteCont) {
            this.note = note;
            this.noteController = noteCont;
        }

        /**
         * Removes my note and the NoteController from the tracking lists.
         * If There are no windows left open, the database closes and the program exits.
         * @param event
         */
        @Override
        public void handle(WindowEvent event) {
            windows.remove(windows.get(openNotes.indexOf(note)));
            openNotes.remove(note);
            note.setHost(null);

            //check if all windows are closed
            //if so, exit the program
            if(allWindowsClosed())
                exit();
        }
    }

    @Override
    public ArrayList<Note> getOpenNotes() {
        return this.openNotes;
    }

    public interface Window {
        Stage getStage();
        Note getNote();
    }
}
