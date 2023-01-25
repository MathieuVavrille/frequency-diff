# FrequencyDiff

This repository contains the implementation of the frequencyDiff search strategy for t-wise coverage.

## Usage

An example of full computations can be found in the makefile, and run using `make experiments`. It will create a folder `experiments` with the outputs of the approaches, and a folder `results` with the plots. UVL models can be pulled from the [uvl-models repository](https://github.com/Universal-Variability-Language/uvl-models). Two small examples extracted from this dataset are already given in our repository.

The command lines are:
- To compile the code `mvn -q clean compile assembly:single`. It generates a jar with dependencies in `target`. This jar has a command line interface for all the options (seed, parameters of TableSampling, ...)
- To reorder the uvl-models repository :`python3 scripts/reorder-repository.py uvl-models experiments scripts/uvlToDimacs.jar`
- To run the computations: `python3 scripts/computations.py experiments target/frequencyDiff-0.0.1-SNAPSHOT-jar-with-dependencies.jar`
- To compute the pairwise coverages: `python3 scripts/generate_all_coverage_files.py experiments 2 scripts/generate_coverage_file.py`
- To plot the pairwise results: `python3 scripts/parse_results.py experiments 2 results`

Parameters are in the file `scripts/constants`. It contains
- `NB_SAMPLES` the number of samples to draw
- `NB_RANDOM_RETRIES` the number of times the experiments are launched (to average random factors)
- `TABLE_PARAMETERS` the set of parameters used for the table computations
- `NB_ROUNDS` the number of rounds used for baital
- `TIMEOUT` a timeout, but not all approaches allow a timeout
- `NB_THREADS` the number of threads to use for the experiments. Each experiment is run on a single thread. Multiple threads are used to run independent instances.

### Running Baital (and uniform sampling)

Baital and uniform sampling are not run by default in this repository. It requires to download and install [baital](https://github.com/meelgroup/baital). On top of that, some modifications of baital are required. `d4` and `DSharp_Compile` are referenced using a relative path, requiring the code to be run from the baital's `src` folder. It can be changed in lines 601 and 604 of `waps_upd.py` with an absolute path. Due to Baital's storage of weights in files, by default it is impossible to call more than one time the sampling in parallel. Either the number of threads has to be set to 1, or filenames can be changed by doing the following in `sampling.py`:
- Removing lines 35-37 (prefixes of files)
- adding at the beginning of the `run` function (line 143 before modification, line 140 after the previous modification)
```python
    TMPSAMPLEFILE = outputFile+'samples_temp.txt'
    PICKLEFILE = outputFile+'saved.pickle'
    WEIGHTFILEPREF = outputFile+'weights'
```

To run the computations, the file `sampling.py` has to be added when running `scripts/computations.py`. By default results are not processed for baital, there are lines to uncomment in the scripts:
- lines 20,21 in `generate_all_coverage_files.py`
- lines 60, 77, 115-116, 129-130 in `parse_results.py`
In `parse_results.py`, to have consistent colors, the approaches plotted are hardcoded. It may only work with `NB_ROUNDS` equal to 1, 5 and 10, otherwise some modifications are required.

## Implementation

The FrequencyDiff search strategy implementation (main contribution of the article) is given in file [FrequencyDiff.java](./src/main/java/frequencyDiff/twise/FrequencyDiff.java).