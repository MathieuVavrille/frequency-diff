package frequencyDiff.types;

import java.util.Set;
import java.util.HashSet;

public class Configuration {
	
  private Set<Feature> innerSet;
  private String signature; // Useful in ConfSet.class
	
  /*Creates a new empty Conf*/
  public Configuration() {
    innerSet = new HashSet<>();
    signature = "[]";
  }
	
  public Configuration(Set<Feature> newSet){
    innerSet = Set.copyOf(newSet); // immutable set
    Object[] signatureList = innerSet.stream().map(ft -> ft.toString()).sorted().toArray(); // Sorts and concatenates the elements of the set -> to be hashed right after.  
		
    // Efficient concatenation
		
    StringBuilder tmpSig = new StringBuilder("");
		
    Object c;
		
    if(signatureList.length>0) {
      for(int i = 0; i< signatureList.length-1; i++) {
        c = signatureList[i];
        tmpSig = tmpSig.append((String) c).append(",");
      }
      c = signatureList[signatureList.length-1];
      tmpSig = tmpSig.append((String) c);
    }
		
		
		
    signature = tmpSig.toString();
	
  }

  public boolean contains(final Feature feature) {
    return innerSet.contains(feature);
  }

  public Set<Feature> getInnerSet() {
    return Set.copyOf(innerSet);
  }
	
  public String getSignature() {
    return signature;
  }
	
  @Override
  public String toString() {
    return "[" + signature +"]"; // TODO -> Clean + and use StringBuilder
  }

  public Configuration copy(){ // TODO We could make another constructor in order to avoid to compute the hash twice
    return new Configuration(innerSet);
  }
	
  @Override
  public int hashCode() {
    return signature.hashCode();
  }
	
  @Override
  public boolean equals(Object obj) {
		
    if(this == obj) return true; // Same references
		
    if(obj == null || obj.getClass()!=this.getClass()) return false; // Different types
		
    Configuration tempConf = (Configuration) obj; // type casting (ClassCastException cannot happen because types have already been checked above)
    return tempConf.getSignature().equals(this.signature); // strings comparison
  }
	
  public Configuration union(Configuration addedConf) { // "immutable" union

    Set<Feature> newSet = new HashSet<>();
    newSet.addAll(this.getInnerSet());
    newSet.addAll(addedConf.getInnerSet());
    return new Configuration(newSet);
  }
	
  public boolean isEmpty() {
    return innerSet.isEmpty();
  }
}
