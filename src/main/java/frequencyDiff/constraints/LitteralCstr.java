package frequencyDiff.constraints;

import frequencyDiff.types.*;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class LitteralCstr extends CrossConstraint {
  private final Feature litteral;

  public LitteralCstr(final Feature litteral) {
    this.litteral = litteral;
  }

  public static CrossConstraint boolOrVar(final String litteral) {
    switch (litteral) {
    case "true":
      return new TrueCstr();
    case "false":
      return new FalseCstr();
    default:
      return new LitteralCstr(new Feature(litteral));
    }
  }

  @Override
  public Set<Feature> getVariables() {
    return Set.of(litteral);
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return configuration.contains(litteral);
  }

  @Override
  public void postCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    featureToVar.get(litteral).eq(1).post();
  }
  
  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return featureToVar.get(litteral);
  }

  public Feature getFeature() {
    return litteral;
  }

  @Override
  public String toString() {
    return litteral.toString();
  }

  @Override
  public String toUVL() {
    return litteral.toString();
  }
}
