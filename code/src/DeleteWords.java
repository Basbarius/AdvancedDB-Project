import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class DeleteWords{

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
            System.out.println(e);
        }
        return false;
    }

    // Function to add to the hashmap the content of a txt
    public static HashMap<String, String> txtToHashMap(String path){
        HashMap<String, String> hashMap = new HashMap<String, String>();
        File file = new File(path);
        try{
            FileReader fr = new FileReader(file);
            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                while ((line = br.readLine())!= null){
                    String[] content = line.split(" ");
                    for(int i = 1 ;i < content.length; i++){
                        hashMap.put(content[i], content[0]);
                    }
                }
            } catch (FileNotFoundException e) {
                throw e;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch(FileNotFoundException ex){
            System.out.println(ex);
        }
        return hashMap;
    }

    public HashMap<String, Integer> deleteWords(String inputDoc, String outputDoc) throws Exception {

        HashMap<String, Integer> frequency = new HashMap<>();
        Integer frequencyValue;

        HashMap<String, String> irregularVerbs;
        irregularVerbs = txtToHashMap("D:\\Usuarios\\Escritorio\\Tareas\\2022 I\\Bases de datos avanzados\\Proyecto final\\AdvancedDB-Project\\code\\irregularVerbs.txt");

        try {
            // Document file
            File file = new File(inputDoc);
            FileReader fr = new FileReader(file);

            // Output file
            FileWriter fw = new FileWriter(outputDoc);
            BufferedWriter out = new BufferedWriter(fw);

            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                // Till we have a new line
                while ((line = br.readLine()) != null) {
                    // Split the content to store it in an array
                    String[] content = line.split(" ");
                    String result;
                    String nextWord;
                    String[] verbs = new String[content.length];
                    for (int i = 0; i < content.length; i++) {
                        // Delete the punctuation marks
                        result = content[i].replaceAll("[\\p{Punct}&&[^'-]]+", "");
                        nextWord = irregularVerbs.get(result);
                        if (nextWord != null) {
                            verbs[i] = nextWord;
                        } else {
                            verbs[i] = result;
                        }
                    }
                    for (String stopWord : verbs) {
                        // If the word is not a stop word
                        if (!txtSearch("stopWords2.txt", stopWord)) {
                            // Then write it on the text file
                            if(!txtSearch(outputDoc, stopWord)) {
                                out.write(stopWord + "\n");
                                // Check if the stop word is added to the txt
                                frequencyValue = frequency.get(stopWord);
                                if (frequencyValue == null) {
                                    frequency.put(stopWord, 1);
                                } else {
                                    frequency.put(stopWord, frequencyValue + 1);
                                }
                            }
                        }
                    }
                }
            }
            fr.close();
            out.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        return frequency;
    }

}
