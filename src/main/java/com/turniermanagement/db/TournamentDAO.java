package com.turniermanagement.db;

import com.turniermanagement.model.Tournament;
import java.sql.SQLException;

/**
 * Interface für den Datenbankzugriff auf Tournament-Objekte.
 */
public interface TournamentDAO extends DAO<Tournament, Long> {
    // Spezifische Methoden für Tournament, falls benötigt
}