package frequencyDiff.twise;

import frequencyDiff.types.Feature;


import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;


import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Random;



public class RandomFrequencyStrategy extends AbstractStrategy<IntVar> implements IMonitorSolution, SolutionTimeRecorder {

  
  private final List<Map<Feature,Boolean>> solutions;
  private final Map<Feature,Double> featureFrequency; // theoretical frequency
  private final Map<Feature,BoolVar> featureToVar;
  private Map<Feature,Integer> foundSolsPerFeature;
  private Map<Feature,Double> frequencyDifference; // observed-theoretical
  private final double varWeightExp;
  private final double valWeightExp;
  private final Random random;
  private final boolean verbose;
  private final List<Long> solutionTime = new ArrayList<Long>();
  private Long startTime = System.nanoTime();

  public RandomFrequencyStrategy(final List<Map<Feature,Boolean>> solutions, final Map<Feature,BigInteger> totalSolsPerFeature, final BigInteger totalCount, final Map<Feature,BoolVar> featureToVar, final double varWeightExp, final double valWeightExp, final Random random, final boolean verbose) {
    this.solutions = solutions;
    BigDecimal totalCountDouble = new BigDecimal(totalCount);
    this.featureFrequency = totalSolsPerFeature.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new BigDecimal(e.getValue()).divide(totalCountDouble, 100, RoundingMode.HALF_DOWN).doubleValue()));
    this.featureToVar = featureToVar;
    this.foundSolsPerFeature = totalSolsPerFeature.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> 0));
    this.frequencyDifference = totalSolsPerFeature.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> 1.));
    this.varWeightExp = varWeightExp;
    this.valWeightExp = valWeightExp;
    this.random = random;
    this.verbose = verbose;
  }
  

  @Override
  public Decision<IntVar> getDecision() {
    List<Feature> uninstantiatedFeatures = new ArrayList<Feature>();
    List<Double> absFrequency = new ArrayList<Double>();
    for (Map.Entry<Feature, Double> diff : frequencyDifference.entrySet()) {
      if (!featureToVar.get(diff.getKey()).isInstantiated()) {
        uninstantiatedFeatures.add(diff.getKey());
        absFrequency.add(Math.abs(diff.getValue()));
      }
    }
    if (uninstantiatedFeatures.size() == 0)
      return null;
    List<Double> weights = weightFunction(absFrequency);
    int randomID = getRandomWeightedID(weights);
    Feature chosenFeature = uninstantiatedFeatures.get(randomID);
    int chosenValue;
    if (2*random.nextDouble() < 1+Math.pow(absFrequency.get(randomID), valWeightExp)) // logical choice. The higher the absolute difference, the higher the probability
      chosenValue = frequencyDifference.get(chosenFeature) > 0 ? 0 : 1;
    else
      chosenValue = frequencyDifference.get(chosenFeature) > 0 ? 1 : 0; // opposite choice, with lower probability
    //System.out.println(featureToVar.get(chosenFeature) + " " + chosenValue);
    return makeIntDecision(featureToVar.get(chosenFeature), chosenValue);
  }

  private int getRandomWeightedID(final List<Double> weights) {
    double sum = weights.stream().mapToDouble(e -> e).sum();
    if (sum == 0.)
      return random.nextInt(weights.size());
    double rdValue = sum*random.nextDouble();
    int i = 0;
    while (weights.get(i) < rdValue) {
      rdValue -= weights.get(i);
      i++;
    }
    return i;
  }

  private List<Double> weightFunction(List<Double> values) {
    return values.stream().map(v -> Math.pow(v, varWeightExp)).collect(Collectors.toList());
  }

  @Override
  public void onSolution() {
    solutionTime.add(System.nanoTime()-startTime);
    Map<Feature,Boolean> currentSolution = featureToVar.entrySet().stream().collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().getValue() == 1));
    for (Map.Entry<Feature,Boolean> entry : currentSolution.entrySet())
      if (entry.getValue())
        foundSolsPerFeature.put(entry.getKey(), foundSolsPerFeature.get(entry.getKey()) + 1);
    solutions.add(currentSolution);
    recomputeDifferences();
    if (verbose)
      System.out.println(solutions.size());
  }

  private void recomputeDifferences() {
    for (Map.Entry<Feature,Double> theoretical : featureFrequency.entrySet()) {
      this.frequencyDifference.put(theoretical.getKey(), foundSolsPerFeature.get(theoretical.getKey())/((double) solutions.size()) - theoretical.getValue());
    }
  }

  public void setStartTime() {
    this.startTime = System.nanoTime();
  }

  public List<Long> getSolutionTime() {
    return solutionTime;
  }
  
  /** Returns one variable. There is no property, and it should only be used as a placeholder */
  private BoolVar getOneVar() {
    for (Map.Entry<Feature,BoolVar> entry : featureToVar.entrySet())
      return entry.getValue();
    throw new IllegalStateException("There are no variables");
  }
}
