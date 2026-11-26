# Mini-Shopify

Mini-Shopify is a Spring Boot web application built for SYSC 4806. It supports a merchant-to-customer shopping flow: merchants create and manage shops and products, while customers browse storefronts, manage a shopping cart, and place orders.

## Milestones

**Milestone 1:** Merchant and shop creation, persistence, shop listings, and search.

**Milestone 2:** Product management, customer-facing product details, session-based shopping cart, transactional checkout, inventory updates, order confirmation, and expanded automated tests.

## Features

### Merchants and products

- Create merchants and shops; view a merchant's shops and edit shop details and tags.
- Manage products from a shop dashboard: add, edit, delete, and update available inventory.
- Upload product images (JPEG, PNG, GIF, or WebP) and display them in the storefront.
- Use a JPA `@Version` field on products for optimistic locking during updates.

### Customer shopping flow

- Browse shops and search by shop name or tag, including case-insensitive partial matches.
- View storefronts, product descriptions, prices, images, and available stock.
- Add products to a session-scoped cart; update quantities or remove items.
- Review quantities, unit prices, subtotals, and the order total at checkout.
- Place an order after validating that the cart is nonempty and the requested quantities are valid and in stock.
- Save the order and its line items, deduct inventory, clear the cart on success, and show the order ID and total on a confirmation page.
- On checkout validation failure, show an error without saving an order or clearing the cart.

**Note:** Checkout is a simulated purchase flow; no payment gateway or real payment processing is implemented.

## Technology

- **Java 21** and **Spring Boot 4.1.2**
- Spring MVC, Thymeleaf, and Bootstrap for the web interface
- Spring Data JPA / Hibernate for entities and persistence
- H2 for local development and automated tests; PostgreSQL for the Azure production profile
- Maven Wrapper (`mvnw` / `mvnw.cmd`) for builds and tests

## Running Locally

**Prerequisite:** JDK 21. Verify with `java -version`.

Clone the repository and enter the project directory:

```bash
git clone https://github.com/Peter-Tourkoyiannis/SYSC-4806-Group.git
cd SYSC-4806-Group
```

Start the application on **Windows (PowerShell)**:

```powershell
.\mvnw.cmd spring-boot:run
```

Or on **macOS/Linux**:

```bash
./mvnw spring-boot:run
```

Then visit **http://localhost:8080/**. Choose **Merchant** to set up a shop and add products, or **Customer** to browse shops and test the cart-to-checkout flow. The local H2 database is in memory, so local demo data may need to be recreated after restarting the application.

## Main Routes

| Route | Purpose |
|---|---|
| `/` | Landing page |
| `/merchant/home` | Merchant home and shop selection |
| `/merchant/shops/new` | Create a shop |
| `/merchant/shops/{shopId}/dashboard` | Manage a shop's products |
| `/shops` | Browse and search shops |
| `/shops/{id}` | Customer storefront |
| `/shops/{shopId}/products/{productId}` | Product details and Add to cart |
| `/cart` | Review and update cart |
| `/checkout` | Review the purchase (GET) or place an order (POST) |
| `/orders/{id}` | Order confirmation |

For the full controller and route list, see the [UML and application documentation](./docs/uml-model.md).

## Checkout Design

`CheckoutService.checkoutCart()` runs within a `@Transactional` boundary. It first validates all cart entries (product, quantity, and stock) and then creates `Order`/`OrderItem` records, calculates the total, and updates product inventory. Database changes roll back on unexpected transactional failures. The controller clears the session cart only after checkout succeeds; validation errors return the customer to the checkout view with the cart retained.

## Automated Tests

The repository includes Spring Boot / JUnit tests for merchant and shop workflows, product functionality, cart behavior, and checkout. Checkout tests cover successful order persistence and inventory deduction, insufficient stock, and empty-cart rejection.

Run the test suite on **Windows**:

```powershell
.\mvnw.cmd test
```

Or on **macOS/Linux**:

```bash
./mvnw test
```

GitHub Actions is configured for CI and Azure deployment.

## Azure Deployment

[Open the deployed Mini-Shopify application](https://4806-group-project-ebexg9h8ckanc0ar.canadacentral-01.azurewebsites.net/)

The `prod` Spring profile uses PostgreSQL. Deployment configuration expects the environment variables `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. These are deployment settings, not required when running locally with H2.

## Documentation

- [Milestone 2 UML diagrams](./docs/uml-model.md) — domain entities, application layers, and routes.
- [Database schema](./docs/database-schema.md) — JPA-derived tables, keys, and relationships.

See the repository's GitHub Issues and Project board for implementation tasks and milestone tracking.
