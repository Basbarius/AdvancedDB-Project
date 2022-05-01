import java.io.*;

import org.tartarus.snowball.ext.*;


public class Snowball {


    public String stemTxt(String outDoc) {

        englishStemmer stemmer = new englishStemmer();

        try
        {
            File file = new File(outDoc);
            BufferedReader br = new BufferedReader(new FileReader(file));

            // Output file
            File stemmedDoc = new File(outDoc + "_" + "Stemmed.txt");
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

           System.out.println(stemmedDoc.toString());
            return "\\" + stemmedDoc.toString();


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
