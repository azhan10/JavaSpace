package Extras;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * 
 * @author u1476904 Azhan Rashid
 *	This class is here to allow me to navigate to other windows in the project.
 */


public class accessWindow {
	
	/**
	 * This method is used to direct me to another valid window.
	 * It requires the stage path and stage window.
	 * @param view
	 * @param stage
	 */
    public accessWindow(String view, Stage stage) {
        try {
        	//setting the root as parent
            Parent root = FXMLLoader.load(getClass().getResource(view));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            //finally displaying the new window.
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
