package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.InventoryModel;
import model.InventoryObserver;
import model.Product;

/**
 * The View of the application. It shows the inventory in a table and it has
 * the form and the buttons. It is the "Observer": when the model is changed,
 * the model call update() and the view draw the table again.
 *
 * It is a JPanel, so the window (JFrame) is created in Main. The view does not change the model. It only gives the user input to the
 * controller (with the listeners) and it shows what the model has.
 */
public class InventoryView extends JPanel implements InventoryObserver {

    private static final String[] COLUMNS = {"ID", "Name", "Category", "Price", "Quantity", "Status"};
    private static final Color LOW_STOCK_COLOR = new Color(255, 214, 214);
    private static final Color SELECTED_COLOR = new Color(173, 204, 240);
    private static final Color ERROR_COLOR = new Color(176, 0, 32);
    private static final Color OK_COLOR = new Color(0, 110, 40);

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // user can not edit the table directly
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField searchField = new JTextField(18);
    private final JCheckBox lowStockOnly = new JCheckBox("Show low stock only");

    private final JTextField nameField = new JTextField(16);
    private final JTextField categoryField = new JTextField(16);
    private final JTextField priceField = new JTextField(16);
    private final JTextField quantityField = new JTextField(16);
    private final JSpinner stockAmount = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

    private final JButton addButton = new JButton("Add Product");
    private final JButton editButton = new JButton("Edit Selected");
    private final JButton deleteButton = new JButton("Delete Selected");
    private final JButton increaseButton = new JButton("+ Add Stock");
    private final JButton decreaseButton = new JButton("- Remove Stock");
    private final JButton clearButton = new JButton("Clear Form");

    private final JLabel statusLabel = new JLabel(" ");

    // true when we are drawing the table, so selection listener do not fill the form
    private boolean refreshing = false;

    public InventoryView() {
        super(new BorderLayout(10, 10));
        buildLayout();
        setupTable();
        setPreferredSize(new Dimension(960, 620));
    }

    // ---------- Build the screen ----------

    private void buildLayout() {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top: search
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.add(new JLabel("Search (name or category):"));
        top.add(searchField);
        top.add(lowStockOnly);
        add(top, BorderLayout.NORTH);

        // center: table
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setPreferredSize(new Dimension(560, 440));
        add(scroll, BorderLayout.CENTER);

        // right: form
        add(buildFormPanel(), BorderLayout.EAST);

        // bottom: status
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 2));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Product Details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, c, 0, "Name:", nameField);
        addRow(form, c, 1, "Category:", categoryField);
        addRow(form, c, 2, "Price:", priceField);
        addRow(form, c, 3, "Start quantity:", quantityField);

        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 4;
        form.add(addButton, c);
        c.gridy = 5;
        form.add(editButton, c);
        c.gridy = 6;
        form.add(deleteButton, c);
        c.gridy = 7;
        form.add(clearButton, c);

        JPanel stockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        stockPanel.setBorder(BorderFactory.createTitledBorder("Update Stock"));
        stockPanel.add(new JLabel("Amount:"));
        stockPanel.add(stockAmount);
        c.gridy = 8;
        form.add(stockPanel, c);
        c.gridy = 9;
        form.add(increaseButton, c);
        c.gridy = 10;
        form.add(decreaseButton, c);

        // little hint under the form
        JLabel hint = new JLabel("<html><i>Select a row to edit it.<br>Low stock = less than "
                + InventoryModel.LOW_STOCK_LEVEL + " pieces.</i></html>");
        c.gridy = 11;
        form.add(hint, c);

        return form;
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, Component field) {
        c.gridy = row;
        c.gridwidth = 1;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }

    private void setupTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(5).setMaxWidth(90);

        // color the rows of low stock products
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, column);
                boolean low = "Low stock".equals(t.getValueAt(row, 5));
                if (isSelected) {
                    comp.setBackground(SELECTED_COLOR);
                } else {
                    comp.setBackground(low ? LOW_STOCK_COLOR : Color.WHITE);
                }
                comp.setForeground(Color.BLACK);
                return comp;
            }
        });

        // when the user select a row, show its details in the form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || refreshing) {
                return;
            }
            int row = table.getSelectedRow();
            if (row >= 0) {
                nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
                categoryField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
                priceField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
                quantityField.setText("");
            }
        });
    }

    // ---------- Observer: the model call this when data is changed ----------

    @Override
    public void update(InventoryModel model) {
        Integer selectedId = getSelectedProductId();
        List<Product> found = model.search(searchField.getText());
        Set<Product> low = model.getLowStockProducts();

        refreshing = true;
        try {
            tableModel.setRowCount(0);
            for (Product p : found) {
                boolean isLow = low.contains(p);
                if (lowStockOnly.isSelected() && !isLow) {
                    continue;
                }
                tableModel.addRow(new Object[] {
                    p.getId(), p.getName(), p.getCategory(), p.getPriceText(),
                    p.getQuantity(), isLow ? "Low stock" : "OK"
                });
            }
            // keep the same selected row if it is still there
            if (selectedId != null) {
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    if (selectedId.equals(tableModel.getValueAt(row, 0))) {
                        table.setRowSelectionInterval(row, row);
                        break;
                    }
                }
            }
        } finally {
            refreshing = false;
        }
    }

    // ---------- Read the user input (the controller use these) ----------

    public String getNameInput() {
        return nameField.getText();
    }

    public String getCategoryInput() {
        return categoryField.getText();
    }

    public String getPriceInput() {
        return priceField.getText();
    }

    public String getQuantityInput() {
        return quantityField.getText();
    }

    public int getStockAmount() {
        return (Integer) stockAmount.getValue();
    }

    /** @return id of the selected product, or null if no row is selected */
    public Integer getSelectedProductId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (Integer) tableModel.getValueAt(row, 0);
    }

    // ---------- Change what the user see ----------

    public void setFormValues(String name, String category, String price, String quantity) {
        nameField.setText(name);
        categoryField.setText(category);
        priceField.setText(price);
        quantityField.setText(quantity);
    }

    public void setStockAmount(int amount) {
        stockAmount.setValue(amount);
    }

    public void setSearchText(String text) {
        searchField.setText(text);
    }

    public void setLowStockOnly(boolean value) {
        lowStockOnly.setSelected(value);
    }

    public void selectProduct(int id) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (Integer.valueOf(id).equals(tableModel.getValueAt(row, 0))) {
                table.setRowSelectionInterval(row, row);
                return;
            }
        }
    }

    public void clearForm() {
        setFormValues("", "", "", "");
        table.clearSelection();
    }

    public void showMessage(String text) {
        statusLabel.setForeground(OK_COLOR);
        statusLabel.setText(text);
    }

    public void showError(String text) {
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setText(text);
    }

    // ---------- Listeners (the controller add its handlers here) ----------

    public void addAddListener(ActionListener l) {
        addButton.addActionListener(l);
    }

    public void addEditListener(ActionListener l) {
        editButton.addActionListener(l);
    }

    public void addDeleteListener(ActionListener l) {
        deleteButton.addActionListener(l);
    }

    public void addIncreaseListener(ActionListener l) {
        increaseButton.addActionListener(l);
    }

    public void addDecreaseListener(ActionListener l) {
        decreaseButton.addActionListener(l);
    }

    public void addClearListener(ActionListener l) {
        clearButton.addActionListener(l);
    }

    /** The task is called when the search text or the low stock check box is changed. */
    public void addFilterChangeListener(Runnable task) {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                task.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                task.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                task.run();
            }
        });
        lowStockOnly.addActionListener(e -> task.run());
    }
}
