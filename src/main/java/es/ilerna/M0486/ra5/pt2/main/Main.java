package es.ilerna.M0486.ra5.pt2.main;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        testConnection();

        int opcio;

        do {
            showMenu();
            opcio = llegirOpcio();

            switch (opcio) {
                case 1:
                    testConnection();
                    break;
                case 2:
                    System.out.print("Introdueix el nom de la nova subcol·lecció: ");
                    String newCollectionName = scanner.nextLine().trim();
                    new_subcollection(newCollectionName);
                    break;
                case 3:
                    System.out.print("Introdueix el nom de la subcol·lecció a eliminar: ");
                    String dropCollectionName = scanner.nextLine().trim();
                    drop_subcollection(dropCollectionName);
                    break;
                case 4:
                    System.out.print("Introdueix el nom de la subcol·lecció: ");
                    String addSubcollectionName = scanner.nextLine().trim();
                    System.out.print("Introdueix la ruta del fitxer XML: ");
                    String xmlFile = scanner.nextLine().trim();
                    add_document(addSubcollectionName, xmlFile);
                    break;
                case 5:
                    System.out.print("Introdueix el nom de la subcol·lecció: ");
                    String dropSubcollectionName = scanner.nextLine().trim();
                    System.out.print("Introdueix el nom del fitxer XML: ");
                    String fileName = scanner.nextLine().trim();
                    drop_document(dropSubcollectionName, fileName);
                    break;
                case 0:
                    System.out.println("Programa finalitzat.");
                    break;
                default:
                    System.out.println("Opció no vàlida.");
            }
        } while (opcio != 0);
        scanner.close();
    }

    public static void showMenu() {
        System.out.println("\n===== MENÚ =====");
        System.out.println("1. Provar connexió");
        System.out.println("2. CREAR NOVA SUBCOL·LECCIÓ");
        System.out.println("3. ELIMINAR UNA SUBCOL·LECCIÓ");
        System.out.println("4. AFEGIR UN DOCUMENT XML A UNA SUBCOL·LECCIÓ");
        System.out.println("5. ELIMINAR UN DOCUMENT XML D'UNA SUBCOL·LECCIÓ");
        System.out.println("0. Sortir\n");
        System.out.print("Escull una opció: ");
    }

    public static int llegirOpcio() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void testConnection() {
        Collection spotydam = null;
        Collection data = null;

        try {
            spotydam = ExistDbManager.connect();

            if (spotydam == null) {
                System.out.println("No s'ha pogut obrir SPOTYDAM.");
                return;
            }

            data = spotydam.getChildCollection("DATA");

            if (data == null) {
                System.out.println("No s'ha pogut obrir DATA.");
                return;
            }

            System.out.println("Connexió OK a DATA");
            System.out.println("Nom: " + data.getName());

            String[] resources = data.listResources();

            System.out.println("Documents dins de DATA:");
            if (resources == null || resources.length == 0) {
                System.out.println("No hi ha documents.");
            } else {
                for (String resource : resources) {
                    System.out.println("- " + resource);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (data != null) data.close();
                if (spotydam != null) spotydam.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Crea una nova subcol·lecció dins de /db/SPOTYDAM
    public static void new_subcollection(String name) {
        Collection spotydam = null;

        try {
            spotydam = ExistDbManager.connect();

            if (spotydam == null) {
                System.out.println("Error: no s'ha pogut connectar a SPOTYDAM.");
                return;
            }

            CollectionManagementService cms = (CollectionManagementService)
                    spotydam.getService("CollectionManagementService", "1.0");

            cms.createCollection(name);
            System.out.println("Subcol·lecció '" + name + "' creada correctament.");

        } catch (Exception e) {
            System.out.println("Error en crear la subcol·lecció: " + e.getMessage());
        } finally {
            try {
                if (spotydam != null) spotydam.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Elimina una subcol·lecció de /db/SPOTYDAM
    public static void drop_subcollection(String name) {
        Collection spotydam = null;

        try {
            spotydam = ExistDbManager.connect();

            if (spotydam == null) {
                System.out.println("Error: no s'ha pogut connectar a SPOTYDAM.");
                return;
            }

            // Comprovem si existeix abans d'eliminar
            Collection target = spotydam.getChildCollection(name);
            if (target == null) {
                System.out.println("Error: la subcol·lecció '" + name + "' no existeix.");
                return;
            }
            target.close();

            CollectionManagementService cms = (CollectionManagementService)
                    spotydam.getService("CollectionManagementService", "1.0");

            cms.removeCollection(name);
            System.out.println("Subcol·lecció '" + name + "' eliminada correctament.");

        } catch (Exception e) {
            System.out.println("Error en eliminar la subcol·lecció: " + e.getMessage());
        } finally {
            try {
                if (spotydam != null) spotydam.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Afegeix un document XML a una subcol·lecció de /db/SPOTYDAM
    public static void add_document(String subcollectionName, String xmlFilePath) {
        Collection spotydam = null;
        Collection subcollection = null;

        try {
            spotydam = ExistDbManager.connect();

            if (spotydam == null) {
                System.out.println("Error: no s'ha pogut connectar a SPOTYDAM.");
                return;
            }

            subcollection = spotydam.getChildCollection(subcollectionName);
            if (subcollection == null) {
                System.out.println("Error: la subcol·lecció '" + subcollectionName + "' no existeix.");
                return;
            }

            String xmlContent;
            try {
                xmlContent = new String(Files.readAllBytes(Paths.get(xmlFilePath)));
            } catch (IOException e) {
                System.out.println("Error: no s'ha pogut llegir el fitxer '" + xmlFilePath + "'.");
                return;
            }

            // Nom del fitxer sense la ruta completa
            String documentName = Paths.get(xmlFilePath).getFileName().toString();

            XMLResource resource = (XMLResource) subcollection.createResource(documentName, "XMLResource");
            resource.setContent(xmlContent);
            subcollection.storeResource(resource);

            System.out.println("Document '" + documentName + "' afegit correctament a '" + subcollectionName + "'.");

        } catch (Exception e) {
            System.out.println("Error en afegir el document: " + e.getMessage());
        } finally {
            try {
                if (subcollection != null) subcollection.close();
                if (spotydam != null) spotydam.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Elimina un document XML d'una subcol·lecció de /db/SPOTYDAM
    public static void drop_document(String subcollectionName, String fileName) {
        Collection spotydam = null;
        Collection subcollection = null;

        try {
            spotydam = ExistDbManager.connect();

            if (spotydam == null) {
                System.out.println("Error: no s'ha pogut connectar a SPOTYDAM.");
                return;
            }

            subcollection = spotydam.getChildCollection(subcollectionName);
            if (subcollection == null) {
                System.out.println("Error: la subcol·lecció '" + subcollectionName + "' no existeix.");
                return;
            }

            Resource resource = subcollection.getResource(fileName);
            if (resource == null) {
                System.out.println("Error: el document '" + fileName + "' no existeix a '" + subcollectionName + "'.");
                return;
            }

            subcollection.removeResource(resource);
            System.out.println("Document '" + fileName + "' eliminat correctament de '" + subcollectionName + "'.");

        } catch (Exception e) {
            System.out.println("Error en eliminar el document: " + e.getMessage());
        } finally {
            try {
                if (subcollection != null) subcollection.close();
                if (spotydam != null) spotydam.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
