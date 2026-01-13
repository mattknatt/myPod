package org.example;

import org.example.entity.Artist;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ArtistRepoTest extends RepoTest {

    @Test
    void existsByUniqueId_shouldFindSpecificArtistIfPresent() {
        // Given, When
        boolean artistExists = artistRepo.existsByUniqueId(testArtist1);

        // Then
        assertThat(artistExists).isTrue();
    }

    @Test
    void save_shouldSaveNewArtist() {
        // Given
        Artist testArtist3 = new Artist(12L,"Test Gärdestad", "Sweden");

        // When
        artistRepo.save(testArtist3);
        List<Artist> artists = artistRepo.findAll();

        // Then
        assertThat(artists).contains(testArtist3);
    }

    @Test
    void findAll_shouldFindArtists() {
        // Given, When
        List<Artist> artists = artistRepo.findAll();

        // Then
        assertThat(artists).contains(testArtist1, testArtist2);
    }

    @Test
    void count_shouldReturnNumberOfArtists() {
        // Given, When
        Long count = artistRepo.count();

        // Then
        assertThat(count).isEqualTo(2L);
    }
}
