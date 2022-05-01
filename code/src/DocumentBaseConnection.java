import java.io.*;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;

public class DocumentBaseConnection {
    Transaction transactionManager;

    public DocumentBaseConnection(Transaction transactionManager) throws Exception {
        this.transactionManager = transactionManager;
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

    public int createDocument(String url, String title, String author, String date ) throws SQLException {
        int documentIndex = -1;
        try {
            transactionManager.manipulation("insert into document values();");
            ArrayList<String[]> result = transactionManager.query("select max(id) from document;");
            documentIndex = Integer.parseInt(result.get(0)[0]);
            transactionManager.manipulation("insert into text values (" +
                    documentIndex + ", " +
                    "\"" + url + "\", " +
                    "\"" + title + "\", " +
                    "\"" + author + "\", " +
                    "\"" + date + "\");");
        }catch (SQLException e){
            System.out.println(e);
            transactionManager.rollbackChanges();
        }
        return documentIndex;
    }
    public void addDataBaseDocument(int id, String name, int frequency) throws Exception {
        try {
            transactionManager.manipulation(addToHas(id, name, frequency));
            //System.out.println(id + name + frequency);
            transactionManager.commitChanges();
        }catch (SQLException e){
            System.out.println(e);
            transactionManager.rollbackChanges();
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
                        transactionManager.manipulation(addToTerm(lineT));
                        transactionManager.manipulation(addToWord(lineW, lineT));
                    }catch (SQLException e){
                        System.out.println(e);
                        transactionManager.rollbackChanges();
                    }
                    transactionManager.commitChanges();
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        frT.close();
        frW.close();
    }
}