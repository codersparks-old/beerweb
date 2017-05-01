#!/bin/bash

read -p "About to Delete all data...Press enter to continue"
curl -XDELETE http://localhost:8080/v2/api/

read -p "About to setup pumps and assign beers...Press enter to continue"

for i in {1..8}
do

    echo "creting pump: 'pump %i'"
	curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump%20$i
	echo ""
	sleep .5
	echo "Add beer rfid $i$i$i$i$i to Pump pump $i"
	curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump%20$i/beer/$i$i$i$i$i
	echo ""
	sleep .5
done

read -p "About to start generating date...Press enter to continue - Press CTRL+c to stop"

while True
do
	PUMP=$((1 + RANDOM % 8))
	RATING=$((1 + RANDOM % 5))
	echo "Submitting Rating $RATING for pump $PUMP"
	curl -XPOST -H"Accept: application/json" http://localhost:8080/v2/api/pump/pump%20$PUMP/rating/$RATING
	SLEEPTIME=$(( (1 + RANDOM % 10000) / 1000  ))
	echo ""
	echo "Sleeping for $SLEEPTIME..."
	sleep $SLEEPTIME
	echo "...done - Press CTRL+c to stop"
done
