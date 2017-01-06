package Extras;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace05;
import java.rmi.RMISecurityManager;

/**
 * The class allows the user to connect to Apache and JavaSpace.
 * @author u1476904 Azhan Rashid
 *
 */


public class SpaceUtils {

	public static JavaSpace05 getSpace(String hostname) {
		JavaSpace05 js = null;
		try {
			LookupLocator l = new LookupLocator("jini://" + hostname);

			ServiceRegistrar sr = l.getRegistrar();

			Class c = Class.forName("net.jini.space.JavaSpace05");
			Class[] classTemplate = {c};

			js = (JavaSpace05) sr.lookup(new ServiceTemplate(null, classTemplate, null));

		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
		return js;
	}

	//You would need to update this name every time the user compile the program
	public static JavaSpace05 getSpace() {
		return getSpace("waterloo");
	}


	public static TransactionManager getManager(String hostname) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		TransactionManager tm = null;
		try {
			LookupLocator l = new LookupLocator("jini://" + hostname);

			ServiceRegistrar sr = l.getRegistrar();

			Class c = Class.forName("net.jini.core.transaction.server.TransactionManager");
			Class[] classTemplate = {c};

			tm = (TransactionManager) sr.lookup(new ServiceTemplate(null, classTemplate, null));

		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
		return tm;
	}

	//You would need to update this name every time the user compile the program
	public static TransactionManager getManager() {
		return getManager("waterloo");
	}
}

