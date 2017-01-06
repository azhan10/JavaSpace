package Controller;

import java.rmi.RemoteException;

import Extras.SpaceUtils;
import Extras.accessWindow;
import Model.userAccountsModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;


/**
 * @author u1476904 Azhan Rashid
 * This class is the first class executed when the user first run the code. 
 * It consists two method. The methods uses the accessWindow class to navigate the user to either the register interface or sign in interface.
 */
public class indexController {

	//First need to check if the program is connected to the JavaSpace.
	public JavaSpace05 javaSpace;

	public indexController(){
		//Notifies the user if the program is not connect to JavaSpace.
		javaSpace = SpaceUtils.getSpace();
		if (javaSpace == null) {
			System.err.println("Failed to find the javaspace");
			System.exit(0);
		}else{
			try {
				//Register following details to JavaSpace
				userAccountsModel usr = new userAccountsModel("everyuser", null, null);
				userAccountsModel template = new userAccountsModel("everyuser", null, null);
				userAccountsModel currentUser = (userAccountsModel) javaSpace.readIfExists(template, null, 1000);
				// Write user to JavaSpace.
				if(currentUser == null){
					javaSpace.write(usr, null, Long.MAX_VALUE);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	//Method to direct users to the register page
	@FXML
	protected void goToRegister(ActionEvent event){
		//get the stage and new window path using the accessWindow class.
		Node node = (Node) event.getSource();
		Stage stage = (Stage) node.getScene().getWindow();
		accessWindow register = new accessWindow("../View/register.fxml", stage);
	}

	//Method to direct users to the sign in page
	@FXML
	protected void gotoSignIn(ActionEvent event){
		//get the stage and new window path using the accessWindow class.
		Node node = (Node) event.getSource();
		Stage stage = (Stage) node.getScene().getWindow();
		accessWindow signin = new accessWindow("../View/sign_in.fxml", stage);
	}
}
