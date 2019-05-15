# iiif-url-generator

Simple script to generate a list of IIIF URLs from a spreadsheet of images.

How to use:

    java -jar target/iiif-url-generator-0.0.1.jar -o file-out.txt -i ~/50-60MB_sample_tiffs.txt

To see what arguments mean:

    java -jar target/iiif-url-generator-0.0.1.jar -h

The commands we're using:

    java -jar iiif-url-generator-0.0.1.jar -i 50-60MB_tiffs.txt -o output-50-60MB.csv \
      -p "50-60MB_JP2s,50-60MB_lossy" -m 100 -e jpx
    java -jar iiif-url-generator-0.0.1.jar -i 50-60MB_tiffs.txt -o output-50-60MB.csv \
      -p "50-60MB" -m 100 -e tif
    
    java -jar iiif-url-generator-0.0.1.jar -i 110-130MB_tiffs.txt -o output-110-300MB.csv \
      -p "110-130MB" -m 100 -e tif
    java -jar iiif-url-generator-0.0.1.jar -i 110-130MB_tiffs.txt -o output-110-300MB.csv \
      -p "110-130MB_JP2s,110-130MB_lossy" -m 100 -e jpx

