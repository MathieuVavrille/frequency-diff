package frequencyDiff;

import frequencyDiff.featureDiagram.FeatureDiagram;
import frequencyDiff.twise.*;
import frequencyDiff.constraints.CrossConstraint;
import frequencyDiff.types.Feature;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.search.limits.SolutionCounter;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


import java.math.BigInteger;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.FileWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * @author Mathieu Vavrille
 * since 01/09/2022
 * @version 0.1
 * */

@Command(name = "HighestDiffStrategy", mixinStandardHelpOptions = true, version = "0.1", description = "Generates solutions by doing a strategy choosing the variable that has been sampled with a frequency far from its expected frequency")
public final class MainFrequencyDiff implements Runnable {
	
  @Option(names = {"-f", "--file"}, required = true, description = ".ulv file from which the feature model will be parsed.")
  private String inFile;
	
  @Option(names = {"-o", "--outfile"}, required = true, description = "Output file where the samples will be saved.")
  private String outFile;
	
  @Option(names = {"-s", "--samples"}, required = false, description = "Number of samples to draw.")
  private int nbSamples = 100;
	
  @Option(names = {"-l", "--luby_restarts"}, required = false, description = "Luby restart frequency. Default is 50.")
  private long lubyRestarts = 50L;

  // Parameters for FrequencyDiff
  @Option(names = {"--var_weight_exp"}, required = false, description = "Exponent on the variable weight during random choice. > 1. means that big weights are boosted, < 1. means that small weights are increased in comparison to big weights. Must be ]0, +inf[, default is 1.")
  private double varWeightExp = 1.;
  @Option(names = {"--val_weight_exp"}, required = false, description = "Exponent on the value weight during random choice. < 1. means that the ~logical~ choice is boosted (i.e. like the deterministic strategy), > 1. makes the choice closer to uniform (1/2 chance for one value, 1/2 chance for the other value). Must be ]0, +inf[, default is 1.")
  private double valWeightExp = 1.;

  // Parameters for TableSampling
  @Option(names = {"--table_nb_vars"}, required = false, description = "Number of variables in table (only used if -algo = table). Default is 2.")
  private int tableNbVars = 2;
  @Option(names = {"--table_pivot"}, required = false, description = "pivot value in TableSampling (only used if -algo = table). Default is 10.")
  private int tablePivot = 10;
  @Option(names = {"--table_proba"}, required = false, description = "Probability of tuple in table (only used if -algo = table). Default is 0.5.")
  private double tableProba = 0.5;

  @Option(names = {"-a", "--algo"}, required = false, description = "Algorithm to apply. It can be `frequency` for the FrequencyDiff strategy, `randomsearch` for the RandomSearch strategy, or `table` for TableSampling. By default it is `frequency`")
  private String algorithm = "frequency";

  @Option(names = {"-t", "--time"}, required = false, description = "Time limit for each sample, in seconds. No limit by default")
  private long timeLimit = 0L;

  @Option(names = {"-v", "--verbose"}, required = false, description = "Verbosity level. By default do not print anything.")
  private boolean verbose = false;

  @Option(names = {"-rs", "--random_seed"}, required = false, description = "The random seed used for the random instance. Default takes the processor time (System.nanoTime())")
  private Long seed = null;
  
