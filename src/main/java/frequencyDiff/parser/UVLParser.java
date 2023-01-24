package frequencyDiff.parser;

import frequencyDiff.constraints.*;
import frequencyDiff.featureDiagram.*;
import frequencyDiff.FeatureModel;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class UVLParser {

  public static FeatureModel parse(final String fileName) {
    List<String> lines;
    try {
      BufferedReader inputFile = new BufferedReader(new FileReader(fileName));
      lines = inputFile.lines().collect(Collectors.toList());
      inputFile.close();
    }
    catch (Exception e) {
      throw new IllegalStateException("Bug while reading file");
    }
    List<ParsedLine> featureTree = new ArrayList<ParsedLine>();
    List<String> crossConstraints = new ArrayList<String>();
    boolean isFeatures = false;
    boolean isConstraints = false;
    for (String line : lines) {
      ParsedLine pl = ParsedLine.parseLine(line);
      if (pl != null) {
        if (pl.getIndentation() == 0 && pl.getLabel().equals("features"))
          isFeatures = true;
        else if (pl.getIndentation() == 0 && pl.getLabel().equals("constraints")) {
          isConstraints = true;
          isFeatures = false;
        }
        else if (isFeatures) {
          featureTree.add(pl);
        }
        else if (isConstraints) {
          crossConstraints.add(line.trim());//substring(pl.getIndentation()));
        }
      }
    }
    FeatureDiagram mainFD = parseFeatureList(featureTree, 0, featureTree.size(), 1).get(0);
    List<CrossConstraint> cc = crossConstraints.stream().map(s -> parseConstraint(s, 0, s.length())).collect(Collectors.toList());
    return new FeatureModel(mainFD, cc);
  }

  /** end excluded */
  private static List<FeatureDiagram> parseFeatureList(final List<ParsedLine> lines, final int start, final int end, final int indentationLevel) {
    List<FeatureDiagram> fds = new ArrayList<FeatureDiagram>();
    int current = start+1;
    int fdStart = start;
    while (current < end) {
      if (lines.get(current).getIndentation() == indentationLevel) {
        fds.add(parseFeature(lines, fdStart, current, indentationLevel));
        fdStart = current;
      }
      current++;
    }
    fds.add(parseFeature(lines, fdStart, end, indentationLevel));
    return fds;
  }
  
  /** end excluded */
  private static FeatureDiagram parseFeature(final List<ParsedLine> lines, final int start, final int end, final int indentationLevel) {
    if (start+1 == end)
      return new FDLeaf(lines.get(start).getLabel());
    String first = lines.get(start+1).getLabel();
    int current = start+2;
    while (current < end && lines.get(current).getIndentation() != indentationLevel+1)
      current++;
    if (current != end) // We are at a new group, we make the assumption that it is an optional, and suppose that mandatory is done first
      return new FDMandOpt(lines.get(start).getLabel(), parseFeatureList(lines, start+2, current, indentationLevel+2), parseFeatureList(lines, current+1, end, indentationLevel+2));
    else {
      switch (first) {
      case "mandatory":
        return new FDMandOpt(lines.get(start).getLabel(), parseFeatureList(lines, start+2, current, indentationLevel+2), List.of());
      case "optional":
        return new FDMandOpt(lines.get(start).getLabel(), List.of(), parseFeatureList(lines, start+2, end, indentationLevel+2));
      case "alternative":
        return new FDXor(lines.get(start).getLabel(), parseFeatureList(lines, start+2, end, indentationLevel+2));
      case "or":
        return new FDOr(lines.get(start).getLabel(), parseFeatureList(lines, start+2, end, indentationLevel+2));
      default:
        throw new UnsupportedOperationException("Not Implemented");
      }
    }
  }

  /** end excluded */
  private static int findHighestPriorityOperatorPosition(final String line, int start, int end) {
    int parenthesisDepth = 0;
    int priority = -1;
    int position = -1; // Priorities : !:0, &:1, |:2, =>:3, <=>:4
    int current = start;
    while (current < end) {
      switch (line.charAt(current)) {
      case '!':
        if (priority < 0 && parenthesisDepth == 0) {
          position = current;
          priority = 0;
        }
        break;
      case '&':
        if (priority < 1 && parenthesisDepth == 0) {
          position = current;
          priority = 1;
        }
        break;
      case '|':
        if (priority < 2 && parenthesisDepth == 0) {
          position = current;
          priority = 2;
        }
        break;
      case '=':
        if (priority < 3 && parenthesisDepth == 0) {
          position = current;
          priority = 3;
        }
        current++;
        break;
      case '<':
        if (priority < 4 && parenthesisDepth == 0) {
          position = current;
          priority = 4;
        }
        current += 2;
        break;
      case '(':
        parenthesisDepth++;
        break;
      case ')':
        parenthesisDepth--;
        if (parenthesisDepth < 0)
          throw new IllegalStateException("Parenthesis error of : " + line);
        break;
      default:
        break;
      }
      current++;
    }
    if (parenthesisDepth > 0)
      throw new IllegalStateException("Parenthesis error at the end of : " + line);
    return position;
  }
  
  /** end excluded. start and end are not final and modified at the start of the function to exclude whitespaces */
  private static CrossConstraint parseConstraint(final String line, int start, int end) {
    while (line.charAt(start) == ' ')
      start++;
    while (line.charAt(end-1) == ' ')
      end--;
    int priorityPos = findHighestPriorityOperatorPosition(line, start, end);
    if (priorityPos == -1) {
      if (line.charAt(start) == '(')
        return parseConstraint(line,start+1,end-1);
      else
        return LitteralCstr.boolOrVar(line.substring(start,end));
    }
    else if (priorityPos == start) // case Not
      return NotCstr.of(parseConstraint(line, start+1, end));
    else {
      switch (line.charAt(priorityPos)) {
      case '&':
        return AndCstr.of(parseConstraint(line, start, priorityPos), parseConstraint(line, priorityPos+1,end));
      case '|':
        return OrCstr.of(parseConstraint(line, start, priorityPos), parseConstraint(line, priorityPos+1,end));
      case '=':
        return ImplCstr.of(parseConstraint(line, start, priorityPos), parseConstraint(line, priorityPos+2,end)); // '+2' jump two elements !
      case '<':
        return EquivCstr.of(parseConstraint(line, start, priorityPos), parseConstraint(line, priorityPos+3,end)); // '+3' jump two elements !
      default:
        throw new IllegalStateException("No more case implemented ___ " + line + " ___ " + priorityPos + " " + line.charAt(priorityPos));
      }
    }
  }
  
}
