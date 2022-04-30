import java.io.*;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static boolean txtSearch(String path, String word) {
        boolean contains;
        try {
            File file = new File(path);
            try (Scanner entry = new Scanner(file)) {
                String line;
                while (entry.hasNext()) {
                    line = entry.nextLine();
                    contains = line.contains(word);
                    if(contains) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public static String addToTerm(String name){
        return "INSERT IGNORE INTO term (name) " +
                "VALUES (\"" + name + "\");";
    }

    public static String addToWord(String word, String name){
        return "INSERT IGNORE INTO word (word, name) " +
                "VALUES (\"" + word + "\", \"" + name + "\");";
    }

    public static void main(String[] args) throws SQLException, Exception {
        Transaction transactionManager = new Transaction();

        try {
            // Document file
            File file = new File("inputTest.txt");
            FileReader fr = new FileReader(file);

            // Output file
            FileWriter fw = new FileWriter("outputTest.txt");
            BufferedWriter out = new BufferedWriter(fw);

            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                // Till we have a new line
                while ((line = br.readLine()) != null) {
                    // Split the content to store it in an array
                    String[] content = line.split(" ");
                    String result;
                    for (String s : content) {
                        // Delete the punctuation marks
                        result = s.replaceAll("\\p{Punct}", "");
                        // If the word is not a stop word
                        if (!txtSearch("stopWords2.txt", result)) {
                            // Then write it on the text file
                            out.write(result + "\n");
                        }
                    }
                }
            }
            fr.close();
            out.close();
        } catch (IOException ignored) {

        }
        // Document file
        File fileTerm = new File("stemmedTest.txt");
        FileReader frT = new FileReader(fileTerm);

        // Document file
        File fileWord = new File("outputTest.txt");
        FileReader frW = new FileReader(fileWord);

        try (BufferedReader brT = new BufferedReader(frT)) {
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
                        transactionManager.rollBack();
                    }
                }
            }
        }
        frT.close();
        frW.close();
    }
}
