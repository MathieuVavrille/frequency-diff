
import subprocess
import sys
import os
import shutil
from constants import UVL_EXTENSION, DIMACS_EXTENSION
from util import create_folder

def convert_to_dimacs(jar_converter, input_file):
    """Converts a Feature Model in UVL format into DIMACS"""
    output_file = input_file[:-len(UVL_EXTENSION)] + DIMACS_EXTENSION
    if os.path.exists(output_file):
        return output_file
    subprocess.run(["java", "-jar", jar_converter, input_file, output_file])
    if os.path.exists(output_file):
        return output_file
    return None

def create_folder(folder):
    os.makedirs(folder,exist_ok=True)

def rewrite_bugged_features(instance_name):
    """Changes the names of some features (if easy) to avoid bugs in the dimacs convertion"""
    subprocess.run(['sed', '-i', 's/_POSIX/uPOSIX/g', instance_name])
    subprocess.run(['sed', '-i', 's/__Root__/uuRootuu/g', instance_name])
    subprocess.run(['sed', '-i', 's/\\.0/_0/g', instance_name]) # for some dm_***
    subprocess.run(['sed', '-i', 's/BMD¦/BMD/g', instance_name]) #only for dm_HICSSDM.csv
    subprocess.run(['sed', '-i', 's/2\\./2_/g', instance_name]) # only for dm_mobile_phone
    subprocess.run(['sed', '-i', 's/3\\./3_/g', instance_name]) # only for dm_mobile_phone
    subprocess.run(['sed', '-i', 's/5 MP/5_MP/g', instance_name]) # only for dm_mobile_phone
    subprocess.run(['sed', '-i', 's/-/_/g', instance_name]) # only for embtoolkit
    if instance_name[-16:] == "ovm_MPPL.ovm.uvl":
        print("Possible error with spacing in feature")
    

def create_instance_folder(instance_path, output_folder):
    _, instance_name = os.path.split(instance_path)
    copy_folder = os.path.join(output_folder, instance_name[:-4])
    create_folder(copy_folder)
    output_file = os.path.join(copy_folder, instance_name)
    shutil.copy(instance_path, output_file)
    rewrite_bugged_features(output_file)
    return os.path.join(copy_folder, instance_name)

cpt = 0
def find_all_uvl_models(input_repository, output_folder, dimacs_converter):
    global cpt
    dir_list = os.listdir(input_repository)
    for d in dir_list:
        if d not in {"Linux_v1", "Automotive","Evolution","linux-2.6.33.3.uvl","financialsercives01.uvl"}: # empty model, or redundant, or bugged
            full_path = os.path.join(input_repository, d)
            if os.path.isfile(full_path) and full_path[-4:] == UVL_EXTENSION:
                print(full_path)
                output_file = create_instance_folder(full_path, output_folder)
                convert_to_dimacs(dimacs_converter, output_file)
                cpt += 1
            elif os.path.isdir(full_path):
                find_all_uvl_models(full_path, output_folder, dimacs_converter)

def main(base_repo, output_folder, dimacs_converter):
    find_all_uvl_models(base_repo, output_folder, dimacs_converter)

if __name__ == "__main__":
    input_folder = sys.argv[1]
    output_folder = sys.argv[2]
    dimacs_converter = sys.argv[3]
    create_folder(output_folder)
    main(input_folder, output_folder, dimacs_converter)
    
