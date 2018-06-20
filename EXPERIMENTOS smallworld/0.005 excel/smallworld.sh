#!/bin/sh
i=1
while [ $i -le 30 ]; do
echo $i
i=$(($i+1))
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load smallworld+v+100+beta+0.5+degree+2.graph 10 0.005 random smallworld+v+100+beta+0.5+degree+2.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load smallworld+v+100+beta+0.5+degree+2.graph 10 0.005 randomMarking smallworld+v+100+beta+0.5+degree+2.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load smallworld+v+100+beta+0.5+degree+2.graph 10 0.005 carriers smallworld+v+100+beta+0.5+degree+2.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load smallworld+v+100+beta+0.5+degree+2.graph 10 0.005 carriersMarking smallworld+v+100+beta+0.5+degree+2.loc -1
done