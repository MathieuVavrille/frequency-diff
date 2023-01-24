package frequencyDiff.featureDiagram;

import frequencyDiff.types.*;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;

import org.javatuples.Triplet;

import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.NoSuchElementException;
import java.util.Random;
import java.io.IOException;
import java.io.FileWriter;
import java.math.BigInteger;

public abstract class FeatureDiagram {
	
  protected final static int PRECISION = 100;
	
  protected final Feature label;
  protected BigInteger nbConfigurations;
	
  public FeatureDiagram(final Feature label) {
    this.label = label;
    this.nbConfigurations = null;
  }
	
  public FeatureDiagram(String label) {
    this(new Feature(label));
  }

  /** Returns all the features in the FD */
  public abstract Set<Feature> getFeatures();
    
  /** Adds the constraints of the tree in the CP model. Also create the BoolVars associated to each feature.
   * @param model in which to add the constraints
   * @param featureToVar the mapping from features to variables of the model
   * @returns the BoolVar representing the root feature (also added in the map)
   */
  public abstract BoolVar addConstraints(final Model model, final Map<Feature,BoolVar> featureToVar);

  /**
   * Returns a map containing for every feature, the number of configurations satisfying the feature diagram containing this feature.
   */
  public Map<Feature,BigInteger> countSolutionsPerFeature(final boolean onlyLeaves) {
    final Map<Feature,BigInteger> map = new HashMap<Feature,BigInteger>();
    countSolutionsPerFeatureRec(map, BigInteger.ONE, onlyLeaves);
    return map;
  }

  /**
   * Recursively fill the map `solsPerFeature`. `factor` is the factor from the recursive calls to directly have the right count at the bottom of the tree.
   * @param solsPerFeature the map to be filled
   * @param factor the current factor to be multiplied
   * @param onlyLeaves set to true iff only the leaves should be added in the map
   */
  protected abstract void countSolutionsPerFeatureRec(final Map<Feature,BigInteger> solsPerFeature, final BigInteger factor, final boolean onlyLeaves);

  
  public abstract BigInteger count();
  
  public abstract ConfSet enumerate();
  
  public abstract Configuration sample(final Random random);

  public Feature getRootFeature() {
    return label;
  }

  /** Outputs the uvl format */
  public abstract String toUVL(final String indentation);
}
