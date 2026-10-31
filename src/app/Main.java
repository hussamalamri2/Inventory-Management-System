package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controller.InventoryController;
import model.InventoryModel;
import view.InventoryView;

/** Start point of the application. It creates the Model, the View and the Controller. */
public class Main {

    /** Put some sample products so the user can see something when the app open. */
    public static void addSampleData(InventoryModel model) {
        model.addProduct("Rice 5kg", "Grocery", 2450, 40);
        model.addProduct("Fresh Milk 1L", "Dairy", 675, 3);
        model.addProduct("Notebook A4", "Stationery", 300, 120);
        model.addProduct("Blue Pen", "Stationery", 150, 4);
        model.addProduct("Dish Soap", "Cleaning", 999, 15);
        model.addProduct("Tea Bags", "Grocery", 1200, 2);
        model.addProduct("Yogurt Cup", "Dairy", 225, 30);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                // we can use the default look, no problem
            }
            InventoryModel model = new InventoryModel();
            addSampleData(model);
            InventoryView view = new InventoryView();
            new InventoryController(model, view);

            JFrame frame = new JFrame("Inventory Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(view);
            frame.pack();
            frame.setMinimumSize(frame.getSize());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
