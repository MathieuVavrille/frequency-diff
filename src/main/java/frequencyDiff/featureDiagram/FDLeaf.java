package frequencyDiff.featureDiagram;

import frequencyDiff.types.*;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Triplet;

import java.math.BigInteger;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Random;

public final class FDLeaf extends FeatureDiagram {
  
  public FDLeaf(final Feature label) {
    super(label);
  }
  
  public FDLeaf(String label) {
    this(new Feature(label));
  }

  @Override
  public Set<Feature> getFeatures() {
    return Set.of(label);
  }

  @Override
  public BoolVar addConstraints(final Model model, final Map<Feature,BoolVar> featureToVar) {
    BoolVar mainVar = model.boolVar(label.getName());
    featureToVar.put(label, mainVar);
    return mainVar;
  }

  @Override
  protected void countSolutionsPerFeatureRec(final Map<Feature,BigInteger> solsPerFeature, final BigInteger factor, final boolean onlyLeaves) {
    solsPerFeature.put(label, factor); // There is only one solution on the subtree, so just put the factor to account for the recursive calls
  }
  
  @Override
  public BigInteger count() {
    if(this.nbConfigurations == null) {
      this.nbConfigurations = BigInteger.ONE;
    } 
    return nbConfigurations;
  }

  @Override
  public ConfSet enumerate() {
    return ConfSet.singletonCS(this.label);
  }

  @Override
  public Configuration sample(final Random random) {
    return new Configuration(Set.of(label));
  }

  @Override
  public String toString() {
    return label.toString();
  }

  @Override
  public String toUVL(final String baseIndentation) {
    return baseIndentation + label + "\n";
  }

}