  @Override
  public void run() {
    if (seed == null)
      seed = System.nanoTime();
    long startTime = System.nanoTime();
    FeatureModel fm = FeatureModel.parse(inFile);
    List<Map<Feature,Boolean>> solutions = new ArrayList<Map<Feature,Boolean>>();
    final Map<Feature,BoolVar> featureToVar = new HashMap<Feature,BoolVar>();
    Model model = generateModel(fm, featureToVar);
    Solver solver = model.getSolver();
    long parseTime = System.nanoTime();
    if (algorithm.equals("table")) {
      runTableSampling(solver, featureToVar, parseTime-startTime);
      return;
    }
    // Now either RandomSearch or 
    Map<Feature,BigInteger> totalSolsPerFeature = fm.getFeatureDiagram().countSolutionsPerFeature(false);
    long countingTime = System.nanoTime();
    SolutionTimeRecorder strat;
    if (algorithm.equals("randomsearch")) {
      RandomSearch rdStrat = new RandomSearch(solutions, featureToVar, model.retrieveIntVars(true), new Random(seed));
      solver.plugMonitor(rdStrat);
      solver.setSearch(rdStrat);
      strat = rdStrat;
    }
    else {
      FrequencyDiffStrategy frStrat = new FrequencyDiffStrategy(solutions, totalSolsPerFeature, fm.getFeatureDiagram().count(), featureToVar, varWeightExp, valWeightExp, new Random(seed), verbose);
      solver.plugMonitor(frStrat);
      solver.setSearch(frStrat);
      strat = frStrat;
    }
    solver.setRestartOnSolutions();
    solver.setNoGoodRecordingFromSolutions(model.retrieveIntVars(true));
    solver.setNoGoodRecordingFromRestarts();
    solver.setLubyRestart(lubyRestarts, new FailCounter(model, lubyRestarts), Integer.MAX_VALUE);
    strat.setStartTime();
    if (timeLimit == 0L)
      solver.findAllSolutions(new SolutionCounter(model, nbSamples));
    else
      solver.findAllSolutions(new SolutionCounter(model, nbSamples), new TimeCounter(model, timeLimit*1000000000L));
    long totalTime = System.nanoTime();
    List<Feature> features = featureToVar.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
    try {
      FileWriter myWriter = new FileWriter(outFile);
      myWriter.write(solutions.size() + " " + (parseTime - startTime) + " " + (countingTime-parseTime) + " " + (totalTime-countingTime)+"\n");
      myWriter.write(strat.getSolutionTime() + "\n");
      myWriter.write(features + "\n");
      for (int i = 0; i < solutions.size(); i++) {
        myWriter.write(i + ",");
        for (int j = 0; j < features.size(); j++)
          myWriter.write((solutions.get(i).get(features.get(j)) ? " " : " -") + (j+1));
        myWriter.write("\n");
      }
      myWriter.close();
    }
    catch (IOException e) {
      System.out.println("Cannot write to file");
    }
  }

  private void runTableSampling(final Solver solver, final Map<Feature,BoolVar> featureToVar, final long parseTime) {
    long startTime = System.nanoTime();
    List<Feature> features = featureToVar.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
    Stream<Solution> solStream = solver.tableSampling(tablePivot, tableNbVars, tableProba, new Random(seed));
    List<Solution> solutions = solStream.limit(100).collect(Collectors.toList());
    long endTime = System.nanoTime();
    try {
      FileWriter myWriter = new FileWriter(outFile);
      myWriter.write(solutions.size() + " " + parseTime + " 0 " + (endTime - startTime) + "\n");
      myWriter.write("[]\n");// No list of times for table sampling
      myWriter.write(features + "\n");
      for (int i = 0; i < solutions.size(); i++) {
        myWriter.write(i + ",");
        for (int j = 0; j < features.size(); j++)
          myWriter.write((solutions.get(i).getIntVal(featureToVar.get(features.get(j))) == 1 ? " " : " -") + (j+1));
        myWriter.write("\n");
      }
      myWriter.close();
    }
    catch (IOException e) {
      System.out.println("Cannot write to file");
    }
  }
	
  public static void main(String[] args) {
    int exitCode = new CommandLine(new MainFrequencyDiff()).execute(args);
    System.exit(exitCode);
  }

  private Model generateModel(final FeatureModel fm, final Map<Feature,BoolVar> featureToVar) {
    final Model model = new Model("Generated");
    fm.getFeatureDiagram().addConstraints(model, featureToVar).eq(1).post();
    for (CrossConstraint cstr : fm.getCrossConstraints()) {
      cstr.postCPConstraint(featureToVar);
    }
    return model;
  }
		
}
