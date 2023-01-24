
import sys
import os
import itertools
import matplotlib.pyplot as plt
from util import *
from constants import *

def parse_frequency_samples(input_file):
    with open(input_file, "r") as f:
        str_samples = f.read().split("\n")[3:-1]
    return [tuple(map(int,sample.split(", ")[1].split())) for sample in str_samples]

def parse_baital_samples(input_file):
    with open(input_file, "r") as f:
        str_samples = f.read().split("\n")[:-1]
    return [tuple(map(int,sample.split(", ")[1].split())) for sample in str_samples]

def plot_coverage(instance_coverages, instance_name, file_name):
    print(instance_coverages["frequency-diff"][0], instance_name)
    colors = {"randomsearch":"tab:purple", "frequency-diff":"black","table-(4, 4, 0.25)":"tab:cyan","table-(8, 6, 0.125)":"lightblue","table-(16, 8, 0.0625)":"blue"}#,"uniform":"green", "baital-5":"pink", "baital-10":"red"}
    fig, ax = plt.subplots()
    for name, coverages in instance_coverages.items():
        ax.plot(coverages, label=name.capitalize().replace("0.25","1/4").replace("0.125","1/8").replace("0.0625","1/16") if name not in {"frequency-diff","randomsearch"} else {"frequency-diff":"FrequencyDiff","randomsearch":"RandomSearch"}[name], color=colors[name], linestyle = "dashed" if name[:5] == "table" else "dashdot" if name[:6] == "baital" else "solid")
    ax.set_xlabel("#Solution")
    ax.set_ylabel(f"#{['','feature-','pair'][t] if t <= 2 else (str(t)+'-')}wise combinations covered")
    ax.legend()
    ax.ticklabel_format(scilimits=(-5,5), axis="y")
    fig.savefig(file_name, bbox_inches='tight',pad_inches = 0)
    plt.close(fig)

def generate_all_plots(output_folder, parsed_results):
    create_folder(output_folder)
    for instance_name, instance_data in parsed_results.items():
        plot_coverage(instance_data["coverages"], instance_name, os.path.join(output_folder, f"{instance_name}-pairwise.pdf"))

def generate_one_scatter_plot(versions, times, timeFrequencyDiff, use_table=True, xlims = None):
    fig, ax = plt.subplots()
    all_max = max(timeFrequencyDiff)
    ax.plot([min(timeFrequencyDiff),all_max], [min(timeFrequencyDiff), all_max], linestyle="dotted")
    for version, (color,alpha) in versions.items():
        ax.scatter(times[version], timeFrequencyDiff, color=color, alpha = alpha, label=version.capitalize().replace("0.25","1/4").replace("0.125","1/8").replace("0.0625","1/16"))
    ax.set_xlabel(f"Time for other approach (in s)")
    ax.set_ylabel("Time for `FrequencyDiff` approach (in s)")
    #ax.set_ylabel("Time for RandomSearch (in s)")
    ax.set_xscale("log")
    ax.set_yscale("log")
    if xlims != None:
        ax.set_xlim(xlims)
    leg = ax.legend(loc="lower right")
    for lh in leg.legendHandles:
        lh.set_alpha(1)
    fig.set_figwidth(8)
    fig.set_figheight(4)
    fig.savefig(os.path.join(output_folder, f"all_times-{'table' if use_table else 'baital'}.pdf"), bbox_inches='tight',pad_inches = 0)
    plt.close(fig)
    return ax.get_xlim()
    

def generate_all_times_scatter_plot(output_folder, parsed_results, use_table=True):
    other_versions = ["randomsearch","table-(4, 4, 0.25)","table-(8, 6, 0.125)","table-(16, 8, 0.0625)"]#+["uniform","baital-5","baital-10"]
    table_versions = {"randomsearch":("tab:purple",0.3), "table-(4, 4, 0.25)":("tab:cyan",0.3), "table-(8, 6, 0.125)":("lightblue",0.4), "table-(16, 8, 0.0625)":("blue",0.4)}
    baital_versions = {"uniform":("green",0.3),"baital-5":("blue",0.3),"baital-10":("red",0.3)}
    fig, ax = plt.subplots()
    all_max = 0
    times = {}
    timeFrequency = [data["times"]["frequency-diff"]/1000000000 for _, data in parsed_results.items()]
    for version in other_versions:
        timesY = []  
        for instance_name, data in parsed_results.items():
            timesY.append(data["times"][version]/1000000000)
        times[version] = timesY
        #all_max = max(all_max, max(timesY))
    used_versions = baital_versions
    if use_table:
        used_versions = table_versions
    xlims = generate_one_scatter_plot(table_versions, times, timeFrequency, use_table=True)
    #generate_one_scatter_plot(baital_versions, times, timeFrequency, use_table=False, xlims = xlims)
    

def find_ratio(l1,l2,i):
    """supposing that l1[i] >= l2[i], find first j such that l1[j-1] < l2, and return i/j"""
    if l1[i] < l2[i]:
        return 1/find_ratio(l2,l1,i)
    j = i
    while l1[j-1] >= l2[i]:
        j -= 1
    return i/j
    
