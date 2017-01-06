package Controller;

import java.rmi.RemoteException;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import Extras.SpaceUtils;
import Extras.accessWindow;
import Model.userAccountsModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;

/**
 * 
 * @author u1476904 Azhan Rashid
 * This is the class for the register window. 
 * It consists users being registered to JavaSpace using a valid user name and password.
 * It also contains several error handling such as existing user names.
 */
public class registerController {


	@FXML
	private TextField register_Username;
	@FXML
	private PasswordField register_Password;
	@FXML
	private Label registerNotification;
	private JavaSpace05 javaSpace;
	@FXML
	private PasswordField register_PasswordRetype;

	//Check if we getting a connection to JavaSpace.
	public registerController(){
		javaSpace = SpaceUtils.getSpace();
		if (javaSpace == null) {
			System.err.println("Failed to find the javaspace");
			System.exit(0);
		}
	}

	/**
	 * Method below handles users being registered to JavaSpace. 
	 * It also deals with error handling.
	 * @param event
	 */
	@FXML
	protected void registerUserbtn(ActionEvent event){
		try{
			//Avoiding blank inputs
			if(register_Username.getText().isEmpty() || register_Password.getText().isEmpty() || register_PasswordRetype.getText().isEmpty()){
				registerNotification.setText("Please enter your details");
			}else{
				//If information is valid, hash the password using MD5.
				userAccountsModel get = new userAccountsModel();
				String hash_password = get.hashPassword(register_Password.getText());

				System.out.println("Register Page username: " + register_Username.getText());
				//System.out.println("Register Page encryption password: " + hash_password);

				//Register account details
				userAccountsModel usr = new userAccountsModel(register_Username.getText(), hash_password, null);

				userAccountsModel template = new userAccountsModel(register_Username.getText(), null, null);
				userAccountsModel currentUser = (userAccountsModel) javaSpace.readIfExists(template, null, 1000);

				if (currentUser == null) {
					if(register_PasswordRetype.getText().equals(register_Password.getText())){
						// Write user to JavaSpace.
						javaSpace.write(usr, null, Long.MAX_VALUE);

						//Notify the user everything is right. Set the label green.
						registerNotification.setStyle("-fx-text-fill: green;");
						registerNotification.setText("User " + usr.getUsername() + " Created. Directing you to sign in page."); 

						//Give the user time to read the notification above. 
						//The direct the user to the sign in interface.
						Timer timer = new Timer();
						timer.schedule(new TimerTask(){
							@Override
							public void run(){
								Platform.runLater(new Runnable(){
									public void run(){
										goto_SignIn(event);
									}
								});
							}
						}, 4000);
					}else{
						//The statement is triggered if passwords does not match.
						registerNotification.setText("Passwords does not match");
					}
				} else {
					//The statement is triggered if JavaSpace already holds the user name.
					registerNotification.setText("Username already exists");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}



	@FXML
	protected void goto_SignIn(ActionEvent event){
		Node node = (Node) event.getSource();
		Stage stage = (Stage) node.getScene().getWindow();

		accessWindow signin = new accessWindow("../View/sign_in.fxml", stage);
	}

}
