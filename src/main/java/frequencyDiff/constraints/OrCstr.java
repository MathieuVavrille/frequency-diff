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

public class OrCstr extends BinaryCrossConstraint {
  
  private OrCstr(final CrossConstraint left, final CrossConstraint right) {
    super(left,right);
  }

  public static CrossConstraint of(final CrossConstraint left, final CrossConstraint right) {
    if (left instanceof TrueCstr || right instanceof TrueCstr)
      return new TrueCstr();
    else if (left instanceof FalseCstr)
      return right;
    else if (right instanceof FalseCstr)
      return left;
    else
      return new OrCstr(left, right);
  }

  @Override
  public boolean isSatisfied(final Configuration configuration) {
    return left.isSatisfied(configuration) || right.isSatisfied(configuration);
  }

  @Override
  public ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar) {
    return left.getCPConstraint(featureToVar).or(right.getCPConstraint(featureToVar));
  }

  @Override
  public String toString() {
    return "OR("+left.toString()+","+right.toString()+")";
  }

  @Override
  public String toUVL() {
    return "("+left.toUVL()+" | "+right.toUVL()+")";
  }
}
