package frequencyDiff.twise;

import frequencyDiff.types.Feature;

import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandom;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandomBound;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.BoolVar;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class RandomSearch extends AbstractStrategy<IntVar> implements IMonitorSolution, SolutionTimeRecorder{

  private final List<Map<Feature,Boolean>> solutions;
  private final Map<Feature,BoolVar> featureToVar;
  private final java.util.Random random;
  private final Random<IntVar> varSelector;
  private final IntValueSelector valueSelector;
  private final List<Long> solutionTime = new ArrayList<Long>();
  private Long startTime = System.nanoTime();

  public RandomSearch(final List<Map<Feature,Boolean>> solutions, final Map<Feature,BoolVar> featureToVar, final IntVar[] vars, final java.util.Random random) {
    super(vars);
    this.solutions = solutions;
    this.featureToVar = featureToVar;
    this.random = random;
    varSelector = new Random(random.nextLong());
    IntValueSelector value = new IntDomainRandom(random.nextLong());
    IntValueSelector bound = new IntDomainRandomBound(random.nextLong());
    valueSelector = var -> {
      if (var.hasEnumeratedDomain()) {
        return value.selectValue(var);
      } else {
        return bound.selectValue(var);
      }
    };
  }

  @Override
  protected Decision<IntVar> computeDecision(IntVar var) {
    return makeIntDecision(var,valueSelector.selectValue(var));
  }

  @Override
  public Decision<IntVar> getDecision() {
    IntVar decisionVar = varSelector.getVariable(vars);
    if (decisionVar != null)
      return computeDecision(decisionVar);
    return null;
  }

  
  
  @Override
  public void onSolution() {
    solutionTime.add(System.nanoTime()-startTime);
    Map<Feature,Boolean> currentSolution = featureToVar.entrySet().stream().collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().getValue() == 1));
    solutions.add(currentSolution);
  }

  public void setStartTime() {
    this.startTime = System.nanoTime();
  }
  
  public List<Long> getSolutionTime() {
    return solutionTime;
  }
}
