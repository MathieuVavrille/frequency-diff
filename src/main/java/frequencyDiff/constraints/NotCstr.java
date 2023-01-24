package frequencyDiff.constraints;

import frequencyDiff.types.*;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;

public class NotCstr extends CrossConstraint {
  private final Feature litteral;
  
  private NotCstr(final Feature litteral) {
    this.litteral = litteral;
  }

  public static CrossConstraint of(final CrossConstraint child) {
    if (child instanceof TrueCstr)
      return new FalseCstr();
    else if (child instanceof FalseCstr)
      return new TrueCstr();
    else if (child instanceof NotCstr)
      return new LitteralCstr(((NotCstr) child).litteral);
    else if (child instanceof AndCstr) {
      AndCstr andCstr = (AndCstr) child;
      return OrCstr.of(NotCstr.of(andCstr.left),NotCstr.of(andCstr.right));
    }
    else if (child instanceof OrCstr) {
      OrCstr orCstr = (OrCstr) child;
      return AndCstr.of(NotCstr.of(orCstr.left),NotCstr.of(orCstr.right));
    }
    else if (child instanceof LitteralCstr) {
      LitteralCstr litteralCstr = (LitteralCstr) child;
      return new NotCstr(litteralCstr.getFeature());
    }
    else
      throw new IllegalStateException("There is no more possibility of constraint.");
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return !configuration.contains(litteral);
  }

  @Override
  public void postCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    featureToVar.get(litteral).eq(0).post();
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return featureToVar.get(litteral).not();
  }

  @Override
  public Set<Feature> getVariables() {
    return Set.of(litteral);
  }

  @Override
  public String toString() {
    return "NOT("+litteral+")";
  }

  @Override
  public String toUVL() {
    return "!"+litteral;
  }
}
