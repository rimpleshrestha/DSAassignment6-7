import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PhotoResizerGUI {

    private final JFrame mainFrame;
    private final JProgressBar progressBarOverall;
    private final JTextArea textAreaStatus;
    private final JButton buttonStartConversion;
    private final JButton buttonCancelConversion;
    private final JFileChooser fileSelector;
    private File[] imageFilesSelected; // Array to hold selected image files
    private SwingWorker<Void, ImageConversionProgress> imageConversionWorker;
    private static final String OUTPUT_DIRECTORY = "converted_images/"; // Output directory for converted images

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                PhotoResizerGUI appWindow = new PhotoResizerGUI();
                appWindow.mainFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public PhotoResizerGUI() {
        mainFrame = new JFrame();
        mainFrame.setTitle("Image Converter");
        mainFrame.setBounds(100, 100, 800, 500); // Frame size
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        JPanel panelMain = new JPanel();
        panelMain.setBorder(new EmptyBorder(20, 20, 20, 20)); // Padding
        panelMain.setBackground(Color.BLACK); // Black background
        mainFrame.add(panelMain, BorderLayout.CENTER);
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));

        fileSelector = new JFileChooser();
        fileSelector.setMultiSelectionEnabled(true); // Allow multiple files selection
        fileSelector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"));

        // Button for selecting files
        JButton buttonSelectFiles = createStyledButton("Select Images");
        buttonSelectFiles.addActionListener(e -> selectFiles());
        panelMain.add(buttonSelectFiles);

        panelMain.add(Box.createRigidArea(new Dimension(0, 15))); // Space between buttons

        // Button for starting conversion
        buttonStartConversion = createStyledButton("Start Conversion");
        buttonStartConversion.addActionListener(e -> startConversion());
        panelMain.add(buttonStartConversion);

        panelMain.add(Box.createRigidArea(new Dimension(0, 15))); // Space between buttons

        // Button for canceling conversion
        buttonCancelConversion = createStyledButton("Cancel");
        buttonCancelConversion.setEnabled(false); // Initially disabled
        buttonCancelConversion.setForeground(Color.WHITE); // Ensure cancel button text is white
        buttonCancelConversion.addActionListener(e -> cancelConversion());
        panelMain.add(buttonCancelConversion);

        progressBarOverall = new JProgressBar(0, 100);
        progressBarOverall.setStringPainted(true);
        progressBarOverall.setForeground(new Color(0, 255, 0)); // Green progress
        progressBarOverall.setBackground(new Color(50, 50, 50)); // Dark background for the progress bar
        progressBarOverall.setPreferredSize(new Dimension(600, 30)); // Set preferred size (width, height)
        panelMain.add(new JLabel("Overall Progress:") {{
            setForeground(Color.WHITE); // White label text
        }});
        panelMain.add(progressBarOverall);

        textAreaStatus = new JTextArea();
        textAreaStatus.setEditable(false);
        textAreaStatus.setFont(new Font("Arial", Font.PLAIN, 14)); // Font size
        textAreaStatus.setForeground(Color.WHITE); // White text
        textAreaStatus.setBackground(Color.BLACK); // Black background
        panelMain.add(new JScrollPane(textAreaStatus));

        mainFrame.setVisible(true);
    }

    private JButton createStyledButton(String buttonText) {
        JButton styledButton = new JButton(buttonText) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // Rounded corners
                super.paintComponent(g);
            }
        };
        styledButton.setBackground(new Color(255, 105, 180)); // Pink color
        styledButton.setForeground(Color.WHITE); // White text
        styledButton.setFont(new Font("Arial", Font.BOLD, 16)); // Font size
        styledButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        styledButton.setFocusPainted(false);
        styledButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        styledButton.setOpaque(false); // Ensure transparency
        styledButton.setBorderPainted(false); // No border
        styledButton.setFocusable(false); // Remove focus ring
        return styledButton;
    }

    private void selectFiles() {
        int selectionResult = fileSelector.showOpenDialog(mainFrame);
        if (selectionResult == JFileChooser.APPROVE_OPTION) {
            imageFilesSelected = fileSelector.getSelectedFiles(); // Get selected image files
            textAreaStatus.append("Selected images:\n");
            for (File file : imageFilesSelected) {
                textAreaStatus.append(file.getAbsolutePath() + "\n"); // Append each selected file path
            }
        }
    }

    private void startConversion() {
        if (imageFilesSelected == null || imageFilesSelected.length == 0) {
            JOptionPane.showMessageDialog(mainFrame, "No images selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        buttonStartConversion.setEnabled(false);
        buttonCancelConversion.setEnabled(true);
        progressBarOverall.setValue(0);
        textAreaStatus.append("Starting conversion...\n");

        new File(OUTPUT_DIRECTORY).mkdirs(); // Create output directory if it doesn't exist

        imageConversionWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                int totalFilesCount = imageFilesSelected.length; // Total number of selected files
                for (int index = 0; index < totalFilesCount; index++) {
                    if (isCancelled()) {
                        return null; // Exit if cancelled
                    }

                    File currentFile = imageFilesSelected[index]; // Get the current file
                    publish(new ImageConversionProgress(currentFile.getName(), index + 1, totalFilesCount, "Processing"));

                    try {
                        // Load image
                        BufferedImage imageLoaded = ImageIO.read(currentFile);

                        // Convert image (resize to 500x500)
                        BufferedImage resizedImage = resizeImage(imageLoaded, 500, 500); // Resize to 500x500

                        // Save converted image
                        File outputFile = new File(OUTPUT_DIRECTORY + currentFile.getName());
                        ImageIO.write(resizedImage, "png", outputFile); // Save as PNG

                        publish(new ImageConversionProgress(currentFile.getName(), index + 1, totalFilesCount, "Completed"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        publish(new ImageConversionProgress(currentFile.getName(), index + 1, totalFilesCount, "Error"));
                    }

                    // Update overall progress bar after processing each file
                    int progressPercentage = (int) ((index + 1) * 100 / totalFilesCount);
                    setProgress(progressPercentage);
                }
                return null;
            }

            @Override
            protected void process(List<ImageConversionProgress> chunks) {
                for (ImageConversionProgress progress : chunks) {
                    textAreaStatus.append(String.format("Image: %s, %s\n", progress.fileName, progress.status));
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Ensure that any exception during processing is thrown
                    textAreaStatus.append("All conversions completed.\n");
                    progressBarOverall.setValue(100); // Set progress bar to 100% after completion
                } catch (InterruptedException | ExecutionException e) {
                    textAreaStatus.append("Conversion interrupted or failed.\n");
                } finally {
                    buttonStartConversion.setEnabled(true);
                    buttonCancelConversion.setEnabled(false);
                }
            }
        };

        imageConversionWorker.execute(); // Start the worker
    }

    private void cancelConversion() {
        if (imageConversionWorker != null) {
            imageConversionWorker.cancel(true); // Cancel the worker
            textAreaStatus.append("Conversion cancelled.\n");
        }
        buttonStartConversion.setEnabled(true);
        buttonCancelConversion.setEnabled(false);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image temporaryImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(temporaryImage, 0, 0, null); // Draw the resized image
        graphics2D.dispose(); // Clean up graphics context
        return resizedImage;
    }

    private static class ImageConversionProgress {
        String fileName; // Name of the current file being processed
        int currentFile; // Index of the current file
        int totalFiles; // Total number of files
        String status; // Status of the current conversion

        ImageConversionProgress(String fileName, int currentFile, int totalFiles, String status) {
            this.fileName = fileName;
            this.currentFile = currentFile;
            this.totalFiles = totalFiles;
            this.status = status;
        }
    }
}
