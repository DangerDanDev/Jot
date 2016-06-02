package Model;

/**
 * Listens for changes in a note and saves them to the database using the NoteSaveExecutor
 */
public class NoteSaveListener implements Note.NoteListener {

    /**
     * The note whose changes I listen to
     */
    private Note note;

    /**
     * Sets my note and adds me as a listener for changes
     * @param note
     */
    public NoteSaveListener(Note note) {
        setNote(note);
    }

    public void setNote(Note note) {
        if(this.note != null) {
            this.note.removeListener(this);
        }

        this.note = note;

        if(this.note != null) {
            this.note.addListener(this);
        }
    }

    /**
     * Saves all changes to database when the note notifies me of a change
     * @param note
     */
    @Override
    public void noteChanged(Note note) {
        NoteSaveExecutor.getInstance().queueNote(this.note);
    }
}
