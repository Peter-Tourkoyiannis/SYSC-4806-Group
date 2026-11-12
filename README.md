# Mini-Shopify

## Project Description

Mini-Shopify is a Spring MVC web application that allows merchants to create shops and manage products, while customers can browse and search available shops.

## Milestone 1

The goal of Milestone 1 is to implement one working end-to-end use case:

Merchant creates a shop → shop is stored → shops can be listed and searched.

## Current Functionality

The current Milestone 1 implementation includes:

- Merchant shop creation
- Merchant, Shop, and Product JPA entities
- Repository support for the main model entities
- Shop persistence and merchant-to-shop relationships
- Shop dashboard
- Product creation and inventory fields
- Product-to-shop relationships
- Shop tags stored using a JPA element collection
- UML model documentation
- Database schema documentation

Shop search, customer-facing UI, and additional Milestone 1 testing are being completed through separate project branches.

## Running the Application

The project uses Maven and Spring Boot.

Build the project with:

`mvn clean package`

Run the generated JAR with:

`java -jar target/*.jar`

Live Link to view on Azure:
<br><br>
*Note: Since Spring Boot is a CPU extensive fwk, it may take a few minutes to load.*

<https://4806-group-project-ebexg9h8ckanc0ar.canadacentral-01.azurewebsites.net/>

## Testing

Milestone 1 testing covers the main application flow through repository and controller-level tests.

Additional test scaffolding and feature-specific testing are being completed as part of the remaining Milestone 1 work.

## UML Model Diagram

The current Milestone 1 UML model diagram documents the Merchant, Shop, and Product model classes and their relationships.

[View UML Model Diagram](./docs/uml-model.md)

## Database Schema

The current Milestone 1 database schema is derived from the JPA entity mappings and documents the main tables, primary keys, foreign keys, and relationships.

[View Database Schema](./docs/database-schema.md)

## Current Sprint Status

Milestone 1 currently includes the initial merchant/shop workflow, model entities, repository layer, documentation, UML model diagram, database schema, CI/CD setup, and supporting UI and testing work.

See the GitHub Issues and Project board for the current status of individual Milestone 1 tasks.

## Next Sprint

Milestone 2 will expand the application toward the full merchant-to-customer flow, including product management, cart functionality, checkout, inventory handling, authentication, and additional customer-facing features.
