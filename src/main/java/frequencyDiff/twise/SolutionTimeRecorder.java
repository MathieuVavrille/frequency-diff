package frequencyDiff.twise;

import java.util.List;

import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;

public interface SolutionTimeRecorder {

  public void setStartTime();

  public List<Long> getSolutionTime();
}
