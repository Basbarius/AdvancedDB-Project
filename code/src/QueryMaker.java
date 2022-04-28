import org.apache.commons.math3.linear.*;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;

public class QueryMaker {
    Transaction databaseConnection;
    DecompositionSolver querySolver = null;

    public QueryMaker(Transaction databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public ArrayList<Integer> makeQuery(ArrayList<String> query, int nDocumentsToRetrieve,
                         int comparisonMeasure) throws SQLException, Exception {
        boolean validIndex = validateIndex(nDocumentsToRetrieve);
        if(!validIndex) return null;

        String queryLabel = createQueryInDatabase(query);

        convertQueryToSVDValues(queryLabel);

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
        System.out.println("\twithout SVD");
        for(int i=0; i<nDocumentsToRetrieve; i++) {
            String[] row = resultSet.get(i);
            System.out.println("\tid: d" + row[0] + ", distance: " + row[1] );
            mostRelevantDocuments.add(Integer.parseInt(row[0]));
        }

        //make query using SVD
        resultSet = getDistancePerDocumentSVD(queryLabel, proximityMeasure, order, resultSet);
        System.out.println("\twithSVD");
        for(int i=0; i<nDocumentsToRetrieve; i++) {
            String[] row = resultSet.get(i);
            System.out.println("\tid: d" + row[0] + ", distance: " + row[1] );
        }
        System.out.println();

        return mostRelevantDocuments;
    }

    public double[] compareDocuments(int firstDocumentIndex, int secondDocumentIndex,
                                   int comparisonMeasure) throws SQLException, Exception{
        boolean validIndexes = validateIndexes(firstDocumentIndex, secondDocumentIndex);
        double[] values = new double[2];
        if(!validIndexes) return values;

        String proximityMeasure = getProximityMeasure(comparisonMeasure, "td1", "td2");

        ArrayList<String[]> resultSet = new ArrayList<>();
        try {
            System.out.println("Comparing documents: d" + firstDocumentIndex + " and d" + secondDocumentIndex);
            resultSet = databaseConnection.query(
                    "select " + proximityMeasure + " distance " +
                    "from complete td1, complete td2 " +
                    "where td1.id = " + firstDocumentIndex +
                            " and td2.id = " + secondDocumentIndex +
                            " and td1.name = td2.name;");
            System.out.println("\twithout SVD");
            values[0] = Double.parseDouble(resultSet.get(0)[0]);
            System.out.println("\t" + values[0]);

            resultSet = databaseConnection.query(
                    "select " + proximityMeasure + " distance " +
                            "from hassvd td1, hassvd td2 " +
                            "where td1.id = " + firstDocumentIndex +
                            " and td2.id = " + secondDocumentIndex +
                            " and td1.termid = td2.termid;");
            System.out.println("\twith SVD");
            values[1] = Double.parseDouble(resultSet.get(0)[0]);
            System.out.println("\t" + values[1]);
            System.out.println();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return values;
    }

    public void createSVDTable(boolean storeInDB) throws SQLException, Exception{
        ArrayList<String[]> result;
        ArrayList<double[]> frequencyTableAL = new ArrayList<>();
        ArrayList<Integer> documentIds = new ArrayList<>();
        int nDocuments = 0;

        //get frequency matrix dimensions and all document ids
        try {
            result = databaseConnection.query("select distinct(id) " +
                    "from complete has " +
                    "where not id = any(select d.id " +
                    "from document d, query q " +
                    "where d.id = q.id)" +
                    "order by id;");
            for(String[] row : result) {
                documentIds.add(Integer.parseInt(row[0]));
            }
            nDocuments = documentIds.size();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        //build the frequency matrix table and convert to real matrix
        try{
            result = databaseConnection.query("select frequency " +
                    "from complete has " +
                    "where not id = any(select d.id " +
                    "from document d, query q " +
                    "where d.id = q.id)" +
                    "order by name, id;");

            int i = 0;
            double[] newRow = new double[nDocuments];
            for (String[] row : result) {
                 newRow[i] = Double.parseDouble(row[0]);
                i++;
                if (i >= nDocuments){
                    i = 0;
                    frequencyTableAL.add(newRow);
                    newRow = new double[nDocuments];
                }
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        //create SVD from has table
        RealMatrix frequencyTable = MatrixUtils.createRealMatrix(frequencyTableAL.toArray(new double[0][0]));
        SingularValueDecomposition svd = new SingularValueDecomposition(frequencyTable);
        RealMatrix us = svd.getU().multiply(svd.getS());
        RealMatrix vt = svd.getVT();

        System.out.print("Effective Rank of the Frequency Table: ");
        System.out.println(svd.getRank());

        if (svd.getRank() < nDocuments){
            System.out.println("Frequency table will be shortened");
        }

        //store VT table in database as hasSVD
        if(storeInDB){
            int termId = 0;
            for(double[] row : vt.getData()){
                int j = 0;
                for(double value : row){
                    databaseConnection.manipulation("insert into hassvd values(" +
                            documentIds.get(j) + ", " + termId + ", " + value + " );");
                    j++;
                }
                termId++;
            }
            databaseConnection.commitChanges();
        }

        DecompositionSolver solver = new LUDecomposition(us).getSolver();
        querySolver = solver;
    }

    public void convertQueryToSVDValues(String queryLabel){
        ArrayList<Double> query = new ArrayList<>();
        try{
            ArrayList<String[]> result = databaseConnection.query("select frequency " +
                    "from complete has, query q " +
                    "where has.id = q.id and q.label = \"" + queryLabel + "\" " +
                    "order by name;");
            for(String[] row : result){
                query.add(Double.parseDouble(row[0]));
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        //add SVD query to database
        RealVector rvQuery = new ArrayRealVector(query.toArray(new Double[0]));
        RealVector svdQuery = querySolver.solve(rvQuery);
        try {
            ArrayList<String[]> result = databaseConnection.query("select id from query " +
                    "where label = \"" + queryLabel + "\";");
            int queryId = Integer.parseInt(result.get(0)[0]);
            int termId = 0;
            for(double value : svdQuery.toArray()){
                databaseConnection.manipulation("insert into hassvd values(" +
                        queryId + ", " + termId + ", " + value + ");");
                termId++;
            }
            databaseConnection.commitChanges();
        }catch (Exception ex){
            ex.printStackTrace();
        }
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

    private ArrayList<String[]> getDistancePerDocument(String queryLabel, String proximityMeasure, String order, ArrayList<String[]> resultSet) {
        try {
            resultSet = databaseConnection.query(
                    "select td.id, " + proximityMeasure + " distance " +
                            "from complete tq, complete td, query q, text d " +
                            "where td.name = tq.name and tq.id = q.id and q.label = \"" + queryLabel + "\" " +
                            "and d.id = td.id " +
                            "group by td.id " +
                            "order by distance " + order + ";"
            );
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return resultSet;
    }

    private ArrayList<String[]> getDistancePerDocumentSVD(String queryLabel, String proximityMeasure, String order, ArrayList<String[]> resultSet) {
        try {
            resultSet = databaseConnection.query(
                    "select td.id, " + proximityMeasure + " distance " +
                            "from hassvd tq, hassvd td, query q " +
                            "where td.termid = tq.termid and tq.id = q.id and q.label = \"" + queryLabel + "\" " +
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
