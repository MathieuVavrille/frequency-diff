package frequencyDiff.featureDiagram;

import frequencyDiff.types.*;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Triplet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;

public class FDOr extends FeatureDiagram {

  final List<FeatureDiagram> children;
  
  public FDOr(final Feature label, final List<FeatureDiagram> children) {
    super(label);
    this.children = children;
  }
  
  public FDOr(final String label, final List<FeatureDiagram> children) {
    this(new Feature(label), children);
  }

  @Override
  public Set<Feature> getFeatures() {
    Set<Feature> allFeatures = new HashSet<Feature>();
    allFeatures.add(label);
    for(FeatureDiagram child : children)
      allFeatures.addAll(child.getFeatures());
    return allFeatures;
  }

  @Override
  public BoolVar addConstraints(final Model model, final Map<Feature,BoolVar> featureToVar) {
    BoolVar mainVar = model.boolVar(label.getName());
    featureToVar.put(label, mainVar);
    BoolVar[] childrenVars = new BoolVar[children.size()];
    for (int i = 0; i < children.size(); i++) {
      childrenVars[i] = children.get(i).addConstraints(model, featureToVar);
      childrenVars[i].le(mainVar).post();
    }
    model.sum(childrenVars, ">=", mainVar).post();
    return mainVar;
  }

  @Override
  protected void countSolutionsPerFeatureRec(final Map<Feature,BigInteger> solsPerFeature, final BigInteger factor, final boolean onlyLeaves) {
    BigInteger selfCount = count();
    if (!onlyLeaves)
      solsPerFeature.put(label, selfCount.multiply(factor));
    children.stream().forEach(child -> child.countSolutionsPerFeatureRec(solsPerFeature, factor.multiply(selfCount.divide(child.count().add(BigInteger.ONE))), onlyLeaves)); // See formulas to understand this
  }

  @Override
  public BigInteger count() {
    if(this.nbConfigurations == null) {
      nbConfigurations = children.stream().map(x->x.count().add(BigInteger.ONE)).reduce(BigInteger.ONE, (a,b)-> a.multiply(b)).subtract(BigInteger.ONE);
    }
    return nbConfigurations;
  }
  
  @Override
  public ConfSet enumerate() {
    Configuration rootConf = new Configuration(Set.of(this.label)); 
    ConfSet root = new ConfSet(Set.of(rootConf));
    ConfSet result = root.expansion(children.stream().map(x -> x.enumerate().union(ConfSet.emptyCS())).reduce(ConfSet.emptyCS(),(a,b) -> a.expansion(b)));
    return result.without(rootConf);
  }

  @Override
  public Configuration sample(final Random random) {
    double draw; 
    double bound;
    Configuration result = new Configuration();
    while(result.isEmpty()) {
      for(FeatureDiagram fm : children) {
        bound = (BigDecimal.ONE).divide(new BigDecimal(fm.count().add(BigInteger.ONE)),PRECISION,RoundingMode.HALF_EVEN).doubleValue();
        draw = random.nextDouble();
        if(bound <= draw) {
          result = result.union(fm.sample(random));
        }
      }
    }
    return result.union(new Configuration(Set.of(this.label)));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(label.getName() + "-OR(");
    for (FeatureDiagram child : children) {
      builder.append(child.toString());
      builder.append(" ");
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String toUVL(final String baseIndentation) {
    StringBuilder builder = new StringBuilder(baseIndentation + label + "\n");
    builder.append("\t" + baseIndentation + "or" + "\n");
    for (FeatureDiagram child : children) {
      builder.append(child.toUVL("\t\t"+baseIndentation));
    }
    return builder.toString();
  }

}
