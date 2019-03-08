# Simple Converter

The Simple Converter converts TIFF images into JP2s using Kakadu and The Bodleian's Image Processing tool.

## Installation

The first thing to do is to install the required dependencies and the Image Processing tool.

    sudo apt install exiftool python-pip
    sudo pip install py pytest uuid pillow jpylyzer pathlib
    sudo pip install git+https://github.com/UCLALibrary/image-processing.git

The next thing to do is to install the Kakadu binaries and libraries. This can be accomplished by using the [kakadu-java](https://github.com/ksclarke/kakadu-java) build and your licensed version of Kakadu. Follow the instructions in that project's README for more information.

Once that is done, set the `KAKADU_HOME` environmental variable to the location within the kakadu-java project where the binaries and libraries were built:

    export KAKADU_HOME=/opt/kakadu-java/target/natives

You will then want to generate a file of TIFFs that you want to convert. This can be done by using the `find` command:

    find `pwd` -type f -name "*.tif" > tiffs.txt

Once you have that file, you can supply it to the JP2 conversion script:

    ./convert.py tiffs.txt

By default, the JP2s will be put in a `jp2_files` directory, but if you want to locate them somewhere else set the `KAKADU_JP2_FILES` environmental property before you run the script.

Lastly, if you want to time the script, run `time` before it:

    /usr/bin/time -f "%e" ./convert.py tiffs.txt

This will output the actual time elapsed. If you are interested in user and system time values as well drop the `-f "%e"` argument.

If there are any issues with this process, or if you encounter any bugs, please file a ticket in this project's [issues queue](https://github.com/uclalibrary/iiif-experiments/issues). Thanks!
