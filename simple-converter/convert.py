#!/usr/bin/python

#
# A very simple JP2 converter script that takes a file of file paths as input.
#
# The input file can be generated with something like:
#   find `pwd` -type f -name "*.tif" > tiffs.txt
#
# The `pwd` can be replaced with a full system path to a directory containing
# images to convert into JP2. Using a relative path for find's start location
# will create an input file with relative paths in it.
#

import sys, os
from image_processing import kakadu
from pathlib import Path

# This must be a path that contains the bins and libs in the same directory
# It's retrieved through a system environmental variable
kdu = kakadu.Kakadu(kakadu_base_path=os.environ.get('KAKADU_HOME', '/opt/kakadu'))

# Get where we want to store the newly created JP2 files
jp2files = os.environ.get('KAKADU_JP2_FILES', 'jp2_files')

# Create a directory, if needed, for the JP2 output files
try:
    if not os.path.exists(jp2files):
        os.makedirs(jp2files)
except OSError:
    print ("Creation of the directory %s failed" % jp2files)

# Do the conversion from the input file's list of source files to JP2 outputs
if (len(sys.argv) != 2):
  print("You must supply a path to a file of TIFFs to be converted")
else:
  with open(sys.argv[1]) as file:
    for path in file:
      image = path.rstrip()
      # We'll use the JPX extension though Kakadu tells us JPF is more correct
      kdu.kdu_compress(image, jp2files + "/" + Path(image).resolve().stem + ".jpx",
        kakadu_options=kakadu.DEFAULT_LOSSLESS_COMPRESS_OPTIONS)

# To time this process, you can use:
#  /usr/bin/time -f "%e" ./convert.py sample_tiffs.txt
#
# The full path is important so you don't get Bash's time equivalent;
# the '-f "%e"' tells time to only output the real/elapsed time value
