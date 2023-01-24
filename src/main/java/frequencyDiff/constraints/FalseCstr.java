package frequencyDiff.constraints;

import frequencyDiff.types.*;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.List;
import java.util.Set;
import java.util.Map;

public class FalseCstr extends CrossConstraint {

  @Override
  public Set<Feature> getVariables() {
    return Set.of();
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return false;
  }

  @Override
  public void postCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    throw new IllegalStateException("The boolean constants should have been removed");
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    throw new IllegalStateException("The boolean constants should have been removed");
  }

  @Override
  public String toString() {
    return "FALSE";
  }

  @Override
  public String toUVL() {
    return "false";
  }
}
