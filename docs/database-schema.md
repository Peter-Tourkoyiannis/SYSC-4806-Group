# Milestone 1 Database Schema

The Milestone 1 database structure is generated from the current JPA entity mappings.

```mermaid
erDiagram
    MERCHANTS ||--o{ SHOPS : owns
    SHOPS ||--o{ PRODUCTS : contains
    SHOPS ||--o{ SHOP_TAGS : has

    MERCHANTS {
        BIGINT id PK
        VARCHAR name
    }

    SHOPS {
        BIGINT id PK
        VARCHAR name
        VARCHAR description
        BIGINT merchant_id FK
    }

    PRODUCTS {
        BIGINT id PK
        VARCHAR name
        VARCHAR description
        DOUBLE price
        INTEGER inventory
        BLOB imageBytes
        BIGINT shop_id FK
    }

    SHOP_TAGS {
        BIGINT shop_id FK
        VARCHAR tag
    }
```

## Relationships

- One merchant can own multiple shops.
- Each shop belongs to one merchant through `merchant_id`.
- One shop can contain multiple products.
- Each product belongs to one shop through `shop_id`.
- A shop can contain multiple tags, stored in the `shop_tags` collection table.
