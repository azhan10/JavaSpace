package JUnit;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;

import Model.currentMessagesModel;

/**
 * The following tests done is analysing if the variables stored the correct information.
 * I'm also testing if the time and the private boolean method.
 * @author u1476904 Azhan Rashid
 *
 */


public class currentMessagesTest {
	
	public String message = "This is a test";
	public Calendar timestamp = Calendar.getInstance();
	public Boolean isPrivate = true;
	public Boolean isTemplate = false;
	public String chatTopic = "test class";
	public String username = "u1476904";
	
	private final currentMessagesModel testCurrentMessage = new currentMessagesModel(chatTopic, username, message, isTemplate, isPrivate);
	
	//fire() method triggers a JavaFX button.
			

	@Test
	public void testMessage() {
		assertEquals(testCurrentMessage.getMessage(), message);
	}
	
	@Test
	public void testPrivate() {
		assertEquals(testCurrentMessage.getIsPrivate(), isPrivate);
	}
	
	@Test
	public void testUsername() {
		assertEquals(testCurrentMessage.getUsername(), username);
	}
	
	@Test
	public void testChatUser() {
		assertEquals(testCurrentMessage.getChatUser(), chatTopic);
	}
	
	@Test
	public void testTimeStamp() {
		assertEquals(testCurrentMessage.getTimestamp().getTime().toString(), timestamp.getTime().toString());
	}

}
