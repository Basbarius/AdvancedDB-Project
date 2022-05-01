import java.io.*;

import org.tartarus.snowball.ext.*;

public class Snowball {


    public String stemTxt(String inputDoc) {

        englishStemmer stemmer = new englishStemmer();

        try
        {
            File file = new File(inputDoc);
            BufferedReader br = new BufferedReader(new FileReader(file));

            // Output file
            String outputDoc = inputDoc + "_Stemmed.txt";
            File stemmedDoc = new File(outputDoc);
            BufferedWriter out = new BufferedWriter(new FileWriter(stemmedDoc));

            String line;
            while ((line = br.readLine()) != null)
            {
                stemmer.setCurrent(line);
                stemmer.stem();
                out.write(stemmer.getCurrent() + "\n");
            }
            out.close();
            br.close();

            return outputDoc;


        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }
}