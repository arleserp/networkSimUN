#!/bin/sh
i=1
while [ $i -le 30 ]; do
echo $i
i=$(($i+1))
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load communitycircle+v+100+beta+0.5+degree+4+clusters+4.graph 10 0.008 random communitycircle+v+100+beta+0.5+degree+4+clusters+4.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load communitycircle+v+100+beta+0.5+degree+4+clusters+4.graph 10 0.008 randomMarking communitycircle+v+100+beta+0.5+degree+4+clusters+4.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load communitycircle+v+100+beta+0.5+degree+4+clusters+4.graph 10 0.008 carriers communitycircle+v+100+beta+0.5+degree+4+clusters+4.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load communitycircle+v+100+beta+0.5+degree+4+clusters+4.graph 10 0.008 carriersMarking communitycircle+v+100+beta+0.5+degree+4+clusters+4.loc -1
done