package Model;

/**
 * Interface for whatever object will host and manage NoteControllers
 */
public interface NoteControllerHost {
    void createNote();
    void showNotesList();
}
