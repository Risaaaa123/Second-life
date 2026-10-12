import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class SecondLife extends JFrame {

    // Colour theme
    private final Color NAVY = new Color(29, 53, 87);
    private final Color RED = new Color(230, 57, 70);
    private final Color MINT = new Color(241, 250, 238);
    private final Color BLUE = new Color(16, 218, 220);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(45, 55, 72);
    private final Color GREEN = new Color(46, 139, 87);
    private final Color ORANGE = new Color(255, 159, 67);
    private final Color PURPLE = new Color(112, 87, 191);

    private final String DATA_FILE = "secondlife_data.dat";
    private final String PHOTO_FOLDER = "secondlife_photos";

    private ArrayList<Listing> listings = new ArrayList<>();

    private DefaultListModel<Listing> listModel =
            new DefaultListModel<>();

    private JList<Listing> listingList =
            new JList<>(listModel);

    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> typeFilter;

    private JLabel totalLabel;
    private JLabel donationLabel;
    private JLabel petLabel;
    private JLabel imagePreview;

    private JPanel detailsPanel;
    private CardLayout detailsLayout;

    private Listing selectedListing;

    public SecondLife() {

        setTitle("SecondLife | Community Sharing Platform");
        setSize(1200, 760);
        setMinimumSize(new Dimension(950, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadData();

        createInterface();

        refreshListings();
    }

    // ================= MAIN INTERFACE =================

    private void createInterface() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MINT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel logoPanel = new JPanel(new GridLayout(2, 1));
        logoPanel.setBackground(NAVY);

        JLabel title = new JLabel("🌍 SecondLife");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Share More  •  Waste Less  •  Help Others"
        );

        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(BLUE);

        logoPanel.add(title);
        logoPanel.add(subtitle);

        JButton addButton = createButton(
                "＋ Add New Listing", RED
        );

        addButton.addActionListener(e -> addListing());

        header.add(logoPanel, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        // Dashboard statistics
        JPanel dashboard = new JPanel(
                new GridLayout(1, 3, 15, 15)
        );

        dashboard.setBackground(MINT);
        dashboard.setBorder(new EmptyBorder(20, 20, 10, 20));

        totalLabel = new JLabel("0", SwingConstants.CENTER);
        donationLabel = new JLabel("0", SwingConstants.CENTER);
        petLabel = new JLabel("0", SwingConstants.CENTER);

        dashboard.add(createStatCard(
                "📦 Total Listings", totalLabel, NAVY
        ));

        dashboard.add(createStatCard(
                "🎁 Donations", donationLabel, GREEN
        ));

        dashboard.add(createStatCard(
                "🐶 Pet Listings", petLabel, PURPLE
        ));

        // Search and filters
        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10)
        );

        searchPanel.setBackground(WHITE);
        searchPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        searchField = new JTextField(18);

        searchField.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        searchField.setToolTipText(
                "Search by item, district or place"
        );

        categoryFilter = new JComboBox<>(new String[]{
                "All Categories",
                "Furniture",
                "Food",
                "Clothing",
                "Books",
                "Electronics",
                "Household",
                "Other"
        });

        typeFilter = new JComboBox<>(new String[]{
                "All Types",
                "Donation",
                "Exchange",
                "Pet Adoption",
                "Pet Foster"
        });

        JButton searchButton = createButton(
                "🔍 Search", NAVY
        );

        JButton resetButton = createButton(
                "↻ Reset", ORANGE
        );

        searchButton.addActionListener(e -> searchListings());

        searchField.addActionListener(e -> searchListings());

        categoryFilter.addActionListener(e -> searchListings());

        typeFilter.addActionListener(e -> searchListings());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            typeFilter.setSelectedIndex(0);
            refreshListings();
        });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(categoryFilter);
        searchPanel.add(typeFilter);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);

        // Listing display
        listingList.setCellRenderer(new ListingRenderer());
        listingList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        listingList.setBackground(MINT);
        listingList.setFixedCellHeight(100);

        listingList.addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                selectedListing = listingList.getSelectedValue();

                showListingDetails(selectedListing);
            }
        });

        JScrollPane listScroll = new JScrollPane(listingList);

        listScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BLUE),
                "💚 Community Listings"
        ));

        listScroll.getViewport().setBackground(MINT);

        // Left section
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(MINT);
        leftPanel.setBorder(new EmptyBorder(0, 20, 20, 10));

        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(listScroll, BorderLayout.CENTER);

        // Right details section
        detailsLayout = new CardLayout();
        detailsPanel = new JPanel(detailsLayout);

        detailsPanel.add(
                createWelcomePanel(),
                "WELCOME"
        );

        detailsPanel.add(
                createEmptyDetailsPanel(),
                "DETAILS"
        );

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setBackground(WHITE);
        rightContainer.setBorder(new EmptyBorder(0, 5, 20, 20));

        rightContainer.add(detailsPanel, BorderLayout.CENTER);

        // Main content
        JPanel content = new JPanel(new GridLayout(1, 2, 10, 10));
        content.setBackground(MINT);

        content.add(leftPanel);
        content.add(rightContainer);

        JPanel topContent = new JPanel(new BorderLayout());
        topContent.setBackground(MINT);

        topContent.add(dashboard, BorderLayout.NORTH);

        root.add(header, BorderLayout.NORTH);
        root.add(topContent, BorderLayout.CENTER);

        // Dashboard and listing content
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(MINT);

        body.add(dashboard, BorderLayout.NORTH);
        body.add(content, BorderLayout.CENTER);

        root.remove(topContent);
        root.add(body, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel(
                "   SecondLife ©️ 2026  |  Give items a second life 💚",
                SwingConstants.CENTER
        );

        footer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.setForeground(NAVY);
        footer.setBorder(new EmptyBorder(10, 5, 10, 5));
        footer.setOpaque(true);
        footer.setBackground(WHITE);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ================= DASHBOARD CARDS =================

    private JPanel createStatCard(
            String heading,
            JLabel numberLabel,
            Color color
    ) {

        JPanel card = new JPanel(new GridLayout(2, 1, 5, 5));

        card.setBackground(WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 2, true),
                new EmptyBorder(12, 10, 12, 10)
        ));

        JLabel headingLabel = new JLabel(
                heading,
                SwingConstants.CENTER
        );

        headingLabel.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );

        headingLabel.setForeground(color);

        numberLabel.setFont(
                new Font("SansSerif", Font.BOLD, 28)
        );

        numberLabel.setForeground(TEXT);

        card.add(headingLabel);
        card.add(numberLabel);

        return card;
    }

    // ================= BUTTON DESIGN =================

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setBackground(color);
        button.setForeground(WHITE);

        button.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        button.setFocusPainted(false);
        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        return button;
    }

    // ================= WELCOME PANEL =================

    private JPanel createWelcomePanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLUE, 2, true),
                new EmptyBorder(25, 20, 25, 20)
        ));

        JLabel welcome = new JLabel(
                "<html><center>"
                        + "<font size='6' color='#1d3557'>"
                        + "Welcome to SecondLife!"
                        + "</font><br><br>"
                        + "<font size='5'>🌍 🎁 🐶 📚</font><br><br>"
                        + "<font color='#457b9d'>"
                        + "Donate unused items.<br>"
                        + "Exchange useful things.<br>"
                        + "Help animals find a home."
                        + "</font><br><br>"
                        + "<b>Choose a listing to see details.</b>"
                        + "</center></html>",
                SwingConstants.CENTER
        );

        welcome.setFont(
                new Font("SansSerif", Font.PLAIN, 17)
        );

        panel.add(welcome, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEmptyDetailsPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(WHITE);

        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        imagePreview = new JLabel(
                "📷 Select a listing",
                SwingConstants.CENTER
        );

        imagePreview.setPreferredSize(
                new Dimension(300, 220)
        );

        imagePreview.setBorder(
                new LineBorder(BLUE, 2, true)
        );

        imagePreview.setOpaque(true);
        imagePreview.setBackground(MINT);

        panel.add(imagePreview, BorderLayout.NORTH);

        return panel;
    }

    // ================= ADD LISTING =================

    private void addListing() {

        JTextField titleField = new JTextField();
        JTextField districtField = new JTextField();
        JTextField placeField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField photoField = new JTextField();

        JTextArea descriptionArea = new JTextArea(4, 20);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JComboBox<String> categoryBox = new JComboBox<>(
                new String[]{
                        "Furniture",
                        "Food",
                        "Clothing",
                        "Books",
                        "Electronics",
                        "Household",
                        "Pets",
                        "Other"
                }
        );

        JComboBox<String> typeBox = new JComboBox<>(
                new String[]{
                        "Donation",
                        "Exchange",
                        "Pet Adoption",
                        "Pet Foster"
                }
        );

        JComboBox<String> conditionBox = new JComboBox<>(
                new String[]{
                        "New",
                        "Good",
                        "Used",
                        "Needs Repair",
                        "Not Applicable"
                }
        );

        JButton browseButton = createButton(
                "📷 Choose Photo", PURPLE
        );

        browseButton.addActionListener(e -> {

            JFileChooser chooser = new JFileChooser();

            chooser.setDialogTitle("Select an item photo");

            int result = chooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {

                File selectedFile = chooser.getSelectedFile();

                photoField.setText(
                        selectedFile.getAbsolutePath()
                );
            }
        });

        JPanel photoPanel = new JPanel(new BorderLayout(5, 5));

        photoPanel.add(photoField, BorderLayout.CENTER);
        photoPanel.add(browseButton, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout());

        form.setBackground(WHITE);

        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;

        addFormRow(form, gbc, row++, "Listing Title:", titleField);
        addFormRow(form, gbc, row++, "Category:", categoryBox);
        addFormRow(form, gbc, row++, "Listing Type:", typeBox);
        addFormRow(form, gbc, row++, "Condition:", conditionBox);
        addFormRow(form, gbc, row++, "District:", districtField);
        addFormRow(form, gbc, row++, "Place / Town:", placeField);
        addFormRow(form, gbc, row++, "Contact:", contactField);
        addFormRow(form, gbc, row++, "Photo:", photoPanel);
        addFormRow(
                form,
                gbc,
                row,
                "Description:",
                new JScrollPane(descriptionArea)
        );

        JScrollPane formScroll = new JScrollPane(form);

        formScroll.setPreferredSize(new Dimension(600, 470));

        int result = JOptionPane.showConfirmDialog(
                this,
                formScroll,
                "✨ Create a New Listing",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String title = titleField.getText().trim();
        String district = districtField.getText().trim();
        String place = placeField.getText().trim();
        String contact = contactField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isEmpty()
                || district.isEmpty()
                || contact.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter the title, district and contact.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String selectedType =
                (String) typeBox.getSelectedItem();

        String category =
                (String) categoryBox.getSelectedItem();

        String condition =
                (String) conditionBox.getSelectedItem();

        // Pets automatically use Pets category.
        if (selectedType.startsWith("Pet")) {
            category = "Pets";
        }

        String photoPath = "";

        if (!photoField.getText().trim().isEmpty()) {

            photoPath = copyPhotoToProject(
                    photoField.getText().trim()
            );

            if (photoPath == null) {
                return;
            }
        }

        Listing listing = new Listing(
                title,
                category,
                selectedType,
                condition,
                district,
                place,
                contact,
                description,
                photoPath
        );

        listings.add(0, listing);

        saveData();

        refreshListings();

        JOptionPane.showMessageDialog(
                this,
                "🎉 Listing added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            Component input
    ) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        JLabel text = new JLabel(label);

        text.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        panel.add(text, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(input, gbc);
    }

    // ================= PHOTO HANDLING =================

    private String copyPhotoToProject(String originalPath) {

        try {

            Path source = Paths.get(originalPath);

            if (!Files.exists(source)
                    || !Files.isRegularFile(source)) {

                JOptionPane.showMessageDialog(
                        this,
                        "The selected photo could not be found."
                );

                return null;
            }

            String fileName = source.getFileName()
                    .toString()
                    .toLowerCase();

            if (!(fileName.endsWith(".jpg")
                    || fileName.endsWith(".jpeg")
                    || fileName.endsWith(".png")
                    || fileName.endsWith(".gif")
                    || fileName.endsWith(".webp"))) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please choose a JPG, PNG, GIF or WEBP image."
                );

                return null;
            }

            Path folder = Paths.get(PHOTO_FOLDER);

            Files.createDirectories(folder);

            String extension = "";

            int dot = source.getFileName()
                    .toString()
                    .lastIndexOf('.');

            if (dot >= 0) {
                extension = source.getFileName()
                        .toString()
                        .substring(dot);
            }

            String newName = UUID.randomUUID() + extension;

            Path destination = folder.resolve(newName);

            Files.copy(
                    source,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return destination.toString();

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Photo could not be saved:\n" + ex.getMessage()
            );

            return null;
        }
    }

    // ================= LISTING DISPLAY =================

    private void refreshListings() {

        listModel.clear();

        for (Listing listing : listings) {
            listModel.addElement(listing);
        }

        updateStatistics();

        selectedListing = null;

        detailsLayout.show(detailsPanel, "WELCOME");
    }

    private void searchListings() {

        listModel.clear();

        String keyword = searchField.getText()
                .trim()
                .toLowerCase();

        String selectedCategory =
                (String) categoryFilter.getSelectedItem();

        String selectedType =
                (String) typeFilter.getSelectedItem();

        for (Listing listing : listings) {

            String searchable =
                    (listing.title + " "
                            + listing.category + " "
                            + listing.district + " "
                            + listing.place + " "
                            + listing.description)
                            .toLowerCase();

            boolean keywordMatch =
                    keyword.isEmpty()
                            || searchable.contains(keyword);

            boolean categoryMatch =
                    "All Categories".equals(selectedCategory)
                            || listing.category.equals(selectedCategory);

            boolean typeMatch =
                    "All Types".equals(selectedType)
                            || listing.type.equals(selectedType);

            if (keywordMatch
                    && categoryMatch
                    && typeMatch
                    && !listing.status.equals("COMPLETED")) {

                listModel.addElement(listing);
            }
        }

        selectedListing = null;

        detailsLayout.show(detailsPanel, "WELCOME");

        if (listModel.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No matching listings found.",
                    "Search Results",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // ================= LISTING DETAILS =================

    private void showListingDetails(Listing listing) {

        if (listing == null) {
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        panel.setBackground(WHITE);

        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel photo = new JLabel(
                "📷 No Photo Available",
                SwingConstants.CENTER
        );

        photo.setPreferredSize(new Dimension(350, 240));

        photo.setOpaque(true);
        photo.setBackground(MINT);

        photo.setBorder(new LineBorder(BLUE, 2, true));

        if (listing.photoPath != null
                && !listing.photoPath.isEmpty()) {

            File file = new File(listing.photoPath);

            if (file.exists()) {

                ImageIcon icon = loadScaledImage(
                        file,
                        350,
                        240
                );

                if (icon != null) {

                    photo.setIcon(icon);
                    photo.setText("");
                }
            }
        }

        JPanel information = new JPanel();

        information.setLayout(
                new BoxLayout(information, BoxLayout.Y_AXIS)
        );

        information.setBackground(WHITE);

        addDetailText(information, "📦 " + listing.title, 21, NAVY);

        addDetailText(
                information,
                "Category: " + listing.category,
                14,
                TEXT
        );

        addDetailText(
                information,
                "Type: " + listing.type,
                14,
                PURPLE
        );

        addDetailText(
                information,
                "Condition: " + listing.condition,
                14,
                TEXT
        );

        addDetailText(
                information,
                "📍 " + listing.place + ", " + listing.district,
                14,
                GREEN
        );

        addDetailText(
                information,
                "📞 Contact: " + listing.contact,
                14,
                RED
        );

        addDetailText(
                information,
                "Status: " + listing.status,
                14,
                ORANGE
        );

        addDetailText(
                information,
                "Description: " + listing.description,
                13,
                TEXT
        );

        JButton deleteButton = createButton(
                "🗑 Delete", RED
        );

        JButton completeButton = createButton(
                "✓ Mark Completed", GREEN
        );

        JButton statusButton = createButton(
                "↔️ Change Status", PURPLE
        );

        deleteButton.addActionListener(e -> deleteListing(listing));

        completeButton.addActionListener(e -> {

            listing.status = "COMPLETED";

            saveData();
            refreshListings();

            JOptionPane.showMessageDialog(
                    this,
                    "Listing marked as completed."
            );
        });

        statusButton.addActionListener(e -> changeStatus(listing));

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 5, 5)
        );

        buttons.setBackground(WHITE);

        buttons.add(statusButton);
        buttons.add(completeButton);
        buttons.add(deleteButton);

        panel.add(photo, BorderLayout.NORTH);
        panel.add(new JScrollPane(information), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        detailsPanel.add(panel, "DETAILS");

        detailsLayout.show(detailsPanel, "DETAILS");

        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void addDetailText(
            JPanel panel,
            String text,
            int size,
            Color color
    ) {

        JLabel label = new JLabel(
                "<html>" + escapeHtml(text) + "</html>"
        );

        label.setFont(
                new Font("SansSerif", Font.PLAIN, size)
        );

        label.setForeground(color);

        label.setBorder(new EmptyBorder(6, 4, 6, 4));

        panel.add(label);
    }

    private String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private ImageIcon loadScaledImage(
            File file,
            int width,
            int height
    ) {

        try {

            BufferedImage image = javax.imageio.ImageIO.read(file);

            if (image == null) {
                return null;
            }

            Image scaled = image.getScaledInstance(
                    width,
                    height,
                    Image.SCALE_SMOOTH
            );

            return new ImageIcon(scaled);

        } catch (IOException ex) {

            return null;
        }
    }

    // ================= CHANGE STATUS =================

    private void changeStatus(Listing listing) {

        String[] statuses = {
                "AVAILABLE",
                "RESERVED",
                "COMPLETED"
        };

        String result = (String) JOptionPane.showInputDialog(
                this,
                "Choose listing status:",
                "Update Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                listing.status
        );

        if (result != null) {

            listing.status = result;

            saveData();
            refreshListings();
        }
    }

    // ================= DELETE LISTING =================

    private void deleteListing(Listing listing) {

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this listing?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (answer == JOptionPane.YES_OPTION) {

            listings.remove(listing);

            saveData();
            refreshListings();

            JOptionPane.showMessageDialog(
                    this,
                    "Listing deleted successfully!"
            );
        }
    }

    // ================= STATISTICS =================

    private void updateStatistics() {

        int donations = 0;
        int pets = 0;

        for (Listing listing : listings) {

            if (listing.status.equals("COMPLETED")) {
                continue;
            }

            if (listing.type.equals("Donation")) {
                donations++;
            }

            if (listing.type.startsWith("Pet")) {
                pets++;
            }
        }

        totalLabel.setText(
                String.valueOf(listings.size())
        );

        donationLabel.setText(
                String.valueOf(donations)
        );

        petLabel.setText(
                String.valueOf(pets)
        );
    }

    // ================= SAVE DATA =================

    private void saveData() {

        try (ObjectOutputStream output =
                     new ObjectOutputStream(
                             new FileOutputStream(DATA_FILE))) {

            output.writeObject(listings);

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Data could not be saved:\n" + ex.getMessage()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {

        File file = new File(DATA_FILE);

        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(file))) {

            Object data = input.readObject();

            if (data instanceof ArrayList<?>) {

                listings = (ArrayList<Listing>) data;
            }

        } catch (IOException | ClassNotFoundException ex) {

            listings = new ArrayList<>();

            JOptionPane.showMessageDialog(
                    this,
                    "Saved data could not be loaded."
            );
        }
    }

    // ================= LISTING MODEL =================

    private static class Listing implements Serializable {

        private static final long serialVersionUID = 1L;

        String title;
        String category;
        String type;
        String condition;
        String district;
        String place;
        String contact;
        String description;
        String photoPath;
        String status;

        Listing(
                String title,
                String category,
                String type,
                String condition,
                String district,
                String place,
                String contact,
                String description,
                String photoPath
        ) {

            this.title = title;
            this.category = category;
            this.type = type;
            this.condition = condition;
            this.district = district;
            this.place = place;
            this.contact = contact;
            this.description = description;
            this.photoPath = photoPath;
            this.status = "AVAILABLE";
        }

        @Override
        public String toString() {

            return title
                    + " | " + type
                    + " | " + district;
        }
    }

    // ================= COLOURFUL LIST CARDS =================

    private class ListingRenderer
            extends JPanel
            implements ListCellRenderer<Listing> {

        private JLabel photoLabel;
        private JLabel titleLabel;
        private JLabel categoryLabel;
        private JLabel locationLabel;
        private JLabel typeLabel;

        ListingRenderer() {

            setLayout(new BorderLayout(12, 5));

            setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(5, 5, 5, 5),
                    new EmptyBorder(8, 8, 8, 8)
            ));

            photoLabel = new JLabel(
                    "📷",
                    SwingConstants.CENTER
            );

            photoLabel.setPreferredSize(
                    new Dimension(80, 75)
            );

            photoLabel.setOpaque(true);
            photoLabel.setBackground(BLUE);

            JPanel textPanel = new JPanel();

            textPanel.setLayout(
                    new BoxLayout(textPanel, BoxLayout.Y_AXIS)
            );

            textPanel.setOpaque(false);

            titleLabel = new JLabel();
            categoryLabel = new JLabel();
            locationLabel = new JLabel();
            typeLabel = new JLabel();

            titleLabel.setFont(
                    new Font("SansSerif", Font.BOLD, 15)
            );

            categoryLabel.setFont(
                    new Font("SansSerif", Font.PLAIN, 12)
            );

            locationLabel.setFont(
                    new Font("SansSerif", Font.PLAIN, 12)
            );

            typeLabel.setFont(
                    new Font("SansSerif", Font.BOLD, 12)
            );

            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(5));
            textPanel.add(categoryLabel);
            textPanel.add(locationLabel);
            textPanel.add(typeLabel);

            add(photoLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Listing> list,
                Listing listing,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {

            titleLabel.setText(listing.title);

            categoryLabel.setText(
                    "Category: " + listing.category
            );

            locationLabel.setText(
                    "📍 " + listing.district
                            + (listing.place.isEmpty()
                            ? ""
                            : ", " + listing.place)
            );

            typeLabel.setText(
                    listing.type + "  •  " + listing.status
            );

            if (listing.type.equals("Donation")) {
                typeLabel.setForeground(GREEN);
            } else if (listing.type.equals("Exchange")) {
                typeLabel.setForeground(ORANGE);
            } else {
                typeLabel.setForeground(PURPLE);
            }

            if (listing.photoPath != null
                    && !listing.photoPath.isEmpty()) {

                File file = new File(listing.photoPath);

                if (file.exists()) {

                    ImageIcon icon = loadScaledImage(
                            file,
                            80,
                            75
                    );

                    photoLabel.setIcon(icon);
                    photoLabel.setText("");

                } else {

                    photoLabel.setIcon(null);
                    photoLabel.setText("📷");
                }

            } else {

                photoLabel.setIcon(null);
                photoLabel.setText("📷");
            }

            if (isSelected) {

                setBackground(new Color(220, 240, 250));
                setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(NAVY, 2, true),
                        new EmptyBorder(8, 8, 8, 8)
                ));

            } else {

                setBackground(WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(225, 230, 235), 1, true),
                        new EmptyBorder(8, 8, 8, 8)
                ));
            }

            setOpaque(true);

            return this;
        }
    }

    // ================= MAIN METHOD =================

    public static void main(String[] args) {

        try {

            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );

        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {

            SecondLife app = new SecondLife();

            app.setVisible(true);
        });
    }
}
