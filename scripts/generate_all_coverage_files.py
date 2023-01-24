
import sys
import os
import subprocess
from util import *
from constants import *
import threading

def run_computation(file_name, t, executable, is_baital):
    subprocess.run(["python3", executable, file_name, t, str(is_baital)])

def compute_all_iterated_combinations(main_folder, t, executable_file):
    all_threads = []
    for instance_name in sorted(os.listdir(main_folder)):
        for try_number in range(NB_RANDOM_RETRIES):
            all_threads.append(threading.Thread(group=None, target=run_computation, name=f"{instance_name}_frequency_{try_number}", args = (os.path.join(main_folder, instance_name, "random_diff", random_diff_file(instance_name, 1, 1, try_number)), t, executable_file, 0)))
            all_threads.append(threading.Thread(group=None, target=run_computation, name=f"{instance_name}_randomsearch_{try_number}", args = (os.path.join(main_folder, instance_name, "randomsearch", randomsearch_file(instance_name, 1, 1, try_number)), t, executable_file, 0)))
            for table_parameters in TABLE_PARAMETERS:
                all_threads.append(threading.Thread(group=None, target=run_computation, name=f"{instance_name}_baital_{try_number}", args = (os.path.join(main_folder, instance_name, f"table-{table_parameters}", table_extension(instance_name, table_parameters, try_number)), t, executable_file, 0)))
            #for nb_rounds in NB_ROUNDS:
            #    all_threads.append(threading.Thread(group=None, target=run_computation, name=f"{instance_name}_baital_{try_number}", args = (os.path.join(main_folder, instance_name, f"baital-{nb_rounds}", samples_extension(instance_name, nb_rounds, try_number)), t, executable_file, 1)))
    run_threads(all_threads, NB_THREADS)

if __name__ == "__main__":
    main_folder = sys.argv[1]
    t = sys.argv[2]
    executable_file = sys.argv[3]
    compute_all_iterated_combinations(main_folder, t, executable_file)
