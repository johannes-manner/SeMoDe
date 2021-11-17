package de.uniba.dsg.serverless.pipeline.calibration;

import de.uniba.dsg.serverless.pipeline.calibration.model.LinpackResult;
import de.uniba.dsg.serverless.pipeline.model.config.MachineConfig;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to only parse the Linpack results locally and on providers' platfrom. See {@link #parseLinpack()}.
 * <p>
 * Different outputs are possible for the linpack calibration...
 * <p>
 * FILE 1:
 * <p>
 * btime 1614011721
 * <p>
 * model		: 63
 * model name	: Intel(R) Xeon(R) Processor @ 2.50GHz
 * model		: 63
 * model name	: Intel(R) Xeon(R) Processor @ 2.50GHz
 * <p>
 * This is a SAMPLE run script for SMP LINPACK. Change it to reflect
 * the correct number of CPUs/threads, problem input files, etc..
 * Mon Feb 22 16:50:01 UTC 2021
 * Intel(R) Optimized LINPACK Benchmark data
 * <p>
 * Current date/time: Mon Feb 22 16:50:01 2021
 * <p>
 * CPU frequency:    3.086 GHz
 * Number of CPUs: 1
 * Number of cores: 2
 * Number of threads: 2
 * <p>
 * Parameters are set to:
 * <p>
 * Number of tests: 11
 * Number of equations to solve (problem size) : 1000  1500  2000  3000  4000  5000  6000  8000  10000 12000 15000
 * Leading dimension of array                  : 1000  2000  5008  10000 15000 18008 20016 22008 25000 26000 27000
 * Number of trials to run                     : 4     2     2     2     2     2     2     2     2     2     1
 * Data alignment value (in Kbytes)            : 4     4     4     4     4     4     4     4     4     4     4
 * <p>
 * Maximum memory requested that can be used=2496524096, at the size=12000
 * <p>
 * =================== Timing linear equation system solver ===================
 * <p>
 * Size   LDA    Align. Time(s)    GFlops   Residual     Residual(norm) Check
 * 1000   1000   4      0.051      13.1372  9.642009e-13 3.288173e-02   pass
 * 1000   1000   4      0.030      22.2684  9.394430e-13 3.203742e-02   pass
 * 1000   1000   4      0.034      19.8667  1.023182e-12 3.489312e-02   pass
 * 1000   1000   4      0.021      31.4962  9.394430e-13 3.203742e-02   pass
 * 1500   2000   4      0.097      23.1767  2.225962e-12 3.378536e-02   pass
 * 1500   2000   4      0.094      23.8950  2.544909e-12 3.862628e-02   pass
 * 2000   5008   4      0.178      29.9839  4.085732e-12 3.554086e-02   pass
 * 2000   5008   4      0.179      29.9230  4.085732e-12 3.554086e-02   pass
 * 3000   10000  4      0.596      30.2194  9.672485e-12 3.724642e-02   pass
 * 3000   10000  4      0.583      30.9244  9.672485e-12 3.724642e-02   pass
 * 4000   15000  4      1.340      31.8550  2.049588e-11 4.467271e-02   pass
 * 4000   15000  4      1.342      31.8190  2.049588e-11 4.467271e-02   pass
 * 5000   18008  4      2.620      31.8284  2.368622e-11 3.302852e-02   pass
 * 5000   18008  4      2.595      32.1328  2.368622e-11 3.302852e-02   pass
 * 6000   20016  4      4.372      32.9555  3.625171e-11 3.515639e-02   pass
 * 6000   20016  4      4.376      32.9233  3.625171e-11 3.515639e-02   pass
 * 8000   22008  4      10.334     33.0430  6.635287e-11 3.649983e-02   pass
 * 8000   22008  4      10.318     33.0927  6.635287e-11 3.649983e-02   pass
 * 10000  25000  4      20.342     32.7828  9.899764e-11 3.490757e-02   pass
 * 10000  25000  4      20.018     33.3135  9.899764e-11 3.490757e-02   pass
 * Done: Mon Feb 22 16:51:49 UTC 2021
 * <p>
 * <p>
 * <p>
 * FILE 2
 * btime 1614010798
 * <p>
 * model		: 63
 * model name	: Intel(R) Xeon(R) Processor @ 2.50GHz
 * model		: 63
 * model name	: Intel(R) Xeon(R) Processor @ 2.50GHz
 * <p>
 * This is a SAMPLE run script for SMP LINPACK. Change it to reflect
 * the correct number of CPUs/threads, problem input files, etc..
 * Mon Feb 22 16:49:40 UTC 2021
 * Intel(R) Optimized LINPACK Benchmark data
 * <p>
 * Current date/time: Mon Feb 22 16:49:40 2021
 * <p>
 * CPU frequency:    3.071 GHz
 * Number of CPUs: 1
 * Number of cores: 2
 * Number of threads: 2
 * <p>
 * Parameters are set to:
 * <p>
 * Number of tests: 11
 * Number of equations to solve (problem size) : 1000  1500  2000  3000  4000  5000  6000  8000  10000 12000 15000
 * Leading dimension of array                  : 1000  2000  5008  10000 15000 18008 20016 22008 25000 26000 27000
 * Number of trials to run                     : 4     2     2     2     2     2     2     2     2     2     1
 * Data alignment value (in Kbytes)            : 4     4     4     4     4     4     4     4     4     4     4
 * <p>
 * Maximum memory requested that can be used=2496524096, at the size=12000
 * <p>
 * =================== Timing linear equation system solver ===================
 * <p>
 * Size   LDA    Align. Time(s)    GFlops   Residual     Residual(norm) Check
 * 1000   1000   4      0.037      17.8584  9.394430e-13 3.203742e-02   pass
 * 1000   1000   4      0.030      22.0642  9.394430e-13 3.203742e-02   pass
 * 1000   1000   4      0.022      31.0926  9.394430e-13 3.203742e-02   pass
 * 1000   1000   4      0.029      22.8054  9.394430e-13 3.203742e-02   pass
 * 1500   2000   4      0.073      30.6844  2.544909e-12 3.862628e-02   pass
 * 1500   2000   4      0.081      27.7667  2.544909e-12 3.862628e-02   pass
 * 2000   5008   4      0.157      33.9881  4.085732e-12 3.554086e-02   pass
 * 2000   5008   4      0.154      34.6980  4.085732e-12 3.554086e-02   pass
 * 3000   10000  4      0.516      34.8988  9.672485e-12 3.724642e-02   pass
 * 3000   10000  4      0.513      35.1229  9.672485e-12 3.724642e-02   pass
 * 4000   15000  4      1.182      36.1326  2.049588e-11 4.467271e-02   pass
 * 4000   15000  4      1.160      36.7975  2.049588e-11 4.467271e-02   pass
 * 5000   18008  4      2.263      36.8399  2.368622e-11 3.302852e-02   pass
 * 5000   18008  4      2.219      37.5745  2.368622e-11 3.302852e-02   pass
 * 6000   20016  4      3.796      37.9551  3.625171e-11 3.515639e-02   pass
 * 6000   20016  4      3.899      36.9555  3.625171e-11 3.515639e-02   pass
 * 8000   22008  4      9.070      37.6475  6.635287e-11 3.649983e-02   pass
 * 8000   22008  4      8.737      39.0815  6.635287e-11 3.649983e-02   pass
 * 10000  25000  4      16.742     39.8315  9.899764e-11 3.490757e-02   pass
 * 10000  25000  4      16.744     39.8282  9.899764e-11 3.490757e-02   pass
 * 12000  26000  4      29.687     38.8149  1.344501e-10 3.300057e-02   pass
 * 12000  26000  4      29.853     38.5990  1.344501e-10 3.300057e-02   pass
 * <p>
 * Performance Summary (GFlops)
 * <p>
 * Size   LDA    Align.  Average  Maximal
 * 1000   1000   4       23.4552  31.0926
 * 1500   2000   4       29.2256  30.6844
 * 2000   5008   4       34.3431  34.6980
 * 3000   10000  4       35.0108  35.1229
 * 4000   15000  4       36.4650  36.7975
 * 5000   18008  4       37.2072  37.5745
 * 6000   20016  4       37.4553  37.9551
 * 8000   22008  4       38.3645  39.0815
 * 10000  25000  4       39.8298  39.8315
 * 12000  26000  4       38.7069  38.8149
 * <p>
 * Residual checks PASSED
 * <p>
 * End of tests
 * <p>
 * Done: Mon Feb 22 16:52:19 UTC 2021
 *
 * @author mendress, jmanner
 */
