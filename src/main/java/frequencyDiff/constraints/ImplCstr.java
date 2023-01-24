package frequencyDiff.constraints;

import frequencyDiff.types.*;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

/**
00 | 1
01 | 1
10 | 0
11 | 1
*/
public abstract class ImplCstr {
  
  public static CrossConstraint of(final CrossConstraint left, final CrossConstraint right) {
    if (left instanceof FalseCstr)
      return new TrueCstr();
    else if (left instanceof TrueCstr)
      return right;
    else if (right instanceof FalseCstr)
      return NotCstr.of(left);
    else if (right instanceof TrueCstr)
      return new TrueCstr();
    else
      return OrCstr.of(NotCstr.of(left),right);
  }
}
