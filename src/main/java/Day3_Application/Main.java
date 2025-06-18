package Day3_Application;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class Main {
    public static void main(String[] args) {
        // Connect localhost:27017
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");

        // Connect to database "school"
        MongoDatabase database = mongoClient.getDatabase("school");


        MongoCollection<Document> studentsCol = database.getCollection("students");
        MongoCollection<Document> coursesCol = database.getCollection("courses");
        MongoCollection<Document> enrollmentsCol = database.getCollection("enrollments");

        studentsCol.drop();
        coursesCol.drop();
        enrollmentsCol.drop();


        Document student1 = new Student("Deepa", 20).toDocument();
        Document student2 = new Student("Riya", 22).toDocument();
        studentsCol.insertMany(java.util.Arrays.asList(student1, student2));

        Document course1 = new Course("Python Programming", 4).toDocument();
        Document course2 = new Course("Database Systems", 3).toDocument();
        coursesCol.insertMany(java.util.Arrays.asList(course1, course2));

        ObjectId student1Id = student1.getObjectId("_id");
        ObjectId student2Id = student2.getObjectId("_id");
        ObjectId course1Id = course1.getObjectId("_id");
        ObjectId course2Id = course2.getObjectId("_id");

        // enrollments

        // Embedded
        Document embeddedEnrollment = new Document("type", "embedded")
                .append("student", student1)
                .append("course", course1);
        enrollmentsCol.insertOne(embeddedEnrollment);

        // Referenced
        Document referencedEnrollment = new Document("type", "referenced")
                .append("studentId", student2Id)
                .append("courseId", course2Id);
        enrollmentsCol.insertOne(referencedEnrollment);



        System.out.println("----- All Enrollments -----");
        FindIterable<Document> enrollments = enrollmentsCol.find();
        for (Document enrollment : enrollments) {
            String type = enrollment.getString("type");
            System.out.println("Enrollment Type: " + type);

            if (type.equals("embedded")) {
                // Embedded - student and course are embedded docs
                Document student = (Document) enrollment.get("student");
                Document course = (Document) enrollment.get("course");
                System.out.println("Student: " + student.toJson());
                System.out.println("Course: " + course.toJson());
            } else if (type.equals("referenced")) {
                // Referenced - fetch student and course documents separately
                ObjectId sId = enrollment.getObjectId("studentId");
                ObjectId cId = enrollment.getObjectId("courseId");

                Document student = studentsCol.find(eq("_id", sId)).first();
                Document course = coursesCol.find(eq("_id", cId)).first();

                System.out.println("Student: " + (student != null ? student.toJson() : "Not found"));
                System.out.println("Course: " + (course != null ? course.toJson() : "Not found"));
            }
            System.out.println("----------------------");
        }

        System.out.println("\n--- Updating student's name ---");
        studentsCol.updateOne(eq("_id", student2Id), new Document("$set", new Document("name", "Robert")));

        Document newEmbeddedStudent = new Document("name", "Alicia").append("age", 20);
        enrollmentsCol.updateOne(
                Filters.and(eq("type", "embedded"), eq("student.name", "Alice")),
                new Document("$set", new Document("student", newEmbeddedStudent))
        );

        System.out.println("After update:");
        enrollments = enrollmentsCol.find();
        for (Document enrollment : enrollments) {
            System.out.println(enrollment.toJson());
        }

        System.out.println("\nDifference in updating:");
        System.out.println(" - For referenced documents, update affects only the single student document.");
        System.out.println(" - For embedded documents, you must update the entire embedded sub-document inside the enrollment.");

        studentsCol.createIndex(new Document("name", 1), new IndexOptions().background(true));
        studentsCol.createIndex(new Document("age", 1), new IndexOptions().background(true));

        System.out.println("\nIndexes created on 'students' collection.");

        // Close Mongo client
        mongoClient.close();
    }
}
