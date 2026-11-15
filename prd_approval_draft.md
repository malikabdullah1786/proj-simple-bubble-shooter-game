# Product Requirements Document: Java Swing Bubble Shooter

## 1. Overview & Goals
- **Core Problem:** Deliver a lightweight, zero-dependency, retro arcade Bubble Shooter game built entirely in Java using Swing for rendering and UI controls.
- **User Benefits:** Provides instant-load, offline-capable casual entertainment with responsive mouse-aim mechanics and progressive difficulty scaling.
- **KPI Metrics:** Maintain 60 FPS render loop stability, zero memory leaks over 30-minute sessions, and <100ms projectile launch latency.

## 2. Personas (Compact)
- [Casual Gamer]: Launches the desktop app via a runnable JAR to play quick 5-minute arcade sessions offline.
- [Retro Enthusiast]: Expects authentic grid-snapping mechanics, color-matching physics, and score persistence.

## 3. User Stories & Epics (Dense List)
* [EPIC-01: Game Board & Physics] As a player, I want to aim and fire colored bubbles so that I can match 3 or more of the same color.
  - AC: [1] Given game is active, When player moves mouse, Then aiming trajectory line updates in real-time. [2] Given projectile is fired, When it collides with grid or ceiling, Then it snaps to the nearest hexagonal grid coordinate. [3] Given 3+ matching adjacent bubbles exist post-snap, When checked, Then all connected matches and floating unanchored clusters clear simultaneously.
* [EPIC-02: Scoring & Progression] As a player, I want my score to track dynamically and the board to descend over time so that there is a failure state.
  - AC: [1] Given bubbles are cleared, When calculated, Then score increases by `(ClearedCount * 10 * ComboMultiplier)`. [2] Given a counter reaches 0 failed shots, When decremented, Then the entire bubble grid shifts down by one row. [3] Given any bubble crosses the bottom danger line, When checked, Then trigger Game Over state.

## 4. Functional Specs (Bullet Tree)
- `MainFrame (JFrame)`: Root container window fixed at 800x600px, non-resizable.
  - `GameCanvas (JPanel)`: Double-buffered rendering surface for the grid, shooter cannon, and projectiles.
    - Render Loop: `javax.swing.Timer` executing at ~60 FPS (16ms interval).
    - Grid Specs: Hexagonal offset grid layout, 12 columns by 10 visible initial rows. Radius = 20px per bubble.
    - Colors: 5 distinct RGB color constants (Red, Blue, Green, Yellow, Purple).
  - `ControlPanel (JPanel)`: Bottom HUD overlay tracking current Score, High Score, Next Bubble preview, and Missed Shots counter.
    - Click Action -> Success: Spawns projectile from cannon toward cursor angle, Error: Ignored if projectile currently in flight.
    - Collision Handling -> Success: Snaps to grid / triggers pop logic, Error: Bounces off left/right window boundaries via vector reflection.

## 5. Non-Functional Requirements
- **Performance:** Render loop must maintain steady 60 FPS with <5% CPU utilization on standard JVM (Java 17+). Garbage collection pauses must not exceed 50ms during gameplay loops.
- **Compatibility:** Fully cross-platform via Java Swing (`JPanel`/`Graphics2D`), executing natively on Windows, macOS, and Linux with standard JRE 17 installed.
- **Architecture:** Decoupled Model-View-Controller (MVC) separating game state matrix (`BubbleGrid`), physics engine (`Projectile`), and UI rendering (`GameCanvas`).

## 6. Out of Scope & Release Plan
- **Out of Scope:** 
  - Network multiplayer modes or global leaderboard integrations.
  - Custom skinning, sprite asset loaders, or external audio synthesizers (use basic `Toolkit.getDefaultToolkit().beep()` if needed, or mute).
  - Mobile platform ports (iOS/Android) or web browser wrappers.
- **Release Plan:**
  - *Phase 1 (Alpha):* Implement core hexagonal grid matrix, collision detection, and basic mouse-aim firing logic.
  - *Phase 2 (Beta):* Add floating cluster removal, score tracking, row-drop descent mechanics, and Game Over state.
  - *Phase 3 (GA):* Finalize Swing UI HUD, package as an executable shaded Uber-JAR via Maven/Gradle, and verify cross-platform execution.