#!/bin/sh
grep csv $1 | cut -d, -f2- > $1.csv
