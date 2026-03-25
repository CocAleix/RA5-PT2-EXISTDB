package es.ilerna.M0486.ra5.pt2.main;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;

public class ExistDbManager {

	private static final String URI = "xmldb:exist://localhost:8080/exist/xmlrpc/db/SPOTYDAM";
	private static final String USER = "admin";
	private static final String PASSWORD = "admin";

	public static Collection connect() {
		try {
			// 1. Es carrega el driver d’eXist-db
			Class cl = Class.forName("org.exist.xmldb.DatabaseImpl");
			// 2. Es crea un objecte Database
			Database database = (Database) cl.getDeclaredConstructor().newInstance();
			// 3. Es configura l'objecte Database i es registra
			database.setProperty("create-database", "true");
			DatabaseManager.registerDatabase(database);
			
			// 4. S'obre la col·lecció de treball
			return DatabaseManager.getCollection(URI, USER, PASSWORD);
	
		} catch (Exception e) {
			throw new RuntimeException("Error connectant amb eXist-db", e);
		}
	}
}

