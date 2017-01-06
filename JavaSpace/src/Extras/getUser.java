package Extras;

import Model.userAccountsModel;

/**
 * This class holds the current user logged in
 * The method is used on many conditions in the chatFunctions and roomFunctions class.
 * @author u1476904 Azhan Rashid
 *
 */

public class getUser {

	public final static getUser instance = new getUser();
	userAccountsModel user;


	private getUser(){
		user = null;
	}

	public void setCurrentUser(userAccountsModel user){
		this.user = user;
	}

	public userAccountsModel getUser(){
		return this.user;
	}

	public static getUser getInstance() {
		return instance;
	}

}
