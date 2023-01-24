package frequencyDiff.constraints;

import frequencyDiff.types.*;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class AndCstr extends BinaryCrossConstraint {
  
  private AndCstr(final CrossConstraint left, final CrossConstraint right) {
    super(left,right);
  }

  public static CrossConstraint of(final CrossConstraint left, final CrossConstraint right) {
    if (left instanceof FalseCstr || right instanceof FalseCstr)
      return new FalseCstr();
    else if (left instanceof TrueCstr)
      return right;
    else if (right instanceof TrueCstr)
      return left;
    return new AndCstr(left,right);
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return left.isSatisfied(configuration) && right.isSatisfied(configuration);
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return left.getCPConstraint(featureToVar).and(right.getCPConstraint(featureToVar));
  }

  @Override
  public String toString() {
    return "AND("+left.toString()+","+right.toString()+")";
  }

  @Override
  public String toUVL() {
    return "("+left.toUVL()+" & "+right.toUVL()+")";
  }
  
}
