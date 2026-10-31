package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The Model of the application. It keeps all the products and the rules of the
 * inventory. It does not know anything about the GUI. It is the "Subject" in
 * the Observer pattern: after each change it tell all the observers.
 */
public class InventoryModel {

    /** If the quantity of a product is less than this number, it is "low stock". */
    public static final int LOW_STOCK_LEVEL = 5;

    // Rep
    private final Map<Integer, Product> products = new LinkedHashMap<>();
    private final List<InventoryObserver> observers = new ArrayList<>();
    private int nextId = 1;

    // RI:
    //   products != null, observers != null
    //   every key in products is equal to the id of its Product value
    //   no key or value in products is null
    //   nextId is bigger than all the ids that are inside products
    //   (so a new product never get an id that was used before)
    //
    // AF:
    //   AF(products, observers, nextId) = the current inventory of the store,
    //   which is the set of all values in 'products' and the observers
    //   are the objects that want to be told about changes.
    //
    // Safety from rep exposure:
    //   products and observers are never returned. Methods return new copies
    //   of the data, and Product is immutable.

    private void checkRep() {
        for (Map.Entry<Integer, Product> e : products.entrySet()) {
            assert e.getValue() != null;
            assert e.getKey() == e.getValue().getId();
            assert e.getKey() < nextId;
        }
    }

    // ---------- Observer pattern ----------

    public void addObserver(InventoryObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer can not be null");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(InventoryObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        // we loop on a copy, so an observer can remove itself safely
        for (InventoryObserver o : new ArrayList<>(observers)) {
            o.update(this);
        }
    }

    // ---------- Commands (they change the state) ----------

    /**
     * Add a new product to the inventory.
     * @return the new product (with its new id)
     * @throws IllegalArgumentException if a value is not valid
     */
    public Product addProduct(String name, String category, long priceCents, int quantity) {
        Product product = new Product(nextId, name, category, priceCents, quantity);
        products.put(product.getId(), product);
        nextId++;
        checkRep();
        notifyObservers();
        return product;
    }

    /**
     * Change name, category and price of an existing product.
     * @throws IllegalArgumentException if the product does not exist or a value is not valid
     */
    public void editProduct(int id, String name, String category, long priceCents) {
        Product old = requireProduct(id);
        Product changed = old.withDetails(name, category, priceCents);
        if (changed.equals(old)) {
            return; // nothing is changed, so no need to tell the observers
        }
        products.put(id, changed);
        checkRep();
        notifyObservers();
    }

    /** Delete a product from the inventory. */
    public void deleteProduct(int id) {
        requireProduct(id);
        products.remove(id);
        checkRep();
        notifyObservers();
    }

    /**
     * Increase (delta > 0) or decrease (delta < 0) the stock of a product.
     * @throws IllegalArgumentException if the new quantity will be negative or too big
     */
    public void changeStock(int id, int delta) {
        Product old = requireProduct(id);
        long newQuantity = (long) old.getQuantity() + delta;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Not enough stock. We have only "
                    + old.getQuantity() + " of '" + old.getName() + "'");
        }
        if (newQuantity > Product.MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity can not be more than "
                    + Product.MAX_QUANTITY);
        }
        if (delta == 0) {
            return;
        }
        products.put(id, old.withQuantity((int) newQuantity));
        checkRep();
        notifyObservers();
    }

    // ---------- Queries (they do not change the state) ----------

    /** @return the product with this id, or null if we do not have it */
    public Product getProduct(int id) {
        return products.get(id);
    }

    /** @return all the products, in the order that they were added */
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    /** @return the products that have less quantity than LOW_STOCK_LEVEL */
    public Set<Product> getLowStockProducts() {
        Set<Product> result = new LinkedHashSet<>();
        for (Product p : products.values()) {
            if (p.isLowStock(LOW_STOCK_LEVEL)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Search by name or category. The search is not case sensitive and it
     * looks for the text anywhere in the name or the category.
     * Empty text gives all the products.
     */
    public List<Product> search(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Product> result = new ArrayList<>();
        for (Product p : products.values()) {
            if (q.isEmpty()
                    || p.getName().toLowerCase(Locale.ROOT).contains(q)
                    || p.getCategory().toLowerCase(Locale.ROOT).contains(q)) {
                result.add(p);
            }
        }
        return result;
    }

    public int getLowStockLevel() {
        return LOW_STOCK_LEVEL;
    }

    private Product requireProduct(int id) {
        Product p = products.get(id);
        if (p == null) {
            throw new IllegalArgumentException("Product with id " + id + " does not exist");
        }
        return p;
    }
}
