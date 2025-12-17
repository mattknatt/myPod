package org.example;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import org.example.entity.Album;
import org.example.entity.Artist;
import org.example.entity.Song;

import java.util.List;

public class SongRepositoryImpl implements SongRepository {

   private final EntityManagerFactory emf = PersistenceManager.getEntityManagerFactory();


    @Override
    public List<Song> findSongByArtist() {
        return List.of();
    }

    @Override
    public Long count() {
        return emf.createEntityManager()
            .createQuery("select count(s) from Song s", Long.class)
            .getSingleResult();
    }

    @Override
    public boolean existsByUniqueId(Song song) {
        return emf.createEntityManager()
            .createQuery("select count(s) from Song s where s.songId = songId", Long.class)
            .getSingleResult() > 0;
    }

    @Override
    public boolean existsByUniqueId(Album album) {
        return emf.createEntityManager()
            .createQuery("select count(a) from Album a where a.albumId = albumId", Long.class)
            .getSingleResult() > 0;
    }

    @Override
    public boolean existsByUniqueId(Artist artist) {
        return emf.createEntityManager()
            .createQuery("select count(a) from Artist a where a.artistId = artistId", Long.class)
            .getSingleResult() > 0;
    }


    @Override
    public void save(Song song) {
        if(!existsByUniqueId(song))
            emf.runInTransaction(em -> em.persist(song));

    }

    @Override
    public void save(Album album) {
        if(!existsByUniqueId(album))
            emf.runInTransaction(em -> em.persist(album));
    }

    @Override
    public void save(Artist artist) {
        if(!existsByUniqueId(artist))
            emf.runInTransaction(em -> em.persist(artist));
    }
}

