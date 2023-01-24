all: run

compile:
	mvn -q clean compile assembly:single

run: compile
	java -ea -jar target/randsampFM-0.0.1-SNAPSHOT-jar-with-dependencies.jar

experiments: compile
	python3 scripts/reorder-repository.py uvl-models experiments scripts/uvlToDimacs.jar # reorders the uvl-model repository (with potentially subfolders) to be a flat hierarchy
	python3 scripts/computations.py experiments target/frequencyDiff-0.0.1-SNAPSHOT-jar-with-dependencies.jar # Runs the computations (in parallel).
	python3 scripts/generate_all_coverage_files.py experiments 2 scripts/generate_coverage_file.py # computes the coverages
	python3 scripts/parse_results.py experiments 2 results # generates the results graphs

clean:
	rm -rf results
	rm -rf experiments
	rm -rf target

