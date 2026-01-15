package org.example.entity;

import jakarta.persistence.*;
import org.example.ItunesDTO;
import org.hibernate.proxy.HibernateProxy;

import javax.imageio.ImageIO;

import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA entity representing an album.
 *
 * <p>An {@code Album} is a persistent JPA entity that belongs to an
 * {@link Artist} and contains one or more {@link Song} entities.</p>
 *
 * <p></p>It also stores optional album artwork as a binary large object (BLOB),
 * which is converted to a JavaFX {@link Image} when displayed in the UI.</p>
 *
 * <p>Album instances are typically created from iTunes API data via
 * {@link #fromDTO(ItunesDTO, Artist)}.</p>
 */
@Entity
public class Album implements DBObject {

    @Id
    @Column(name = "album_id")
    private Long id;

    private String name;

    private String genre;

    @Column(name = "release_year")
    private int year;

    private Long trackCount;

    @Lob
    private byte[] cover;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Song> song = new ArrayList<>();

    protected Album() {
    }

    public Album(Long albumId, String name, String genre, int year, Long trackCount, byte[] cover, Artist artist) {
        this.id = albumId;
        this.name = name;
        this.genre = genre;
        this.year = year;
        this.trackCount = trackCount;
        this.artist = artist;
        this.cover = cover;
    }

    /**
     * Creates an {@code Album} entity from an iTunes API DTO.
     *
     * <p>This factory method extracts album-related data and attempts
     * to download and persist album artwork. If artwork cannot be loaded,
     * {@code null} is stored and a default image is used by the UI.</p>
     *
     * @param dto    source DTO from the iTunes API
     * @param artist associated artist entity
     * @return a new {@code Album} instance
     * @throws IllegalArgumentException if required DTO fields are missing
     */
    public static Album fromDTO(ItunesDTO dto, Artist artist) {
        if (dto.collectionId() == null || dto.collectionName() == null) {
            throw new IllegalArgumentException("Required fields (albumId, albumName) cannot be null");
        }

        // Try to load album cover from URL. If unavailable, store null and let UI handle fallback image.
        byte[] cover = generateAlbumCover(dto.artworkUrl100());

        return new Album(dto.collectionId(), dto.collectionName(), dto.primaryGenreName(), dto.releaseYear(), dto.trackCount(), cover, artist);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long albumId) {
        this.id = albumId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Long getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(Long trackCount) {
        this.trackCount = trackCount;
    }

    public List<Song> getSong() {
        return song;
    }

    public void setSong(List<Song> song) {
        this.song = song;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public byte[] getCover() {
        return cover;
    }

    /**
     * Returns the album cover as a JavaFX {@link Image}.
     *
     * <p>If no cover is stored or if decoding fails, a default placeholder
     * image bundled with the application is returned.</p>
     *
     * @return album cover image or a default image if unavailable
     */
    public Image getCoverImage() {
        byte[] bytes = getCover();
        if (bytes == null || bytes.length == 0) return loadDefaultImage();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            Image img = new Image(bais);
            return img.isError() ? loadDefaultImage() : img;
        } catch (IOException e) {
            return loadDefaultImage();
        }
    }

    public void setCover(byte[] cover) {
        this.cover = cover;
    }

    /**
     * Downloads album artwork from the given URL and converts it to a byte array.
     *
     * @param url URL pointing to the album artwork
     * @return image data as byte array, or {@code null} if loading fails
     */
    public static byte[] generateAlbumCover(URL url) {
        BufferedImage bi = loadUrlImage(url);

        if (bi != null) {
            return imageToBytes(bi);
        }
        return null;
    }

    /**
     * Converts a buffered image into a JPEG byte array suitable for BLOB storage.
     *
     * @param bi buffered image
     * @return image encoded as byte array, or {@code null} on failure
     */
    public static byte[] imageToBytes(BufferedImage bi) {
        if (bi == null) return null;

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(bi, "jpg", stream); //should always be jpg for this application
            return stream.toByteArray();
        } catch (IOException e) {
            System.err.println(e);
            return null;
        }
    }

    /**
     * Loads an image from a remote URL.
     *
     * @param url URL pointing to an image resource
     * @return loaded {@link BufferedImage} or {@code null} if unavailable
     */
    public static BufferedImage loadUrlImage(URL url) {
        if (url == null) return null;

        BufferedImage bi;
        try {
            bi = ImageIO.read(url);
        } catch (IOException e) {
            return null;
        }

        if (bi == null) {
            System.err.println("The URL does not point to a valid image.");
            return null;
        }

        return bi;
    }

    /**
     * Loads the default album artwork bundled with the application.
     *
     * @return default {@link Image}, or {@code null} if the resource cannot be loaded
     */
    public static Image loadDefaultImage() {
        try (InputStream is = Album.class.getResourceAsStream("/itunescover.jpg")) {
            if (is == null) {
                System.err.println("Could not load default image");
                return null;
            }
            return new Image(is);

        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Album album = (Album) o;
        return getId() != null && Objects.equals(getId(), album.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
