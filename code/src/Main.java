import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws SQLException, Exception {
        Transaction transactionManager = new Transaction();
        QueryMaker queryMaker = new QueryMaker(transactionManager);
        DocumentBaseFeeder documentBaseFeeder = new DocumentBaseFeeder(transactionManager);
//        documentBaseFeeder.insertSampleDocumentBase();

        queryMaker.createSVDTable(false);

//        queryMaker.compareDocuments(5, 7, 0);
//        queryMaker.compareDocuments(5, 7, 1);
//        queryMaker.compareDocuments(5, 7, 2);
//        ArrayList<String> query = new ArrayList<>();
//        query.add("stress");
//        query.add("health");
//        queryMaker.makeQuery(query, 3, 0);
//        queryMaker.makeQuery(query, 3, 1);
//        queryMaker.makeQuery(query, 3, 2);
        queryMaker.findDocumentMetadata(1);
    }
}
