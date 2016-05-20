import Model.Database;
import View.ViewLoader;
import controller.NotesListController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by scyth on 4/28/2016.
 */
public class App extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception {
        Database db = Database.getInstance();
        NotesListController masterController = NotesListController.getInstance();
    }


}
