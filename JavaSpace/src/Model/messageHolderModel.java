package Model;

import net.jini.core.entry.Entry;

/**
 * The class is used to store and retrieve current users on one interface.
 * The class is used on the replyToAllFunctions class and interface.
 * @author u1476904
 *
 */

public class messageHolderModel implements Entry {
	public String userMessage;
	public String user;
	public Boolean isDeleted;

	// No argument is required.
	public messageHolderModel(){
	}
	
	public messageHolderModel(String userMessage, String user, Boolean deleted) {
		this.userMessage = userMessage;
		this.user = user;
		this.isDeleted = deleted;
	}

	public Boolean getDeleted(){
		return isDeleted; 
	}

	public void setDeleted(Boolean deleted) {
		isDeleted = deleted; 
	}

	//Get the messages
	public String getMessages() {
		return userMessage;
	}

	public void setMessages(String userMessage) {
		this.userMessage = userMessage;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
