package com.turniermanagement.db;

/**
 * Factory-Klasse zum Erstellen von DAO-Instanzen.
 * Zentraler Punkt für die Erzeugung von DAOs, der es ermöglicht,
 * die Implementation auszutauschen, ohne den Client-Code zu ändern.
 */
public abstract class DAOFactory {
    
    // Singleton-Instanz
    private static DAOFactory instance;
    
    // Protected Konstruktor für Singleton-Pattern und Vererbung
    protected DAOFactory() {
    }
    
    /**
     * Gibt die Singleton-Instanz der DAOFactory zurück.
     * @return DAOFactory-Instanz
     */
    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new SQLiteDAOFactory();
        }
        return instance;
    }
    
    /**
     * Setzt die Singleton-Instanz der DAOFactory.
     * Wird hauptsächlich für Tests verwendet, um eine Testfactory zu injizieren.
     * @param factory Die zu verwendende DAOFactory-Instanz
     */
    public static void setInstance(DAOFactory factory) {
        instance = factory;
    }
    
    /**
     * Erstellt eine PlayerDAO-Instanz.
     * @return PlayerDAO-Implementierung
     */
    public abstract PlayerDAO createPlayerDAO();
    
    /**
     * Erstellt eine TournamentDAO-Instanz.
     * @return TournamentDAO-Implementierung
     */
    public abstract TournamentDAO createTournamentDAO();
    
    /**
     * Erstellt eine RoundDAO-Instanz.
     * @return RoundDAO-Implementierung
     */
    public abstract RoundDAO createRoundDAO();
    
    /**
     * Erstellt eine MatchDAO-Instanz.
     * @return MatchDAO-Implementierung
     */
    public abstract MatchDAO createMatchDAO();
}