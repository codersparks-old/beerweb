#!/bin/bash

curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump2
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump3
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1

read -p "Press enter to continue"

curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/beer/12345
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump2/beer/13579
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump2/beer/54321

read -p "Press enter to continue"

curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/4
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/2
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/3
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/5
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/2
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/1
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/2
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/2
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/3
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/4
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/4
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/4
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/4
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/3
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/2
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/5
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump1/rating/4

read -p "Press enter to continue"
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump3/rating/4

read -p "Press enter to continue"
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump2/rating/4
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump2/rating/4
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump2/rating/4
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump2/rating/4
curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump2/rating/4
