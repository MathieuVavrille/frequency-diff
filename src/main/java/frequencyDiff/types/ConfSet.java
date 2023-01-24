/**
 * 
 */
package frequencyDiff.types;

import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ConfSet {
	
  private Set<Configuration> innerSet;
  //private String signature;
	
  public ConfSet() {
    innerSet = new HashSet<Configuration> ();
    //signature = "";
  }
	
  public ConfSet(Set<Configuration> newSet) {
    innerSet = Set.copyOf(newSet);
    //Object[] signatureList = innerSet.stream().map(x->x.getSignature()).sorted().toArray();
		
    // In order to concatenate efficiently
    /*StringBuilder tmpSig = new StringBuilder("");
    Object c;
		
    if(signatureList.length > 0) {
      for(int i = 0; i< signatureList.length-1; i++) {
        c = signatureList[i];
        tmpSig = tmpSig.append("[" + (String) c).append("],");
      }
    }
				
    c = signatureList[signatureList.length-1];
    tmpSig = tmpSig.append("[" + (String) c).append("]");
		
    signature = tmpSig.toString();*/
  }
	
  public ConfSet createConfSetfromRaw(final Set<Set<Feature>> newSet){ // otherwise, we can't have both ConfSet(Set<Set<Feature>> newSet) and ConfSet(Set<Conf> newSet) in the same time
    Set<Set<Feature>> tmpSet = Set.copyOf(newSet); // immutable set
    Set<Configuration> tmpSet2 = Collections.unmodifiableSet(tmpSet.stream().map(x -> new Configuration(x)).collect(Collectors.toSet())); // Almost the same principle as in Conf.class
    return new ConfSet(tmpSet2);
  }
	
  public static ConfSet singletonCS(final Feature feature) { // for the rootFeatures
    return new ConfSet(Set.of(new Configuration(Set.of(feature))));
  }
	
  public static ConfSet emptyCS() {
    return new ConfSet(Set.of(new Configuration()));
  }
	
  public Set<Configuration> getInnerSetPtr(){
    return innerSet;
  }
	
  public Set<Configuration> getInnerSet(){
    return Set.copyOf(innerSet);
  }
	
  public ConfSet union(final ConfSet addedConf) {
    Set<Configuration> newSet = new HashSet<>(); // new empty set : Set<Configuration> 
    newSet.addAll(this.innerSet);
    newSet.addAll(addedConf.getInnerSet());
    return new ConfSet(newSet);
  }
	
  public int size() {
    return innerSet.size();
  }
	
  public ConfSet expansion(ConfSet cs2) {
    Set<Configuration> set2 = cs2.getInnerSet();
    Configuration tmpNewSet;
    Set<Configuration> newConfSet = new HashSet<Configuration>();
    for(Configuration c1 : innerSet) {
      for(Configuration c2: set2) {
        tmpNewSet = c1.union(c2);
        newConfSet.add(tmpNewSet);
      }
    }
    return new ConfSet(newConfSet);
  }
	
  public static ConfSet expansion(List<ConfSet> listCS) {
    ConfSet result = new ConfSet(Set.of(new Configuration()));
		
    for(ConfSet cs : listCS) {
      result = result.expansion(cs);
    }
    return result;
  }
  // TODO: EXPANSION BY CARDINALITY
	
  public ConfSet without(final Configuration c) {
    if (!innerSet.contains(c)) {
      return new ConfSet(innerSet);
    }else {
      Set<Configuration> result = new HashSet<>();
      result.addAll(innerSet);
      result.remove(c);
      return new ConfSet(result);
    }
  }

  @Override
  public String toString() {
    return innerSet.toString();
  }
}
