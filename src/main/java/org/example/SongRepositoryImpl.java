package org.example;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.Song;

import java.util.List;

public class SongRepositoryImpl implements SongRepository {

   private final EntityManagerFactory emf = PersistenceManager.getEntityManagerFactory();


    @Override
    public List<Song> findSongByArtist() {
        return List.of();
    }
}

