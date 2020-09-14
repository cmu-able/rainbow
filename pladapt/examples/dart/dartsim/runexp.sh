seq 100 | parallel --eta -n0 ./run.sh --seed={#} $* | grep ^csv | cut -d, -f2- > r2phase.csv

#for i in `seq 1 1000`;
#do
#    Debug/dartam2 --threat-sensor-fpr=0 --threat-sensor-fnr=0 --target-sensor-fpr=0 --target-sensor-fnr=0 --lookahead-horizon=8
#    Debug/dartam2 --seed=$i
#    Debug/dartam2 --lookahead-horizon=1 --distrib-approx=1 --non-latency-aware
#    Debug/dartam2 --lookahead-horizon=1 --distrib-approx=1 --non-latency-aware --route-length=50 --change-alt-periods=0

#done
