package Controller;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;

import Extras.SpaceUtils;
import Extras.accessWindow;
import Extras.getUser;
import Model.currentMessagesModel;
import Model.deleteMessageModel;
import Model.messageHolderModel;
import Model.userAccountsModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import net.jini.core.entry.Entry;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
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
 * The class is the described as the chat interface.
 * The class holds all the functions of allowing users to communicate a user
 * The class contains implementation of refreshing the the message page, sending messages to users, 
 * delete messages, view messages and users, close program, view selected message information, extend a sent message time,
 * mark messages as read, logout the interface. The class contains a function to direct users to another interface (replyToAll interface).
 * @author u1476904 Azhan Rashid
 */
public class talkUsersController implements RemoteEventListener{

	@FXML
	private ListView<String> messageList;
	private ObservableList<String> messages = FXCollections.observableArrayList();
	private ObservableList<String> messageUserlist = FXCollections.observableArrayList();
	private userAccountsModel currentUser = getUser.getInstance().getUser();
	private String username;
	private Boolean isOwner;
	private RemoteEventListener remote;
	@FXML
	private ListView<String> userlist;
	@FXML
	private CheckBox twoMinOption;
	@FXML
	private CheckBox oneMinOption;
	@FXML
	private CheckBox notification;
	@FXML
	private Button btn_talkToAll;
	@FXML
	private TextArea messageBox;
	@FXML
	private Button btn_deleteMessage;
	@FXML
	private Button btn_sendMessage;
	@FXML
	private Button btn_logout;
	@FXML
	private Button btn_markRead;
	@FXML
	private Label chatNotification;
	@FXML
	private Label title;
	private JavaSpace05 javaSpace;
	private Timer timer = new Timer();
	currentMessagesModel previousMessage;
	currentMessagesModel notReadMessage;
	@FXML
	private Button btn_messageInformation;

	//Check for a connection
	public talkUsersController(){
		javaSpace = SpaceUtils.getSpace();
		if (javaSpace == null) {
			System.err.println("Failed to find the javaspace");
			System.exit(0);
		}
	}

