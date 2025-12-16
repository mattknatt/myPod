package org.example;

import org.example.entity.Song;

import java.util.List;

public interface SongRepository {
    List<Song> findSongByArtist();
}
