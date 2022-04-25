import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;

public class QueryMaker {
    Transaction databaseConnection;

    public QueryMaker(Transaction databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public ArrayList<Integer> makeQuery(ArrayList<String> query, int nDocumentsToRetrieve,
                         int comparisonMeasure) throws SQLException, Exception {
        boolean validIndex = validateIndex(nDocumentsToRetrieve);
        if(!validIndex) return null;

        String queryLabel = createQueryInDatabase(query);

        String proximityMeasure = getProximityMeasure(comparisonMeasure, "td", "tq");
        String order = "asc";
        //if a similarity measure is used, order is descending, if dissimilarity, then ascending
        if (comparisonMeasure > 0 && comparisonMeasure < 3){
            order = "desc";
        }

        //make query
        ArrayList<String[]> resultSet = new ArrayList<>();
        resultSet = getDistancePerDocument(queryLabel, proximityMeasure, order, resultSet);

        //return most relevant documents
        ArrayList<Integer> mostRelevantDocuments = new ArrayList<>();
        System.out.println("The " + nDocumentsToRetrieve + " most relevant documents are:");
        for(int i=0; i<nDocumentsToRetrieve; i++) {
            String[] row = resultSet.get(i);
            System.out.println("id: " + row[0] + ", distance: " + row[1] );
            mostRelevantDocuments.add(Integer.parseInt(row[0]));
        }

        return mostRelevantDocuments;
    }

    private ArrayList<String[]> getDistancePerDocument(String queryLabel, String proximityMeasure, String order, ArrayList<String[]> resultSet) {
        try {
            resultSet = databaseConnection.query(
                    "select td.id, " + proximityMeasure + " distance " +
                    "from complete tq, complete td, query q " +
                    "where td.name = tq.name and tq.id = q.id and q.label = \"" + queryLabel + "\" " +
                    "and not td.id = any(select d.id " +
                            "from document d, query q " +
                            "where d.id = q.id) " +
                    "group by td.id " +
                    "order by distance " + order + ";"
                    );
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return resultSet;
    }

    private String createQueryInDatabase(ArrayList<String> query) {
        String queryLabel = "q1";
        try {
            ArrayList<String> terms = new ArrayList<>();
            ArrayList<String[]> result;
            int documentId;


            //fetch the terms of the query
            for(String word : query) {
                result = databaseConnection.query(
                        "select name from word where word = \"" + word + "\";"
                );
                terms.add(result.get(0)[0]);
            }

            //create a new document
            databaseConnection.manipulation("insert into document values();");
            //fetch new document id
            result = databaseConnection.query("select max(id) from document;");
            documentId = Integer.parseInt(result.get(0)[0]);
            //fetch next query id
            result = databaseConnection.query("select count(*) from query;");
            queryLabel = "q" +  Integer.toString(Integer.parseInt(result.get(0)[0]) + 1);

            //create new query
            databaseConnection.manipulation("insert into query values(" + documentId + ", " +
                    "\"" + queryLabel + "\");");

            //for each term, add values to has table
            for(String term : terms) {
                result = databaseConnection.query("select max(frequency) from has " +
                        "where name = \"" + term + "\";");
                int termFrequency = Integer.parseInt(result.get(0)[0]);
                databaseConnection.manipulation("insert into has values(" +
                        documentId + ", \"" + term + "\", " + termFrequency + ");");
            }

            //commit changes
            databaseConnection.commitChanges();
        }
        catch (Exception ex){
            ex.printStackTrace();
            try {
                databaseConnection.rollbackChanges();
            }  catch (Exception ex2){
                ex2.printStackTrace();
            }

        }
        return queryLabel;
    }

    public double compareDocuments(int firstDocumentIndex, int secondDocumentIndex,
                                   int comparisonMeasure) throws SQLException, Exception{
        boolean validIndexes = validateIndexes(firstDocumentIndex, secondDocumentIndex);
        if(!validIndexes) return -1;

        String proximityMeasure = getProximityMeasure(comparisonMeasure, "td1", "td2");

        ArrayList<String[]> resultSet = new ArrayList<>();
        try {
            resultSet = databaseConnection.query(
                    "select " + proximityMeasure + " distance " +
                    "from complete td1, complete td2 " +
                    "where td1.id = " + firstDocumentIndex +
                            " and td2.id = " + secondDocumentIndex +
                            " and td1.name = td2.name;");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return Double.parseDouble(resultSet.get(0)[0]);
    }

    private String getProximityMeasure(int comparisonMeasure, String firstTable, String secondTable) {
        String proximityMeasure;
        switch (comparisonMeasure) {
            case 0:
                System.out.println("Using Euclidean Distance");
                proximityMeasure = "sqrt(sum(pow("+ firstTable +".frequency - "+ secondTable +".frequency, 2)))";
                break;
            case 1:
                System.out.println("Using Inner Product");
                proximityMeasure = "sum("+ firstTable +".frequency * "+ secondTable +".frequency)";
                break;
            case 2:
                System.out.println("Using Cosine");
                proximityMeasure = "sum("+ firstTable +".frequency * "+ secondTable +".frequency)/" +
                        "(sqrt(sum(pow("+ firstTable +".frequency, 2))) * " +
                        "sqrt(sum(pow("+ secondTable +".frequency, 2))))";
                break;
            default:
                System.out.println("Using Euclidean Distance");
                proximityMeasure = "sqrt(sum(pow(td1.frequency - td2.frequency, 2)))";
        }
        return proximityMeasure;
    }

    private boolean validateIndex(int firstDocumentIndex) {
        return validateIndexes(firstDocumentIndex, firstDocumentIndex);
    }

    private boolean validateIndexes(int firstDocumentIndex, int secondDocumentIndex) {
        ArrayList<String[]> resultSet = new ArrayList<>();
        try {
            resultSet = databaseConnection.query(
                    "select max(id), min(id) " +
                            "from document;");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        int max = Integer.parseInt(resultSet.get(0)[0]);
        int min = Integer.parseInt(resultSet.get(0)[1]);
        return firstDocumentIndex >= min && secondDocumentIndex >= min &&
            firstDocumentIndex <= max && secondDocumentIndex <= max;
    }
}
