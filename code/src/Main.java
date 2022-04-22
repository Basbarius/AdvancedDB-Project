import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws SQLException, Exception {
        Transaction transactionManager = new Transaction();

        String username = "Pedro";
        try {
            ArrayList<String[]> resultSet = transactionManager.query("select contraseña " +
                    "from clienteRegistrado " +
                    "where nombreCompleto = \"" + username + "\"");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
