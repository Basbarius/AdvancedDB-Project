import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws SQLException, Exception {
        Transaction transactionManager = new Transaction();
        QueryMaker queryMaker = new QueryMaker(transactionManager);

        System.out.println(queryMaker.compareDocuments(4, 2, 2));
        ArrayList<String> query = new ArrayList<>();
        queryMaker.makeQuery(query, 2, 2);
    }
}
