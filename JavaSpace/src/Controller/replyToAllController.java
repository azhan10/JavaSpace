package Controller;
import java.awt.Checkbox;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import Extras.SpaceUtils;
import Extras.getUser;
import Model.currentMessagesModel;
import Model.deleteMessageModel;
import Model.messageHolderModel;
import Model.userAccountsModel;
import javafx.scene.paint.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.AvailabilityEvent;
import net.jini.space.JavaSpace05;
import net.jini.space.MatchSet;

/**
 * The class is allow all users to communicate to each other in one place.
 * Users can view messages and delete their own messages.
 * User can also refresh messages list, close the program, leave interface and return back to the chat interface, 
 * notify users for new messages and deleted messages. 
 * It also gets current messages and users and display it on a list view
 * @author u1476904 Azhan Rashid
 *
 */


public class replyToAllController implements RemoteEventListener {


	@FXML
	private ListView<String> messageList;
	private ObservableList<String> messages = FXCollections.observableArrayList();
	private ObservableList<String> messageUserlist = FXCollections.observableArrayList();
	private userAccountsModel currentUser = getUser.getInstance().getUser();
	private String username;
	private Boolean isOwner;
	@FXML
	private ListView<String> userlist;
	@FXML
	private CheckBox twoMinOption;
	@FXML
	private CheckBox oneMinOption;
	@FXML
	private TextArea messageBox;
	@FXML
	private Button say_button;
	@FXML 
	private Button leaveInterface;
	@FXML
	private Label replyToAllNotification;
	@FXML
	private Label everyoneHeader;
	private JavaSpace05 javaSpace;
	private RemoteEventListener remote;
	@FXML 
	private Button btn_deleteMessage;
	private Timer timer = new Timer();
	currentMessagesModel previousMessage;
	@FXML
	private CheckBox notification;


	//Checking for a connection
	public replyToAllController(){
		javaSpace = SpaceUtils.getSpace();
		if (javaSpace == null) {
			System.err.println("Failed to find the javaspace");
			System.exit(0);
		}
	}

	/**
	 * closeProgram method lets users to terminate program.
	 * @param event
	 */
	@FXML
	private void closeProgram(ActionEvent event){
		System.out.println("Program is terminated");
		System.exit(0);
	}

	/**
	 * The following method lets users to refresh the message list.
	 * @param event
	 */
	@FXML
	private void btn_refresh(ActionEvent event){
		String selectedUser = "everyuser";

		try {
			userAccountsModel template = new userAccountsModel(selectedUser, null, null);
			userAccountsModel exists = (userAccountsModel) javaSpace.readIfExists(template, null, 1000);

			userAccountsModel chatroom_template = new userAccountsModel(selectedUser, null, null);
			//Security reasons
			if (exists != null){

				Platform.runLater(new Runnable(){

					@Override
					public void run() {
						try{
							// TODO Auto-generated method stub
							//The following code will reopen the same window
							userAccountsModel chatroom = (userAccountsModel) javaSpace.read(chatroom_template, null, Long.MAX_VALUE);
							messageHolderModel chatUser = new messageHolderModel(selectedUser, getUser.getInstance().getUser().getUsername(), false);
							javaSpace.write(chatUser, null, Long.MAX_VALUE);

							//It will direct users to the same interface.
							FXMLLoader loader = new FXMLLoader(
									getClass().getResource(
											"../View/replyToAll.fxml"
											)
									);

							Node  source = (Node)  event.getSource();
							Stage stage  = (Stage) source.getScene().getWindow();

							stage.setScene(
									new Scene(loader.load())
									);
							//refresh the interface and remove expired messages from the list view
							replyToAllController controller = loader.<replyToAllController>getController();
							controller.chatAllUsers(chatroom);
							stage.show();
						}catch(Exception e){
							e.printStackTrace();
						}
					}

				});
			}else{
				//write the information to JavaSpace
				userAccountsModel everyoneUser = new userAccountsModel("everyuser", null, null);
				javaSpace.write(everyoneUser, null, Long.MAX_VALUE);
			}
		} catch (Exception e){
			e.printStackTrace();
		}

	}

