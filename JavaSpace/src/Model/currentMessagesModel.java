package Model;

import java.awt.Color;
import java.util.Calendar;
import net.jini.core.entry.Entry;

/**
 * This class is used to handle all the messages
 * @author u1476904 Azhan Rashid
 *
 */

public class currentMessagesModel implements Entry{

	public String message;
	public Calendar timestamp;
	public Boolean isPrivate;
	public String chatUser;
	public String username;

	public String fullMessageText;

	// No argument is required.
	public currentMessagesModel(){
	}

	public currentMessagesModel(String username, String message){
		this.username = username;
		this.message = message;
	}

	public currentMessagesModel(String chatUser, String username, String message, Boolean isTemplate, Boolean isPrivate){
		this.chatUser = chatUser ;
		this.username = username;
		this.message = message;
		if (isTemplate){
			this.timestamp = null;
		} else {
			this.timestamp = Calendar.getInstance();
		}
		this.isPrivate = isPrivate;
	}

	public String formatMessage() {
		String message_body = this.getMessage();
		return message_body;
	}

	public String getFullMessage(){
		return fullMessageText;
	}

	public String getUsername() {
		return this.username;
	}

	public String getChatUser() {
		return this.chatUser;
	}

	public void setChatUser(String chatUser) {
		this.chatUser = chatUser;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Calendar getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}

	public Boolean getIsPrivate(){
		return this.isPrivate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
