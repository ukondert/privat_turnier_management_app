package com.turniermanagement.db;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * Hauptklasse zum Aufrufen des DatabaseCleaners.
 * Diese Klasse bietet eine einfache Benutzeroberfläche zur Verwendung des DatabaseCleaners.
 */
public class DatabaseCleanerMain {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DatabaseCleaner cleaner = new DatabaseCleaner();

    public static void main(String[] args) {
        System.out.println("=== Datenbank-Bereinigungstool ===");
        
        boolean running = true;
        while (running) {
            printMenu();
            int choice = getChoice();
            
            try {
                switch (choice) {
                    case 1 -> deletePlayer();
                    case 2 -> deleteTournament();
                    case 3 -> deleteRound();
                    case 4 -> deleteMatch();
                    case 5 -> clearTable();
                    case 6 -> deleteAllData();
                    case 0 -> running = false;
                    default -> System.out.println("Ungültige Auswahl. Bitte versuchen Sie es erneut.");
                }
            } catch (SQLException e) {
                System.err.println("Datenbankfehler: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Fehler: " + e.getMessage());
                e.printStackTrace();
            }
            
            if (running) {
                System.out.println("\nDrücken Sie Enter, um fortzufahren...");
                scanner.nextLine();
            }
        }
        
        System.out.println("Programm beendet.");
        scanner.close();
    }
    
    private static void printMenu() {
        System.out.println("\nWählen Sie eine Option:");
        System.out.println("1. Spieler löschen");
        System.out.println("2. Turnier löschen");
        System.out.println("3. Runde löschen");
        System.out.println("4. Match löschen");
        System.out.println("5. Tabelle leeren");
        System.out.println("6. Alle Daten löschen");
        System.out.println("0. Beenden");
        System.out.print("Ihre Wahl: ");
    }
    
    private static int getChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static void deletePlayer() throws SQLException {
        System.out.print("Geben Sie die ID des zu löschenden Spielers ein: ");
        long playerId = Long.parseLong(scanner.nextLine().trim());
        
        System.out.println("WARNUNG: Dies wird auch alle Matches und Turnier-Beziehungen des Spielers löschen!");
        System.out.print("Sind Sie sicher? (j/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("j")) {
            // Zuerst die abhängigen Daten löschen
            int matchesDeleted = cleaner.deleteMatchesForPlayer(playerId);
            int relationshipsDeleted = cleaner.deletePlayerFromAllTournaments(playerId);
            
            // Dann den Spieler selbst löschen
            boolean success = cleaner.deletePlayer(playerId);
            
            if (success) {
                System.out.println("Spieler erfolgreich gelöscht.");
                System.out.println("Gelöschte Matches: " + matchesDeleted);
                System.out.println("Gelöschte Turnier-Beziehungen: " + relationshipsDeleted);
            } else {
                System.out.println("Spieler konnte nicht gefunden werden oder ein Fehler ist aufgetreten.");
            }
        } else {
            System.out.println("Löschvorgang abgebrochen.");
        }
    }
    
    private static void deleteTournament() throws SQLException {
        System.out.print("Geben Sie die ID des zu löschenden Turniers ein: ");
        long tournamentId = Long.parseLong(scanner.nextLine().trim());
        
        System.out.println("WARNUNG: Dies wird auch alle Runden, Matches und Spieler-Beziehungen des Turniers löschen!");
        System.out.print("Sind Sie sicher? (j/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("j")) {
            boolean success = cleaner.deleteTournament(tournamentId);
            
            if (success) {
                System.out.println("Turnier und alle zugehörigen Daten erfolgreich gelöscht.");
            } else {
                System.out.println("Turnier konnte nicht gefunden werden oder ein Fehler ist aufgetreten.");
            }
        } else {
            System.out.println("Löschvorgang abgebrochen.");
        }
    }
    
    private static void deleteRound() throws SQLException {
        System.out.print("Geben Sie die ID der zu löschenden Runde ein: ");
        long roundId = Long.parseLong(scanner.nextLine().trim());
        
        System.out.println("WARNUNG: Dies wird auch alle Matches dieser Runde löschen!");
        System.out.print("Sind Sie sicher? (j/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("j")) {
            boolean success = cleaner.deleteRound(roundId);
            
            if (success) {
                System.out.println("Runde und alle zugehörigen Matches erfolgreich gelöscht.");
            } else {
                System.out.println("Runde konnte nicht gefunden werden oder ein Fehler ist aufgetreten.");
            }
        } else {
            System.out.println("Löschvorgang abgebrochen.");
        }
    }
    
    private static void deleteMatch() throws SQLException {
        System.out.print("Geben Sie die ID des zu löschenden Matches ein: ");
        long matchId = Long.parseLong(scanner.nextLine().trim());
        
        System.out.print("Sind Sie sicher? (j/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("j")) {
            boolean success = cleaner.deleteMatch(matchId);
            
            if (success) {
                System.out.println("Match erfolgreich gelöscht.");
            } else {
                System.out.println("Match konnte nicht gefunden werden oder ein Fehler ist aufgetreten.");
            }
        } else {
            System.out.println("Löschvorgang abgebrochen.");
        }
    }
    
    private static void clearTable() throws SQLException {
        System.out.println("Verfügbare Tabellen:");
        System.out.println("- player");
        System.out.println("- tournament");
        System.out.println("- round");
        System.out.println("- match");
        System.out.println("- tournament_player");
        
        System.out.print("Geben Sie den Namen der zu leerenden Tabelle ein: ");
        String tableName = scanner.nextLine().trim();
        
        System.out.println("WARNUNG: Dies wird ALLE Daten aus der Tabelle '" + tableName + "' löschen!");
        System.out.print("Sind Sie sicher? (j/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("j")) {
            try {
                boolean success = cleaner.clearTable(tableName);
                
                if (success) {
                    System.out.println("Tabelle '" + tableName + "' erfolgreich geleert.");
                } else {
                    System.out.println("Fehler beim Leeren der Tabelle.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Fehler: " + e.getMessage());
            }
        } else {
            System.out.println("Löschvorgang abgebrochen.");
        }
    }
    
    private static void deleteAllData() throws SQLException {
        System.out.println("WARNUNG: Dies wird ALLE Daten aus ALLEN Tabellen löschen!");
        System.out.println("Dieser Vorgang kann nicht rückgängig gemacht werden!");
        System.out.print("Sind Sie WIRKLICH sicher? (ja/nein): ");
        
        if (scanner.nextLine().trim().equalsIgnoreCase("ja")) {
            System.out.print("Zur Bestätigung, geben Sie 'LÖSCHEN' ein: ");
            
            if (scanner.nextLine().trim().equals("LÖSCHEN")) {
                boolean success = cleaner.deleteAllData();
                
                if (success) {
                    System.out.println("Alle Daten wurden erfolgreich gelöscht.");
                } else {
                    System.out.println("Fehler beim Löschen aller Daten.");
                }
            } else {
                System.out.println("Löschvorgang abgebrochen.");
            }
        } else {
            System.out.println("Löschvorgang abgebrochen.");
        }
    }
}