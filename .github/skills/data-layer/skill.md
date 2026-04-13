# Data Layer Skill

- **Single Source of Truth:** The database (Room) is the source of truth. Use `Flow` to observe changes.
- **Offline-First:** Design for offline use. Repositories should handle data synchronization logic.
- **Encapsulation:** DAOs must be internal or hidden behind Repositories.
- **Entities:** Keep DB entities separate from UI models if they differ significantly.
