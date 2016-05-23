package Model;

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
     * Show the notes list
     */
    void showNotesList();
}
