# System Architecture

## 1. Tech Stack
- **Language**: Java 17+ (LTS)
- **UI Framework**: Java Swing (`JFrame`, `JPanel`, `Graphics2D`)
- **Build System**: Maven (Shade Plugin for executable Uber-JAR)
- **Database**: None — Single-session desktop arcade game; high scores maintained in-memory with optional local flat-file (`prefs.properties`) persistence.

## 2. Topology
- **Desktop Runtime (JRE 17+)**: Executes single-process monolithic JVM thread model.
  - `MainFrame`: Window container (800x600px, fixed).
  - `GameCanvas`: Active render loop (60 FPS via `javax.swing.Timer`), handles `MouseListener`/`MouseMotionListener` inputs, renders grid and active `Projectile` via `Graphics2D`.
  - `GameState`: In-memory model tracking hexagonal offset matrix (`BubbleGrid`), active projectile vector math, score metrics, and progression state.
  - `ControlPanel`: HUD rendering score, high score, next-bubble preview, and missed shot counter.

## 3. Database Schema
```yaml
Database: None — State is volatile per arcade session. High score cached locally via `java.util.prefs.Preferences` API (OS registry/plist configuration store).
```

## 4. API Contracts
```yaml
API: None — Monolithic desktop application with direct method invocation across decoupled MVC packages.
```

## 5. Directory Tree
```text
bubble-shooter/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── arcade/
                    └── bubbleshooter/
                        ├── App.java
                        ├── controller/
                        │   ├── GameController.java
                        │   └── InputController.java
                        ├── model/
                        │   ├── Bubble.java
                        │   ├── BubbleGrid.java
                        │   ├── ColorEnum.java
                        │   ├── GameState.java
                        │   └── Projectile.java
                        └── view/
                            ├── ControlPanel.java
                            ├── GameCanvas.java
                            └── MainFrame.java
```

## 6. Security/Perf
- **Render Loop Optimization**: Utilize `BufferStrategy` or double-buffered `JPanel` (`setDoubleBuffered(true)`) combined with dirty-rectangle repainting (`repaint(int x, int y, int width, int height}`) to restrict CPU utilization below 5%.
- **Memory Management**: Avoid object allocation inside the 60 FPS update/render loops by pre-allocating math vectors and reusing collections for cluster-matching breadth-first search (BFS) algorithms to prevent GC pauses exceeding 50ms.
- **Thread Safety**: Confining all UI mutations and rendering updates strictly to the Swing Event Dispatch Thread (EDT), handling physics calculations synchronously prior to EDT paint cycles via `javax.swing.Timer`.
- **Input Debouncing**: Gate firing mechanics by ignoring mouse-click triggers while an active `Projectile` is currently in flight (`inFlight` boolean lock).