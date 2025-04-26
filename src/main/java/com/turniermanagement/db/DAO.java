package com.turniermanagement.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generisches DAO-Interface, das die grundlegenden CRUD-Operationen definiert.
 * @param <T> Der Entitätstyp
 * @param <ID> Der Typ des Primärschlüssels
 */
public interface DAO<T, ID> {
    
    /**
     * Speichert eine neue Entität in der Datenbank.
     * @param entity Die zu speichernde Entität
     * @throws SQLException Bei Datenbankfehlern
     */
    void save(T entity) throws SQLException;
    
    /**
     * Aktualisiert eine bestehende Entität in der Datenbank.
     * @param entity Die zu aktualisierende Entität
     * @throws SQLException Bei Datenbankfehlern
     */
    void update(T entity) throws SQLException;
    
    /**
     * Findet eine Entität anhand ihrer ID.
     * @param id Die ID der zu findenden Entität
     * @return Optional mit der gefundenen Entität oder leer, wenn keine gefunden wurde
     * @throws SQLException Bei Datenbankfehlern
     */
    Optional<T> findById(ID id) throws SQLException;
    
    /**
     * Findet alle Entitäten.
     * @return Liste aller Entitäten
     * @throws SQLException Bei Datenbankfehlern
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Löscht eine Entität anhand ihrer ID.
     * @param id Die ID der zu löschenden Entität
     * @throws SQLException Bei Datenbankfehlern
     */
    void delete(ID id) throws SQLException;
}