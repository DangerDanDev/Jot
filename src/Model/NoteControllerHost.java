package Model;

import java.util.ArrayList;

/**
 * Interface for whatever object will host and manage NoteControllers
 */
public interface NoteControllerHost {
    /**
     * Instantiate a new note from the database
     */
    void createNote();

    /**
     * Delete a note from the database
     * @param note
     */
    void deleteNote(Note note);

    /**
     * Closes all open notes
     */
    void exitAllNotes();

    /**
     * Show a specific note
     * @param note
     */
    void showNote(Note note);

    /**
     * Show the notes list
     */
    void showNotesList();

    /**
     * Returns a list of all the notes currently open
     * @return
     */
    ArrayList<Note> getOpenNotes();
}
