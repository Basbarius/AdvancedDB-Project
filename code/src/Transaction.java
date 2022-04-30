import java.sql.*;
import java.io.*;
import java.util.ArrayList;

public class Transaction {

    Connection conn = null;
    Statement stmt = null;
    BufferedReader in = null;

    static final String URL = "jdbc:mysql://localhost/";
    static final String BD = "indexationproject";        // especificar: el nombre de la BD,
    static final String USER = "root";        // el nombre de usuario
    static final String PASSWD = "proyectofinal";// el password del usuario

    public Transaction() throws SQLException, Exception {

        // this will load the MySQL driver, each DB has its own driver
//        Class.forName("com.mysql.jdbc.Driver");
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.print("Connecting to the database... ");

        // setup the connection with the DB
        conn = DriverManager.getConnection(URL + BD, USER, PASSWD);
        System.out.println("Connected\n");

        conn.setAutoCommit(false);         // inicio de la 1a transacción
        stmt = conn.createStatement();
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    private ArrayList dumpResultSet(ResultSet rset) throws SQLException {

        ResultSetMetaData rsetmd = rset.getMetaData();
        int i = rsetmd.getColumnCount();
        String[] currentRow;
        ArrayList<String[]> resultSet = new ArrayList<>();

        while (rset.next()) {
            currentRow = new String[i];
            for (int j = 1; j <= i; j++) {
                //System.out.print(rset.getString(j) + "\t");
                currentRow[j-1] = rset.getString(j);
            }
            resultSet.add(currentRow);
        }
        return resultSet;
    }

    public ArrayList query(String statement) throws SQLException {

        ResultSet rset = stmt.executeQuery(statement);
        ArrayList<String[]> resultSet =  dumpResultSet(rset);

        rset.close();
        return resultSet;
    }

    public void executeUpdate(String statement) throws SQLException {
        stmt.executeUpdate(statement);
    }

    public void commitChanges() throws SQLException {
        conn.commit();
    }

    public void rollBack() throws SQLException {
        conn.rollback();
    }


    private void close() throws SQLException {
        stmt.close();
        conn.close();
    }
}