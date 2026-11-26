# UML Diagrams (Milestone 2)

These diagrams reflect the code currently on `main`. Getters and setters are omitted.

## Domain model

```mermaid
classDiagram

class Merchant {
    <<Entity>>
    - Long id
    - String name
    - List~Shop~ shops
    + Merchant()
    + Merchant(String name)
}

class Shop {
    <<Entity>>
    - Long id
    - String name
    - String description
    - Set~String~ tags
    - Merchant merchant
    - List~Product~ products
    + Shop()
    + Shop(String name, String description, Set~String~ tags, Merchant merchant)
}

class Product {
    <<Entity>>
    - Long id
    - String name
    - String description
    - double price
    - int inventory
    - Long version
    - byte[] imageBytes
    - String imageContentType
    - Shop shop
    + Product()
}

class Order {
    <<Entity>>
    - Long id
    - LocalDateTime createdAt
    - double total
    - List~OrderItem~ orderItems
    + Order()
    + addOrderItem(OrderItem item) void
}

class OrderItem {
    <<Entity>>
    - Long id
    - int quantity
    - double price
    - Product product
    - Order order
    + OrderItem(int quantity, double price, Product product, Order order)
}

class Cart {
    <<SessionScope>>
    - Map~Long, Integer~ items
    + getItems() Map~Long, Integer~
    + add(Long productId, int qty) void
    + update(Long productId, int qty) void
    + remove(Long productId) void
    + clear() void
    + getItemCount() int
}

Merchant "1" *-- "0..*" Shop : owns
Shop "1" *-- "0..*" Product : contains
Order "1" *-- "1..*" OrderItem : contains
OrderItem "0..*" --> "1" Product : purchased
Cart ..> Product : holds ids and quantities
```

Notes:
- `Product.version` is a JPA `@Version` field, used for optimistic locking when stock changes.
- `OrderItem.price` stores the price at the time of purchase, so later price changes don't affect past orders.
- `Cart` is not a database entity. It lives in the HTTP session and maps product IDs to quantities.

## Application layers

```mermaid
classDiagram
direction LR

class HomeController {
    <<Controller>>
    + index() String
}

class ShopController {
    <<Controller>>
    + listShops(String q, Model model) String
    + storefront(Long id, Model model) String
    + productDetails(Long shopId, Long productId, Model model) String
}

class MerchantShopController {
    <<Controller>>
    + merchantHome(String merchantName, Model model) String
    + showCreateMerchantForm(Model model) String
    + createMerchant(MerchantForm form) String
    + showCreateShopForm(String merchantName, Model model) String
    + createShop(ShopForm form) String
    + shopDashboard(Long shopId, Model model) String
    + showEditShopForm(Long shopId, Model model) String
    + editShop(Long shopId, EditShopForm form) String
}

class ProductController {
    <<Controller>>
    + addProduct(Long shopId, ..., MultipartFile image) String
    + getProductImage(Long id) ResponseEntity~byte[]~
    + showEditProductForm(Long id, Model model) String
    + editProduct(Long id, ..., Long version) String
    + deleteProduct(Long id, Long version) String
    + updateStock(Long id, int inventory, Long version) String
}

class CartController {
    <<Controller>>
    + viewCart(Model model) String
    + addToCart(Long productId, int quantity) String
    + updateCart(Long productId, int quantity) String
    + removeFromCart(Long productId) String
}

class CheckoutController {
    <<Controller>>
    + showCheckout(Model model) String
    + placeOrder(Model model) String
    + showOrder(Long id, Model model) String
}

class CartModelAdvice {
    <<ControllerAdvice>>
    + cartItemCount() int
}

class CheckoutService {
    <<Service>>
    + checkoutCart(Map~Long, Integer~ cart) Order
}

class MerchantRepository {
    <<Repository>>
    + findAllByNameIgnoreCase(String name) List~Merchant~
}

class ShopRepository {
    <<Repository>>
    + findByQ(String q) List~Shop~
    + findByMerchant_NameIgnoreCase(String merchantName) List~Shop~
}

class ProductRepository {
    <<Repository>>
}

class OrderRepository {
    <<Repository>>
}

class Cart {
    <<SessionScope>>
}

ShopController --> ShopRepository
ShopController --> ProductRepository
MerchantShopController --> MerchantRepository
MerchantShopController --> ShopRepository
ProductController --> ProductRepository
ProductController --> ShopRepository
CartController --> Cart
CartController --> ProductRepository
CartModelAdvice --> Cart
CheckoutController --> CheckoutService
CheckoutController --> Cart
CheckoutController --> ProductRepository
CheckoutController --> OrderRepository
CheckoutService --> ProductRepository
CheckoutService --> OrderRepository
```

All repositories extend `JpaRepository<Entity, Long>`. `CheckoutService.checkoutCart` is `@Transactional`: it checks every item's stock, decrements inventory, and saves the order, and if any item is out of stock, the whole checkout rolls back.

## Routes

| Controller | Method | Path | Purpose |
|---|---|---|---|
| HomeController | GET | `/` | Landing page |
| ShopController | GET | `/shops?q=` | List shops, search by name or tag |
| ShopController | GET | `/shops/{id}` | Storefront |
| ShopController | GET | `/shops/{shopId}/products/{productId}` | Product details |
| MerchantShopController | GET | `/merchant/home?merchantName=` | Merchant home, lists their shops |
| MerchantShopController | GET | `/merchant/new` | Create merchant form |
| MerchantShopController | POST | `/merchant/merchants` | Create merchant |
| MerchantShopController | GET | `/merchant/shops/new` | Create shop form |
| MerchantShopController | POST | `/merchant/shops` | Create shop |
| MerchantShopController | GET | `/merchant/shops/{shopId}/dashboard` | Shop dashboard |
| MerchantShopController | GET / POST | `/merchant/shops/{shopId}/edit` | Edit shop |
| ProductController | POST | `/merchant/shops/{shopId}/products` | Add product with image |
| ProductController | GET | `/merchant/products/{id}/image` | Serve product image |
| ProductController | GET / POST | `/merchant/products/{id}/edit` | Edit product |
| ProductController | POST | `/merchant/products/{id}/delete` | Delete product |
| ProductController | POST | `/merchant/products/{id}/stock` | Update inventory |
| CartController | GET | `/cart` | View cart |
| CartController | POST | `/cart/add`, `/cart/update`, `/cart/remove` | Change cart contents |
| CheckoutController | GET / POST | `/checkout` | Review cart / place order |
| CheckoutController | GET | `/orders/{id}` | Order confirmation |
