#!/bin/bash

# A simple (meaning zero validation of your usage) script to ALTER all tables to InnoDB Engine.
# If the table(s) are already InnoDB engine, they will be rebuilt, hence reclaiming fragmented space
# making this useful for just that operation on InnoDB.

# Assumptions: You have a ~/.my.cnf file in your home dir having user name and password with
# appropriate perissions. You can just just do this temporarily on your remote server.

# Test this on a clone of your server first to satisfy yourself with the results.
# Use at your own risk. 

# Argument: database name(s)
# Usage: $0 database1 [database2 database3 ...]

set -xv

DATABASES="$@"
echo $DATABASES

for DATABASE in $DATABASES
do
	
	TABLES=`mysql --database=$DATABASE --skip-column-names --execute="SHOW TABLES;"`
	
	for TABLE in $TABLES
	do
		mysql --database=$DATABASE --execute="ALTER TABLE $TABLE ENGINE=InnoDB;"
	done
done