package Controller;

import java.rmi.RemoteException;
import java.time.chrono.IsoEra;

import Extras.SpaceUtils;
import Extras.accessWindow;
import Extras.getUser;
import Model.userAccountsModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;

/**
 * This class handles all the functions when a user signs in with a existing account.
 * It contains several error handling such as avoid blank inputs.
 * @author u1476904 Azhan Rashid
 *
 */

public class signInController {
	
	@FXML
	private TextField username_SignIn;
	@FXML
	private PasswordField password_SignIn;
	@FXML
	private Label signInNotification;
	private JavaSpace05 javaSpace;

	//Check if there is a connection.
	public signInController(){
		javaSpace = SpaceUtils.getSpace();
		if (javaSpace == null) {
			System.err.println("Failed to find the javaspace");
			System.exit(0);
		}
	}
	
	//This method allows users to sign in to there accounts
	@FXML
	protected void signIn_Button(ActionEvent event) throws RemoteException, UnusableEntryException, TransactionException, InterruptedException{
		try{
			
			userAccountsModel currentUser;
			userAccountsModel template;
	        
	        //Avoiding blank inputs
	        if(username_SignIn.getText().isEmpty() || password_SignIn.getText().isEmpty()){
	        	signInNotification.setText("Please fill in all content");
	        }else{
	        	//check if the user name is correct
	        	template = new userAccountsModel(username_SignIn.getText(), null, null);
	            currentUser = (userAccountsModel) javaSpace.readIfExists(template, null, 2500);
				
	            
	            if (currentUser != null) {
	            	//If so, then check the password.
	            	userAccountsModel get = new userAccountsModel();
					String hash_input= get.hashPassword(password_SignIn.getText());
					String hashedPassword = currentUser.getPassword();
					getUser.getInstance().setCurrentUser(currentUser);
					userAccountsModel currentThisUser = getUser.getInstance().getUser();
					
					if(hash_input.equals(hashedPassword)){
						//if both user name and password is correct, direct users to the room interface.
						
						System.out.println("Sign in Username: " + username_SignIn.getText());
					//	System.out.println("Sign in encryption: " + hash_input);
						
						//It will direct users to the user interface using the user name. 
						//The data is passed to the chatFunction class.
						FXMLLoader loader = new FXMLLoader(
								getClass().getResource(
										"../View/talkUsers.fxml"
										)
								);

						Node  source = (Node)  event.getSource();
						Stage stage  = (Stage) source.getScene().getWindow();

						stage.setScene(
								new Scene(loader.load())
								);
						//Sending data to the chatFunctions class and finally show the interface.
						talkUsersController controller = loader.<talkUsersController>getController();
						controller.getMessages(currentThisUser);
						stage.show();
					}else{
						//This is triggered is users inputed a invalid password
						signInNotification.setText("The Password is invalid");
					}
	            	
	            	
	            }else{
	            	//This is triggered is users input details are wrong
	            	signInNotification.setText("The account is not found");
	            }
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
        
	}
	
	//This method is used if users wants to direct themselves to the register interface.
	@FXML
	protected void goto_Register(ActionEvent event){
		Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();

        accessWindow signin = new accessWindow("../View/register.fxml", stage);
	}
}
