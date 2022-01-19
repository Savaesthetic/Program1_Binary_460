import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/*
    TODO: QUESTIONS
        Every 13 values is a record.
        Every integer in a record is 4 bytes.
        Every string in a record is the same lenght (length of the largest string).
        Different records can have different string lengths.
        Output file must be inputFileName.bin.
        Have to parse inputFileName for filename even if given filepath.
        Best way to parse inputFile, regex?
        How to deal with commas inside quotes. Using regex with even quote protection or csv parsing library?
*/
public class Prog1A {
    public static void main (String [] args) {
        try {
            ArrayList<DataRecord> records = createRecords(args[0]);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error: CSV input file required.");
            System.exit(0);
        }
    }

    // Parse input file, create records, 
    // and return an arraylist of sorted records
    static ArrayList<DataRecord> createRecords(String inputFile) {
        try {
            File inputRef = new File(inputFile);
            Scanner fileReader = new Scanner(inputRef);
            boolean firstLine = true;
            while (fileReader.hasNextLine()) {
                if (firstLine) {
                    firstLine = false;
                    fileReader.nextLine();
                    continue;
                }

                String data = fileReader.nextLine();
                String[] values = data.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                for (int i = 0; i < 13; i++) {
                    if (i > 8) {
                        if (values[i] == "") {

                        }
                        // TODO: PARSE INT
                    } else {

                    }
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.out.printf("Error: Can not find given file (%s)\n", inputFile);
            System.exit(0);
        }
        return null;
    }

    // Take sorted DataRecords and write them to binary output file
    static void createBinaryOutput (DataRecord[] sortedRecords) {
        return;
    }
}

// Should I store the lenght of string in the data record
class DataRecord {
    private String projectId;
    private String projectName;
    private String status;
    private String scope;
    private String type;
    private String methodology;
    private String region;
    private String country;
    private String state;
    private int creditsIssued;
    private int creditsRetired;
    private int creditsRemaining;
    private int firstYear;
}