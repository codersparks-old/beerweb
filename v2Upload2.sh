#!/bin/bash

if [ -z "$HOST" ]
then
	HOST=localhost
fi

if [ -z "$PORT" ]
then
	PORT=8080
fi

if [ -z "$NUM_PUMPS" ]
then
	NUM_PUMPS=15
fi

echo "HOST: $HOST & PORT: $PORT & NUM_PUMPS: $NUM_PUMPS"

read -p "About to Delete all data...Press enter to continue"
curl -XDELETE http://$HOST:$PORT/v2/api/

read -p "About to setup pumps and assign beers...Press enter to continue"

for i in $( seq 1 $NUM_PUMPS)
do

    if (( $i < 10 ))
    then
        i="0$i"
    fi

    echo "creting pump: pump $i"
	curl -XPOST -H"Accept: application/json" http://$HOST:$PORT/v2/api/pump/pump%20$i
	echo ""
	sleep .5
	echo "Add beer rfid $i$i$i$i$i to Pump pump $i"
	curl -XPOST -H"Accept: application/json" http://$HOST:$PORT/v2/api/pump/pump%20$i/beer/$i$i$i$i$i
	echo ""
	sleep .5
done

read -p "About to start generating date...Press enter to continue - Press CTRL+c to stop"

while True
do
	PUMP=$((1 + RANDOM % $NUM_PUMPS))

	if (( $PUMP < 10 ))
	then
	    PUMP="0$PUMP"
	fi
	RATING=$((1 + RANDOM % 5))
	echo "Submitting Rating $RATING for pump $PUMP"
	curl -XPOST -H"Accept: application/json" http://$HOST:$PORT/v2/api/pump/pump%20$PUMP/rating/$RATING
	if [ -z "$SLEEPTIME" ]
	then
		SLEEPTIME=$(( (1 + RANDOM % 10000) / 1000  ))
	fi
	echo ""
	echo "Sleeping for $SLEEPTIME..."
	sleep $SLEEPTIME
	echo "...done - Press CTRL+c to stop"
done
