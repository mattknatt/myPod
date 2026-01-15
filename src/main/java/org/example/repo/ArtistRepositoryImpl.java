package org.example.repo;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.Artist;

import java.util.List;

/**
 * JPA-based implementation of {@link ArtistRepository}.
 *
 * <p>
 * Handles persistence and retrieval of {@link Artist} entities, providing
 * basic CRUD operations and simple aggregate queries.
 * </p>
 */
public class ArtistRepositoryImpl implements ArtistRepository {
    private final EntityManagerFactory emf;

    /**
     * Creates a new {@code ArtistRepositoryImpl}.
     *
     * @param emf the {@link EntityManagerFactory} used to create entity managers
     */
    public ArtistRepositoryImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Checks whether an artist with the same unique identifier already exists.
     *
     * @param artist the artist whose identifier should be checked
     * @return {@code true} if an artist with the given ID exists, otherwise {@code false}
     */
    @Override
    public boolean existsByUniqueId(Artist artist) {
        return emf.callInTransaction(em ->
            em.createQuery("select count(a) from Artist a where a.id = :artistId", Long.class)
                .setParameter("artistId", artist.getId())
                .getSingleResult() > 0
        );
    }

    /**
     * Returns the total number of artists stored in the database.
     *
     * @return the artist count
     */
    @Override
    public Long count() {
        return emf.callInTransaction(em ->
            em.createQuery("select count(a) from Artist a", Long.class)
                .getSingleResult());
    }

    /**
     * Persists a new artist.
     *
     * @param artist the artist to persist
     */
    @Override
    public void save(Artist artist) {
        emf.runInTransaction(em -> em.persist(artist));
    }

    /**
     * Retrieves all artists.
     *
     * @return a list of all artists
     */
    @Override
    public List<Artist> findAll() {
        return emf.callInTransaction(em ->
            em.createQuery("select a from Artist a", Artist.class)
                .getResultList());
    }
}
