
import sys
import os
import subprocess
from util import *
from constants import *
import time

# RANDOMSEARCH
def compute_randomsearch_samples(frequency_diff_jar, main_folder, instance_name, try_number):
    """Computes the `try_number`-th set of samples using the frequency difference strategy, with or without randomness"""
    instance_folder = os.path.join(main_folder, instance_name)
    instance_uvl = os.path.join(instance_folder, instance_name+UVL_EXTENSION)
    output_folder = os.path.join(instance_folder, "randomsearch")
    create_folder(output_folder)
    output_file = os.path.join(output_folder, randomsearch_file(instance_name, 1, 1, try_number))
    if os.path.exists(output_file):
        return output_file
    subprocess.run(["java", "-jar", frequency_diff_jar, "--samples", str(NB_SAMPLES), "--file", instance_uvl, "--outfile", output_file, "--time", str(TIMEOUT), "-a", "randomsearch"])
    print(f"{instance_name} {1} {1} {try_number}")
    return

def compute_all_randomsearch_samples(frequency_diff_jar, input_folder, instance_name):
    return ([threading.Thread(group=None, target=compute_randomsearch_samples, name=f"randomsearch_{instance_name}_{try_number}", args = (frequency_diff_jar, input_folder, instance_name, try_number)) for try_number in range(NB_RANDOM_RETRIES)]
    )

# FREQUENCY DIFF
def compute_frequency_diff_samples(frequency_diff_jar, main_folder, instance_name, try_number):
    """Computes the `try_number`-th set of samples using the frequency difference strategy, with or without randomness"""
    instance_folder = os.path.join(main_folder, instance_name)
    instance_uvl = os.path.join(instance_folder, instance_name+UVL_EXTENSION)
    output_folder = os.path.join(instance_folder, "random_diff")
    create_folder(output_folder)
    output_file = os.path.join(output_folder, random_diff_file(instance_name, 1, 1, try_number))
    if os.path.exists(output_file):
        return output_file
    subprocess.run(["java", "-jar", frequency_diff_jar, "--samples", str(NB_SAMPLES), "--file", instance_uvl, "--outfile", output_file, "--time", str(TIMEOUT), "-a", "frequency"])
    print(f"{instance_name} {1} {1} {try_number}")
    return

def compute_all_frequency_diff_samples(frequency_diff_jar, input_folder, instance_name):
    return ([threading.Thread(group=None, target=compute_frequency_diff_samples, name=f"frequency_{instance_name}_{try_number}", args = (frequency_diff_jar, input_folder, instance_name, try_number)) for try_number in range(NB_RANDOM_RETRIES)]
    )

# TABLE SAMPLING
def compute_table_samples(frequency_diff_jar, main_folder, instance_name, table_parameters, try_number):
    """Computes the `try_number`-th set of samples using the frequency difference strategy, with or without randomness"""
    instance_folder = os.path.join(main_folder, instance_name)
    instance_uvl = os.path.join(instance_folder, instance_name+UVL_EXTENSION)
    output_folder = os.path.join(instance_folder, f"table-{table_parameters}")
    create_folder(output_folder)
    output_file = os.path.join(output_folder, table_extension(instance_name, table_parameters, try_number))
    if os.path.exists(output_file):
        return output_file
    subprocess.run(["java", "-jar", frequency_diff_jar, "--samples", str(NB_SAMPLES), "--file", instance_uvl, "--outfile", output_file, "--time", str(TIMEOUT), "-a", "table", "--table_pivot", str(table_parameters[0]), "--table_nb_vars", str(table_parameters[1]), "--table_proba", str(table_parameters[2])])
    print(f"{instance_name} table {table_parameters} {try_number}")
    return

def compute_all_table_samples(frequency_diff_jar, input_folder, instance_name):
    return ([threading.Thread(group=None, target=compute_table_samples, name=f"table_{instance_name}_{try_number}", args = (frequency_diff_jar, input_folder, instance_name, table_parameters, try_number)) for table_parameters in TABLE_PARAMETERS for try_number in range(NB_RANDOM_RETRIES)])

# BAITAL
def compute_baital_samples_try(baital_sampling_exe, input_folder, instance_name, nb_rounds, try_number):
    """Computes the `try_number`-th set of samples using mode `mode` on the given instance"""
    instance_folder = os.path.join(input_folder, instance_name)
    instance_dimacs = os.path.join(instance_folder, instance_name+DIMACS_EXTENSION)
    output_folder = os.path.join(instance_folder, f"baital-{nb_rounds}")
    create_folder(output_folder)
    output_file = os.path.join(output_folder, samples_extension(instance_name, nb_rounds, try_number))
    if os.path.exists(output_file):
        return output_file
    start_time = time.time_ns()
    full_baital_output = subprocess.check_output(["python3", baital_sampling_exe, "--strategy", "5", "--samples", str(NB_SAMPLES), "--rounds", str(nb_rounds), "--outputfile", output_file, instance_dimacs])
    end_time = time.time_ns()
    with open(output_file+".time", "w") as f:
        f.write(str(end_time-start_time) + "\n")
        f.write(str(full_baital_output, 'UTF-8'))
    print(f"{instance_name} baital {nb_rounds} {try_number}")
    
def compute_all_baital_samples(baital_sampling_exe, input_folder, instance_name):
    return [threading.Thread(group=None, target=compute_baital_samples_try, name=f"baital_{instance_name}_{nb_rounds}_{try_number}", args = (baital_sampling_exe, input_folder, instance_name, nb_rounds, try_number))
                             for nb_rounds in NB_ROUNDS
                             for try_number in range(NB_RANDOM_RETRIES)]

def compute_instance(main_folder, instance_name, baital_sampling_exe, frequency_diff_jar):
    """From a folder containing only the instance in UVL format, compute the samples using the four modes"""
    return ((compute_all_baital_samples(baital_sampling_exe, main_folder, instance_name) if baital_sampling_exe != None else []) +
            compute_all_randomsearch_samples(frequency_diff_jar, main_folder, instance_name) +
            compute_all_frequency_diff_samples(frequency_diff_jar, main_folder, instance_name) +
            compute_all_table_samples(frequency_diff_jar, main_folder, instance_name)) 


def compute_all_instances(main_folder, frequency_diff_jar, baital_sampling_exe = None):
    all_threads = []
    for instance_name in sorted(os.listdir(main_folder)):
        all_threads += compute_instance(main_folder, instance_name, baital_sampling_exe, frequency_diff_jar)
    run_threads(all_threads, NB_THREADS)



if __name__ == "__main__":
    """Args are `main_folder, baital_sampling_exe, frequency_diff_jar`"""
    main_folder = sys.argv[1]
    frequency_diff_jar = sys.argv[2]
    baital_sampling_exe = sys.argv[3] if len(sys.argv) > 3 else None
    compute_all_instances(main_folder, frequency_diff_jar, baital_sampling_exe)    
