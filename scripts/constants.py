
"""
This file contains constants used by the other scripts.
You can directly change the parameters, however if you do some computations with some parameters, then change the parameters, then do some other computations, the scripts may not work as intended (for example is not all the sample files have the same number of samples)
"""

NB_SAMPLES = 100
NB_RANDOM_RETRIES = 2
TABLE_PARAMETERS = [(4, 4, 1/4), (8, 6, 1/8), (16, 8, 1/16)] #(pivot, vars, proba)
NB_ROUNDS = [1, 5, 10]
TIMEOUT = 300

NB_THREADS = 2

""" The name of the folder should be the name of the uvl instance (without the extension).
The created file will always start by the instance name, and then some extensions"""
UVL_EXTENSION = ".uvl"
DIMACS_EXTENSION = ".dimacs"
COUNTS_EXTENSION = "_counts.json"
FREQUENCY_EXTENSION = "_frequencies.json"
GREEDY_DIFF_EXTENSION = "_greedy-diff.samples"
def random_diff_folder(var_weight, val_weight):
    return f"random-diff-{var_weight}-{val_weight}"
def random_diff_file(instance_name, var_weight, val_weight, try_number):
    return f"{instance_name}-random_diff-{var_weight}-{val_weight}-{try_number}.samples"
def randomsearch_file(instance_name, var_weight, val_weight, try_number):
    return f"{instance_name}-randomsearch-{var_weight}-{val_weight}-{try_number}.samples"
def samples_extension(instance_name, nb_rounds, try_number):
    return f"{instance_name}-baital-{nb_rounds}-{try_number}.samples"
def table_extension(instance_name, table_parameters, try_number):
    return f"{instance_name}-table-{table_parameters}-{try_number}"