	/**
	 * sendMessage method allow users to send a message. 
	 * Users requires to choose the message duration before sending the message.
	 * @param event
	 */
	@FXML
	private void sendMessage(ActionEvent event){
		replyToAllNotification.setStyle("-fx-text-fill: red;");
		long getDuration = 0;
		if(messageBox.getText().isEmpty()){
			//Avoiding blank inputs
			replyToAllNotification.setText("Please enter something in the message box");
		}else{
			//Getting user preference of time limit
			if(twoMinOption.isSelected() && !oneMinOption.isSelected()){
				getDuration = 120000;
			}else if(oneMinOption.isSelected() && !twoMinOption.isSelected()){
				getDuration = 60000;
			}else{
				//avoiding unselected value
				replyToAllNotification.setText("Please select one message duration");
			}

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			//get current date time with Date()
			Date date = new Date();

			if(getDuration > 0){
				//Insert data to the space. Try catch method is used to handle view any exception
				try {	
					//Turns blank if user inputs a blank inputs and then inputs a string.
					replyToAllNotification.setText("");
					String fullMessageText = currentUser.getUsername() + ": " + messageBox.getText() + " [" + dateFormat.format(date) + "]";
					currentMessagesModel message = new currentMessagesModel(username, currentUser.getUsername(), fullMessageText, false, false);
					//the purpose for the following line will be explained shortly.
					previousMessage = new currentMessagesModel(username, currentUser.getUsername(), fullMessageText, false, false);
					//write data to JavaSpace
					javaSpace.write(message, null, getDuration);
					System.out.println("Message Duration: " + getDuration);
					//set the message box blank again
					messageBox.setText("");
					if(notification.isSelected() || notification.isSelected() == true){
						notification.setSelected(true); 
					}else{
						notification.setSelected(false); 
					}
					Thread.sleep(1000);
					//Call this function to allow senders to extend a time of a message (its optional).
					extendMessageTime(message);
				}catch (Exception e) {
					e.printStackTrace();
				}


			}
		}
	}

