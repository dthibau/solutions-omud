#! /bin/sh

kubectl delete service/delivery-service

kubectl delete deployment/delivery-service

kubectl delete service postgres-service

kubectl delete configMap postgres-config


