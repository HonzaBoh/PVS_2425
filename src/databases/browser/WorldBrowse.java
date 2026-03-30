package databases.browser;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class WorldBrowse extends JFrame {

    MyText IDText, countryText, populationText, nameText;
    Operation currentOperation;
    MyButton saveButton, cancelButton, updateButton;
    MyButton firstButton, nextButton, prevButton, lastButton;
    MyButton addButton, deleteButton;
    static ResultSet set;

    private static final String USER = "pvs";
    private static final String PASSWORD = "infis";
    private static final String DB_URL = "jdbc:mysql://10.1.12.1:3306/world";

    public WorldBrowse() {
        setSize(700, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        currentOperation = Operation.NONE;

        JPanel gridPanel = new JPanel(new GridLayout(4, 2, 2, 2));
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));

        // ID
        MyLabel IDLabel = new MyLabel("ID:");
        gridPanel.add(IDLabel);
        IDText = new MyText("");
        IDText.setEnabled(false);
        gridPanel.add(IDText);

        // Name
        MyLabel nameLabel = new MyLabel("City name:");
        gridPanel.add(nameLabel);
        nameText = new MyText("");
        gridPanel.add(nameText);

        // Country
        MyLabel countryLabel = new MyLabel("Country:");
        gridPanel.add(countryLabel);
        countryText = new MyText("");
        gridPanel.add(countryText);

        // Population
        MyLabel populationLabel = new MyLabel("Population:");
        gridPanel.add(populationLabel);
        populationText = new MyText("");
        gridPanel.add(populationText);

        // Read buttons
        prevButton = new MyButton("Prev");
        nextButton = new MyButton("Next");
        firstButton = new MyButton("First");
        lastButton = new MyButton("Last");

        prevButton.addActionListener(e -> previous());
        nextButton.addActionListener(e -> next());
        firstButton.addActionListener(e -> first());
        lastButton.addActionListener(e -> last());

        JPanel readButtons = new JPanel(new FlowLayout());
        readButtons.add(firstButton);
        readButtons.add(prevButton);
        readButtons.add(nextButton);
        readButtons.add(lastButton);
        buttonPanel.add(readButtons);

        // Edit buttons
        addButton = new MyButton("Add new");
        deleteButton = new MyButton("Delete");
        updateButton = new MyButton("Update");

        addButton.addActionListener(e -> startAdd());
        deleteButton.addActionListener(e -> deleteRecord());
        updateButton.addActionListener(e -> startUpdate());

        JPanel editButtons = new JPanel(new FlowLayout());
        editButtons.add(addButton);
        editButtons.add(deleteButton);
        editButtons.add(updateButton);
        buttonPanel.add(editButtons);

        // Save buttons
        saveButton = new MyButton("Save");
        cancelButton = new MyButton("Cancel");

        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> cancel());

        JPanel saveButtons = new JPanel(new FlowLayout());
        saveButtons.add(saveButton);
        saveButtons.add(cancelButton);
        buttonPanel.add(saveButtons);

        add(buttonPanel, BorderLayout.SOUTH);
        add(gridPanel, BorderLayout.CENTER);

        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        setFields(false);

        first();
    }

    void loadInfo() {
        try {
            IDText.setText(set.getString("ID"));
            nameText.setText(set.getString("Name"));
            countryText.setText(set.getString("CountryCode"));
            populationText.setText(String.valueOf(set.getInt("Population")));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL issue: " + e.getMessage(), " :(", JOptionPane.ERROR_MESSAGE);
        }
    }

    void startAdd() {
        currentOperation = Operation.ADD;

        IDText.setText("");
        nameText.setText("");
        countryText.setText("");
        populationText.setText("");

        setFields(true);
        setBrowseButtonsEnabled(false);
        saveButton.setEnabled(true);
        cancelButton.setEnabled(true);
    }

    void startUpdate() {
        currentOperation = Operation.UPDATE;

        setFields(true);
        setBrowseButtonsEnabled(false);
        saveButton.setEnabled(true);
        cancelButton.setEnabled(true);
    }

    void save() {
        if (currentOperation == Operation.ADD) {
            insertRecord();
        } else if (currentOperation == Operation.UPDATE) {
            updateRecord();
        }

        currentOperation = Operation.NONE;
        setFields(false);
        setBrowseButtonsEnabled(true);
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    void insertRecord(){
        try{
            set.moveToInsertRow();
            set.updateString("Name", nameText.getText());
            set.updateString("CountryCode", countryText.getText());
            set.updateInt("Population", Integer.parseInt(populationText.getText()));
            set.insertRow();
            set.moveToCurrentRow();
            JOptionPane.showMessageDialog(this, "Inserted new city");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL issue: " + e.getMessage(), " :(", JOptionPane.ERROR_MESSAGE);
        }
    }

    void updateRecord(){
        try{
            set.updateString("Name", nameText.getText());
            set.updateString("CountryCode", countryText.getText());
            set.updateInt("Population", Integer.parseInt(populationText.getText()));
            set.updateRow();
            JOptionPane.showMessageDialog(this, "City updated");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL issue: " + e.getMessage(), " :(", JOptionPane.ERROR_MESSAGE);
        }
    }

    void cancel() {
        currentOperation = Operation.NONE;
        setFields(false);
        setBrowseButtonsEnabled(true);
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);

        JOptionPane.showMessageDialog(this, "Changes cancelled.");
    }

    void deleteRecord() {
        try {
            set.deleteRow();

            if (set.next()) {
                next();
            } else {
                previous();
            }
            JOptionPane.showMessageDialog(this, "Record deleted successfully");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL issue: " + e.getMessage(), " :(", JOptionPane.ERROR_MESSAGE);
        }
    }

    void next() {
        try {
            if (set.next()) {
                loadInfo();
            } else {
                set.previous();
                JOptionPane.showMessageDialog(this, "End of the city list");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL issue: " + e.getMessage(), " :(", JOptionPane.ERROR_MESSAGE);
        }
    }

    void previous() {
        try {
            if (set.previous()) {
                loadInfo();
            } else {
                set.next();
                JOptionPane.showMessageDialog(this, "Start of the city list");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL issue: " + e.getMessage(), " :(", JOptionPane.ERROR_MESSAGE);
        }
    }

    void first() {
        try {
            if (set.first()) {
                loadInfo();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL issue: " + e.getMessage(), " :(", JOptionPane.ERROR_MESSAGE);
        }
    }

    void last() {
        try {
            if (set.last()) {
                loadInfo();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL issue: " + e.getMessage(), " :(", JOptionPane.ERROR_MESSAGE);
        }
    }

    void setFields(boolean status) {
        //IDText.setEnabled(status);
        nameText.setEnabled(status);
        countryText.setEnabled(status);
        populationText.setEnabled(status);
    }

    void setBrowseButtonsEnabled(boolean status) {
        firstButton.setEnabled(status);
        lastButton.setEnabled(status);
        nextButton.setEnabled(status);
        prevButton.setEnabled(status);
    }

    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String queryAll = "SELECT * FROM city";
            set = statement.executeQuery(queryAll);
            set.next(); //nikoliv next() zde
            SwingUtilities.invokeLater(() -> new WorldBrowse().setVisible(true));
        } catch (SQLException e) {
            System.out.println("SQL error: " + e.getMessage());
        }

    }
}

class MyLabel extends JLabel {
    public MyLabel(String text) {
        super(text);
        setFont(new Font("Consolas", Font.PLAIN, 18));
        setOpaque(true);
        setBackground(new Color(0xF5ECBC));
        setForeground(new Color(0x000000));
    }
}

class MyText extends JTextField {
    public MyText(String text) {
        super(text);
        setFont(new Font("Consolas", Font.PLAIN, 18));
        setOpaque(true);
        setBackground(new Color(0xF5ECBC));
        setForeground(new Color(0x000000));
    }
}

class MyButton extends JButton {
    public MyButton(String text) {
        super(text);
        setFont(new Font("Consolas", Font.PLAIN, 18));
        setOpaque(true);
        setBackground(Color.BLUE);
        setForeground(Color.WHITE);
    }
}

enum Operation {
    ADD, UPDATE, NONE
}