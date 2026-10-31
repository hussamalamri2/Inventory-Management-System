import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.InventoryModel;
import model.InventoryObserver;
import model.Product;

/** Simple tests without any library. Run it and it print PASS or FAIL for each test. */
public class AllTests {

    private static int passed = 0;
    private static int failed = 0;

    interface Body {
        void run() throws Exception;
    }

    static void test(String name, Body body) {
        try {
            body.run();
            passed++;
            System.out.println("PASS  " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("FAIL  " + name + "  -> " + t);
        }
    }

    static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void expectIAE(Body body, String message) {
        try {
            body.run();
        } catch (IllegalArgumentException ex) {
            return;
        } catch (Exception ex) {
            throw new AssertionError("Wrong exception: " + ex);
        }
        throw new AssertionError(message);
    }

    public static void main(String[] args) {
        // ---------- Product ----------
        test("Product keeps its values", () -> {
            Product p = new Product(1, "  Rice ", " Grocery ", 2450, 10);
            check(p.getId() == 1, "id");
            check(p.getName().equals("Rice"), "name is trimmed");
            check(p.getCategory().equals("Grocery"), "category is trimmed");
            check(p.getPriceText().equals("24.50"), "price text");
            check(p.getQuantity() == 10, "quantity");
        });
        test("Product rejects bad values (RI)", () -> {
            expectIAE(() -> new Product(0, "A", "B", 1, 1), "id 0");
            expectIAE(() -> new Product(1, "", "B", 1, 1), "empty name");
            expectIAE(() -> new Product(1, "   ", "B", 1, 1), "blank name");
            expectIAE(() -> new Product(1, "A", "", 1, 1), "empty category");
            expectIAE(() -> new Product(1, null, "B", 1, 1), "null name");
            expectIAE(() -> new Product(1, "A", "B", -1, 1), "negative price");
            expectIAE(() -> new Product(1, "A", "B", 1, -1), "negative quantity");
            expectIAE(() -> new Product(1, "A", "B", 1, Product.MAX_QUANTITY + 1), "too big quantity");
        });
        test("Product is immutable (withQuantity gives new object)", () -> {
            Product p = new Product(1, "A", "B", 100, 5);
            Product q = p.withQuantity(9);
            check(p.getQuantity() == 5, "old object not changed");
            check(q.getQuantity() == 9, "new object changed");
            check(q.getId() == p.getId() && q.getName().equals(p.getName()), "other values same");
        });
        test("Product equals and hashCode", () -> {
            Product a = new Product(1, "A", "B", 100, 5);
            Product b = new Product(1, "A", "B", 100, 5);
            Product c = new Product(1, "A", "B", 100, 6);
            check(a.equals(b) && b.equals(a), "same values are equal");
            check(a.hashCode() == b.hashCode(), "same hash code");
            check(!a.equals(c), "different quantity not equal");
            check(!a.equals(null), "not equal to null");
            check(!a.equals("text"), "not equal to other type");
            Set<Product> set = new HashSet<>();
            set.add(a);
            check(set.contains(b), "set find an equal product");
            check(!set.contains(c), "set does not find a different product");
        });
        test("Product low stock", () -> {
            Product p = new Product(1, "A", "B", 100, 4);
            check(p.isLowStock(5), "4 < 5 is low");
            check(!p.withQuantity(5).isLowStock(5), "5 is not low");
        });

        // ---------- InventoryModel ----------
        test("Model add product gives new ids", () -> {
            InventoryModel m = new InventoryModel();
            Product a = m.addProduct("A", "X", 100, 1);
            Product b = m.addProduct("B", "X", 100, 1);
            check(a.getId() == 1 && b.getId() == 2, "ids");
            check(m.getAllProducts().size() == 2, "size");
        });
        test("Model does not reuse the id of deleted product", () -> {
            InventoryModel m = new InventoryModel();
            m.addProduct("A", "X", 100, 1);
            m.deleteProduct(1);
            Product b = m.addProduct("B", "X", 100, 1);
            check(b.getId() == 2, "id is 2");
        });
        test("Model add with bad value fails and state is not changed", () -> {
            InventoryModel m = new InventoryModel();
            expectIAE(() -> m.addProduct("", "X", 100, 1), "empty name");
            check(m.getAllProducts().isEmpty(), "nothing added");
        });
        test("Model edit product", () -> {
            InventoryModel m = new InventoryModel();
            Product a = m.addProduct("A", "X", 100, 7);
            m.editProduct(a.getId(), "New", "Y", 500);
            Product e = m.getProduct(a.getId());
            check(e.getName().equals("New") && e.getCategory().equals("Y"), "text");
            check(e.getPriceCents() == 500, "price");
            check(e.getQuantity() == 7, "quantity is same");
            expectIAE(() -> m.editProduct(99, "N", "C", 1), "unknown id");
        });
        test("Model delete product", () -> {
            InventoryModel m = new InventoryModel();
            Product a = m.addProduct("A", "X", 100, 1);
            m.deleteProduct(a.getId());
            check(m.getProduct(a.getId()) == null, "deleted");
            expectIAE(() -> m.deleteProduct(a.getId()), "delete again");
        });
        test("Model change stock", () -> {
            InventoryModel m = new InventoryModel();
            Product a = m.addProduct("A", "X", 100, 10);
            m.changeStock(a.getId(), 5);
            check(m.getProduct(a.getId()).getQuantity() == 15, "increase");
            m.changeStock(a.getId(), -15);
            check(m.getProduct(a.getId()).getQuantity() == 0, "decrease to zero");
            expectIAE(() -> m.changeStock(a.getId(), -1), "below zero");
            expectIAE(() -> m.changeStock(a.getId(), Product.MAX_QUANTITY + 1), "too big");
            check(m.getProduct(a.getId()).getQuantity() == 0, "state not changed after errors");
        });
        test("Model search by name and category", () -> {
            InventoryModel m = new InventoryModel();
            m.addProduct("Fresh Milk", "Dairy", 100, 10);
            m.addProduct("Yogurt", "Dairy", 100, 10);
            m.addProduct("Rice", "Grocery", 100, 10);
            check(m.search("milk").size() == 1, "by name, any case");
            check(m.search("DAIRY").size() == 2, "by category");
            check(m.search("  ").size() == 3, "blank gives all");
            check(m.search("zzz").isEmpty(), "no result");
            check(m.search(null).size() == 3, "null gives all");
        });
        test("Model low stock", () -> {
            InventoryModel m = new InventoryModel();
            m.addProduct("A", "X", 100, 4);
            m.addProduct("B", "X", 100, 5);
            m.addProduct("C", "X", 100, 0);
            Set<Product> low = m.getLowStockProducts();
            check(low.size() == 2, "A and C are low");
            check(low.contains(m.getProduct(1)) && low.contains(m.getProduct(3)), "contains");
        });
        test("Model returns copies (no rep exposure)", () -> {
            InventoryModel m = new InventoryModel();
            m.addProduct("A", "X", 100, 1);
            List<Product> list = m.getAllProducts();
            list.clear();
            check(m.getAllProducts().size() == 1, "model not changed");
        });

        // ---------- Observer pattern ----------
        test("Observer is told after each change", () -> {
            InventoryModel m = new InventoryModel();
            int[] count = {0};
            InventoryObserver o = model -> count[0]++;
            m.addObserver(o);
            Product a = m.addProduct("A", "X", 100, 10);
            check(count[0] == 1, "after add");
            m.editProduct(a.getId(), "B", "X", 100);
            check(count[0] == 2, "after edit");
            m.changeStock(a.getId(), 1);
            check(count[0] == 3, "after stock");
            m.deleteProduct(a.getId());
            check(count[0] == 4, "after delete");
        });
        test("Observer is not told if nothing changed or if error", () -> {
            InventoryModel m = new InventoryModel();
            Product a = m.addProduct("A", "X", 100, 10);
            int[] count = {0};
            m.addObserver(model -> count[0]++);
            m.editProduct(a.getId(), "A", "X", 100); // same values
            m.changeStock(a.getId(), 0);
            expectIAE(() -> m.changeStock(a.getId(), -50), "too much");
            check(count[0] == 0, "no notification");
        });
        test("Removed observer is not told", () -> {
            InventoryModel m = new InventoryModel();
            List<String> log = new ArrayList<>();
            InventoryObserver o = model -> log.add("x");
            m.addObserver(o);
            m.removeObserver(o);
            m.addProduct("A", "X", 100, 1);
            check(log.isEmpty(), "not told");
        });

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
