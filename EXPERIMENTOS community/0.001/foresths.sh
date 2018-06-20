#!/bin/sh
i=1
while [ $i -le 30 ]; do
echo $i
i=$(($i+1))
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load foresthubandspoke+v+100+clusters+4.graph 10 0.001 random foresthubandspoke+v+100+clusters+4.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load foresthubandspoke+v+100+clusters+4.graph 10 0.001 randomMarking foresthubandspoke+v+100+clusters+4.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load foresthubandspoke+v+100+clusters+4.graph 10 0.001 carriers foresthubandspoke+v+100+clusters+4.loc -1
java  -classpath ../../dist/NetworkSimulator.jar unalcol.agents.NetworkSim.DataCollectionMain load foresthubandspoke+v+100+clusters+4.graph 10 0.001 carriersMarking foresthubandspoke+v+100+clusters+4.loc -1
done