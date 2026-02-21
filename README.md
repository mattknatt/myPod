# myPod — Music Player with iTunes Integration

A sleek, retro-inspired music player built with **JavaFX** and **JPA/Hibernate**.
Developed as a group assignment at **IT-Högskolan**, focusing on modern Java development, relational persistence, and external API integration.

![Java](https://img.shields.io/badge/Java-25-orange)
![Hibernate](https://img.shields.io/badge/Hibernate-7.2-blue)
![JavaFX](https://img.shields.io/badge/JavaFX-25-green)
![MySQL](https://img.shields.io/badge/MySQL-9.5-blue)

---

## Overview

**myPod** is a desktop application that recreates the classic iPod experience.
It features a custom **Click Wheel** navigation interface for browsing artists, albums, and songs retrieved from the **iTunes Search API**.

Fetched data is stored locally, allowing users to build and manage their own music library and playlists.

---

## Tech Stack

**Core**
- Java 25
- JavaFX

**Persistence & Data**
- JPA & Hibernate
- MySQL
- Jackson

**Tooling & Quality**
- Maven
- JUnit 5 & AssertJ
- Log4j2

---

## Key Features

**iTunes Search Integration**
Fetches real-world music data and stores it locally.

**Click Wheel Navigation**
Custom JavaFX interface inspired by the iconic circular control.

**Library Management**
Persistent storage of artists, albums, and tracks using JPA.

**Playlist Support**
Create and manage **Library** and **Favorites** playlists.

**Audio Previews**
Built-in media player for listening to song snippets.

**Database Persistence**
Repository-based architecture for clean and reliable data access.

---

## My Contributions

While this was a collaborative project, my primary responsibilities included:

- **iTunes API Integration**
  Developed `ItunesApiClient` to handle HTTP requests, JSON parsing, and data normalization.

- **Data Persistence (JPA)**
  Designed the core entity model (`Artist`, `Album`, `Song`, `Playlist`) and their relational mappings.

- **Repository Layer**
  Implemented repositories (`ArtistRepository`, `SongRepository`, etc.) to abstract database operations.

- **Database Initialization**
  Built `DatabaseInitializer` to populate the database with API data on first launch.

---

*Developed as a collaborative project at IT-Högskolan.*
