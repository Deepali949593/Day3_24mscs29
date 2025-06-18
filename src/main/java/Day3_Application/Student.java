package Day3_Application;
import org.bson.Document;
public class Student {
    public String name;
    public int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Document toDocument() {
        return new Document("name", name)
                .append("age", age);
    }
}
