import java.io.*;
import java.sql.SQLException;

public class DataBase {
    Transaction transactionManager = new Transaction();

    public DataBase() throws Exception {
    }

    public static String addToTerm(String name){
        return "INSERT IGNORE INTO term (name) " +
                "VALUES (\"" + name + "\");";
    }

    public static String addToWord(String word, String name){
        return "INSERT IGNORE INTO word (word, name) " +
                "VALUES (\"" + word + "\", \"" + name + "\");";
    }

    public static String addToHas(int id, String name, int frequency){
        return "INSERT IGNORE INTO has (id, name, frequency) " +
                "VALUES (\"" + id + "\", \"" + name + "\", \"" + frequency + "\")" +
                "ON DUPLICATE KEY UPDATE frequency = \"" + frequency + "\";";
    }

    public void createDocument() throws SQLException {
        try {
            transactionManager.executeUpdate("insert into document values();");
        }catch (SQLException e){
            System.out.println(e);
            transactionManager.rollBack();
        }
    }
    public void addDataBaseDocument(int id, String name, int frequency) throws Exception {
        try {
            transactionManager.executeUpdate(addToHas(id, name, frequency));
            System.out.println(id + name + frequency);
            transactionManager.commitChanges();
        }catch (SQLException e){
            System.out.println(e);
            transactionManager.rollBack();
        }
    }

    public void addDataBaseTermWord(String stemmedPath, String outputPath) throws IOException {

        // Add to term and word
        // Document file
        File fileTerm = new File(stemmedPath);
        FileReader frT = new FileReader(fileTerm);

        // Document file
        File fileWord = new File(outputPath);
        FileReader frW = new FileReader(fileWord);

        try (
                BufferedReader brT = new BufferedReader(frT)) {
            try (BufferedReader brW = new BufferedReader(frW)) {
                String lineT;
                String lineW;
                // Till we have a new line
                while ((lineT = brT.readLine()) != null && (lineW = brW.readLine()) != null) {
                    try {
                        transactionManager.executeUpdate(addToTerm(lineT));
                        transactionManager.executeUpdate(addToWord(lineW, lineT));
                        transactionManager.commitChanges();
                    }catch (SQLException e){
                        System.out.println(e);
                        transactionManager.rollBack();
                    }
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        frT.close();
        frW.close();
    }
}
