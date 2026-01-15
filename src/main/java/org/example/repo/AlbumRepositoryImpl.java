package org.example.repo;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.Album;
import org.example.entity.Artist;

import java.util.List;

/**
 * JPA-based implementation of {@link AlbumRepository}.
 *
 * <p>
 * Provides persistence operations for {@link Album} entities, including
 * existence checks, basic CRUD functionality, and simple query methods.
 * </p>
 *
 * <p>
 * All operations are executed using an {@link EntityManagerFactory}, with
 * transactions managed internally where required.
 * </p>
 */
public class AlbumRepositoryImpl implements AlbumRepository {
    private final EntityManagerFactory emf;

    /**
     * Creates a new {@code AlbumRepositoryImpl}.
     *
     * @param emf the {@link EntityManagerFactory} used to create entity managers
     */
    public AlbumRepositoryImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Checks whether an album with the same unique identifier already exists.
     *
     * @param album the album whose identifier should be checked
     * @return {@code true} if an album with the given ID exists, otherwise {@code false}
     */
    @Override
    public boolean existsByUniqueId(Album album) {
        return emf.callInTransaction(em ->
            em.createQuery("select count(a) from Album a where a.id = :albumId", Long.class)
                .setParameter("albumId", album.getId())
                .getSingleResult() > 0
        );
    }

    /**
     * Returns the total number of albums stored in the database.
     *
     * @return the album count
     */
    @Override
    public Long count() {
        return emf.callInTransaction(em ->
            em.createQuery("select count(a) from Album a", Long.class)
                .getSingleResult());
    }

    /**
     * Persists a new album.
     *
     * @param album the album to persist
     */
    @Override
    public void save(Album album) {
        emf.runInTransaction(em -> em.persist(album));
    }

    /**
     * Retrieves all albums.
     *
     * @return a list of all albums
     */
    @Override
    public List<Album> findAll() {
        return emf.callInTransaction(em ->
            em.createQuery("select a from Album a", Album.class)
                .getResultList());
    }

    /**
     * Retrieves all albums by the given artist.
     *
     * @param artist the artist whose albums should be retrieved
     * @return a list of albums associated with the given artist
     */
    @Override
    public List<Album> findByArtist(Artist artist) {
        return emf.callInTransaction(em ->
            em.createQuery("select a from Album a where a.artist = :artist", Album.class)
                .setParameter("artist", artist)
                .getResultList()
        );
    }
}
