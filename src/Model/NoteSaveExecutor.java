package Model;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by DanDan on 5/16/2016.
 */
public class NoteSaveExecutor {
    /**
     * The executor that waits to execute saving of notes
     */
    private final Executor executor = Executors.newFixedThreadPool(1);

    /**
     * ArrayList of notes waiting to be saved
     */
    private ArrayList<Note> notesWaiting = new ArrayList<>();

    /**
     * If a note is not already queued up, adds it to the queue of notes waiting, and
     * adds it to the executor's action queue
     * @param note
     */
    public void queueNote(Note note) {

        //make sure the note is not already awaiting completion
        if(!notesWaiting.contains(note)) {

            //add the note to our list of notes awaiting completion
            notesWaiting.add(note);

            //the new runnable to be saved
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    //first save the note
                    Database.getInstance().updateNote(note);

                    //after the note is saved, remember to remove it from the notes awaiting completion
                    notesWaiting.remove(note);

                    //notify me of completion
                    System.out.println("Executor successfully saved note: " + note.getTitle());
                }
            });
        }
    }

    public void removeNote(Note note) {
        notesWaiting.remove(note);
    }
}
