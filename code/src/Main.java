import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws SQLException, Exception {
        Transaction transactionManager = new Transaction();

        try {
            ArrayList<String[]> resultSet = transactionManager.query("select * " +
                    "from worker ");
            for(String[] worker: resultSet){
                System.out.println(worker[0]);
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
