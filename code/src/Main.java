import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws Exception {

        String path = "C:\\Users\\jorge\\Documents\\GitHub\\AdvancedDB-Project\\code\\documents";

        DeleteWords doc1 = new DeleteWords();
        DeleteWords doc2 = new DeleteWords();

        HashMap<String, Integer> frequencyDoc1 = doc1.deleteWords(path + "\\D1. STRESS AND HEALTH.txt", path + "\\D1. output.txt");
//        HashMap<String, Integer> frequencyDoc2 = doc2.deleteWords(path + "\\test d2.txt", path + "\\D2. output.txt");

        DataBase dataBase1 = new DataBase();
        dataBase1.createDocument();

         Snowball snow = new Snowball();
         String stem1 = snow.stemTxt(path + "\\D1. output.txt");


        //dataBase1.addDataBaseTermWord(path + "\\D1. stemmed.txt", path + "\\D1. output.txt");
        dataBase1.addDataBaseTermWord(stem1, path + "\\D1. output.txt");
        frequencyDoc1.forEach((key, value) -> {
            try {
                dataBase1.addDataBaseDocument(1, key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
