package org.example;

import org.example.entity.Album;
import org.example.entity.Artist;
import org.example.entity.Playlist;
import org.example.entity.Song;
import org.example.repo.AlbumRepositoryImpl;
import org.example.repo.ArtistRepositoryImpl;
import org.example.repo.PlaylistRepositoryImpl;
import org.example.repo.SongRepositoryImpl;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * Beskriv var ifrån variablerna kommer ifrån (testSong1 etc)
 */
class PlaylistRepoTest extends RepoTest {

    @Test
    void createPlaylist_shouldPersistAndBeFindable() {
        // Given, when
        Playlist playlist = playlistRepo.createPlaylist("playlist");

        // Then
        assertThat(playlist.getPlaylistId()).isNotNull();
        assertThat(playlistRepo.existsByUniqueId(playlist.getPlaylistId())).isTrue();
    }

    @Test
    void existsByUniqueId_shouldFindSpecificPlaylistIfPresent() {
        // Given
        Playlist playlist = playlistRepo.createPlaylist("playlist");

        // When
        boolean playlistExists = playlistRepo.existsByUniqueId(playlist.getPlaylistId());

        // Then
        assertThat(playlistExists).isTrue();
    }

    @Test
    void addSongToPlaylist_shouldPersistRelation() {
        // Given
        Playlist playlist = playlistRepo.createPlaylist("Playlist");

        // When
        playlistRepo.addSong(playlist, testSong1);

        // Then
        Playlist reloaded = playlistRepo.findById(playlist.getPlaylistId());

        assertThat(reloaded.getSongs())
            .hasSize(1)
            .contains(testSong1);
    }

    @Test
    void addSongsToPlaylist_shouldPersistRelation() {
        // Given
        Playlist playlist = playlistRepo.createPlaylist("Playlist");
        List<Song> testSongs = new ArrayList<>();
        testSongs.add(testSong1);
        testSongs.add(testSong2);
        testSongs.add(testSong3);

        // When
        playlistRepo.addSongs(playlist, testSongs);

        // Then
        Playlist reloaded = playlistRepo.findById(playlist.getPlaylistId());

        assertThat(reloaded.getSongs()).hasSize(3);
        assertThat(reloaded.getSongs().contains(testSong1));
        assertThat(reloaded.getSongs().contains(testSong2));
        assertThat(reloaded.getSongs().contains(testSong3));
    }

    @Test
    void removeSong_shouldRemoveSongFromPlaylist() {
        // Given
        Playlist playlist = playlistRepo.createPlaylist("Playlist");
        playlistRepo.addSong(playlist, testSong1);

        // When
        playlistRepo.removeSong(playlist, testSong1);

        // Then
        Playlist reloaded = playlistRepo.findById(playlist.getPlaylistId());

        assertThat(reloaded.getSongs()).hasSize(0);
        assertThat(reloaded.getSongs().contains(testSong1)).isFalse();
    }

    @Test
    void deletePlaylist_shouldDeletePlaylist() {
        // Given
        Playlist playlist = playlistRepo.createPlaylist("Playlist");

        // When
        playlistRepo.deletePlaylist(playlist);

        // Then
        assertThat(playlistRepo.findAll().isEmpty());
    }

    @Test
    void renamePlaylist_shouldSavePlaylistWithNewName() {
        // Given
        Playlist playlist = playlistRepo.createPlaylist("Playlist");

        // When
        playlistRepo.renamePlaylist(playlist, "NewPlaylist");

        // Then
        assertThat(playlist.getName().equals("NewPlaylist"));
    }

    @Test
    void isSongInPlaylist_shouldConfirmSongInPlaylist() {
        // Given
        Playlist playlist = playlistRepo.createPlaylist("Playlist");
        playlistRepo.addSong(playlist, testSong1);

        // When
        boolean songInPlaylist = playlistRepo.isSongInPlaylist(playlist, testSong1);

        // Then
        assertThat(songInPlaylist).isTrue();
    }
}
