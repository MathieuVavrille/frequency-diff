package frequencyDiff;

import frequencyDiff.types.*;
import frequencyDiff.constraints.CrossConstraint;
import frequencyDiff.featureDiagram.FeatureDiagram;
import frequencyDiff.parser.*;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;

import org.javatuples.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.math.BigInteger;

public class FeatureModel {

  private final FeatureDiagram featureDiagram;
  private final List<CrossConstraint> crossConstraints;
  private final StringIntLink siLink;

  private BigInteger nbConfigurations;

  public FeatureModel(final FeatureDiagram featureDiagram, final List<CrossConstraint> crossConstraints) {
    this(featureDiagram, crossConstraints, StringIntLink.fromSet(featureDiagram.getFeatures()));
  }

  public FeatureModel(final FeatureDiagram featureDiagram, final List<CrossConstraint> crossConstraints, final StringIntLink siLink) {
    this.featureDiagram = featureDiagram;
    this.crossConstraints = crossConstraints;
    this.siLink = siLink;
  }

  public static FeatureModel parse(final String fileName) {
    return UVLParser.parse(fileName);
  }

  public BigInteger count() {
    if(this.nbConfigurations == null) {
      if (crossConstraints.size() > 0)
        return BigInteger.valueOf(enumerate().size());
      else
        nbConfigurations = featureDiagram.count();
    }
    return nbConfigurations;
  }

  public ConfSet enumerate() { // TODO, improve by lowering the constraints, but requires a deeper implementation
    ConfSet notConstrained = featureDiagram.enumerate();
    for (CrossConstraint cstr : crossConstraints)
      notConstrained = cstr.filterConfSet(notConstrained);
    return notConstrained;
  }

  public Configuration sample(final Random random) {
    Configuration sample;
    boolean satisfiesAllConstraints;
    do {
      sample = featureDiagram.sample(random);
      satisfiesAllConstraints = true;
      for (CrossConstraint cstr : crossConstraints) {
        if (!cstr.isSatisfied(sample))
          satisfiesAllConstraints = false;
      }
    } while (!satisfiesAllConstraints);
    return sample;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(featureDiagram.toString());
    for (CrossConstraint cstr : crossConstraints) {
      builder.append("\n");
      builder.append(cstr.toString());
    }
    return builder.toString();
  }

  public String toUVL() {
    StringBuilder builder = new StringBuilder("namespace mainFD\n\nfeatures\n");
    builder.append(featureDiagram.toUVL("\t"));
    builder.append("\nconstraints\n");
    for (CrossConstraint cc : crossConstraints) {
      builder.append("\t"+cc.toUVL()+"\n");
    }
    return builder.toString();
  }
  
  public FeatureDiagram getFeatureDiagram() {
    return featureDiagram;
  }

  public List<CrossConstraint> getCrossConstraints() {
    return crossConstraints;
  }

  public StringIntLink getSiLink() {
    return siLink;
  }
}
