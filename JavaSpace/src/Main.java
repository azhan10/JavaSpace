import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.net.URL;

import Extras.SpaceUtils;
import Model.currentMessagesModel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

/**
 * This is the main method. This is the class that needs to be compiled.
 * The class require a path for the config file which allows the user to grant full permission of the transaction.
 * @author u1476904 Azhan Rashid
 *
 */

public class Main extends Application {
	//Generate interface
	@Override
	public void start(Stage primaryStage) throws Exception{
		System.setProperty("java.security.policy","file:///stud/u1476904/Documents/U1476904_Project_Assignment_1_D&CSS/config");
		Parent root = FXMLLoader.load(getClass().getResource("View/index.fxml"));
		primaryStage.setTitle("Lets Talk");

		//set Stage boundaries to the upper left corner of the visible bounds of the main screen
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	/**
	 * Launch the program. 
	 * Here I've added a transaction management system to ensure no message is lost due to a system failure.
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
		//Connect to space
		JavaSpace space = SpaceUtils.getSpace(); 
		//Get transaction management and checking if it exists
		TransactionManager mgr = SpaceUtils.getManager(); 
		if (mgr == null){ 
			System.err.println("Failed to find the transaction manager"); 
			System.exit(1); 
		}

		Transaction.Created trc = null; 
		try { 
			//Create transaction
			trc = TransactionFactory.create(mgr, 3000); 
		} catch (Exception e) { 
			System.out.println("Could not create transaction " + e);
			System.exit(1); 
		} 

		Transaction txn = trc.transaction; 
		//Getting messages from space
		currentMessagesModel currentMessages= new currentMessagesModel(); 

		try { 
			try { 
				//Check if there is any?
				currentMessagesModel checkMessages = (currentMessagesModel)space.take(currentMessages, txn, 2000); 
				//If-else statement either writes the stored messages to the space or notify the user that there is no messages in the space
				if (checkMessages == null) { 
					System.out.println("No messages found in space"); 
				}
				//write the data to JavaSpace
				space.write(checkMessages, txn, 60000);
			} catch ( Exception e) { 
				System.out.println("Failed to read or write to space " + e); 
				txn.abort(); 
				System.exit(1); 
			}
			txn.commit(); 
		} catch (Exception e) { 
			System.out.print("Transaction failed " + e); 
		}
	}
}