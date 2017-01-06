package JUnit;

import static org.junit.Assert.*;

import org.junit.Test;

import Model.messageHolderModel;

/**
 * The following tests done is analysing if the variables stored the correct information.
 * I'm also testing if a variables is not null.
 * @author u1476904 Azhan Rashid
 *
 */

public class messageHolderTest {

	public String userMessage = "testing the messageHolder class";
	public String user = "u1476904";
	public Boolean deleted = true;
	
	private final messageHolderModel testMessageHolder = new messageHolderModel(userMessage, user, deleted);
	
	@Test
	public void testUser() {
		assertEquals(testMessageHolder.getUser(), user);
	}
	
	@Test
	public void testUserMessage() {
		assertEquals(testMessageHolder.getMessages(), userMessage);
	}
	
	@Test
	public void testDeleted() {
		assertSame(testMessageHolder.getDeleted(), deleted);
	}
	
	
	@Test
	public void testUserNotNull() {
		assertNotNull(testMessageHolder.getUser());
	}
	
	@Test
	public void testUserMessageNotNull() {
		assertNotNull(testMessageHolder.getMessages());
	}
}
