package org.example.entity;

import jakarta.persistence.*;
import org.example.ItunesDTO;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA entity representing a musical artist.
 *
 * <p>An {@code Artist} is the top-level domain object in the music model and
 * owns one or more {@link Album} entities. Artists are typically created
 * from external data sources (e.g. iTunes API) and persisted using JPA.</p>
 *
 * <p>Entity identity is based solely on the database identifier.</p>
 */
@Entity
public class Artist implements DBObject {

    @Id
    @Column(name = "artist_id")
    private Long id;

    private String name;

    private String country;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Album> album = new ArrayList<>();

    protected Artist() {
    }

    public Artist(String name, String country) {
        this.name = name;
        this.country = country;
    }

    public Artist(Long artistId, String name, String country) {
        this.id = artistId;
        this.name = name;
        this.country = country;
    }

    /**
     * Factory method for creating an {@code Artist} from an iTunes DTO.
     *
     * @param dto source DTO
     * @return new {@code Artist} instance
     * @throws IllegalArgumentException if required DTO fields are missing
     */
    public static Artist fromDTO(ItunesDTO dto) {
        if (dto.artistId() == null || dto.artistName() == null) {
            throw new IllegalArgumentException("Required fields (artistId, artistName) cannot be null");
        }
        return new Artist(dto.artistId(), dto.artistName(), dto.country());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long artistId) {
        this.id = artistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<Album> getAlbum() {
        return album;
    }

    public void setAlbum(List<Album> album) {
        this.album = album;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Artist artist = (Artist) o;
        return getId() != null && Objects.equals(getId(), artist.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
