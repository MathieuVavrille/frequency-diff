package frequencyDiff.constraints;

import frequencyDiff.types.*;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public abstract class CrossConstraint {
    
  /** check whether `this` is satisfied by the configuration  */
  public abstract boolean isSatisfied(final Configuration configuration);

  public ConfSet filterConfSet(final ConfSet notConstrained) {
    Set<Configuration> filteredConfigurations = new HashSet<Configuration>();
    for (Configuration conf : notConstrained.getInnerSet()) {
      if (this.isSatisfied(conf))
        filteredConfigurations.add(conf);
    }
    return new ConfSet(filteredConfigurations);
  }
  
  /** Returns all the variables of the constraint */
  public abstract Set<Feature> getVariables();

  /** Posts the constraint in the solver */
  public abstract void postCPConstraint(final Map<Feature,BoolVar> featureToVar);
    
  /** Returns the ReExpression representing the crossConstraint */
  public abstract ReExpression getCPConstraint(final Map<Feature,BoolVar> featureToVar);
  
  public abstract String toUVL();
}
