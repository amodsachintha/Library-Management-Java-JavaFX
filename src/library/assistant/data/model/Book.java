package library.assistant.data.model;

/**
 *
 * @author amodsachintha
 */
public class Book {
    String id;
    String title;
    String author;
    String price;
    Boolean isAvail;

    public Book(String id, String title, String author, String price, Boolean isAvail) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.isAvail = isAvail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Boolean getAvailability() {
        return isAvail;
    }

    public void setIsAvail(Boolean isAvail) {
        this.isAvail = isAvail;
    }
    
    
}
