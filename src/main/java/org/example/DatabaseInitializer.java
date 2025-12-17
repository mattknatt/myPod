package org.example;

import org.example.entity.Album;
import org.example.entity.Artist;
import org.example.entity.Song;

import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {

    private final ItunesApiClient apiClient;

    private final SongRepository songRepo;

    public DatabaseInitializer(ItunesApiClient apiClient, SongRepository songRepo) {
        this.apiClient = apiClient;
        this.songRepo = songRepo;
    }

    public void init() {
        if (songRepo.count() > 0) { //check if there is data already
            return;
        }

        List<String> searches = List.of("the+war+on+drugs",
                                        "refused",
                                        "thrice",
                                        "16+horsepower",
                                        "viagra+boys",
                                        "geese",
                                        "ghost",
                                        "run+the+jewels",
                                        "rammstein",
                                        "salvatore+ganacci",
                                        "baroness"
        );
        for (String term : searches) {
            try {
                apiClient.searchSongs(term).forEach(dto -> {
                    Song s = Song.fromDTO(dto);
                    Album al = Album.fromDTO(dto);
                    Artist ar = Artist.fromDTO(dto);
//                    s.setTitle(dto.trackName());
//                    s.setSongId((long) dto.trackId());
//                    s.setLength((long) dto.trackTimeMillis());
                    songRepo.save(s);
                    songRepo.save(al);
                    songRepo.save(ar);

                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


}

