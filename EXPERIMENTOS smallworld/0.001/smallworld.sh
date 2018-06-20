#!/bin/sh
i=1
while [ $i -le 30 ]; do
echo $i
i=$(($i+1))
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load smallworld+v+100+beta+0.5+degree+2.graph 10 0.001 random smallworld+v+100+beta+0.5+degree+2.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load smallworld+v+100+beta+0.5+degree+2.graph 10 0.001 randomMarking smallworld+v+100+beta+0.5+degree+2.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load smallworld+v+100+beta+0.5+degree+2.graph 10 0.001 carriers smallworld+v+100+beta+0.5+degree+2.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load smallworld+v+100+beta+0.5+degree+2.graph 10 0.001 carriersMarking smallworld+v+100+beta+0.5+degree+2.loc -1
done