@Slf4j
public class LinpackParser {

    private final Path linpackCalibrationPath;

    public LinpackParser(final Path linpackCalibrationPath) {
        this.linpackCalibrationPath = linpackCalibrationPath;
    }

    /**
     * Parses the Linpack benchmark/calibration result due to the fixed structure of the Linpack output written to the
     * logFiles.
     *
     * @return the average GFLOPS performance for 10000 equations to solve with an array of 25000 leading dimensions.
     * @throws SeMoDeException if the file can't be read.
     */
    public LinpackResult parseLinpack() throws SeMoDeException {
        if (!Files.exists(this.linpackCalibrationPath)) {
            throw new SeMoDeException("Calibration benchmark file does not exist.");
        }
        try {
            final List<String> lines = Files.readAllLines(this.linpackCalibrationPath);
            // find the last line in the "linear equation solver" part
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).equals("Size   LDA    Align. Time(s)    GFlops   Residual     Residual(norm) Check")) {
                    return this.extractAverageOfGflops(lines, i + 1);
                }
            }
            throw new SeMoDeException();
        } catch (final IOException | SeMoDeException | NoSuchElementException e) {
            throw new SeMoDeException("An error occured during parsing of the file " + this.linpackCalibrationPath.toString());
        }
    }

    /**
     * As shown at the class level comment here, the output of the linpack computation is sometimes different.
     * That is not a problem since the size of equations and the dimension of array is high enough, but the summary is not printed.
     * <p>
     * This method extracts the average of GFLOPS for the highest number of computations.
     *
     * @param lines of the linpack result file
     * @param i     start of the equations section withing the line file
     * @return
     */
    private LinpackResult extractAverageOfGflops(List<String> lines, int i) throws SeMoDeException, NoSuchElementException {
        // key is the number of size of the system solver
        Map<Integer, GflopAndExecutionTimeLists> sizeMapTimeAndGlops = new HashMap<>();
        String[] lineSplit = lines.get(i).split("\\s+");
        while (lineSplit.length == 8) {
            int size = Integer.parseInt(lineSplit[0]);
            double executionTime = Double.parseDouble(lineSplit[3]);
            double gflops = Double.parseDouble(lineSplit[4]);

            if (!sizeMapTimeAndGlops.containsKey(size)) {
                sizeMapTimeAndGlops.put(size, new GflopAndExecutionTimeLists());
            }

            sizeMapTimeAndGlops.get(size).addGflopsAndExecTime(gflops, executionTime);

            // reinitialize
            i++;
            lineSplit = lines.get(i).split("\\s+");
        }

        Optional<Integer> maxSize = sizeMapTimeAndGlops.keySet().stream().max(Integer::compareTo);
        if (maxSize.isEmpty()) {
            throw new SeMoDeException();
        }

        GflopAndExecutionTimeLists summary = sizeMapTimeAndGlops.get(maxSize.get());
        MachineConfig machineConfig = extractMachineConfig(lines);
        return new LinpackResult(summary.getGflopsAverage(), summary.getExecutionTimeAverage(), machineConfig);
    }

    private MachineConfig extractMachineConfig(List<String> lines) {
        MachineConfig config = new MachineConfig();
        List<String> cpuInfo = lines.stream().filter(s -> s.startsWith("model")).distinct().collect(Collectors.toList());
        if (cpuInfo.isEmpty() == false) {
            config.setModelNr(cpuInfo.get(0).split(":")[1].trim());
            config.setCpuModelName(cpuInfo.get(1).split(":")[1].trim());
        }
        return config;
    }

    private class GflopAndExecutionTimeLists {
        private List<Double> gflops;
        private List<Double> executionTime;

        GflopAndExecutionTimeLists() {
            this.gflops = new ArrayList<>();
            this.executionTime = new ArrayList<>();
        }

        void addGflopsAndExecTime(double gflops, double execTime) {
            this.gflops.add(gflops);
            this.executionTime.add(execTime);
        }

        double getGflopsAverage() {
            return this.gflops.stream().mapToDouble(d -> d).average().getAsDouble();
        }

        double getExecutionTimeAverage() {
            return this.executionTime.stream().mapToDouble(d -> d).average().getAsDouble();
        }
    }

}
