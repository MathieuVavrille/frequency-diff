package frequencyDiff.parser;

public class ParsedLine {

  private final int indentationLevel;
  private final String label;
  //private final Attributes attributes;

  private ParsedLine(final int indentationLevel, final String label) {
    this.indentationLevel = indentationLevel;
    this.label = label;
  }

  public static ParsedLine parseLine(final String line) {
    int indentation = 0;
    while (indentation < line.length() && line.charAt(indentation) == '\t')
      indentation++;
    String withoutIndent = line.substring(indentation);
    if (withoutIndent.equals(""))
      return null;
    String[] splitted = withoutIndent.split(" ");
    return new ParsedLine(indentation, splitted[0].trim());
  }

  public int getIndentation() {
    return indentationLevel;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return indentationLevel + "-" + label;
  }
}
