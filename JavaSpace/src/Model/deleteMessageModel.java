package Model;

import net.jini.core.entry.Entry;

/**
 * The class is used for deletion purposes. When deleting a message. 
 * This is helpful to allow the program to delete the message from JavaSpace.
 * It requires the message and user name.
 * @author u1476904 Azhan Rashid
 *
 */

public class deleteMessageModel implements Entry {
	public String message;
	public String username;

	public deleteMessageModel(String message) {
		this.message = message;
	}
	public deleteMessageModel(String username, String message) {
		this.username = username;
		this.message = message;
	}

	public String getMsssage() {
		return message;
	}
	
	public String getUsername(){
		return username;
	}

	// No argument is required.
	public deleteMessageModel(){
	}

	public void setUsername(String username) {
		this.message = username;
	}
}
