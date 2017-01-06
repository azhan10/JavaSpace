package JUnit;

import static org.junit.Assert.*;

import org.junit.Test;

import Model.deleteMessageModel;

/**
 * The following test consists check if the user name and password stored in this class is correct.
 * @author u1476904 Azhan Rashid
 *
 */

public class deleteTest {
	
	String username = "testUsername";
	String password = "testPassword";
	
	deleteMessageModel del = new deleteMessageModel(username, password);

	@Test
	public void testUsername() {
		assertEquals(del.getUsername(), username);
	}

}
