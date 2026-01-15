package org.example.repo;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.Album;
import org.example.entity.Artist;
import org.example.entity.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA-based implementation of {@link SongRepository}.
 *
 * <p>
 * Provides persistence and query operations for {@link Song} entities,
 * including lookups by artist and album.
 * </p>
 *
 * <p>
 * Certain queries eagerly fetch related album and artist entities to
 * avoid lazy loading issues in the presentation layer.
 * </p>
 */
public class SongRepositoryImpl implements SongRepository {
    private static final Logger logger = LoggerFactory.getLogger(SongRepositoryImpl.class);
    private final EntityManagerFactory emf;

    /**
     * Creates a new {@code SongRepositoryImpl}.
     *
     * @param emf the {@link EntityManagerFactory} used to create entity managers
     */
    public SongRepositoryImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Returns the total number of songs stored in the database.
     *
     * @return the song count
     */
    @Override
    public Long count() {
        try (var em = emf.createEntityManager()) {
            return em.createQuery("select count(s) from Song s", Long.class)
                .getSingleResult();
        }
    }

    /**
     * Checks whether a song with the same unique identifier already exists.
     *
     * @param song the song whose identifier should be checked
     * @return {@code true} if a song with the given ID exists, otherwise {@code false}
     */
    @Override
    public boolean existsByUniqueId(Song song) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery("select count(s) from Song s where s.id = :songId", Long.class)
                .setParameter("songId", song.getId())
                .getSingleResult() > 0;
        }
    }

    /**
     * Persists a new song.
     *
     * @param song the song to persist
     */
    @Override
    public void save(Song song) {
        emf.runInTransaction(em -> em.persist(song));
    }

    /**
     * Retrieves all songs.
     *
     * @return a list of all songs
     */
    @Override
    public List<Song> findAll() {
        return emf.callInTransaction(em ->
            em.createQuery("select s from Song s", Song.class)
                .getResultList());
    }

    /**
     * Retrieves all songs by the given artist.
     *
     * <p>
     * Album and artist associations are eagerly fetched.
     * </p>
     *
     * @param artist the artist whose songs should be retrieved
     * @return a list of songs, or an empty list if {@code artist} is {@code null}
     */
    @Override
    public List<Song> findByArtist(Artist artist) {
        if (artist == null) {
            logger.debug("findByArtist: artist is null");
            return new ArrayList<>();
        }

        return emf.callInTransaction(em ->
            em.createQuery(
                    """
                        select s
                        from Song s
                        join fetch s.album a
                        join fetch a.artist art
                        where art = :artist
                        """,
                    Song.class
                )
                .setParameter("artist", artist)
                .getResultList());
    }

    /**
     * Retrieves all songs from the given album.
     *
     * @param album the album whose songs should be retrieved
     * @return a list of songs, or an empty list if {@code album} is {@code null}
     */
    @Override
    public List<Song> findByAlbum(Album album) {
        if (album == null) {
            logger.debug("findByAlbum: album is null");
            return new ArrayList<>();
        }

        return emf.callInTransaction(em ->
            em.createQuery(
                    """
                        select s
                        from Song s
                        join fetch s.album a
                        join fetch a.artist art
                        where a = :album
                        """,
                    Song.class
                )
                .setParameter("album", album)
                .getResultList());
    }
}
