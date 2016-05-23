package Model;

import controller.NoteController;
import controller.NotesListController;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by DanDan on 5/22/2016.
 */
public class WindowManager implements NoteControllerHost{

    /**
     * A list of all notes currently showing in their own window
     */
    private ArrayList<Note> openNotes = new ArrayList<>();

    /**
     * A list of all the currently open note windows
     */
    private ArrayList<Window> windows = new ArrayList<>();

    private NotesListController notesListController;

    /**
     * Loads all notes from the database, and shows them all at once
     */
    public WindowManager() {
        ArrayList<Note> notes = Database.getInstance().getNotes();

        notesListController = new NotesListController(this);
        notesListController.getStage().setOnHidden(event -> {
            if(allWindowsClosed())
                exit();
        });

        for(Note note : notes) {
            showNote(note);
        }
    }

    /**
     * Shows a note. This is accomplished by instantiating a NoteController, adding the note to our tracking list, and the
     * notesListController to our tracking list as well.
     * @param note
     */
    public void showNote(Note note) {
        NoteController noteController = new NoteController(note,this);
        openNotes.add(note);
        windows.add(noteController);

        noteController.getStage().setOnHidden(new CloseNoteListener(note, noteController));
    }

    /**
     * Creates and shows a brand new note, instantiated directly from the database
     */
    @Override
    public void createNote() {
        try {
            showNote(Database.getInstance().newNote());
        } catch(SQLException ex) {
            System.out.println("Error creating a new note");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
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

    @Override
    public void deleteNote(Note note) {
        //TODO: Delete the note from the database
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

            //check if all windows are closed
            //if so, exit the program
            if(allWindowsClosed())
                exit();
        }
    }

    public interface Window {
        Stage getStage();
        Note getNote();
    }
}
