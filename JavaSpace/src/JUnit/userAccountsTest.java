package JUnit;

import static org.junit.Assert.*;

import org.junit.Test;

import Model.userAccountsModel;


/**
 * The following tests done is analysing if the variables stored the correct information.
 * I'm also testing if the password encryption works.
 * @author u1476904 Azhan Rashid
 *
 */

public class userAccountsTest {
	
	private String username = "u1476904";
	private String password = "testPassword";
	private String messageOwner = username;
	
	private final userAccountsModel accountTextVariables = new userAccountsModel(username, password, messageOwner);

	@Test
	public void testSavedUsername() {
		assertEquals(accountTextVariables.getUsername(), username);
	}
	
	@Test
	public void testSavedPassword() {
		assertEquals(accountTextVariables.getPassword(), password);
	}

	@Test
	public void testSavedMessageOwner() {
		assertEquals(accountTextVariables.getMessageOwner(), messageOwner);
	}
	
	@Test
	public void testHashPassword() {
		String hash = "fed3b61b26081849378080b34e693d2e";
		assertEquals(accountTextVariables.hashPassword(password), hash);
	}
	
}
