//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class DeliveryPoint extends JFrame {
    private JTextArea deliveryTextArea;
    private JComboBox<String> algorithmCombo;
    private JTextField capacityInput;
    private JTextField distanceInput;
    private JButton optimizeButton;
    private JPanel routePanel;
    private List<DeliveryStop> stops;

    public DeliveryPoint() {
        this.setTitle("Delivery Route Optimization");
        this.setExtendedState(6);
        this.setDefaultCloseOperation(3);
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(Color.decode("#F8BBD0"));
        this.stops = new ArrayList();
        this.createDeliveryListPanel();
        this.createControlPanel();
        this.createRoutePanel();
        this.optimizeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DeliveryPoint.this.optimizeDeliveryRoute();
            }
        });
    }

    private void createDeliveryListPanel() {
        JPanel deliveryListPanel = new JPanel(new BorderLayout());
        this.deliveryTextArea = new JTextArea(10, 30);
        this.deliveryTextArea.setLineWrap(true);
        this.deliveryTextArea.setBackground(Color.WHITE);
        this.deliveryTextArea.setForeground(Color.BLACK);
        this.deliveryTextArea.setFont(new Font("Arial", 0, 16));
        JScrollPane scrollPane = new JScrollPane(this.deliveryTextArea);
        deliveryListPanel.add(new JLabel("Delivery Stops:"), "North");
        deliveryListPanel.add(scrollPane, "Center");
        JButton addStopButton = new JButton("Add Delivery Stop");
        addStopButton.setFont(new Font("Arial", 1, 16));
        addStopButton.setBackground(Color.decode("#FF80AB"));
        addStopButton.setForeground(Color.WHITE);
        addStopButton.setFocusPainted(false);
        addStopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DeliveryPoint.this.addDeliveryStop();
            }
        });
        deliveryListPanel.add(addStopButton, "South");
        deliveryListPanel.setBackground(Color.decode("#F8BBD0"));
        this.add(deliveryListPanel, "West");
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(5, 2));
        controlPanel.setBackground(Color.decode("#D81B60"));
        String[] algorithms = new String[]{"Nearest Neighbor", "Genetic Algorithm", "Simulated Annealing"};
        this.algorithmCombo = new JComboBox(algorithms);
        this.algorithmCombo.setFont(new Font("Arial", 0, 16));
        this.capacityInput = new JTextField("10");
        this.distanceInput = new JTextField("100");
        this.optimizeButton = new JButton("Optimize Route");
        this.optimizeButton.setFont(new Font("Arial", 1, 16));
        this.optimizeButton.setBackground(Color.decode("#FF80AB"));
        this.optimizeButton.setForeground(Color.WHITE);
        this.optimizeButton.setFocusPainted(false);
        controlPanel.add(new JLabel("Choose Algorithm:", 4));
        controlPanel.add(this.algorithmCombo);
        controlPanel.add(new JLabel("Vehicle Capacity:", 4));
        controlPanel.add(this.capacityInput);
        controlPanel.add(new JLabel("Max Distance:", 4));
        controlPanel.add(this.distanceInput);
        controlPanel.add(new JLabel(""));
        controlPanel.add(this.optimizeButton);
        this.add(controlPanel, "South");
    }

    private void createRoutePanel() {
        this.routePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                DeliveryPoint.this.drawDeliveryRoute(g);
            }
        };
        this.routePanel.setPreferredSize(new Dimension(400, 400));
        this.routePanel.setBackground(Color.WHITE);
        this.add(this.routePanel, "Center");
    }

    private void addDeliveryStop() {
        String address = JOptionPane.showInputDialog(this, "Enter delivery address:");
        if (address != null && !address.trim().isEmpty()) {
            int priority = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter priority (1-10):"));
            DeliveryStop newStop = new DeliveryStop(address, priority);
            this.stops.add(newStop);
            this.updateDeliveryList();
        }

    }

    private void updateDeliveryList() {
        StringBuilder sb = new StringBuilder();
        Iterator var2 = this.stops.iterator();

        while(var2.hasNext()) {
            DeliveryStop stop = (DeliveryStop)var2.next();
            sb.append(stop.toString()).append("\n");
        }

        this.deliveryTextArea.setText(sb.toString());
    }

    private void optimizeDeliveryRoute() {
        String selectedAlgorithm = (String)this.algorithmCombo.getSelectedItem();
        int vehicleCapacity = Integer.parseInt(this.capacityInput.getText());
        int maxDistance = Integer.parseInt(this.distanceInput.getText());
        JOptionPane.showMessageDialog(this, "Optimizing route using " + selectedAlgorithm + "\nVehicle Capacity: " + vehicleCapacity + "\nMax Distance: " + maxDistance);
        this.routePanel.repaint();
    }

    private void drawDeliveryRoute(Graphics g) {
        g.setColor(Color.BLUE);

        for(int i = 0; i < this.stops.size(); ++i) {
            int x = (int)(Math.random() * (double)this.routePanel.getWidth());
            int y = (int)(Math.random() * (double)this.routePanel.getHeight());
            g.fillOval(x, y, 15, 15);
            g.drawString("Stop " + (i + 1), x + 20, y + 20);
            if (i > 0) {
                int prevX = (int)(Math.random() * (double)this.routePanel.getWidth());
                int prevY = (int)(Math.random() * (double)this.routePanel.getHeight());
                g.drawLine(prevX, prevY, x, y);
            }
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                (new DeliveryPoint()).setVisible(true);
            }
        });
    }

    private class DeliveryStop {
        String address;
        int priority;

        DeliveryStop(String address, int priority) {
            this.address = address;
            this.priority = priority;
        }

        public String toString() {
            return this.address + " (Priority: " + this.priority + ")";
        }
    }
}
