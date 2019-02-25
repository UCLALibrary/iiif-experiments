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
from image_processing import openjpeg
from pathlib import Path

# This must be a path that contains the bins and libs in the same directory
# It's retrieved through a system environmental variable
ojp = openjpeg.OpenJPEG(openjpeg_base_path='/usr/bin')

# Get where we want to store the newly created JP2 files
jp2files = os.environ.get('OPENJPEG_JP2_FILES', 'opj_jp2_files')

# Create a directory, if needed, for the JP2 output files
try:
    os.makedirs(jp2files)
except OSError:
    print ("Creation of the directory %s failed" % jp2files)

# Do the conversion from the input file's list of source files to JP2 outputs
if (len(sys.argv) != 2 and len(sys.argv) != 3):
  print("You must supply a path to a file of TIFFs to be converted")
else:
  position = 0

  if (len(sys.argv) != 3):
    start = 0
  else:
    start = int(sys.argv[2])

  with open(sys.argv[1]) as file:
    for path in file:
      position += 1

      if (position >= start):
        image = path.rstrip()

        try:
          ojp.opj_compress(image, jp2files + "/" + Path(image).resolve().stem + ".jp2",
            openjpeg_options=openjpeg.LOSSLESS_COMPRESS_OPTIONS)
        except:
          print 'Conversion failed at record: ' + position

# To time this process, you can use:
#  /usr/bin/time -f "%e" ./convert-ojp.py sample_tiffs.txt
#
# The full path is important so you don't get Bash's time equivalent;
# the '-f "%e"' tells time to only output the real/elapsed time value
