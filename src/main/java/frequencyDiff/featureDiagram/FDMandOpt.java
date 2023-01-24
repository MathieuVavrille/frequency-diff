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
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;


public final class FDMandOpt extends FeatureDiagram {
			
  final List<FeatureDiagram> mandChildren;
  final List<FeatureDiagram> optChildren;
	
  public FDMandOpt(final Feature label, final List<FeatureDiagram> mandChildren , final List<FeatureDiagram> optChildren) {
    super(label);
    this.mandChildren = mandChildren;
    this.optChildren = optChildren;
  }
	
  public FDMandOpt(final String label, final List<FeatureDiagram> mandChildren , final List<FeatureDiagram> optChildren) {
    super(label);
    this.mandChildren = mandChildren;
    this.optChildren = optChildren;
  }
	
  @Override
  public Set<Feature> getFeatures() {
    Set<Feature> allFeatures = new HashSet<Feature>();
    allFeatures.add(label);
    for(FeatureDiagram mandChild : mandChildren)
      allFeatures.addAll(mandChild.getFeatures());
    for(FeatureDiagram optChild : optChildren)
      allFeatures.addAll(optChild.getFeatures());
    return allFeatures;
  }

  @Override
  public BoolVar addConstraints(final Model model, final Map<Feature,BoolVar> featureToVar) {
    BoolVar mainVar = model.boolVar(label.getName());
    featureToVar.put(label, mainVar);
    for (FeatureDiagram mandChild : mandChildren) {
      BoolVar mandVar = mandChild.addConstraints(model, featureToVar);
      mandVar.eq(mainVar).post();
    }
    for (FeatureDiagram optChild : optChildren) {
      BoolVar optVar = optChild.addConstraints(model, featureToVar);
      optVar.le(mainVar).post();
    }
    return mainVar;
  }

  @Override
  protected void countSolutionsPerFeatureRec(final Map<Feature,BigInteger> solsPerFeature, final BigInteger factor, final boolean onlyLeaves) {
    BigInteger selfCount = count();
    if (!onlyLeaves)
      solsPerFeature.put(label, selfCount.multiply(factor));
    mandChildren.stream().forEach(mand -> mand.countSolutionsPerFeatureRec(solsPerFeature, factor.multiply(selfCount.divide(mand.count())), onlyLeaves));
    optChildren.stream().forEach(opt -> opt.countSolutionsPerFeatureRec(solsPerFeature, factor.multiply(selfCount.divide(opt.count().add(BigInteger.ONE))), onlyLeaves)); // See formulas to understand this
  }

  @Override
  public BigInteger count() {
    if(this.nbConfigurations == null) {
      BigInteger optCount;
      BigInteger mandCount;
      if(optChildren.isEmpty())
        optCount = BigInteger.ONE;
      else
        optCount = optChildren.stream().map(x -> x.count().add(BigInteger.ONE)).reduce((a,b)->a.multiply(b)).get();
      if(mandChildren.isEmpty())
        mandCount = BigInteger.ONE;
      else
        mandCount = mandChildren.stream().map(x -> x.count()).reduce((a,b)->a.multiply(b)).get();
      this.nbConfigurations = mandCount.multiply(optCount);
    }
    return nbConfigurations;
  }
  	
  public ConfSet enumerate() {
    ConfSet root = ConfSet.singletonCS(label);
    Stream<ConfSet> mandStream = Stream.empty(); 
    Stream<ConfSet> optStream = Stream.empty();	
    ConfSet result = new ConfSet();
    int nbEmpty = 0;
    // mandChildren and optChildren cannot be simultaneously empty
    if(mandChildren.isEmpty()) {
      result = ConfSet.expansion(optChildren.stream().map(x -> x.enumerate().union(ConfSet.emptyCS())).collect(Collectors.toList()));
      nbEmpty++;
    }
    else {
      mandStream = mandChildren.stream().map(x -> x.enumerate());
    }

    if(optChildren.isEmpty()) {
      result = ConfSet.expansion(mandChildren.stream().map(x -> x.enumerate()).collect(Collectors.toList()));
      nbEmpty++;
    }
    else {
      optStream = optChildren.stream().map(x -> x.enumerate().union(ConfSet.emptyCS()));
    }	
    switch(nbEmpty) {
    case 0:
      ConfSet tempMand = mandStream.reduce(ConfSet.emptyCS(), (a,b)->a.expansion(b));
      ConfSet tempOpt = optStream.reduce(ConfSet.emptyCS(), (a,b)->a.expansion(b));
      result = tempMand.expansion(tempOpt);
      break;
    case 1: 
      break;	
    default: // ~ case 0
      throw new NoSuchElementException("Both mandStream and optStrem cannot be empty");
    }
    return root.expansion(result);
  }
	
  public Configuration sample(final Random random) {
    Configuration result = new Configuration(Set.of(this.label));
    for(FeatureDiagram fm : mandChildren) {
      result = result.union(fm.sample(random));
    }
    for(FeatureDiagram fm : optChildren) {
      double bound = (BigDecimal.ONE).divide(new BigDecimal(fm.count().add(BigInteger.ONE)),PRECISION,RoundingMode.HALF_EVEN).doubleValue();
      double draw = random.nextDouble();
      if(bound <= draw) {
        result = result.union(fm.sample(random));
      }
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(label.getName() + "-MandOpt(");
    for (FeatureDiagram mandChild : mandChildren) {
      builder.append(mandChild.toString());
      builder.append(" ");
    }
    builder.append("|");
    for (FeatureDiagram optChild : optChildren) {
      builder.append(" ");
      builder.append(optChild.toString());
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String toUVL(final String baseIndentation) {
    StringBuilder builder = new StringBuilder(baseIndentation + label + "\n");
    if (mandChildren.size() > 0) {
    builder.append("\t" + baseIndentation + "mandatory" + "\n");
    for (FeatureDiagram mand : mandChildren)
      builder.append(mand.toUVL("\t\t"+baseIndentation));
    }
    if (optChildren.size() > 0) {
    builder.append("\t" + baseIndentation + "optional" + "\n");
    for (FeatureDiagram opt : optChildren)
      builder.append(opt.toUVL("\t\t"+baseIndentation));
    }
    return builder.toString();
  }

}
