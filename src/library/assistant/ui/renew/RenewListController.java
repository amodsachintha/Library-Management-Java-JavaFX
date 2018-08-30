package library.assistant.ui.renew;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import library.assistant.database.DatabaseHandler;
import library.assistant.util.LibraryAssistantUtil;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ResourceBundle;


public class RenewListController implements Initializable {

    ObservableList<Member> list = FXCollections.observableArrayList();

    @FXML
    private TableView<Member> tableView;
    @FXML
    private JFXTextField search;
    @FXML
    private TableColumn<Member, String> idCol;
    @FXML
    private TableColumn<Member, String> nameCol;
    @FXML
    private TableColumn<Member, String> mobileCol;
    @FXML
    private TableColumn<Member, String> dateCol;
    @FXML
    private TableColumn buttonCol;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setCols();
    }

    public void setCols() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        mobileCol.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        buttonCol.setCellValueFactory(new PropertyValueFactory<>("RENEW"));

        Callback<TableColumn<Member, String>, TableCell<Member, String>> cellFactory
                =
                new Callback<TableColumn<Member, String>, TableCell<Member, String>>() {
                    @Override
                    public TableCell call(final TableColumn<Member, String> param) {
                        final TableCell<Member, String> cell = new TableCell<Member, String>() {

                            final Button btn = new Button("Just Do It");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        Member Member = getTableView().getItems().get(getIndex());
                                        System.out.println(Member.getName()
                                                + "   " + Member.getId());
                                    });
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };

        buttonCol.setCellFactory(cellFactory);
        loadData();
        tableView.setItems(list);
    }

    @FXML
    public void exit() {

    }

    public static class Member {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty mobile;
        private final SimpleStringProperty date;

        public Member(String id, String name, String mobile, Date date) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.mobile = new SimpleStringProperty(mobile);
            this.date = new SimpleStringProperty(LibraryAssistantUtil.getDateString(date));
        }

        public String getId() {
            return id.get();
        }

        public String getName() {
            return name.get();
        }

        public String getMobile() {
            return mobile.get();
        }

        public String getDate() {
            return date.get();
        }

    }


    private void loadData() {
        list.clear();
        DatabaseHandler handler = DatabaseHandler.getInstance();
        String query = "SELECT ID, NAME, MOBILE, RENEWED_AT FROM MEMBER";
        ResultSet rs = handler.execQuery(query);
        try {
            while (rs.next()) {
                String id = rs.getString("ID");
                String name = rs.getString("NAME");
                String mobile = rs.getString("MOBILE");
                Date date = rs.getTimestamp("RENEWED_AT");

                DateTime start = new DateTime(date);
                DateTime end = new DateTime();
                Interval interval = new Interval(start, end);
                long days = interval.toDuration().getStandardDays();
                System.out.println(days);
                if (days >= 365)
                    list.add(new Member(id, name, mobile, date));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        tableView.setItems(list);
    }


}
