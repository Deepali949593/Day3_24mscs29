package Day3_Application;
import org.bson.Document;
public class Course {
    public String title;
    public int duration;

    public Course(String title, int duration) {
        this.title = title;
        this.duration = duration;
    }

    public Document toDocument() {
        return new Document("title", title)
                .append("duration", duration);
    }
}
