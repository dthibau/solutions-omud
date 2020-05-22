#! /bin/sh

kubectl delete deployment/postgres-service
kubectl delete deployment/delivery-service


kubectl delete service postgres-service

kubectl delete configMap postgres-config


