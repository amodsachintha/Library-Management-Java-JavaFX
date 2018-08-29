package library.assistant.ui.listmember;

import com.jfoenix.controls.JFXButton;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXComboBox;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import library.assistant.alert.AlertMaker;
import library.assistant.database.DatabaseHandler;
import library.assistant.export.pdf.ListToPDF;
import library.assistant.ui.addbook.BookAddController;
import library.assistant.ui.addmember.MemberAddController;
import library.assistant.ui.main.MainController;
import library.assistant.util.LibraryAssistantUtil;

public class MemberListController implements Initializable {

    ObservableList<Member> list = FXCollections.observableArrayList();

    @FXML
    private JFXTextField search;
    @FXML
    private TableView<Member> tableView;
    @FXML
    private TableColumn<Member, String> nameCol;
    @FXML
    private TableColumn<Member, String> idCol;
    @FXML
    private TableColumn<Member, String> mobileCol;
    @FXML
    private TableColumn<Member, String> emailCol;
    @FXML
    private TableColumn<Member, String> mobileCol2;
    @FXML
    private TableColumn<Member, String> nicCol;
    @FXML
    private TableColumn<Member, String> addressCol;
    @FXML
    private JFXComboBox<String> searchType;
    @FXML
    private StackPane rootPane;
    @FXML
    private AnchorPane contentPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        searchType.setItems(FXCollections.observableArrayList("Name","Member ID", "National ID"));
        searchType.getSelectionModel().selectFirst();
        initCol();
        loadData();
    }

    private void initCol() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        mobileCol.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        mobileCol2.setCellValueFactory(new PropertyValueFactory<>("mobile2"));
        nicCol.setCellValueFactory(new PropertyValueFactory<>("nic"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
    }

    private Stage getStage() {
        return (Stage) tableView.getScene().getWindow();
    }

    private void loadData() {
        list.clear();

        DatabaseHandler handler = DatabaseHandler.getInstance();
        String qu = "SELECT * FROM MEMBER";
        ResultSet rs = handler.execQuery(qu);
        appendToList(rs);
        tableView.setItems(list);
    }

    @FXML
    private void handleMemberDelete(ActionEvent event) {
        //Fetch the selected row
        MemberListController.Member selectedForDeletion = tableView.getSelectionModel().getSelectedItem();
        if (selectedForDeletion == null) {
            AlertMaker.showErrorMessage("No member selected", "Please select a member for deletion.");
            return;
        }
        if (DatabaseHandler.getInstance().isMemberHasAnyBooks(selectedForDeletion)) {
            AlertMaker.showErrorMessage("Cant be deleted", "This member has some books.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deleting book");
        alert.setContentText("Are you sure want to delete " + selectedForDeletion.getName() + " ?");
        Optional<ButtonType> answer = alert.showAndWait();
        if (answer.get() == ButtonType.OK) {
            Boolean result = DatabaseHandler.getInstance().deleteMember(selectedForDeletion);
            if (result) {
                AlertMaker.showSimpleAlert("Book deleted", selectedForDeletion.getName() + " was deleted successfully.");
                list.remove(selectedForDeletion);
            } else {
                AlertMaker.showSimpleAlert("Failed", selectedForDeletion.getName() + " could not be deleted");
            }
        } else {
            AlertMaker.showSimpleAlert("Deletion cancelled", "Deletion process cancelled");
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadData();
    }

    @FXML
    private void handleMemberEdit(ActionEvent event) {
        //Fetch the selected row
        Member selectedForEdit = tableView.getSelectionModel().getSelectedItem();
        if (selectedForEdit == null) {
            AlertMaker.showErrorMessage("No member selected", "Please select a member for edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/library/assistant/ui/addmember/member_add.fxml"));
            Parent parent = loader.load();

            MemberAddController controller = (MemberAddController) loader.getController();
            controller.infalteUI(selectedForEdit);

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Edit Member");
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
    private void exportAsPDF(ActionEvent event) {
        List<List> printData = new ArrayList<>();
        String[] headers = {"   Name    ", "ID", "Mobile", "    Email   ","Mobile2","NIC","    Address    "};
        printData.add(Arrays.asList(headers));
        for (Member member : list) {
            List<String> row = new ArrayList<>();
            row.add(member.getName());
            row.add(member.getId());
            row.add(member.getMobile());
            row.add(member.getEmail());
            row.add(member.getMobile2());
            row.add(member.getNic());
            row.add(member.getAddress());
            printData.add(row);
        }
        LibraryAssistantUtil.initPDFExprot(rootPane, contentPane, getStage(), printData);
    }

    @FXML
    private void closeStage(ActionEvent event) {
        getStage().close();
    }

    public static class Member {

        private final SimpleStringProperty name;
        private final SimpleStringProperty id;
        private final SimpleStringProperty mobile;
        private final SimpleStringProperty email;
        private final SimpleStringProperty mobile2;
        private final SimpleStringProperty nic;
        private final SimpleStringProperty address;

        public Member(String name, String id, String mobile, String email,String mobile2, String nic, String address) {
            this.name = new SimpleStringProperty(name);
            this.id = new SimpleStringProperty(id);
            this.mobile = new SimpleStringProperty(mobile);
            this.email = new SimpleStringProperty(email);
            this.mobile2 = new SimpleStringProperty(mobile2);
            this.nic = new SimpleStringProperty(nic);
            this.address = new SimpleStringProperty(address);
        }

        public String getName() {
            return name.get();
        }

        public String getId() {
            return id.get();
        }

        public String getMobile() {
            return mobile.get();
        }

        public String getEmail() {
            return email.get();
        }

        public String getMobile2() {
            return mobile2.get();
        }

        public String getNic() {
            return nic.get();
        }

        public String getAddress() {
            return address.get();
        }

    }

    @FXML
    private void loadFromSearch() {
        String searchParam = search.getText();
        String type = searchType.getSelectionModel().getSelectedItem().toLowerCase();
        if(type.equals("name"))
            type = "NAME";
        else if(type.equals("member id"))
            type = "ID";
        else if (type.equals("national id"))
            type = "NIC";
        else
            type = "NAME";

        if (searchParam.isEmpty()) {
            list.clear();
            loadData();
        }
        else {
            list.clear();
            DatabaseHandler handler = DatabaseHandler.getInstance();
            String qu = "SELECT * FROM MEMBER WHERE LOWER( "+ type +" ) LIKE '%" + searchParam.toLowerCase() + "%'";
            System.out.println(qu);
            ResultSet rs = handler.execQuery(qu);
            appendToList(rs);
            tableView.setItems(list);
        }

    }

    private void appendToList(ResultSet rs) {
        try {
            while (rs.next()) {
                String name = rs.getString("name");
                String mobile = rs.getString("mobile");
                String id = rs.getString("id");
                String email = rs.getString("email");
                String mobile2 = rs.getString("mobile2");
                String nic = rs.getString("nic");
                String address = rs.getString("address");
                list.add(new Member(name, id, mobile, email,mobile2,nic,address));
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookAddController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
