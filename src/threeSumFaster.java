import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

public class threeSumFaster {
    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static long MAXVALUE =  1000000;
    static long MINVALUE = -1000000;
    static int numberOfTrials = 10;
    static int MAXINPUTSIZE  = (int) Math.pow(2,10); // 2^29 size start with smaller first maybe 2^10
    static int MININPUTSIZE  =  1;
    static int cnt = 0;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time


    static String ResultsFolderPath = "/home/matt/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;


    public static void main(String[] args) {

        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("threeSum-Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("threeSum-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("threeSum-Exp3.txt");
    }

    static void runFullExperiment(String resultsFileName){

        // To open a file to write to
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch(Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize += inputSize) {
            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");

            long[] testList = createRandomIntegerList(inputSize);

            // Print array
            System.out.print(Arrays.toString(testList));


            System.out.println("...done.");
            System.out.print("    Running trial batch...");
            /* int length = testList.length;
            System.out.println("length: " + length); */

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random key to search in the range of a the min/max numbers in the list
                // ////////long testSearchKey = (long) (0 + Math.random() * (testList[testList.length-1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                count(testList);

                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n",inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
            System.out.println("count: " + cnt);
        }
    }

    /* return index of the searched number if found, or -1 if not found */
    public static int count(long[] list) {
        int N = list.length;
        int cnt = 0, sum=0;

        // Run MergeSort to sort the list
        mergeSort(list, N);

        // Taking the number of index i and subtracting the sum of that number with the next number
        // of index j
        for (int i = 0; i < N; ++i) {
            for (int j = i+1; j < N; ++j) {
                // Call binarySearch. We just need to find the number
                // that is the positive of the sum passed.
                int k = binarySearch(list, -(list[i] + list[j]));

                // If the found index is greater than j then a number was found
                // and we can increment cnt.
                if (k > j) {
                    System.out.println(list[i] + " " + list[j] + " " + list[k]);
                    ++cnt;
                }
            }
        }


        // Print array
        // System.out.print("Sorted: ");
        // System.out.print(Arrays.toString(list));

        // pos = posMin(list, N);
        // System.out.println("Position: " + pos);


        // System.out.println("count: " + cnt);
        /*
        for (int i = 0; i < N; ++i)
            for (int j = i + 1; j < N; ++j)
                for (int k = j+1; k < N; ++k)
                    if (list[i] + list[j] + list[k] == 0)
                        cnt++;

         */
        return cnt;
    }

    public static int binarySearch (long[] list, long sum) {
        int i = 0;
        int j= list.length-1;
        if (list[i] == sum) return i;
        if (list[j] == sum) return j;
        int k = (i+j)/2;
        while(j-i > 1){
            if (list[k]== sum) return k;
            else if (list[k] < sum) i=k;
            else j=k;
            k=(i+j)/2;
        }
        return -1;
    }

    public static void mergeSort(long[] list, int j) {
        int i = 0;
        int mid = j/2;
        long[] top = new long[mid];
        long[] bottom = new long[j - mid];

        if (j <= 1) {
            return;
        }
        for (i = 0; i < mid; ++i) {
            top[i] = list[i];
        }
        for (i = mid; i < j; ++i) {
            bottom[i-mid] = list[i];
        }
        j = j-mid;
        mergeSort(top, mid);
        mergeSort(bottom, j);
        merge(list, top, bottom, mid, j);
    }

    public static void merge(long[] list, long[] top, long[] bottom, int left, int right) {
        int i = 0, j = 0, k = 0;
        while (i < left && j < right) {
            if (top[i] <= bottom[j]) {
                list[k++] = top[i++];
            }
            else {
                list[k++] = bottom[j++];
            }
        }
        while (i < left) {
            list[k++] = top[i++];
        }
        while (j < right) {
            list[k++] = bottom[j++];
        }
    }


    public static long[] createRandomIntegerList(int size) {
        long[] newList = new long[size];
        for(int j=0;j<size;j++){
            newList[j] = (long)(MINVALUE + Math.random() * (MAXVALUE - MINVALUE));
        }

        return newList;

    }
}