	/**
	 * The method below is used to allow users to save a message in JavaSpace.
	 * The message is stored in JavaSpace for long period of time.
	 * @param event
	 */
	@FXML
	private void btn_saveRead(ActionEvent event){
		String saveSelectedMessage = messageList.getSelectionModel().getSelectedItem();

		if(saveSelectedMessage != null){
			//Insert data to JavaSpace. Try catch method is used to handle any exception
			try {	
				currentMessagesModel saveMessage_template = new currentMessagesModel(username, null, saveSelectedMessage, true, null);
				currentMessagesModel getSaveMessage = (currentMessagesModel) javaSpace.readIfExists(saveMessage_template, null, Long.MAX_VALUE);
				if (getSaveMessage != null){
					//Turns blank if user inputs a blank inputs and then inputs a string.
					chatNotification.setText("");
					//String formats for the saved message.
					String fullMessageTextSave =  getSaveMessage.getMessage() + " [ Saved ]";
					currentMessagesModel saveMessage = new currentMessagesModel(currentUser.getUsername(), currentUser.getUsername(), fullMessageTextSave, false, true);
					//write data to JavaSpace for long period of time
					javaSpace.write(saveMessage, null, Long.MAX_VALUE);
					//remove selected data from list view.
					userlist.getSelectionModel().select(null);
					if(notification.isSelected() || notification.isSelected() == true){
						notification.setSelected(true); 
					}else{
						notification.setSelected(false); 
					}
					Thread.sleep(1000);
				}else{
					//It executed if the selected message is expired
					chatNotification.setStyle("-fx-text-fill: red;");
					chatNotification.setText("Message is removed from JavaSpace");
					labelVanquish();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			//Following code is triggered when the user has not selected a message from the list view.
			chatNotification.setStyle("-fx-text-fill: red;");
			chatNotification.setText("Please select a message to save");
			//Remove the notification after a fixed period of time
			labelVanquish();
		}
	}

	/**
	 * The following code is used to refresh a interface. This is used to refresh the message list.
	 * This way any expired messages will be gone from the list view.
	 * @param event
	 */
	@FXML
	private void btn_refresh(ActionEvent event){
		try{
			Platform.runLater(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					userAccountsModel currentThisUser = new userAccountsModel(currentUser.getUsername(), null, null);
					FXMLLoader loader = new FXMLLoader(
							getClass().getResource(
									"../View/talkUsers.fxml"
									)
							);

					Node  source = (Node)  event.getSource();
					Stage stage  = (Stage) source.getScene().getWindow();

					try {
						stage.setScene(
								new Scene(loader.load())
								);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//Refreshing the same interface
					talkUsersController controller = loader.<talkUsersController>getController();
					controller.getMessages(currentThisUser);
					stage.show();
				}

			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * The closeProgram method terminates the program
	 * @param event
	 */
	@FXML
	private void closeProgram(ActionEvent event){
		System.out.println("Program is terminated");
		System.exit(0);
	}

	/**
	 * The talkToall method takes users to another interface that allows all users to interact to each other.
	 * @param event
	 */
	@FXML
	protected void btn_talkToAll(ActionEvent event){
		String selectedUser = "everyuser";

		try {
			userAccountsModel template = new userAccountsModel(selectedUser, null, null);
			userAccountsModel exists = (userAccountsModel) javaSpace.readIfExists(template, null, 1000);

			userAccountsModel chatroom_template = new userAccountsModel(selectedUser, null, null);
			//checking if data is not deleted.
			if (exists != null){
				Platform.runLater(new Runnable(){

					@Override
					public void run() {
						try{
							// TODO Auto-generated method stub
							//This part direct users to that interface which displays all current data.
							userAccountsModel chatroom = (userAccountsModel) javaSpace.read(chatroom_template, null, Long.MAX_VALUE);
							messageHolderModel chatUser = new messageHolderModel(selectedUser, getUser.getInstance().getUser().getUsername(), false);
							javaSpace.write(chatUser, null, Long.MAX_VALUE);

							//It will direct users to the another interface. The data is passed to the replyToAllFunctions class.
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
							//Sending data to the replyToAllFunctions class and finally show the interface.
							replyToAllController controller = loader.<replyToAllController>getController();
							controller.chatAllUsers(chatroom);
							stage.show();
						}catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}

				});
			}else{
				//write the information and restart the function
				userAccountsModel everyoneUser = new userAccountsModel("everyuser", null, null);
				javaSpace.write(everyoneUser, null, Long.MAX_VALUE);
				btn_talkToAll.fire();
			}
		} catch (Exception e){
			e.printStackTrace();
		}


	}

	/**
	 * The following methods let users to view information of a selected message.
	 * The function views the message sender, receiver and the message itself.
	 * @param event
	 */
	@FXML
	private void btn_messageInformation(ActionEvent event){
		String selectedMessageInformation = messageList.getSelectionModel().getSelectedItem();
		//avoiding unselected data
		if(selectedMessageInformation != null){
			try{
				currentMessagesModel getSelectedInformation = new currentMessagesModel(username, null, selectedMessageInformation, true, null);
				currentMessagesModel messageInformation = (currentMessagesModel) javaSpace.readIfExists(getSelectedInformation, null, Long.MAX_VALUE);

				//View message format if the message is a saved copy.
				if(messageInformation != null && messageInformation.getMessage().indexOf("[ Saved ]") != -1){
					Alert alert = new Alert(AlertType.INFORMATION,  "This is a saved message \n" +    
							"Message Owner " + currentUser.getUsername() + "\n" + "Message Statement: \n" + messageInformation.getMessage() + "\n"
							, ButtonType.OK);
					alert.showAndWait();
				}else{
					if(messageInformation != null && messageInformation.getMessage().indexOf("[ Copy ]") == -1){
						//View the following if its not a copy message
						Alert alert = new Alert(AlertType.INFORMATION, "Message From " + messageInformation.getUsername() + "\n" +
								"Message To: " + currentUser.getUsername() + "\n" + "Message Statement: \n" + messageInformation.getMessage() + "\n"
								, ButtonType.OK);
						alert.showAndWait();
					}else{
						//View the following if the message is a copy message
						if(messageInformation != null && messageInformation.getMessage().indexOf("[ Copy ]") != -1){
							Alert alert = new Alert(AlertType.INFORMATION,  "This is a copy message \n" +    
									"Message From " + currentUser.getUsername() + "\n" +
									"Message To: " + messageInformation.getUsername() + "\n" + "Message Statement: \n" + messageInformation.getMessage() + "\n"
									, ButtonType.OK);
							alert.showAndWait();
						}else{
							//View the following if message is expired.
							Alert alert = new Alert(AlertType.INFORMATION, selectedMessageInformation + " is expired" , ButtonType.OK);
							alert.showAndWait();
							//					chatNotification.setStyle("-fx-text-fill: red;");
							//					chatNotification.setText(selectedMessageInformation + " is expired");
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * The following method allows users to send messages to each other.
	 * The code send messages to one user which is private between the sender and receiver.
	 * @param event
	 */
	@FXML
	private void btn_sendMessage(ActionEvent event){
		chatNotification.setStyle("-fx-text-fill: red;");
		long getDuration = 0;
		if(messageBox.getText().isEmpty()){
			//Avoiding blank inputs
			chatNotification.setText("Please enter something in the message box");
		}else{
			String userSend = userlist.getSelectionModel().getSelectedItem();
			//avoiding unselected data
			if(userSend != null){
				//getting the user time limit choice
				if(twoMinOption.isSelected() && !oneMinOption.isSelected()){
					getDuration = 120000;
				}else if(oneMinOption.isSelected() && !twoMinOption.isSelected()){
					getDuration = 60000;
				}else{
					//notify user to choose a duration
					chatNotification.setText("Please select one message duration");
				}

				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				//get current date time with Date()
				Date date = new Date();

				if(getDuration > 0){
					//Insert data to JavaSpace. Try catch method is used to handle any exception
					try {	
						//Turns blank if user inputs a blank inputs and then inputs a string.
						chatNotification.setText("");
						//String formats for both sender and receiver.
						String fullMessageText = currentUser.getUsername() + ": " + messageBox.getText() + " [" + dateFormat.format(date) + "]";
						String fullMessageTextCopy = currentUser.getUsername() + ": " + messageBox.getText() + " [" + dateFormat.format(date) + "]" + " [ Copy ]";
						currentMessagesModel message = new currentMessagesModel(userSend, currentUser.getUsername(), fullMessageText, false, true);
						currentMessagesModel saveMessage = new currentMessagesModel(currentUser.getUsername(), userSend, fullMessageTextCopy, false, true);
						//write data to JavaSpace and send message to user and send a copy of message to the sender.
						javaSpace.write(message, null, getDuration);
						javaSpace.write(saveMessage, null, getDuration);
						System.out.println("Message Duration: " + getDuration);
						//set the message box blank again
						messageBox.setText("");
						//remove selected data from list view.
						userlist.getSelectionModel().select(null);
						if(notification.isSelected() || notification.isSelected() == true){
							notification.setSelected(true); 
						}else{
							notification.setSelected(false); 
						}
						Thread.sleep(1000);
						//Call this function to allow senders to extend a time of a message (its optional).
						extendMessageTime(userSend, message);

					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				getDuration = 0;
			}else{
				//print statement if user does not select a message
				chatNotification.setText("Please choose a user to send the message");
			}
		}
	}

	/**
	 * The code below is the implementation of extending a time of a previous message their sent. 
	 * The function is triggered after a period
	 * @param userSend 
	 */
	private void extendMessageTime(String userSend, currentMessagesModel message){
		timer.schedule(new TimerTask(){
			@Override
			public void run(){
				try{
					//check if message still exists
					currentMessagesModel recentMessage = (currentMessagesModel) javaSpace.readIfExists(message, null, Long.MAX_VALUE);
					if(recentMessage != null){
						//debugging
						System.out.println("message still exists");
						//do the following to the sender interface
						if(currentUser.getUsername().equals(recentMessage.getUsername())){
							Platform.runLater(new Runnable(){
								public void run(){
									try{
										//Gives user's option to extend message or not.
										Alert alert = new Alert(AlertType.WARNING, currentUser.getUsername() + "\n" + "Your recent message to " + username + " is about to expire \n " + 
												"Message: " + recentMessage.getMessage() + "\n"
												+"Click Yes to extend message for 2 minutes or click No to do nothing", ButtonType.YES, ButtonType.NO);
										alert.showAndWait();
										//If users extend time, the message is written into JavaSpace with "Extended Time" notification
										if (alert.getResult() == ButtonType.YES) {
											String fullMessageText = recentMessage.getMessage() + " [Extended Time]";
											previousMessage = new currentMessagesModel(userSend, currentUser.getUsername(), fullMessageText, false, true);
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
						//debugging
						System.out.print("No notification");
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}, (20000));
	}

	/**
	 * The following code is used to allow users to log out their account.
	 * The function will direct users back to the index interface.
	 * @param event
	 */
	@FXML
	private void btn_logout(ActionEvent event){
		Platform.runLater(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Node node = (Node) event.getSource();
				Stage stage = (Stage) node.getScene().getWindow();
				//direct users to the index interface using accessWindow class.
				accessWindow index = new accessWindow("../View/index.fxml", stage);
			}

		});
	}

	/**
	 * The following code is used to notify senders that their senders has read their message.
	 * This is done by sending the same message with an additional text at the right side stating the receiver has read this message.
	 * @param event
	 */
	@FXML
	private void btn_markRead(ActionEvent event){
		//get the selected topic name from the list view.
		String selectedMessageRead = messageList.getSelectionModel().getSelectedItem();

		if(selectedMessageRead != null){
			try{
				currentMessagesModel markUnread_template = new currentMessagesModel(username, null, selectedMessageRead, true, null);
				currentMessagesModel unreadMessage = (currentMessagesModel) javaSpace.readIfExists(markUnread_template, null, Long.MAX_VALUE);
				//only accepting received messages from other users. 
				if(unreadMessage != null && unreadMessage.getMessage().indexOf(" [" + unreadMessage.getUsername() +  " read this message]") == -1 
						&& unreadMessage.getMessage().indexOf("[ Copy ] [" + unreadMessage.getUsername() +  " read this message]") == -1 && 
						unreadMessage.getMessage().indexOf("[ Copy ]") == -1){
					System.out.println("unreadMessage: " + unreadMessage.getMessage());
					System.out.println("selctedMessage: " + selectedMessageRead);
					//Write the following
					String fullMessageTextNotRead = unreadMessage.getMessage() + " [" + currentUser.getUsername() +  " read this message]";
					notReadMessage = new currentMessagesModel(unreadMessage.getUsername(), currentUser.getUsername(), fullMessageTextNotRead, false, true);
					javaSpace.write(notReadMessage, null, 60000);
				}else{
					//Notify users if selected message does not meet the condition above.
					chatNotification.setStyle("-fx-text-fill: red;");
					Platform.runLater(() -> chatNotification.setText("You can only mark received messages once"));
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			//Avoiding blank selections
			chatNotification.setStyle("-fx-text-fill: red;");
			chatNotification.setText("Please select a message");
		}
	}

	/**
	 * The following code is used to delete a message.
	 * Only the message owner can delete a message. Since the interface is entirely private to the user, 
	 * It did not require many conditions compared to the replyToAll interface.
	 * @param event
	 */
	@FXML
	private void btn_deleteMessage(ActionEvent event){
		//get the selected user message from the list view.
		String selectedMessage = messageList.getSelectionModel().getSelectedItem();

		//Avoiding blank selections
		if (null != selectedMessage){
			try {

				//delete the message
				System.out.println("Delete Fired");
				//Trigger the notify method
				deleteMessageModel delete = new deleteMessageModel(username, selectedMessage);
				javaSpace.write(delete, null, Long.MAX_VALUE);

				// clean up the arrays.
				ArrayList<Entry> entry_template_array = new ArrayList<>();
				currentMessagesModel delete_template = new currentMessagesModel(username, null, selectedMessage, true, null);
				currentMessagesModel room_messages = (currentMessagesModel) javaSpace.readIfExists(delete_template, null, Long.MAX_VALUE);
				System.out.println(room_messages);
				entry_template_array.add(room_messages);

				// remove the data from JavaSpace
				Collection room_entries = javaSpace.take(entry_template_array, null, 1000, 1000);
				System.out.println(room_entries.toString());
				// Null local entries for java garbage collection
				room_entries = null;
				Thread.sleep(1000);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			//This line of code is triggered if users did not selected a message.
			chatNotification.setStyle("-fx-text-fill: red;");
			chatNotification.setText("Please select a message.");
		}
	}

	/**
	 * The following code is used to get current messages and users that are stored in JavaSpace.
	 * Then display those data on the list views.
	 * The following code also shows implementation of labels in the interface such as the user name.
	 * It also uses the security manager and remote listener
	 * @param currentThisUser
	 */
	public void getMessages(userAccountsModel currentThisUser) {
		// Get the user name.
		username = currentThisUser.getUsername();

		//Add designs to the button
		btn_deleteMessage.setStyle("-fx-font-size:14; -fx-text-fill:white; -fx-border-color:black; -fx-background-color: #cb0000;");
		btn_sendMessage.setStyle("-fx-font-size:14; -fx-text-fill:white; -fx-border-color:black; -fx-background-color: #598234;");
		btn_logout.setStyle("-fx-font-size:14; -fx-text-fill:white; -fx-border-color:black; -fx-background-color: #34888c;");
		btn_talkToAll.setStyle("-fx-font-size:14; -fx-text-fill:white; -fx-border-color:black; -fx-background-color: #505160;");
		btn_messageInformation.setStyle("-fx-font-size:14; -fx-text-fill:white; -fx-border-color:black; -fx-background-color: #ed5752;");
		btn_markRead.setStyle("-fx-font-size:14; -fx-text-fill:white; -fx-border-color:black; -fx-background-color: #011a27;");

		//display the your user name
		title.setText("Welcome " + currentUser.getUsername());

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
		userAccountsModel users_template;
		deleteMessageModel delete_template;

		//Setting message condition for both the message holders and non-holders
		if (isOwner) {
			message_owner_template = new currentMessagesModel(username, null, null, true, null);
			entry_template_array.add(message_owner_template);

		} else {
			message_nonowner_template = new currentMessagesModel(username, null, null, true, false);
			entry_template_array.add(message_nonowner_template);
		}

		// This is the default settings.
		users_template = new userAccountsModel(null, null, null);
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
					//view old messages to the list view
					currentMessagesModel msg = (currentMessagesModel) object;
					messages.add(msg.formatMessage());
					System.out.println("+Message");
				}else if (object.getClass().equals(userAccountsModel.class)) {
					userAccountsModel ctrm = (userAccountsModel) object;
					System.out.println(getUser.getInstance().getUser().getUsername());
					//add users to the list view
					messageUserlist.add(ctrm.getUsername());
					//avoiding the current user on the list.
					if(messageUserlist.contains(getUser.getInstance().getUser().getUsername())){
						messageUserlist.remove(getUser.getInstance().getUser().getUsername());
					}
					//avoiding the following information in the list
					if(messageUserlist.contains("everyuser")){
						messageUserlist.remove("everyuser");
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
	 * The following method is the notify method which is used for the RemoteEventListener.
	 * This is triggered when any transaction takes place which are either new messages or deleted messages.
	 */
	@FXML
	public void notify(RemoteEvent event)  {
		try {
			AvailabilityEvent e = (AvailabilityEvent) event;
			Entry object;
			object = e.getEntry();
			if (object.getClass().equals(currentMessagesModel.class)) {
				//add the new message
				currentMessagesModel msg = (currentMessagesModel) object;
				System.out.println("Sent messages to all users");
				//add message to the list
				Platform.runLater(() -> messages.add(msg.formatMessage()));
				Thread.sleep(1000);
				//if notification is selected to the following command
				if(notification.isSelected() || notification.isSelected() == true ){
					if(!msg.getUsername().equals(currentUser.getUsername()) && msg.getMessage().indexOf("[ Copy ]") == -1){
						Platform.runLater(new Runnable(){
							public void run(){
								try{
									//Notify the receiver that their got a new message
									Alert alert = new Alert(AlertType.INFORMATION, msg.getUsername() + " sent a new message \n" + "Message: " + msg.getMessage(), ButtonType.OK);
									alert.showAndWait();
								}catch(Exception e){
									e.printStackTrace();
								}
							}
						});

						//sleep the program a bit to prevent any race condition
						Thread.sleep(500);
						chatNotification.setStyle("-fx-text-fill: green;");
						Platform.runLater(() -> chatNotification.setText(msg.getUsername() + " sent a new message"));
						labelVanquish();
					}
				}else if(!notification.isSelected() || notification.isSelected() == false || notification.getText() == null){
					//Display the following if notification is off.
					if(!msg.getUsername().equals(currentUser.getUsername())){
						chatNotification.setStyle("-fx-text-fill: green;");
						Platform.runLater(() -> chatNotification.setText(msg.getUsername() + " sent a new message"));
						labelVanquish();
					}
				}

			}else if (object.getClass().equals(userAccountsModel.class)) {
				// add message to JavaSpace
				userAccountsModel chatroom = (userAccountsModel) object;
				Platform.runLater(() -> messageUserlist.add(chatroom.getUsername()));
			} else if (object.getClass().equals(deleteMessageModel.class)) {
				// Remove message data from JavaSpace.
				System.out.println("Message deleted");
				deleteMessageModel deleteRoom = (deleteMessageModel) object;
				chatNotification.setStyle("-fx-text-fill: red;");
				Platform.runLater(() -> chatNotification.setText("A message is deleted"));
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
					Platform.runLater(() -> chatNotification.setText(""));
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}, (10000));
	}

}
