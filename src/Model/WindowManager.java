package Model;

import controller.NoteController;
import controller.NotesListController;
import javafx.event.EventHandler;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.ArrayList;

/**
 * Created by DanDan on 5/22/2016.
 */
public class WindowManager {

    /**
     * A list of all notes currently showing in their own window
     */
    private ArrayList<Note> openNotes = new ArrayList<>();

    /**
     * A list of all the currently open note windows
     */
    private ArrayList<Window> windows = new ArrayList<>();

    /**
     * Loads all notes from the database, and shows them all at once
     */
    public WindowManager() {
        ArrayList<Note> notes = Database.getInstance().getNotes();

        for(Note note : notes) {
            showNote(note);
        }
    }

    /**
     * Shows a note. This is accomplished by instantiating a NoteController, adding the note to our tracking list, and the
     * controller to our tracking list as well.
     * @param note
     */
    public void showNote(Note note) {
        NoteController noteController = new NoteController(note);
        openNotes.add(note);
        windows.add(noteController);

        noteController.getStage().setOnHidden(new CloseNoteListener(note, noteController));
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
         * Remotes my note and the NoteController from the tracking lists.
         * If they were the last ones open, the database closes and the program exits.
         * @param event
         */
        @Override
        public void handle(WindowEvent event) {
            windows.remove(windows.get(openNotes.indexOf(note)));
            openNotes.remove(note);

            System.out.println("Remaining notes: " + openNotes.size());
            System.out.println("Remaining note windows: " + windows.size());

            if(openNotes.size() == 0) {
                Database.getInstance().close();
                System.exit(0);
            }
        }
    }

    /**
     * Removes both the note and its associated controller from our tracking list
     * @param note
     */
    public void closeNote(Note note) {

    }

    public interface Window {
        Stage getStage();
        Note getNote();
    }
}
