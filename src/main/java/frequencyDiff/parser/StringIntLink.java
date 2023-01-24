package frequencyDiff.parser;

import frequencyDiff.types.Feature;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/** Transform strings to integers, and store the mappings */
public class StringIntLink {

  private final Map<String,Integer> stringToInt = new HashMap<String,Integer>();
  private final List<String> intToString = new ArrayList<String>();

  public static StringIntLink fromSet(final Set<Feature> features) {
    StringIntLink res = new StringIntLink();
    for (Feature s : features)
      res.addString(s.getName());
    return res;
  }
  
  public void addString(final String s) {
    stringToInt.put(s, intToString.size());
    intToString.add(s);
  }
  /** Creates a dummy variable, and returns its index */
  public int createDummy() {
    int v = intToString.size();
    String s = "_dummy_"+v;
    stringToInt.put(s,v);
    intToString.add(s);
    return v;
  }
  
  public String getString(final int i) {
    return intToString.get(i);
  }
  public int getInt(final String s) {
    if (!stringToInt.containsKey(s)) {
      addString(s);
    }
    return intToString.size()-1;
  }

  public String toDimacsComments() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < intToString.size(); i++) {
      builder.append("c "+ (i+1) + " " + intToString.get(i) + "\n");
    }
    return builder.toString();
  }

  public int size() {
    return intToString.size();
  }

  @Override
  public String toString() {
    return intToString.toString();
  }
}
