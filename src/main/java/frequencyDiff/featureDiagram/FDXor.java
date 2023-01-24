package frequencyDiff.featureDiagram;

import frequencyDiff.types.*;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Triplet;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public final class FDXor extends FeatureDiagram{

  final List<FeatureDiagram> children;
  
  public FDXor(final Feature label, final List<FeatureDiagram> children) {
    super(label);
    this.children = children;
  }
  
  public FDXor(final String label, final List<FeatureDiagram> children) {
    this(new Feature(label),children);
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
    }
    model.sum(childrenVars, "=", mainVar).post();
    return mainVar;
  }

  @Override
  protected void countSolutionsPerFeatureRec(final Map<Feature,BigInteger> solsPerFeature, final BigInteger factor, final boolean onlyLeaves) {
    BigInteger selfCount = count();
    if (!onlyLeaves)
      solsPerFeature.put(label, selfCount.multiply(factor));
    children.stream().forEach(child -> child.countSolutionsPerFeatureRec(solsPerFeature, factor, onlyLeaves)); // See formulas to understand this
  }
  
  @Override
  public BigInteger count() {
    if(this.nbConfigurations == null) {
      nbConfigurations = children.stream().map(x->x.count()).reduce(BigInteger.ZERO, (a,b)-> a.add(b));
    }
    return nbConfigurations;
  }
    
  @Override
  public ConfSet enumerate() {
    ConfSet root = ConfSet.singletonCS(this.label);
    return root.expansion(children.stream().map(x -> x.enumerate()).reduce(new ConfSet(),(a,b) -> a.union(b)));
  }
	
  @Override
  public Configuration sample(final Random random) {
    Configuration result = new Configuration(Set.of(this.label));
    result = result.union(this.choose(random).sample(random));
    return result;
  }
	
  private FeatureDiagram choose(final Random random){
    double r = random.nextDouble();
    BigDecimal nbConf = new BigDecimal(this.count());
    Object[] childs = children.stream().map(x -> new BigDecimal(x.count())).toArray();
    int i = 0;
    BigDecimal child;
    while(r >= 0) {
      child = (BigDecimal) childs[i];
      double p = child.divide(nbConf,PRECISION,RoundingMode.HALF_EVEN).doubleValue();
      r -= p;
      i++;
    }
    return children.get(i-1);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(label.getName()+"-XOR(");
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
    builder.append("\t" + baseIndentation + "alternative" + "\n");
    for (FeatureDiagram child : children) {
      builder.append(child.toUVL("\t\t"+baseIndentation));
    }
    return builder.toString();
  }

}
