
import sys
import itertools
from util import *
import json
import threading
    
def get_all_valid_t_wise_combinations_one_model(sample, t):
    return list(itertools.combinations(sample, t))

def get_iterated_satisfied_combinations(samples, t):
    iterated = []
    found_combs = set()
    for s in samples:
        combs = get_all_valid_t_wise_combinations_one_model(s, t)
        found_combs.update(combs)
        iterated.append(len(found_combs))
    return iterated

def parse_samples(input_file, is_baital):
    with open(input_file, "r") as f:
        if is_baital:
            str_samples = f.read().split("\n")[:-1]
        else:
            str_samples = f.read().split("\n")[3:-1]
    return [tuple(map(int,sample.split(", ")[1].split())) for sample in str_samples]

def compute_iterated_combinations(input_file, t, is_baital):
    if True or not os.path.exists(input_file+".cov"):
        print(input_file)
        samples = parse_samples(input_file, is_baital)
        iterated_cov = get_iterated_satisfied_combinations(samples, t)
        with open(input_file+".cov", "w") as f:
            json.dump(iterated_cov, f)

if __name__ == "__main__":
    compute_iterated_combinations(sys.argv[1], int(sys.argv[2]), int(sys.argv[3]))
