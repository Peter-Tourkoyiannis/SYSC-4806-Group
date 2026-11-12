# Milestone 1 Database Schema

```mermaid
erDiagram
    MERCHANTS ||--o{ SHOPS : owns
    SHOPS ||--o{ PRODUCTS : contains
    SHOPS ||--o{ SHOP_TAGS : has
```
