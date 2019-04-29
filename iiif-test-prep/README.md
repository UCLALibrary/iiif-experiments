# iiif-test-prep

Simple script to sample a smaller random collection of images from a larger spreadsheet of image collections.
It then moves the sampled records into a new directory.

How to use:

    java -jar target/iiif-test-prep-0.0.1.jar -o /image-gen/jp2-scratch/ -m 10 -c ~/50-60MB_sample.txt -i -d

To see what arguments mean:

    java -jar target/iiif-test-prep-0.0.1.jar-0.0.1.jar -h

It generates a different sample each time so either choose a new output directory or clean up the directory
before running a second time.
