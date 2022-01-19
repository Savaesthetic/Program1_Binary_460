import java.io.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Prog1A.java -- A program meant to convert a formatted csv file
 * into a basic binary file. The csv file is given as a command 
 * line argument input.
 * 
 * The input file contains varying lines of data which will be 
 * converted into DataRecords containing 13 fields. Fields may have 
 * varying sizes but equivalent fields in different DataRecords will
 * be a single uniform size.
 * 
 * Once the DataRecords have been assembled the output binary file is
 * created in the form of a RandomAccessFile. All of the DataRecords 
 * will then be streamed to the output file.
 * 
 * The program does not currently work on the large Offsets-Database.csv
 * file but will work on smaller test files using data from the 
 * Offsets-Database file.
 * 
 * Author: Alex Sava (01/19/2022)
*/
public class Prog1A {
    public static void main (String [] args) {
        try {
            ArrayList<DataRecord> records = createRecords(args[0]);
            createBinaryOutput(records, generateOutputName(args[0]));
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error: CSV input file required.");
            System.exit(-1);
        }
    }

    // Parse input file, create records, and return an arraylist of sorted records
    /*
     * createRecords(inputFile) -- parses the formatted input csv file in order 
     * to create an ArrayList of DataRecords out of the data found in the file.
     * The input file, given by inputFile, is opened to read and iterated through
     * line by line. Each line is then divided into 13 predetermined fields 
     * (9 strings followed by 4 ints) whice are used to create a DataRecord.
     * The data values are trimmed to required specifications and the largest 
     * string lengths for each field are kept track of in an array so that 
     * the fields can be padded out later to get uniform records. The first line 
     * in the file is ignored due to it only containing meta data.
    */
    static ArrayList<DataRecord> createRecords(String inputFile) {
        ArrayList<DataRecord> records = new ArrayList<DataRecord>(); // ArrayList to hold all the records we will parse from the file
        int[] maxStringLengths = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // Array holding the largest string length for every string field in records

        try {
            File inputRef = new File(inputFile);
            Scanner fileReader = new Scanner(inputRef);
            boolean firstLine = true; // Flag used to skip first line in file containing meta data
            while (fileReader.hasNextLine()) {
                if (firstLine) { // Uses flag to skip first line containing metadata
                    firstLine = false;
                    fileReader.nextLine();
                    continue;
                }

                String data = fileReader.nextLine();
                DataRecord record = new DataRecord(); // Create DataRecord for incoming line of data
                data = Normalizer.normalize(data, Normalizer.Form.NFKD).replaceAll("[^\\p{ASCII}]", ""); // Normalize input line to ASCII characters only
                String[] values = data.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Split line by commas ignoring commas in quotes to get individual field data
                for (int i = 0; i < 13; i++) { // Loop through seperate values, should be exactly 13
                    if (i > 8) { // Last four fields are integers
                        values[i] = values[i].replaceAll("[\",]", ""); // Strip any quotes and commas from integers
                        int val;
                        if (values[i] == "") { // Empty integer fields are replaced with 0
                            val = 0;
                        } else {
                            val = Integer.parseInt(values[i]);
                        }
                        switch (i) { // Fill fields in DataRecord
                            case 9: record.setCreditsIssued(val); break;
                            case 10: record.setCreditsRetired(val); break;
                            case 11: record.setCreditsRemaining(val); break;
                            case 12: record.setFirstYear(val); break;
                        }
                    } else {
                        String val;
                        if (values[i].length() == 0) { // Empty field is converted into an empty string
                            val = "";
                        } else if (values[i].charAt(0) == ('\"')) { // If string field is enclosed in quotes then we strip them
                            val = values[i].replaceAll("^\"|\"$", "");
                        } else {
                            val = values[i];
                        }
                        if (val.length() > maxStringLengths[i]) { // Keep track of largest string in each field
                            maxStringLengths[i] = val.length();
                        }
                        switch (i) { // Fill string fields in DataRecord
                            case 0: record.setProjectId(val); break;
                            case 1: record.setProjectName(val); break;
                            case 2: record.setStatus(val); break;
                            case 3: record.setScope(val); break;
                            case 4: record.setType(val); break;
                            case 5: record.setMethodology(val); break;
                            case 6: record.setRegion(val); break;
                            case 7: record.setCountry(val); break;
                            case 8: record.setState(val); break;
                        }
                    }
                }
                records.add(record); // Add new record to list of existing records
            }
            fileReader.close();

            padRecordStrings(records, maxStringLengths); // Run through all records and pad string to correct size
            Collections.sort(records, new DataRecordComparator()); // Sort records by credits issued
        } catch (FileNotFoundException e) {
            System.out.printf("Error: Can not find given file (%s)\n", inputFile);
            System.exit(-1);
        }
        return records;
    }

