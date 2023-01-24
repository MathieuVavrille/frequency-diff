package frequencyDiff.constraints;

import frequencyDiff.types.*;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.List;
import java.util.Set;
import java.util.Map;

/**
00 | 1
01 | 0
10 | 0
11 | 1
*/
public class EquivCstr extends BinaryCrossConstraint {
  
  private EquivCstr(final CrossConstraint left, final CrossConstraint right) {
    super(left,right);
  }

  public static CrossConstraint of(final CrossConstraint left, final CrossConstraint right) {
    if (left instanceof TrueCstr)
      return right;
    else if (right instanceof TrueCstr)
      return left;
    else if (left instanceof FalseCstr)
      return NotCstr.of(right);
    else if (right instanceof FalseCstr)
      return NotCstr.of(left);
    return new EquivCstr(left, right);
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return left.isSatisfied(configuration) == right.isSatisfied(configuration);
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return left.getCPConstraint(featureToVar).iff(right.getCPConstraint(featureToVar));
  }

  @Override
  public String toString() {
    return "EQUIV("+left.toString()+","+right.toString()+")";
  }

  @Override
  public String toUVL() {
    return "("+left.toUVL()+" <=> "+right.toString()+")";
  }
}
