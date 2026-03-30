package databases;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BookBorrow extends JFrame {

    // ===== DB SETTINGS =====
    private static final String DB_URL = "jdbc:mysql://10.1.12.1:3306/bookdb";
    private static final String DB_USER = "pvs";
    private static final String DB_PASSWORD = "infis";

    // ===== GUI COMPONENTS =====
    private JTextField nickField;
    private JButton loginButton;
    private JLabel currentUserLabel;

    private JTable bookTable;
    private DefaultTableModel tableModel;

    private JTextField idField;
    private JTextField titleField;
    private JTextField authorField;
    private JTextField yearField;
    private JTextField borrowedByField;

    private JButton borrowButton;
    private JButton refreshButton;

    private JLabel statusLabel;

    // ===== APP STATE =====
    private String currentNick = null;

    public BookBorrow() {
        super("Book Borrowing - Skeleton");
        initGui();
        refreshBooks();
    }

    private void initGui() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // ===== TOP PANEL =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topPanel.add(new JLabel("Nickname:"));
        nickField = new JTextField(12);
        topPanel.add(nickField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        topPanel.add(loginButton);

        currentUserLabel = new JLabel("Logged in as: not logged in");
        topPanel.add(Box.createHorizontalStrut(15));
        topPanel.add(currentUserLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ===== TABLE =====
        String[] columns = {"ID", "Title", "Author", "Year", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getSelectionModel().addListSelectionListener(this::onRowSelected);

        JScrollPane tableScrollPane = new JScrollPane(bookTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Books"));

        // ===== DETAIL PANEL =====
        JPanel detailPanel = new JPanel(new BorderLayout(8, 8));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Book detail"));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        formPanel.add(new JLabel("ID:"));
        idField = createReadOnlyField();
        formPanel.add(idField);

        formPanel.add(new JLabel("Title:"));
        titleField = createReadOnlyField();
        formPanel.add(titleField);

        formPanel.add(new JLabel("Author:"));
        authorField = createReadOnlyField();
        formPanel.add(authorField);

        formPanel.add(new JLabel("Year:"));
        yearField = createReadOnlyField();
        formPanel.add(yearField);

        formPanel.add(new JLabel("Borrowed by:"));
        borrowedByField = createReadOnlyField();
        formPanel.add(borrowedByField);

        detailPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        borrowButton = new JButton("Borrow selected");
        borrowButton.addActionListener(e -> borrowSelectedBook());
        buttonPanel.add(borrowButton);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshBooks());
        buttonPanel.add(refreshButton);

        detailPanel.add(buttonPanel, BorderLayout.SOUTH);

        // ===== SPLIT PANE =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, detailPanel);
        splitPane.setResizeWeight(0.65);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // ===== STATUS BAR =====
        statusLabel = new JLabel("Ready.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
    }

    private JTextField createReadOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setBackground(new Color(245, 245, 245));
        return field;
    }

    private void login() {
        String nick = nickField.getText().trim();

        if (nick.isEmpty()) {
            setStatus("Please enter a nickname.");
            JOptionPane.showMessageDialog(this, "Please enter a nickname.");
            return;
        }

        currentNick = nick;
        currentUserLabel.setText("Logged in as: " + currentNick);
        setStatus("Logged in as " + currentNick + ".");
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void refreshBooks() {
        tableModel.setRowCount(0);

        String sql = """
                SELECT id, title, author, year, borrowed_by
                FROM book
                ORDER BY title
                """;
        try (Connection conn = createConnection();
             PreparedStatement ps = conn.prepareCall(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String borrowedBy = rs.getString("borrowed_by");
                String status = (borrowedBy == null || borrowedBy.isBlank()) ? "Free" : "Borrowed";

                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("year"),
                        status
                });

            }
            setStatus("Loaded: " + tableModel.getRowCount() + " books");

        } catch (SQLException e) {
            System.out.println("Chyba pri sql: " + e.getMessage());
        }
    }

    private void onRowSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            clearDetailFields();
            return;
        }

        Object value = tableModel.getValueAt(selectedRow, 0);
        if (value == null) return;

        int bookId = (int) value;
        loadBookDetail(bookId);
    }

    private void loadBookDetail(int bookId) {
        String sql = """
                SELECT id, title, author, year, borrowed_by
                FROM book
                WHERE id = ?
                """;
        try (Connection conn = createConnection();
             PreparedStatement ps = conn.prepareCall(sql)) {
            ps.setInt(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idField.setText(String.valueOf(rs.getInt("id")));
                    titleField.setText(rs.getString("title"));
                    authorField.setText(rs.getString("author"));
                    yearField.setText(String.valueOf(rs.getInt("year")));

                    String borrowedBy = rs.getString("borrowed_by");
                    borrowedByField.setText((borrowedBy == null) ? "" : borrowedBy);

                    setStatus("Selected: " + rs.getString("title"));
                } else {
                    clearDetailFields();
                    setStatus("Book not found!");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL error: " + e.getMessage());
        }
    }

    private void borrowSelectedBook() {
        if (currentNick == null || currentNick.isBlank()) {
            setStatus("Please log in first.");
            JOptionPane.showMessageDialog(this, "Please log in first.");
            return;
        }

        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            setStatus("Please select a book first.");
            JOptionPane.showMessageDialog(this, "Please select a book first.");
            return;
        }

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);

        String sql = """
                UPDATE book
                SET borrowed_by = ?
                WHERE id = ? AND borrowed_by IS NULL
                """;

        try (Connection conn = createConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentNick);
            ps.setInt(2, bookId);

            int success = ps.executeUpdate();

            if (success == 1){
                setStatus("Book borrowed successfully.");
                refreshBooks();
                JOptionPane.showMessageDialog(this, "Book borrowed successfully!");
            } else {
                setStatus("Book already borrowed");
                refreshBooks();
                JOptionPane.showMessageDialog(this, "Book already borrowed!");
            }
        } catch (SQLException e) {
            System.out.println("SQL error: " + e.getMessage());
        }
    }

    private void clearDetailFields() {
        idField.setText("");
        titleField.setText("");
        authorField.setText("");
        yearField.setText("");
        borrowedByField.setText("");
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    public static void main(String[] args) {
        new BookBorrow().setVisible(true);
    }
}