    /*
     * padRecordStrings(records, maxStringLengths) -- pads all of the string 
     * fields in all of the records to the size given by the field's 
     * corresponding index in the maxStringLengths array. This is done so that 
     * the output file will have uniformly-sized records. This was done using 
     * the String.format method although another option could have been to use
     * StringBuffers.
    */
    static void padRecordStrings(ArrayList<DataRecord> records, int[] maxStringLengths) {
        for (int i = 0; i < records.size(); i++) {
            DataRecord record = records.get(i);
            for (int j = 0; j < 9; j++) {
                switch (j) {
                    case 0: record.setProjectId(String.format("%-" + maxStringLengths[0] + "s", record.getProjectId())); break;
                    case 1: record.setProjectName(String.format("%-" + maxStringLengths[1] + "s", record.getProjectName())); break;
                    case 2: record.setStatus(String.format("%-" + maxStringLengths[2] + "s", record.getStatus())); break;
                    case 3: record.setScope(String.format("%-" + maxStringLengths[3] + "s", record.getScope())); break;
                    case 4: record.setType(String.format("%-" + maxStringLengths[4] + "s", record.getType())); break;
                    case 5: record.setMethodology(String.format("%-" + maxStringLengths[5] + "s", record.getMethodology())); break;
                    case 6: record.setRegion(String.format("%-" + maxStringLengths[6] + "s", record.getRegion())); break;
                    case 7: record.setCountry(String.format("%-" + maxStringLengths[7] + "s", record.getCountry())); break;
                    case 8: record.setState(String.format("%-" + maxStringLengths[8] + "s", record.getState())); break;
                }
            }
        }
    }

    // Take sorted DataRecords and write them to binary output file
    /*
     * createBinaryOutput(records, outputFileName) -- creates a new
     * RandomAccessFile with the name given by outputFileName and fills 
     * it with DataRecords found in the input csv file.
    */
    static void createBinaryOutput (ArrayList<DataRecord> records, String outputFileName) {
        File outputRef;
        RandomAccessFile dataStream = null;

        outputRef = new File(outputFileName);

        try {
            dataStream = new RandomAccessFile(outputRef,"rw");
        } catch (IOException e) {
            System.out.println("I/O ERROR: Something went wrong with the "
                             + "creation of the RandomAccessFile object.");
            System.exit(-1);
        }

        for (DataRecord record : records) {
            record.dumpObject(dataStream);
        }

        try {
            dataStream.close();
        } catch (IOException e) {
            System.out.println("Error: Could not close output file.");
        }
    }

    /*
     * generateOutputName(inputName) -- parses the input file path/name
     * in order to generate an output file name equivalent to the
     * input file with with a .bin extension instead of .csv
    */
    static String generateOutputName(String inputName) {
        Pattern pattern = Pattern.compile("(.*\\/)*(.*).csv");
        Matcher matcher = pattern.matcher(inputName);
        if (matcher.matches()) {
            return matcher.group(2) + ".bin";
        } else {
            System.out.print("Error: Error parsing input file path for filename.");
            System.exit(-1);
            return null;
        }
    }
}


class DataRecord {
    // Fields that make up a DataRecord
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

    // "Getters" for class field values
    public String getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public String getStatus() { return status; }
    public String getScope() { return scope; }
    public String getType() { return type; }
    public String getMethodology() { return methodology; }
    public String getRegion() { return region; }
    public String getCountry() { return country; }
    public String getState() { return state; }
    public int getCreditsIssued() { return creditsIssued; }
    public int getCreditsRetired() { return creditsRetired; }
    public int getCreditsRemaining() { return creditsRemaining; }
    public int getFirstYear() { return firstYear; }

    // "Setters" for class field values
    public void setProjectId(String val) { projectId = val; }
    public void setProjectName(String val) { projectName = val; }
    public void setStatus(String val) { status = val; }
    public void setScope(String val) { scope = val; }
    public void setType(String val) { type = val; }
    public void setMethodology(String val) { methodology = val; }
    public void setRegion(String val) { region = val; }
    public void setCountry(String val) { country = val; }
    public void setState(String val) { state = val; }
    public void setCreditsIssued(int val) { creditsIssued = val; }
    public void setCreditsRetired(int val) { creditsRetired = val; }
    public void setCreditsRemaining(int val) { creditsRemaining = val; }
    public void setFirstYear(int val) { firstYear = val; }

    /*
     * dumbObject(stream) -- Writes the contents of the DataRecord to 
     * the output file given by the stream object reference.
    */
    public void dumpObject(RandomAccessFile stream)
    {
         try {
             stream.writeBytes(projectId);
             stream.writeBytes(projectName);
             stream.writeBytes(status);
             stream.writeBytes(scope);
             stream.writeBytes(type);
             stream.writeBytes(methodology);
             stream.writeBytes(region);
             stream.writeBytes(country);
             stream.writeBytes(state);
             stream.writeInt(creditsIssued);
             stream.writeInt(creditsRetired);
             stream.writeInt(creditsRemaining);
             stream.writeInt(firstYear);
        } catch (IOException e) {
            System.out.println("I/O ERROR: Couldn't write to the file;\n\t"
                             + "perhaps the file system is full?");
            System.exit(-1);
         }
    }
}

// Creates comparator for DataRecord to be used in sorting
class DataRecordComparator implements Comparator<DataRecord> {
    // Override the compare() method to sort by credits issued
    public int compare(DataRecord r1, DataRecord r2) {
        if (r1.getCreditsIssued() == r2.getCreditsIssued()) {
            return 0;
        } else if (r1.getCreditsIssued() > r2.getCreditsIssued()) {
            return 1;
        } else {
            return -1;
        }
    }
}