#!/bin/sh
i=1
while [ $i -le 30 ]; do
echo $i
i=$(($i+1))
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load scalefree+sn+4+eta+1+numSt+97.graph 10 0.005 FirstNeighbor scalefree+sn+4+eta+1+numSt+97.loc -1
done