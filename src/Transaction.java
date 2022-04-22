import javax.swing.*;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;

public class Transaction {

    Connection conn = null;
    Statement stmt = null;
    BufferedReader in = null;

    static final String URL = "jdbc:mysql://localhost/";
    static final String BD = "proyectoFinal";        // especificar: el nombre de la BD,
    static final String USER = "root";        // el nombre de usuario
    static final String PASSWD = "Chocolatecon1Maria";// el password del usuario

    public Transaction() throws SQLException, Exception {

        // this will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        System.out.print("Connecting to the database... ");

        // setup the connection with the DB
        conn = DriverManager.getConnection(URL + BD, USER, PASSWD);
        System.out.println("connected\n\n");

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
                System.out.print(currentRow[j-1] + "\t");
            }
            System.out.println();
            resultSet.add(currentRow);
        }
        return resultSet;
    }

    public ArrayList query(String statement) throws SQLException {

        ResultSet rset = stmt.executeQuery(statement);
        System.out.println("Results:");
        ArrayList<String[]> resultSet =  dumpResultSet(rset);

        System.out.println();
        rset.close();
        return resultSet;
    }

    private void close() throws SQLException {
        stmt.close();
        conn.close();
    }
}
