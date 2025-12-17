package org.example;

import jakarta.persistence.EntityManagerFactory;

public class App {
    public static void main(String[] args) {
        PersistenceManager pm;
        ItunesApiClient apiClient = new ItunesApiClient();
        SongRepository songRepo = new SongRepositoryImpl();

        try(EntityManagerFactory emf = PersistenceManager.getEntityManagerFactory()) {
            assert emf.isOpen();
            DatabaseInitializer initializer = new DatabaseInitializer(apiClient, songRepo);
            initializer.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
