package org.program.program;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.stage.FileChooser;

import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;


import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class Controller {

    final static String url = "jdbc:postgresql://localhost:5432/Encryption";
    final static String user = "postgres";
    final static String password = "000";
    static String name = "";

    @FXML
    private TextField username;

    @FXML
    private TextField login;

    @FXML
    private PasswordField pass;

    @FXML
    private Label messageLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button openFileButton;

    @FXML
    private TextArea filePathField;

    @FXML
    private TableView<Item> tableView;

    @FXML
    private TableColumn<Item, Integer> idColumn;

    @FXML
    private TableColumn<Item, String> nameColumn;

    @FXML
    private TableColumn<Item, String> actionColumn;

    @FXML
    private TableColumn<Item, String> textColumn;

    @FXML
    protected void handleAuthorization(ActionEvent event) throws IOException, SQLException {
        String loginUser = login.getText();
        String passwordUser = pass.getText();


        if (isUser(loginUser, passwordUser)) {
            if (loginUser.equals("admin")) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("logs.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 800, 500);

                Controller controller = fxmlLoader.getController();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Logs");
                stage.show();


                controller.idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
                controller.nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
                controller.actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
                controller.textColumn.setCellValueFactory(new PropertyValueFactory<>("text"));

                // Загрузка начальных данных или пустая таблица
                controller.tableView.setItems(FXCollections.observableArrayList());

                Connection conn;
                Statement stmt;
                ResultSet rs;

                conn = DriverManager.getConnection(url, user, password);
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT * FROM public.messages ORDER BY id DESC");
                while (rs.next()) {
                    controller.addRow(rs.getInt("id"), rs.getString("name"),rs.getString("action"),rs.getString("text"));
                }
                rs.close();
                conn.close();
            } else {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("encryption.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 800, 500);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Encryption");
                stage.show();
            }
        } else {
            messageLabel.setText("No user");
        }

    }

    @FXML
    protected void handleRegistration(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("registration.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Registration");
        stage.show();
    }

    @FXML
    protected void handleExit(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("authorization.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Program");
        stage.show();
    }

    @FXML
    protected void handleSaveUser(ActionEvent event) throws IOException {
        String userName = username.getText();
        String loginUser = login.getText();
        String passwordUser = pass.getText();

        if (!userName.isEmpty() || !loginUser.isEmpty() || !passwordUser.isEmpty()) {
            try {
                Connection conn;
                PreparedStatement pstmt;

                conn = DriverManager.getConnection(url, user, password);

                pstmt = conn.prepareStatement("INSERT INTO public.users (login, pass, name) VALUES (?, ?, ?)");
                pstmt.setString(1, loginUser);
                pstmt.setString(2, passwordUser);
                pstmt.setString(3, userName);
                pstmt.addBatch();

                pstmt.executeBatch();

                pstmt.close();
                conn.close();

                name = userName;

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("authorization.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 800, 500);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Program");
                stage.show();
            } catch (SQLException e) {
                messageLabel.setText("Incorrect input.");
            }
        }
        else {
            messageLabel.setText("An empty field.");
        }
    }

    @FXML
    protected void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MP3 Files", "*.mp3")
        );

        Stage stage = (Stage) openFileButton.getScene().getWindow();

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            filePathField.setText(file.getAbsolutePath());

            File audioFile = new File(file.getAbsolutePath());

            try {
                AudioFile file1 = AudioFileIO.read(audioFile);
                Tag tag = file1.getTag();

                if (tag != null) {
                    String lyrics = tag.getFirst(FieldKey.LYRICS);
                    filePathField.setText(lyrics);
                    statusLabel.setText(file.getAbsolutePath());
                    saveData("read", lyrics);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void handleSaveFile() {
        if (!Objects.equals(statusLabel.getText(), "")) {
            File audioFile = new File(statusLabel.getText());
            try {
                AudioFile f = AudioFileIO.read(audioFile);
                Tag tag = f.getTagOrCreateAndSetDefault();
                tag.setField(FieldKey.LYRICS, filePathField.getText());
                saveData("write", filePathField.getText());
                f.commit();
                statusLabel.setText("");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveData(String action, String text) throws SQLException {
        Connection conn;
        PreparedStatement pstmt;

        conn = DriverManager.getConnection(url, user, password);

        pstmt = conn.prepareStatement("INSERT INTO public.messages (name, action, text) VALUES (?, ?, ?)");
        pstmt.setString(1, name);
        pstmt.setString(2, action);
        pstmt.setString(3, text);
        pstmt.addBatch();

        pstmt.executeBatch();

        pstmt.close();
        conn.close();
    }

    private boolean isUser(String login, String pass) throws SQLException {
        Connection conn;
        Statement stmt;
        ResultSet rs;

        conn = DriverManager.getConnection(url, user, password);
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT * FROM public.users WHERE users.login = '"+ login +"' AND users.pass = '" + pass+"'");
        while (rs.next()) {
            name = rs.getString("name");
        }
        rs.close();
        conn.close();
        return !name.isEmpty();
    }

    public void addRow(int id, String name, String action, String text) {
        Item newItem = new Item(id, name, action, text);
        tableView.getItems().add(newItem);
    }

    public class Item {
        private int id;
        private String name;
        private String action;
        private String text;

        public Item(int id, String name, String action, String text) {
            this.id = id;
            this.name = name;
            this.action = action;
            this.text = text;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAction() {
            return action;
        }

        public String getText() {
            return text;
        }
    }
}