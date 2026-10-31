package controller;

import java.math.BigDecimal;

import model.InventoryModel;
import model.Product;
import view.InventoryView;


public class InventoryController {

    private final InventoryModel model;
    private final InventoryView view;

    public InventoryController(InventoryModel model, InventoryView view) {
        this.model = model;
        this.view = view;

        model.addObserver(view);

        view.addAddListener(e -> handleAdd());
        view.addEditListener(e -> handleEdit());
        view.addDeleteListener(e -> handleDelete());
        view.addIncreaseListener(e -> handleStock(1));
        view.addDecreaseListener(e -> handleStock(-1));
        view.addClearListener(e -> view.clearForm());
        view.addFilterChangeListener(this::handleFilterChange);

        view.update(model);
    }

    public void handleAdd() {
        try {
            String name = view.getNameInput();
            String category = view.getCategoryInput();
            long price = parsePrice(view.getPriceInput());
            int quantity = parseQuantity(view.getQuantityInput());
            Product added = model.addProduct(name, category, price, quantity);
            view.clearForm();
            view.selectProduct(added.getId());
            view.showMessage("Product '" + added.getName() + "' was added.");
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }

    public void handleEdit() {
        Integer id = view.getSelectedProductId();
        if (id == null) {
            view.showError("Please select a product from the table first.");
            return;
        }
        try {
            long price = parsePrice(view.getPriceInput());
            model.editProduct(id, view.getNameInput(), view.getCategoryInput(), price);
            view.showMessage("Product was updated.");
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }

    public void handleDelete() {
        Integer id = view.getSelectedProductId();
        if (id == null) {
            view.showError("Please select a product from the table first.");
            return;
        }
        try {
            Product old = model.getProduct(id);
            model.deleteProduct(id);
            view.clearForm();
            view.showMessage("Product '" + (old == null ? id : old.getName()) + "' was deleted.");
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }

    public void handleStock(int direction) {
        Integer id = view.getSelectedProductId();
        if (id == null) {
            view.showError("Please select a product from the table first.");
            return;
        }
        try {
            int delta = direction * view.getStockAmount();
            model.changeStock(id, delta);
            Product now = model.getProduct(id);
            view.showMessage("Stock of '" + now.getName() + "' is now " + now.getQuantity() + ".");
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }

    public void handleFilterChange() {
        view.update(model);
    }


    static long parsePrice(String text) {
        BigDecimal value;
        try {
            value = new BigDecimal(text.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Price must be a number, for example 12.50");
        }
        if (value.scale() > 2) {
            throw new IllegalArgumentException("Price can have only 2 digits after the dot");
        }
        try {
            return value.movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Price is too big");
        }
    }

    static int parseQuantity(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Quantity must be a whole number, for example 10");
        }
    }
}
