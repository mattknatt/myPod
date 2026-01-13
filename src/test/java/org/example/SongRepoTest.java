package org.example;

import org.example.entity.Artist;
import org.example.entity.Song;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SongRepoTest extends RepoTest {

    @Test
    void count_shouldReturnNumberOfSongs() {
        // Given, When
        Long count = songRepo.count();

        // Then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    void existsByUniqueId_shouldFindSpecificSongIfPresent() {
        // Given, When
        boolean songExists = songRepo.existsByUniqueId(testSong1);

        // Then
        assertThat(songExists).isTrue();
    }

    @Test
    void saveSong_shouldSaveSong() {
        // Given
        Song testSong = new Song(12L, "Tester of Muppets", 666L, "http", testAlbum1);

        // When
        songRepo.save(testSong);
        List<Song> testSongs = songRepo.findAll();

        // Then
        assertThat(testSongs).contains(testSong);
    }

    @Test
    void findAll_shouldFindAllSongs() {
        // Given, When
        List<Song> testSongs = songRepo.findAll();

        // Then
        assertThat(testSongs).contains(testSong1, testSong2, testSong3, testSong4, testSong5);
    }

    @Test
    void findByArtist_shouldFindSongBySpecificArtist() {
        // Given, When
        List<Song> testSongs = songRepo.findByArtist(testArtist1);

        // Then
        assertThat(testSongs).contains(testSong1, testSong2, testSong3);
    }

    @Test
    void findByArtist_shouldReturnEmptyListWhenArtistHasNoSongs() {
        // Given
        Artist artistWithNoSongs = new Artist(99L, "Jacub", "Denmark");
        artistRepo.save(artistWithNoSongs);

        // When
        List<Song> songs = songRepo.findByArtist(artistWithNoSongs);

        // Then
        assertThat(songs).isEmpty();
    }
}
