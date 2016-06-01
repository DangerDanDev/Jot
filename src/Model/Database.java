package Model;

import Model.color.ColorPack;
import Model.color.NoteColors;
import javafx.scene.paint.Color;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by scyth on 4/25/2016.
 */
public class Database {

    /**
     * The database singleton instance!
     */
    private final static Database instance = new Database();

    public static Database getInstance() {
        return instance;
    }

    /**************************************
     * CONSTANTS
     **************************************/

    private final String DATABASE_NAME = "jot.db";
    private final String DIRECTORY_NAME = "Jot";
    private final String DATABASE_ABS_PATH;

    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_DATE_SAVED = "dateSaved";
    private static final String COLUMN_OPEN = "open";
    private static final String COLUMN_COLOR = "color";

    private static final String TABLE_NEXT_ID = "nextID";
    private static final String COLUMN_NEXT_ID = "id";

    /**
     * My connection to the database file
     */
    private Connection connection;

    /**
     * Statement used to insert new notes into the database
     */
    private PreparedStatement preparedNewNoteStatement;

    /**
     * Statement used to delete a specific note from the database
     */
    private PreparedStatement preparedDeleteNoteStatement;

    /**
     * Statement used to update a note in the database
     */
    private PreparedStatement preparedNoteUpdateStatement;

    /**
     * Statement used to query notes, optionally with a specific
     * title criteria
     */
    private PreparedStatement preparedGetNotesStatement;

    private long nextID = 0;

