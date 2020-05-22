#! /bin/sh

kubectl apply -f postgres-config.yml

kubectl apply -f postgres-service.yml 

kubectl apply -f delivery-service.yml





