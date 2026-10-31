package model;

import java.util.Objects;

/**
 * Product is an immutable ADT. It is one product inside the store inventory.
 * Once we create a Product object, we can not change it. If we want to
 * change something (like quantity), we create a new Product object.
 *
 * Abstract value: one product with unique id, a name, a category, a price
 * and the quantity that we have in the store.
 */
public final class Product {

    public static final int MAX_QUANTITY = 1_000_000;
    public static final long MAX_PRICE_CENTS = 100_000_000L; // 1,000,000.00

    // Rep (all fields are private, final and immutable)
    private final int id;
    private final String name;
    private final String category;
    private final long priceCents;
    private final int quantity;

    // Representation Invariant (RI):
    //   id > 0                           (id must be a positive number)
    //   name != null                     (name is not null)
    //   name is not empty after trim()   (name has at least one real character)
    //   category != null                 (category is not null)
    //   category is not empty after trim()
    //   0 <= priceCents <= MAX_PRICE_CENTS   (price can not be negative)
    //   0 <= quantity <= MAX_QUANTITY        (stock can not be negative)
    //
    // Abstraction Function (AF):
    //   AF(id, name, category, priceCents, quantity) =
    //     a product in the store that is identified by 'id', called 'name',
    //     placed in the group 'category', sold for (priceCents / 100) money
    //     units, and we have 'quantity' pieces of it in the stock.
    //
    // Safety from rep exposure:
    //   All fields are private and final. int and long are primitive and
    //   String is immutable, so no one outside can change the rep. The class
    //   is final so no subclass can break the immutability.

    /**
     * Create a new product.
     * @throws IllegalArgumentException if one of the values break the RI
     */
    public Product(int id, String name, String category, long priceCents, int quantity) {
        if (name == null || category == null) {
            throw new IllegalArgumentException("Name and category can not be null");
        }
        this.id = id;
        this.name = name.trim();
        this.category = category.trim();
        this.priceCents = priceCents;
        this.quantity = quantity;
        checkRep();
    }

    private void checkRep() {
        if (id <= 0) {
            throw new IllegalArgumentException("Id must be a positive number");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name can not be empty");
        }
        if (category.isEmpty()) {
            throw new IllegalArgumentException("Category can not be empty");
        }
        if (priceCents < 0 || priceCents > MAX_PRICE_CENTS) {
            throw new IllegalArgumentException("Price must be between 0 and "
                    + formatPrice(MAX_PRICE_CENTS));
        }
        if (quantity < 0 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between 0 and " + MAX_QUANTITY);
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public long getPriceCents() {
        return priceCents;
    }

    public int getQuantity() {
        return quantity;
    }

    /** @return a new Product with same id and quantity, but with new name, category and price */
    public Product withDetails(String newName, String newCategory, long newPriceCents) {
        return new Product(id, newName, newCategory, newPriceCents, quantity);
    }

    /** @return a new Product that is same as this one, but with new quantity */
    public Product withQuantity(int newQuantity) {
        return new Product(id, name, category, priceCents, newQuantity);
    }

    /** @return true if the quantity is less than the minimum stock level */
    public boolean isLowStock(int minLevel) {
        return quantity < minLevel;
    }

    /** @return the price as text with 2 digits after the dot, for example "12.50" */
    public String getPriceText() {
        return formatPrice(priceCents);
    }

    public static String formatPrice(long cents) {
        return String.format("%d.%02d", cents / 100, cents % 100);
    }

    // Two products are equal if all their values are the same (observational equality).
    // We need this because we use Product inside a Set (low stock products) and we
    // use equals to check if an edit really changed something.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Product)) {
            return false;
        }
        Product p = (Product) other;
        return id == p.id
                && priceCents == p.priceCents
                && quantity == p.quantity
                && name.equals(p.name)
                && category.equals(p.category);
    }

    // Must be consistent with equals: equal products give same hash code.
    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, priceCents, quantity);
    }

    @Override
    public String toString() {
        return "Product[id=" + id + ", name=" + name + ", category=" + category
                + ", price=" + getPriceText() + ", quantity=" + quantity + "]";
    }
}
