package library.assistant.ui.listbook;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import library.assistant.alert.AlertMaker;
import library.assistant.database.DatabaseHandler;
import library.assistant.ui.addbook.BookAddController;
import library.assistant.ui.main.MainController;
import library.assistant.util.LibraryAssistantUtil;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookListController implements Initializable {

    ObservableList<Book> list = FXCollections.observableArrayList();

    @FXML
    private JFXTextField search;
    @FXML
    private StackPane rootPane;
    @FXML
    private TableView<Book> tableView;
    @FXML
    private TableColumn<Book, String> titleCol;
    @FXML
    private TableColumn<Book, String> idCol;
    @FXML
    private TableColumn<Book, String> authorCol;
    @FXML
    private TableColumn<Book, String> catCol;
    @FXML
    private TableColumn<Book, String> priceCol;
    @FXML
    private TableColumn<Book, Boolean> availabilityCol;
    @FXML
    private AnchorPane contentPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initCol();
        loadData();
    }

    private Stage getStage() {
        return (Stage) tableView.getScene().getWindow();
    }

    private void initCol() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        catCol.setCellValueFactory(new PropertyValueFactory<>("cat"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        availabilityCol.setCellValueFactory(new PropertyValueFactory<>("availabilty"));
    }

    private void loadData() {
        list.clear();

        DatabaseHandler handler = DatabaseHandler.getInstance();
        String qu = "SELECT * FROM BOOK";
        ResultSet rs = handler.execQuery(qu);
        try {
            while (rs.next()) {
                String titlex = rs.getString("title");
                String author = rs.getString("author");
                String cat = rs.getString("cat");
                String id = rs.getString("id");
                String price = rs.getString("price");
                Boolean avail = rs.getBoolean("isAvail");

                list.add(new Book(titlex, id, author, cat, price, avail));

            }
        } catch (SQLException ex) {
            Logger.getLogger(BookAddController.class.getName()).log(Level.SEVERE, null, ex);
        }

        tableView.setItems(list);
    }

    @FXML
    private void searchBook() {
        String searchParam = search.getText().toLowerCase();
        if (searchParam.isEmpty()) {
            list.clear();
            loadData();
        } else {
            list.clear();
            DatabaseHandler handler = DatabaseHandler.getInstance();
            String qu = "SELECT * FROM BOOK WHERE LOWER( title ) LIKE '%"+searchParam+"%'";
            ResultSet rs = handler.execQuery(qu);
            System.out.println(qu);
            try {
                while (rs.next()) {
                    String titlex = rs.getString("title");
                    String author = rs.getString("author");
                    String id = rs.getString("id");
                    String cat = rs.getString("cat");
                    String price = rs.getString("price");
                    Boolean avail = rs.getBoolean("isAvail");

                    list.add(new Book(titlex, id, author, cat, price, avail));

                }
            } catch (SQLException ex) {
                Logger.getLogger(BookAddController.class.getName()).log(Level.SEVERE, null, ex);
            }

            tableView.setItems(list);
        }
    }


    @FXML
    private void handleBookDeleteOption(ActionEvent event) {
        //Fetch the selected row
        Book selectedForDeletion = tableView.getSelectionModel().getSelectedItem();
        if (selectedForDeletion == null) {
            AlertMaker.showErrorMessage("No book selected", "Please select a book for deletion.");
            return;
        }
        if (DatabaseHandler.getInstance().isBookAlreadyIssued(selectedForDeletion)) {
            AlertMaker.showErrorMessage("Cant be deleted", "This book is already issued and cant be deleted.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deleting book");
        alert.setContentText("Are you sure want to delete the book " + selectedForDeletion.getTitle() + " ?");
        Optional<ButtonType> answer = alert.showAndWait();
        if (answer.get() == ButtonType.OK) {
            Boolean result = DatabaseHandler.getInstance().deleteBook(selectedForDeletion);
            if (result) {
                AlertMaker.showSimpleAlert("Book deleted", selectedForDeletion.getTitle() + " was deleted successfully.");
                list.remove(selectedForDeletion);
            } else {
                AlertMaker.showSimpleAlert("Failed", selectedForDeletion.getTitle() + " could not be deleted");
            }
        } else {
            AlertMaker.showSimpleAlert("Deletion cancelled", "Deletion process cancelled");
        }
    }

    @FXML
    private void handleBookEditOption(ActionEvent event) {
        //Fetch the selected row
        Book selectedForEdit = tableView.getSelectionModel().getSelectedItem();
        if (selectedForEdit == null) {
            AlertMaker.showErrorMessage("No book selected", "Please select a book for edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/library/assistant/ui/addbook/add_book.fxml"));
            Parent parent = loader.load();

            BookAddController controller = (BookAddController) loader.getController();
            controller.inflateUI(selectedForEdit);

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Edit Book");
            stage.setScene(new Scene(parent));
            stage.show();
            LibraryAssistantUtil.setStageIcon(stage);

            stage.setOnCloseRequest((e) -> {
                handleRefresh(new ActionEvent());
            });

        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadData();
    }

    @FXML
    private void exportAsPDF(ActionEvent event) {
        List<List> printData = new ArrayList<>();
        String[] headers = {"ID", "   Title   ", "  Author  ", "  Category  ", "  Price ", "Avail"};
        printData.add(Arrays.asList(headers));
        for (Book book : list) {
            List<String> row = new ArrayList<>();
            row.add(book.getId());
            row.add(book.getTitle());
            row.add(book.getAuthor());
            row.add(book.getCat());
            row.add(book.getPrice());
            row.add(book.getAvailabilty());
            printData.add(row);
        }
        LibraryAssistantUtil.initPDFExprot(rootPane, contentPane, getStage(), printData);
    }

    @FXML
    private void closeStage(ActionEvent event) {
        getStage().close();
    }

    public static class Book {

        private final SimpleStringProperty title;
        private final SimpleStringProperty id;
        private final SimpleStringProperty author;
        private final SimpleStringProperty cat;
        private final SimpleStringProperty price;
        private final SimpleStringProperty availabilty;

        public Book(String title, String id, String author, String cat, String price, Boolean avail) {
            this.title = new SimpleStringProperty(title);
            this.id = new SimpleStringProperty(id);
            this.author = new SimpleStringProperty(author);
            this.cat = new SimpleStringProperty(cat);
            this.price = new SimpleStringProperty(price);
            if (avail) {
                this.availabilty = new SimpleStringProperty("Available");
            } else {
                this.availabilty = new SimpleStringProperty("Issued");
            }
        }

        public String getTitle() {
            return title.get();
        }

        public String getId() {
            return id.get();
        }

        public String getAuthor() {
            return author.get();
        }

        public String  getCat(){
            return cat.get();
        }

        public String getPrice() {
            return price.get();
        }

        public String getAvailabilty() {
            return availabilty.get();
        }

    }

}
