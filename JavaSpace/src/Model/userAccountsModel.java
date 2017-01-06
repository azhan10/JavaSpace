package Model;

/**
 * The following class is used to write new accounts to JavaSpace, 
 * view current users who made a account and check if account details is correct.
 * @author u1476904 Azhan Rashid
 */


import net.jini.core.entry.Entry;


public class userAccountsModel implements Entry {
	public String username;
	public String password;
	public String messageOwner;


	// No arguement
	public userAccountsModel() {
	}

	public userAccountsModel(String username, String password, String messageOwner) {
		this.username = username;
		this.password = password;
		this.messageOwner = messageOwner;
	}

	/* 
	 * Get current user name
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/* 
	 * Get current password 
	 */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	//Get the message owner
	public String getMessageOwner() {
		return messageOwner;
	}

	public void setMessageOwner(String messageOwner) {
		this.messageOwner = messageOwner;
	}

	/*
	 * This method is used to has the users password when there register. 
	 * I'm using the standard MD5.
	 */
	public String hashPassword(String md5) {
		try {
			//Hashing the password to MD5.
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}
}
