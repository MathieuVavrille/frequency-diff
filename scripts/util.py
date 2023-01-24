
import json
import os
import threading
import time

def create_folder(folder):
    """Creates the given folder. Does not raise error if the folder already exists"""
    os.makedirs(folder,exist_ok=True)
    
def average(l):
    """Average value of a list"""
    return sum(l)/len(l)

def averages(ls):
    """Given a list containing lists of integers of same size, returns a new list containing the averages of the i-th elements of the lists. For example averages([[1,2,3],[3,6,9]]) = [average([1,3]),average([2,6]),average([3,9])]"""
    return list(map(average, zip(*ls)))
    
def geo_average(l):
    """Geometric mean of a list"""
    """res = 1
    for i in l:
        res *= i
    return res**(1/len(l))"""
    res = 1
    p = 1/len(l)
    for i in l:
        res *= i**p
    return res

def geo_averages(ls):
    """Given a list containing lists of integers of same size, returns a new list containing the averages of the i-th elements of the lists. For example averages([[1,2,3],[3,6,9]]) = [average([1,3]),average([2,6]),average([3,9])]"""
    return list(map(geo_average, zip(*ls)))

def export_json(data, output_file):
    json_object = json.dumps(data)
    with open(output_file, "w") as f:
        f.write(json_object)

def parse_json(input_file):
    with open(input_file, 'r') as f:
        return json.loads(f.read())

def run_threads(threads, nb_allowed):
    """Starts the threads in the `threads` list, with a maximum of `nb_allowed` at the same time"""
    running = []
    for t in threads:
        while len(running) >= nb_allowed:
            for t_id in range(len(running)-1,-1,-1):
                if not running[t_id].is_alive():
                    running.pop(t_id)
            time.sleep(0.01)
        t.start()
        running.append(t)
    for r in running:
        r.join()



        
