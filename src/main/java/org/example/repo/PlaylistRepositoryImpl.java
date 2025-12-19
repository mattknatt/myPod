package org.example.repo;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import org.example.PersistenceManager;
import org.example.entity.Playlist;
import org.example.entity.Song;

import java.util.List;
import java.util.Set;

public class PlaylistRepositoryImpl implements PlaylistRepository {

    private final EntityManagerFactory emf = PersistenceManager.getEntityManagerFactory();

    @Override
    public List<Playlist> findAll(){
        return emf.callInTransaction(em ->
        em.createQuery("select pl from Playlist pl", Playlist.class)
        .getResultList());
}

    @Override
    public Set<Song> findSongsInPlaylist(Playlist playlist) {
        return playlist.getSongs();
    }

    @Override
    public void createPlaylist(String name) {
        emf.runInTransaction(em -> em.persist(new Playlist(name)));
    }

    @Override
    public void deletePlaylist(Playlist playlist) {
        emf.runInTransaction(em -> em.remove(playlist));
    }

    @Override
    public void addSong(Playlist playlist, Song song) {
        playlist.addSong(song);
        emf.runInTransaction(em -> em.persist(playlist));
    }

    @Override
    public void removeSong(Playlist playlist, Song song) {
        playlist.removeSong(song);
        emf.runInTransaction(em -> em.remove(playlist));
    }

    @Override
    public void renamePlaylist(Playlist playlist, String newName) {
        playlist.setName(newName);
        emf.runInTransaction(em -> em.merge(playlist));
    }
}
