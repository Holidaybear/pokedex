# Pokémon Illustrated Guide Android App — README

## Project Overview

The Pokémon Illustrated Guide is a simple Android app that allows users to explore a collection of various Pokémon, categorized by their types, view detailed information, and manage capture status. The app aims to deliver a seamless user experience with a responsive UI, efficient data handling, and support for duplicate captures of multi-type Pokémon (e.g., Charizard in Fire and Flying categories).

### Objectives
- Develop a fully functional Android app.
- Enable users to browse Pokémon by type, view details, and manage captures with category-specific tracking.
- Ensure a responsive, animated UI with efficient data loading and offline support.

## Specifications

### Functional Requirements
1. **Captured Pokémon Display (My Pocket List)**:
   - A horizontally scrollable list at the top of the main screen, displaying captured Pokémon.
   - Sorted by capture timestamp (descending, most recent first).
   - Supports duplicates (e.g., Charizard captured multiple times in Fire category will appear multiple times).
   - Each Pokémon has a fixed Pokéball icon in the top-right corner; clicking it releases the specific capture record, removing it from the list.

2. **Pokémon Collection Display (Type List)**:
   - A vertically scrollable list of type categories (e.g., Bug, Dragon, Fire).
   - Each category contains a horizontally scrollable list of Pokémon, grouped by type.
   - Categories are sorted by type name (ascending, e.g., Bug -> Dragon -> Fire).
   - Multi-type Pokémon appear in all relevant categories (e.g., Charizard in Fire and Flying).
   - Each Pokémon has a fixed Pokéball icon in the top-right corner; clicking it captures the Pokémon, adding a new record to My Pocket List. The same Pokémon can be captured multiple times within the same category (e.g., Charizard in Fire can be clicked repeatedly).
   - **Dynamic Loading**:
     - Categories only appear when at least one Pokémon in that category is fully processed (`isProcessed = true`).
     - The count next to each category name (e.g., "Fire (3)") updates in real-time as Pokémon data is processed, without animation.

3. **Data Handling**:
   - Use Room database to store Pokémon data locally.
   - Pre-populate the database with 151 Pokémon fetched from PokéAPI:
     - Basic list: `GET https://pokeapi.co/api/v2/pokemon?limit=151`
     - Details: `GET https://pokeapi.co/api/v2/pokemon/{id}` (image from `sprites.other.official-artwork.front_default`, types from `types`)
     - Species: `GET https://pokeapi.co/api/v2/pokemon-species/{id}` (description from `flavor_text_entries`, evolution from `evolves_from_species`)
   - Process Pokémon data incrementally using WorkManager, marking each as `isProcessed = true` once complete.
   - Support offline usage via Room cache.

4. **User Interaction**:
   - Tap a Pokémon to view a detail screen (name, image, types, description, evolution).
   - Capture Pokémon by clicking the Pokéball icon in Type List (unlimited captures per Pokémon per category).
   - Release Pokémon by clicking the Pokéball icon in My Pocket List (removes specific capture record).

### Non-Functional Requirements
- **Performance**:
  - Handle large datasets (151 Pokémon) with pagination and lazy loading.
  - Use Coil for image caching to optimize loading.
- **Usability**:
  - Responsive layout for various screen sizes.
  - Smooth animations for screen transitions (e.g., fade-in for detail screen).
- **Reliability**:
  - Offline support via Room cache.
  - Unit tests (JUnit) and instrumented tests (Espresso) for core functionality.
- **Code Quality**:
  - Written in Kotlin, following SOLID principles and Android best practices.

### Bonus Feature (Optional)
- Add a clickable "Evolves from" link in the detail screen to navigate to the pre-evolution Pokémon's detail view.

## Technical Architecture

The app follows an **MVVM (Model-View-ViewModel)** architecture for maintainability and testability, leveraging modern Android technologies. The Data Repository directly manages both Remote Data Source (PokéAPI via Retrofit) and Local Data Source (Room).

### Technology Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (declarative UI, animations)
- **Networking**: Retrofit (PokéAPI integration)
- **Storage**: Room (local database, offline support)
- **Image Loading**: Coil (efficient image caching)
- **Navigation**: Jetpack Navigation (screen transitions)
- **Async**: Coroutines + Flow (reactive updates)
- **Dependency Injection**: Hilt (scalable dependency management)
- **Background Tasks**: WorkManager (incremental data processing)
- **Testing**: JUnit (unit tests), Espresso (instrumented tests), MockK (mocking)

### Data Flow
- **Initial Data Fetch**:
  - Fetch 151 Pokémon using `GET https://pokeapi.co/api/v2/pokemon?limit=151`, store basic info (ID, name) in `Pokemon` table (`isProcessed = false`).
  - Use WorkManager to incrementally fetch details (image, types, species) for each Pokémon, updating `isProcessed = true` once complete.
- **Dynamic Loading**:
  - Use Room’s `Flow` to monitor `Pokemon` and `CaptureRecord` tables.
  - `TypeWithCount` is queried with `COUNT` to dynamically update category counts (e.g., "Fire (3)") as Pokémon are processed.
  - My Pocket List only shows Pokémon with `isProcessed = true`.

## UI Design

### Main Screen
- **Structure**:
  ```
  [My Pocket List: Horizontal LazyRow]
  [Captured: Charizard (Fire) | Charizard (Fire) | Pikachu (Electric) | ...]
  [Type List: Vertical LazyColumn]
  [Dragon (2): Horizontal LazyRow]
  [Garchomp | Dragonite | ...]
  [Fire (3): Horizontal LazyRow]
  [Charizard | Charmander | ...]
  ```
- **Dynamic Loading**:
  - Type Lists appear only when at least one Pokémon is processed (`isProcessed = true`).
  - Category counts (e.g., "Fire (3)") update in real-time without animation.

### Detail Screen
- Displays Pokémon details (name, image, types, description, evolution).

## Potential Challenges and Mitigations
- **Challenge**: Large dataset management (151 Pokémon).
  - **Mitigation**: Use WorkManager for incremental processing, Room with `Flow` for dynamic loading, and Compose’s lazy rendering.
- **Challenge**: Network failures.
  - **Mitigation**: Cache data in Room for offline access, handle failures gracefully.
- **Challenge**: Duplicate captures across categories.
  - **Mitigation**: Use `CaptureRecord` with `categoryType` for precise tracking, test release logic.
- **Challenge**: UI performance with scroll behavior.
  - **Mitigation**: Optimize with Coil image caching, Compose’s lazy loading, and efficient scroll state handling.