def aggregated_results(parsed_results, main_version, other_version):
    aggregated_time = []
    aggregated_id_coverages = []
    aggregated_early_coverages = []
    for instance_name, instance_data in parsed_results.items():
        aggregated_time.append(instance_data["times"][other_version]/instance_data["times"][main_version])
        aggregated_id_coverages.append([instance_data["coverages"][main_version][i] / instance_data["coverages"][other_version][i] for i in range(0,100)])
        aggregated_early_coverages.append([find_ratio(instance_data["coverages"][main_version], instance_data["coverages"][other_version], i) for i in range(1,100)])
    #if other_version == "table-(16, 8, 0.0625)":
    #    print(aggregated_time)
    return geo_average(aggregated_time), geo_averages(aggregated_id_coverages), geo_averages(aggregated_early_coverages)
        
            
def get_baital_time(filename):
    with open(filename, "r") as f:
        return int(f.readline())

def get_frequency_time(filename):
    with open(filename, "r") as f:
        f.readline()
        return int(f.readline()[:-2].split()[-1])

def get_table_time(filename):
    with open(filename,"r") as f:
        return int(f.readline().split(" ")[-1])

def generate_parsed_times(main_folder, instance_name):
    parsed_times = {}
    #for nb_rounds in NB_ROUNDS:
    #    parsed_times[f"baital-{nb_rounds}" if nb_rounds != 1 else "uniform"] = sum(get_baital_time(os.path.join(main_folder, instance_name, f"baital-{nb_rounds}", samples_extension(instance_name, nb_rounds, try_nb)+".time")) for try_nb in range(NB_RANDOM_RETRIES))/NB_RANDOM_RETRIES
    parsed_times["frequency-diff"] = sum(get_frequency_time(os.path.join(main_folder, instance_name, "random_diff", random_diff_file(instance_name, 1,1, try_nb))) for try_nb in range(NB_RANDOM_RETRIES))/NB_RANDOM_RETRIES
    parsed_times["randomsearch"] = sum(get_frequency_time(os.path.join(main_folder, instance_name, "randomsearch", randomsearch_file(instance_name, 1,1, try_nb))) for try_nb in range(NB_RANDOM_RETRIES))/NB_RANDOM_RETRIES
    for table_parameters in TABLE_PARAMETERS:
        parsed_times[f"table-{table_parameters}"] = sum(get_table_time(os.path.join(main_folder, instance_name, f"table-{table_parameters}", table_extension(instance_name, table_parameters, try_nb))) for try_nb in range(NB_RANDOM_RETRIES))/NB_RANDOM_RETRIES
    return parsed_times

def get_coverage(input_file):
    with open(input_file+".cov", "r") as f:
        return json.loads(f.read())

def generate_parsed_coverages(main_folder, instance_name):
    parsed_coverages = {}
    #for nb_rounds in NB_ROUNDS:
    #    parsed_coverages[f"baital-{nb_rounds}" if nb_rounds != 1 else "uniform"] = averages([get_coverage(os.path.join(main_folder, instance_name, f"baital-{nb_rounds}", samples_extension(instance_name, nb_rounds, try_nb))) for try_nb in range(NB_RANDOM_RETRIES)])
    for table_parameters in TABLE_PARAMETERS:
        parsed_coverages[f"table-{table_parameters}"] = averages([get_coverage(os.path.join(main_folder, instance_name, f"table-{table_parameters}", table_extension(instance_name,table_parameters,try_nb))) for try_nb in range(NB_RANDOM_RETRIES)])
    parsed_coverages["frequency-diff"] = averages([get_coverage(os.path.join(main_folder, instance_name, "random_diff", random_diff_file(instance_name, 1,1, try_nb))) for try_nb in range(NB_RANDOM_RETRIES)])
    parsed_coverages["randomsearch"] = averages([get_coverage(os.path.join(main_folder, instance_name, "randomsearch", randomsearch_file(instance_name, 1,1, try_nb))) for try_nb in range(NB_RANDOM_RETRIES)])
    return parsed_coverages

                

def generate_parsed_results(main_folder, t):
    parsed_results = {}
    for instance_name in os.listdir(main_folder):
        times = generate_parsed_times(main_folder, instance_name)
        coverages = generate_parsed_coverages(main_folder, instance_name)
        parsed_results[instance_name] = {"times":times, "coverages":coverages}
    return parsed_results

if __name__ == "__main__":
    main_folder = sys.argv[1]
    t = int(sys.argv[2])
    output_folder = sys.argv[3]
    create_folder(output_folder)
    if not os.path.exists(os.path.join(output_folder, f"parsed_results-{t}.json")):
        parsed_results = generate_parsed_results(main_folder, t)
        export_json(parsed_results, os.path.join(output_folder, f"parsed_results-{t}.json"))
    else:
        parsed_results = parse_json(os.path.join(output_folder, f"parsed_results-{t}.json"))
    generate_all_plots(os.path.join(output_folder, "graphs"), parsed_results)
    generate_all_times_scatter_plot(output_folder, parsed_results)
