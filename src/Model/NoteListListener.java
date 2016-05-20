package Model;

import controller.NoteController;

/**
 * Interface that individual note windows use to communicate with the master notes list window.
 */
public interface NoteListListener {
    /**
     * Notifies my listener when I want to create a new note
     * @return The controller for the newly created note.
     */
    void addNewNote();

    /**
     * Notifies my listener when my window was closed
     * @param c
     */
    void noteClosed(NoteController c);

    /**
     * Tells the object to open a given note
     * @param note
     */
    void showNote(Note note);

    /**
     * Tells the listener to show the master notes list
     */
    void showNotesList();

    /**
     * Notifies the listener when the title has been changed
     * @param note
     */
    void noteChanged(Note note);
}