	/**
	 * The code below is the implementation of extending a time of a previous message the user sent. 
	 * The function is triggered after a period
	 * @param message
	 */
	private void extendMessageTime(currentMessagesModel message){
		timer.schedule(new TimerTask(){
			@Override
			public void run(){
				try{
					//check if message still exists
					currentMessagesModel recentMessage = (currentMessagesModel) javaSpace.readIfExists(message, null, Long.MAX_VALUE);
					if(recentMessage != null){
						//debugging
						System.out.println("message still exists");
						//do the following code for senders
						if(currentUser.getUsername().equals(recentMessage.getUsername())){
							Platform.runLater(new Runnable(){
								public void run(){
									try{
										//Gives user's option to extend option or not.
										Alert alert = new Alert(AlertType.WARNING, currentUser.getUsername() + "\n" + "Your recent message to " + username + " is about to expire \n " + 
												"Message: " + recentMessage.getMessage() + "\n"
												+"Click Yes to extend message for 2 minutes or click No to do nothing", ButtonType.YES, ButtonType.NO);
										alert.showAndWait();
										//If users extend time, the message is written into JavaSpace with "Extended Time" notification
										if (alert.getResult() == ButtonType.YES) {
											String fullMessageText = message.getMessage() + " [Extended Time]";
											previousMessage = new currentMessagesModel(username, message.getUsername(), fullMessageText, false, false);
											javaSpace.write(previousMessage, null, 120000);
										}else{
											System.out.println("No action added to the message");
										}
									}catch(Exception e){
										e.printStackTrace();
									}
								}
							});
						}
					}else{
						System.out.print("No notification");
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}, (20000));
	}

	/**
	 * The following function allows users to leave the current interface to the user list interface
	 * @param event
	 * @throws Exception
	 */
	@FXML
	public void leaveInterface(ActionEvent event) throws Exception{
		//Update current records
		messageHolderModel chatroom_currentuser = new messageHolderModel(this.username, getUser.getInstance().getUser().getUsername(), null);
		messageHolderModel user = (messageHolderModel) javaSpace.take(chatroom_currentuser, null, 1000);
		user.setDeleted(true);
		javaSpace.write(user, null, 10000);

		userAccountsModel currentThisUser = getUser.getInstance().getUser();

		//Direct users to the chat interface list interface
		Stage stage = (Stage) say_button.getScene().getWindow();
		stage.close();

		//The path
		FXMLLoader loader = new FXMLLoader(
				getClass().getResource(
						"../View/talkUsers.fxml"
						)
				);

		Node  source = (Node)  event.getSource();
		Stage stageChatroom  = (Stage) source.getScene().getWindow();

		stageChatroom.setScene(
				new Scene(loader.load())
				);

		//Execute the follow method and show the window.
		talkUsersController controller = loader.<talkUsersController>getController();
		controller.getMessages(currentThisUser);
		stage.show();
	}

	/**
	 * This is used to match two strings and either returns true or false.
	 * @param toMatch
	 * @param matchIn
	 * @return
	 */
	public static boolean matchesWord(String toMatch, String matchIn) {
		return Pattern.matches(".*([^A-Za-z]|^)"+toMatch+"([^A-Za-z]|$).*", matchIn);
	}

	/**
	 * This is triggered when a user send a new message or deletes a message
	 */
	@FXML
	public void notify(RemoteEvent event)  {
		// TODO Auto-generated method stub
		try {
			AvailabilityEvent e = (AvailabilityEvent) event;
			Entry object;
			object = e.getEntry();
			if (object.getClass().equals(currentMessagesModel.class)) {
				//add the new message
				currentMessagesModel msg = (currentMessagesModel) object;
				System.out.println("Sent messages to all users");
				Platform.runLater(() -> messages.add(msg.formatMessage()));
				Thread.sleep(1000);
				if(notification.isSelected() || notification.isSelected() == true){
					if(!msg.getUsername().equals(currentUser.getUsername())){
						Platform.runLater(new Runnable(){
							public void run(){
								try{
									//Notify the receiver a new message is received
									Alert alert = new Alert(AlertType.INFORMATION, currentUser.getUsername() + "\n" + "A new message is sent from " + msg.getUsername() + 
											" in the " + username + " interface \n" + "Message: " + msg.getMessage()  , ButtonType.OK);
									alert.showAndWait();

								}catch(Exception e){
									e.printStackTrace();
								}
							}
						});
					}
					replyToAllNotification.setStyle("-fx-text-fill: green;");
					Platform.runLater(() -> replyToAllNotification.setText(msg.getUsername() + " sent a new message"));
					labelVanquish();
				}else if(notification.isSelected() == false || notification.isSelected() == false || notification.getText() == null){
					//Just display a label instead
					if(!msg.getUsername().equals(currentUser.getUsername())){
						replyToAllNotification.setStyle("-fx-text-fill: green;");
						Platform.runLater(() -> replyToAllNotification.setText(msg.getUsername() + " sent a new message"));
						labelVanquish();
					}
				}

			}else if (object.getClass().equals(messageHolderModel.class)) {
				//Triggered when a new user is using the same interface
				messageHolderModel chatuser = (messageHolderModel) object;
				if (chatuser.getDeleted()) {
					//remove a user if the user is offline
					if(messageUserlist.contains(chatuser.getUser())){
						Platform.runLater(() -> messageUserlist.removeIf(Predicate.isEqual(chatuser.getUser())));
					}
					replyToAllNotification.setStyle("-fx-text-fill: red;");
					Platform.runLater(() -> replyToAllNotification.setText(chatuser.getUser() + " is offline"));
					System.out.println("-User:" + chatuser.getUser());
					labelVanquish();
				} else {
					//add a the user if the user is online.
					if(!messageUserlist.contains(chatuser.getUser())){
						Platform.runLater(() -> messageUserlist.add(chatuser.getUser()));
					}
					replyToAllNotification.setStyle("-fx-text-fill: green;");
					Platform.runLater(() -> replyToAllNotification.setText(chatuser.getUser() + " is online"));
					System.out.println("+User:" + chatuser.getUser());
					labelVanquish();
				}
				Thread.sleep(500);
			}else if (object.getClass().equals(deleteMessageModel.class)) {
				// Remove messages data from space.
				System.out.println("Message deleted");
				deleteMessageModel deleteRoom = (deleteMessageModel) object;
				replyToAllNotification.setStyle("-fx-text-fill: red;");
				Platform.runLater(() -> replyToAllNotification.setText("A message is deleted"));
				Platform.runLater(() -> messages.removeIf(Predicate.isEqual(deleteRoom.getMsssage())));
				labelVanquish();
				Thread.sleep(500);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * The code is used to do a particular after a time period is triggered. In this is case its 1 minute.
	 * Since its used several times, its more efficient this way.
	 */
	private void labelVanquish(){
		timer.schedule(new TimerTask(){
			@Override
			public void run(){
				try{
					Platform.runLater(() -> replyToAllNotification.setText(""));
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}, 10000);
	}


	/**
	 * The following method gets all messages from all users and displays it to a list view.
	 * It also collects all current users interacting this interface on the list view.
	 * @param chatroom
	 */
	public void chatAllUsers(userAccountsModel chatroom) {

		// Get the user name.
		username = chatroom.getUsername();

		//Add designs to the button
		btn_deleteMessage.setStyle("-fx-font-size:14; -fx-text-fill:white; -fx-border-color:black; -fx-background-color: #cb0000;");
		say_button.setStyle("-fx-font-size:14; -fx-text-fill:white; -fx-border-color:black; -fx-background-color: #598234;");
		leaveInterface.setStyle("-fx-font-size:14; -fx-text-fill:white; -fx-border-color:black; -fx-background-color: #34888c;");

		//display your user name
		everyoneHeader.setText("| Contact User: Everyone | Chatting as: " + currentUser.getUsername() + " |");

		//Specifying the current user is the owner
		if (username.equals(getUser.getInstance().getUser().getUsername())) {
			this.isOwner = true;
		} else {
			this.isOwner = false;
		}

		// Continuously update the list view.
		messageList.setCellFactory(lv -> new ListCell<String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					TextFlow tf = new TextFlow(new Text(item));
					tf.maxWidthProperty().bind(lv.widthProperty());
					setGraphic(tf);
				}
			}
		});

		// Templates array lists
		ArrayList<Entry> entry_template_array = new ArrayList<>();
		currentMessagesModel message_owner_template;
		currentMessagesModel message_nonowner_template;
		messageHolderModel users_template;
		deleteMessageModel delete_template;

		//Giving message privilege to the message owner and non-owners.
		if (isOwner) {
			message_owner_template = new currentMessagesModel(username, null, null, true, null);
			entry_template_array.add(message_owner_template);

		} else {
			message_nonowner_template = new currentMessagesModel(username, null, null, true, false);
			entry_template_array.add(message_nonowner_template);
		}

		// This is the default settings.
		users_template = new messageHolderModel(username, null, null);
		entry_template_array.add(users_template);
		delete_template = new deleteMessageModel(username, null);
		entry_template_array.add(delete_template);
		System.out.println("Arrays");
		// Get all messages
		try {
			MatchSet allMessages = javaSpace.contents(entry_template_array, null, Lease.FOREVER, Long.MAX_VALUE);
			Entry object;
			// Maybe use instanceOf
			while ((object = allMessages.next()) != null) {
				if (object.getClass().equals(currentMessagesModel.class)) {
					//view old messages
					currentMessagesModel msg = (currentMessagesModel) object;
					messages.add(msg.formatMessage());
					System.out.println("+Message");
				}else if (object.getClass().equals(messageHolderModel.class)) {
					//view current users.
					messageHolderModel chatuser = (messageHolderModel) object;
					userlist.getItems().clear();
					if(!messageUserlist.contains(chatuser.getUser())){
						messageUserlist.add(chatuser.getUser());
					}
				}
			}
			//display message data to the list view
			messageList.setItems(messages);
			//display current users online on the list view.
			userlist.setItems(messageUserlist);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// The security manager
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		// This is the in it exporter
		Exporter myDefaultExporter =
				new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
						new BasicILFactory(), false, true);

		// Setting the remote listener
		try {
			// Register it as a remote object and retrieve the reference to the 'remote'. 
			// Eventually add the listener
			remote = (RemoteEventListener) myDefaultExporter.export(this);
			javaSpace.registerForAvailabilityEvent(entry_template_array, null, true, remote, Lease.FOREVER, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * The following method allow users to delete a messages. 
	 * The function only allows message owners for deletions.
	 * @param event
	 */
	@FXML
	public void btn_deleteMessage(ActionEvent event){
		//get the selected user name from the list view.
		String selectedMessage = messageList.getSelectionModel().getSelectedItem();
		if (null != selectedMessage){
			currentMessagesModel chatroom_template = new currentMessagesModel(username, null, selectedMessage, true, null);
			try {
				//checking if the user is the holder of the message.
				currentMessagesModel chatroom = (currentMessagesModel) javaSpace.readIfExists(chatroom_template, null, Long.MAX_VALUE);
				if (chatroom.getUsername().equals(currentUser.getUsername())){
					//If so, delete the message
					deleteMessageModel delete = new deleteMessageModel(username, selectedMessage);
					javaSpace.write(delete, null, Long.MAX_VALUE);

					// clean up the arrays.
					ArrayList<Entry> entry_template_array = new ArrayList<>();
					currentMessagesModel delete_template = new currentMessagesModel(username, null, selectedMessage, true, null);
					currentMessagesModel room_messages = (currentMessagesModel) javaSpace.readIfExists(delete_template, null, Long.MAX_VALUE);
					System.out.println("room_message: "+room_messages);
					entry_template_array.add(room_messages);


					// remove data from space
					Collection room_entries = javaSpace.take(entry_template_array, null, 1000, 10000);
					System.out.println("room entries: "+room_entries.toString());
					// Null local entries for java garbage collection
					room_entries = null;

				} else {
					//This line of code is triggered if users are not the holders of the selected message.
					replyToAllNotification.setStyle("-fx-text-fill: red;");
					replyToAllNotification.setText("Message owners only have permission to delete messages.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			//This line of code is triggered if users did not selected a message.
			replyToAllNotification.setText("Please select a message.");
		}
	}


}
