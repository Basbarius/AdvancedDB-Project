import java.util.HashMap;

public class DocumentBaseFeeder {
    Transaction transactionManager;

    public DocumentBaseFeeder(Transaction transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void insertSampleDocumentBase() throws Exception {
        String path = "..\\documents\\";
        String[] documents = {
                "D1. STRESS AND HEALTH.txt",
                "D2. NATURE AND HEALTH.txt",
                "D3. Global Warming and Infectious Disease.txt",
                "D4. HURRICANES AND GLOBAL WARMING.txt",
                "D5. Socio-cultural factors and entrepreneurial activity An overview.txt",
                "D6. Globalization Mental Health and Social Economic Factors.txt",
                "D7. Enhancing public safety in primary care.txt",
                "D8. Women, Work, and Welfare Is There a.txt",
                "D9. The welfare state A glossary for public health.txt",
                "D10. ENVIRONMENT AND OBESITY.txt",
        };

        String[][] documentMetaData = {
                {"https://www.annualreviews.org/doi/pdf/10.1146/annurev.pu.05.050184.001535", "Stress and health", "Stanislav V. Kasl", "1984-01-01"},
                {"https://www.annualreviews.org/doi/pdf/10.1146/annurev-publhealth-032013-182443", "Nature and Health", "Terry Hartig, Richard Mitchell, Sjerp de Vries, and Howard Frumkin", "2014-01-02"},
                {"https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.475.4619&rep=rep1&type=pdf", "Global Warming and Infectious Disease", "Atul A. Khasnis and Mary D. Nettleman", "2005-04-01"},
                {"https://doi.org/10.1175/BAMS-86-11-1571", "Hurricanes and Global Warming", "R. A. Pielke Jr, C. Landsea, M. Mayfield, J. Layer, and R. Pasch", "2005-11-01"},
                {"http://www.patriciathornton.com/wp-content/uploads/2012/02/Socio-Cultural-Factors-and-Entrep-Activity.pdf", "Socio-cultural factors and entrepreneurial activity: An overview", "Patricia H. Thornton, Domingo Ribeiro-Soriano, David Urbano", "2011-03-14"},
                {"https://doi.org/10.1177/1468018108095634", "Globalization: Mental Health and Social Economic Factors", "Bhavsar, Vishal; Bhugra, Dinesh", "2008-12-01"},
                {"https://www.ncbi.nlm.nih.gov/pmc/articles/PMC78998/", "Enhancing public safety in primary care", "Tim Wilson, Aziz Sheikh", "2002-03-09"},
                {"https://academic.oup.com/sw/article-abstract/37/1/9/1922343", "Women, Work, and Welfare: Is There a Role for Social Work?", "Jan L. Hagen", "1992-01-01"},
                {"https://jech.bmj.com/content/62/1/3.abstract", "The welfare state: a glossary for public health", "T Eikemo, C Bambra", "2007-12-13"},
                {"https://doi.org/10.1016/j.metabol.2019.07.006", "Environment and obesity", "Stylianos Nicolaidis", "2019-11-01"}
        };

        DeleteWords wordsDeleter = new DeleteWords();
        DocumentBaseConnection documentBaseConnection = new DocumentBaseConnection(transactionManager);
        Snowball snow = new Snowball();

        int index = 1;
        for(String document : documents) {
            System.out.print("Inserting Document: ");
            String documentName = "D" + index;
            System.out.print(documentName);

            HashMap<String, Integer> frequencyDoc = wordsDeleter.deleteWords(
                    path + document, path + documentName);
            int documentId = documentBaseConnection.createDocument(
                    documentMetaData[index-1][0],documentMetaData[index-1][1],
                    documentMetaData[index-1][2],documentMetaData[index-1][3]);
            String stemmedDocument = snow.stemTxt(path + documentName);

            documentBaseConnection.addDataBaseTermWord(path + stemmedDocument, path + documentName);
            frequencyDoc.forEach((key, value) -> {
                try {
                    documentBaseConnection.addDataBaseDocument(documentId, key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            index++;
            System.out.println(" Done");
        }
    }
}