    /**
     * Creates a database object
     */
    private Database() {
        final File directory = new File(System.getProperty("user.home") + File.separator + "Documents" +
                File.separator + DIRECTORY_NAME);

        if(!directory.exists()) {
            directory.mkdirs();
        }

        DATABASE_ABS_PATH = directory + File.separator + DATABASE_NAME;

        System.out.println("Database Directory: " + DATABASE_NAME);

        //create the database
        try {
            //load driver and establish connection
            org.sqlite.JDBC jdbc = new org.sqlite.JDBC();
            connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_ABS_PATH);

            //find out if our Notes table already exists
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet resultSet = meta.getTables(null, null, TABLE_NOTES, new String[] {"TABLE"} );

            //check if the query came up empty: IE if the notes table does not yet exist
            if(!resultSet.next()) {
                //create the table if it doesn't exist
                createNotesTable();
            } else {
                //the notes table did already exist
                System.out.println("Notes table already existed.");
            }

            //find out if our next usable ID table already exists
            //If it does not exist, we will create the table
            resultSet = meta.getTables(null, null, TABLE_NEXT_ID, new String[] { "TABLE" });
            if(!resultSet.next()) {
                createNextIdTable();
            }
             //but if the table does exist, we need to get
            //the next ID out of it
            else {
                initNextId();
            } //else

            //get my prepared update statement for updating notes in the database
            preparedNoteUpdateStatement = connection.prepareStatement(" UPDATE " + TABLE_NOTES +
                    " SET " +   COLUMN_TITLE + " = ?, " +
                                COLUMN_CONTENT + " = ?, " +
                                COLUMN_DATE_SAVED + " = ?, " +
                                COLUMN_OPEN + " = ?, " +
                                COLUMN_COLOR + " = ? " +
                    " WHERE " + COLUMN_ID + " = ? ");

            preparedNewNoteStatement = connection.prepareStatement( " INSERT INTO " + TABLE_NOTES + "( " +
                            COLUMN_ID + ", " +
                            COLUMN_TITLE + ", " +
                            COLUMN_CONTENT + ", " +
                            COLUMN_DATE_SAVED + ", " +
                            COLUMN_OPEN + ", " +
                            COLUMN_COLOR + " ) " +

                        " VALUES (" +
                            "?, " +     //ID
                            "?, " +     //TITLE
                            "?, " +     //CONTENT
                            "?, " +     //DATE
                            "?, " +     //OPEN
                            "?)"        //color
                        );

            //select any notes where the title contains the string supplied in place of '?'
            preparedGetNotesStatement = connection.prepareStatement( " SELECT * FROM " + TABLE_NOTES + " " +
                                                                    " WHERE " + COLUMN_TITLE + " LIKE ? ");

            preparedDeleteNoteStatement = connection.prepareStatement(
                    " DELETE FROM " + TABLE_NOTES + " " +
                    " WHERE " + COLUMN_ID + " = ? "
            );

        } catch (SQLException e) {
            e.printStackTrace();
        } 
    }

    private void initNextId() throws SQLException {
        System.out.println("Next ID table already existed.");

        //we need to find out the current next useable ID:
        String nextIdQuerySQL = "SELECT * " +
                " FROM " + TABLE_NEXT_ID; //we don't need a where clause. This table should only have one element
        Statement statement = connection.createStatement();

        ResultSet nextIdQuery = statement.executeQuery(nextIdQuerySQL);

        if(nextIdQuery.next()) {
            //we have a first returned object. YAY WE HAVE A NEXT ID
            nextID = nextIdQuery.getLong(nextIdQuery.findColumn(COLUMN_NEXT_ID));
            System.out.println("Next id: " + nextID);
        } else {
            //error
            System.out.println("ERROR: NO FIRST ID IN TABLE " + TABLE_NEXT_ID + "!!!");
        }
    }


    private void createNotesTable() throws SQLException {
        //table does not exist. Create it!
        Statement statement = connection.createStatement();

        String createNotesTable = "CREATE TABLE " + TABLE_NOTES + "(" +
                                COLUMN_ID +         " INT PRIMARY KEY, " +
                                COLUMN_TITLE +      " TEXT, " +
                                COLUMN_CONTENT +    " TEXT, " +
                                COLUMN_DATE_SAVED + " DATE, " +
                                COLUMN_OPEN       + " TEXT, " +
                                COLUMN_COLOR      + " TEXT " +
                                ")";
        statement.executeUpdate(createNotesTable);
        statement.close();
        System.out.println("Table " + TABLE_NOTES + " created.");
    }

    private void createNextIdTable() throws SQLException{
        Statement statement = connection.createStatement();

        String createIdsTable = "CREATE TABLE " + TABLE_NEXT_ID + "( " +
                    COLUMN_NEXT_ID + " INT )";

        String insertFirstID = " INSERT INTO " + TABLE_NEXT_ID + "(" + COLUMN_NEXT_ID + ") " +
                "VALUES (" + nextID + ")";

        statement.executeUpdate(createIdsTable);
        statement.executeUpdate(insertFirstID);
        statement.close();
        System.out.println("Next IDs table created: " );
    }

    public Note newNote() throws SQLException {

        final int ID_INDEX = 1,
                TITLE_INDEX = 2,
                TEXT_INDEX = 3,
                DATE_INDEX = 4,
                OPEN_INDEX = 5,
                COLOR_INDEX = 6;

        Note note = new Note(getNextID());
        Color color = note.getColor();

        preparedNewNoteStatement.setLong(ID_INDEX, note.id);
        preparedNewNoteStatement.setString(TITLE_INDEX, note.getTitle());
        preparedNewNoteStatement.setString(TEXT_INDEX, note.getText());
        preparedNewNoteStatement.setDate(DATE_INDEX, new java.sql.Date(note.getDateSaved().getTime()));
        preparedNewNoteStatement.setBoolean(OPEN_INDEX, note.isOpen());
        preparedNewNoteStatement.setString(COLOR_INDEX, color.getRed() + "," + color.getGreen() + "," + color.getBlue());

        preparedNewNoteStatement.execute();

        return note;
    }

    public Note getNote(long id) {
        try{
            Statement statement = connection.createStatement();

            String sql = "SELECT * " +
                        " FROM " + TABLE_NEXT_ID + " " +
                        " WHERE " + COLUMN_ID + " = " + id + " ";

            ResultSet result = statement.executeQuery(sql);

            result.next();

            Note note = new Note(id, result.getString(COLUMN_TITLE), result.getString(COLUMN_CONTENT), new Date(result.getDate(COLUMN_DATE_SAVED).getTime()));

            statement.close();

            return note;

        } catch(SQLException ex) {
            System.out.println("There was an error loading note #" + id);
            return null;
        }
    }

    /**
     * Queries all notes
     * @return
     */
    public ArrayList<Note> getNotes() {
        return getNotes("");
    }



    /**
     * Queries for all notes whose title contains a given string
     * @param query The string that the title must contain to be returned
     * @return
     */
    public ArrayList<Note> getNotes(String query) {

        final int QUERY_TITLE_INDEX = 1;

        ArrayList<Note> notes  = new ArrayList<>();

        try {
            preparedGetNotesStatement.setString(1, "%" + query + "%");

            ResultSet results = preparedGetNotesStatement.executeQuery();

            while(results.next()) {

                notes.add(getNoteFromResultSet(results));
            } //while(results.next())
        } catch (SQLException e1) {
            e1.printStackTrace();
        } finally {
            return notes;
        }
    }

    /**
     * Returns a note object from a result set that contains all columns of the note table
     *
     * @param results
     * @return
     */
    private Note getNoteFromResultSet(ResultSet results) {
        Note note = null;

        try {
             note = new Note(results.getLong(COLUMN_ID),
                    results.getString(COLUMN_TITLE),
                    results.getString(COLUMN_CONTENT),
                    new Date(results.getDate(COLUMN_DATE_SAVED).getTime()));

            try {
                //color is 3 decimals between 0 and 1 all separated by a comma-- so an array of 3 components: R, G, and B!
                String colorStr[] = results.getString(COLUMN_COLOR).split(",");

                //parse each of the 3 strings representing an RGB value
                float r = Float.parseFloat(colorStr[0]);
                float g = Float.parseFloat(colorStr[1]);
                float b = Float.parseFloat(colorStr[2]);

                //alpha is always 1. Our notes are not transparent!
                float a = 1.0f;

                note.setColor(new Color(r,g,b,a)); //1.0 at the end is opacity

            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch(IllegalArgumentException ex) {
                note.setColor(NoteColors.DEFAULT_COLOR.getColor());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return note;
        }
    }

    private long getNextID() throws SQLException{
        long tempID = nextID;
        incrementNextId();
        return tempID;
    }

    /**
     * S
     */
    private void incrementNextId() throws SQLException{
        nextID++;

        Statement statement = connection.createStatement();

        String updateNextID = " UPDATE " + TABLE_NEXT_ID +
                " SET " + COLUMN_NEXT_ID + " = " + nextID + " " +
                " WHERE " + COLUMN_NEXT_ID + " = " + (nextID - 1);

        statement.executeUpdate(updateNextID);
        statement.close();

        System.out.println("Next ID updated to: " + nextID);
    }

    /**
     * Closes the connection to the database
     */
    public void close() {
        try {
            connection.close();
            System.out.println("Closing database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void updateNote(Note note) {
        try {
            Date now = new Date();

            Color color = note.getColor();

            preparedNoteUpdateStatement.setString(1, note.getTitle());
            preparedNoteUpdateStatement.setString(2, note.getText());
            preparedNoteUpdateStatement.setDate(3, new java.sql.Date(now.getTime()));
            preparedNoteUpdateStatement.setBoolean(4, note.isOpen());
            preparedNoteUpdateStatement.setString(5, color.getRed() + "," + color.getGreen() + "," + color.getBlue());

            preparedNoteUpdateStatement.setLong(6, note.id);
            
            preparedNoteUpdateStatement.execute();

            note.setDateSaved(now);
            note.setSaved(true, now);

        } catch (SQLException ex) {
            System.out.println("Unable to use prepared statement on note with id: " + note.id);
            ex.printStackTrace();
        }
    }

    public void deleteNote(Note note) {
        try {
            preparedDeleteNoteStatement.setLong(1, note.id);

            preparedDeleteNoteStatement.execute();

        } catch(SQLException ex) {

        }
    }
}
