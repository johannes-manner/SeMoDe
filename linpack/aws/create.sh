#!/bin/bash

zip linpack.zip *
echo "linpack.zip created"

sizes=(128 256 512 768 1024 1280 1536 1792 2048 2304 2560 2816 3008)

for i in "${sizes[@]}"
do
   aws lambda create-function --function-name linpack_$i --role arn:aws:iam::363072427535:role/aws-lambda-martin --runtime nodejs8.10 --handler index.handler --timeout 900 --memory-size $i --zip-file fileb://linpack.zip
done
