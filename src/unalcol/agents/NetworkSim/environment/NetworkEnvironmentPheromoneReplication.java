package unalcol.agents.NetworkSim.environment;

import unalcol.agents.simulate.util.*;
import unalcol.agents.*;

import java.util.Vector;

import edu.uci.ics.jung.graph.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import unalcol.agents.NetworkSim.ActionParameters;
import unalcol.agents.NetworkSim.GraphElements;
import unalcol.agents.NetworkSim.MobileAgent;
import unalcol.agents.NetworkSim.MotionProgramSimpleFactory;
import unalcol.agents.NetworkSim.Node;
import unalcol.agents.NetworkSim.SimulationParameters;
import static unalcol.agents.NetworkSim.environment.NetworkEnvironmentReplication.setTotalAgents;

public class NetworkEnvironmentPheromoneReplication extends NetworkEnvironmentReplication {

    public NetworkEnvironmentPheromoneReplication(Vector<Agent> _agents, SimpleLanguage _language, Graph<GraphElements.MyVertex, String> gr) {
        super(_agents, _language, gr);
    }

    @Override
    public boolean act(Agent agent, Action action) {
        agent.sleep(30);
        if (agent instanceof MobileAgent) {
            boolean flag = (action != null);
            MobileAgent a = (MobileAgent) agent;
            ActionParameters ac = (ActionParameters) action;

            synchronized (a.getLocation().getData()) {
                //Get data from agent and put information in node
                for (Object data : a.getData()) {
                    if (!a.getLocation().getData().contains(data)) {
                        a.getLocation().getData().add(data);
                    }
                }
            }

            // Communication among agents 
            //detects other agents in network
            ArrayList<Integer> agentNeighbors = getAgentNeighbors(a);

            //serialize messages 
            String[] message = new String[2]; //msg: [from|msg]
            message[0] = String.valueOf(a.getId());
            message[1] = ObjectSerializer.serialize(a.getData());

            //for each neighbor send a message
            for (Integer idAgent : agentNeighbors) {
                NetworkMessageBuffer.getInstance().putMessage(idAgent, message);
                a.incMsgSend();
            }

            String[] inbox = NetworkMessageBuffer.getInstance().getMessage(a.getId());

            int old_size = a.getData().size();
            int new_size = 0;

            //inbox: id | infi 
            if (inbox != null) {
                a.incMsgRecv();

                /*if (a.getIdFather() == Integer.valueOf(inbox[0])) {
                    System.out.println("My father is alive. Freeing memory");
                    a.die();
                    increaseAgentsDie();
                    getLocationAgents().put(a.getId(), null);
                    a.setLocation(null);
                    setChanged();
                    notifyObservers();
                    return flag;

                }*/
                ArrayList senderInf = (ArrayList) ObjectSerializer.deserialize(inbox[1]);

                // Join ArrayLists
                a.getData().removeAll(senderInf);
                a.getData().addAll(senderInf);
                new_size = a.getData().size();

                if (old_size < new_size) {
                    a.setPheromone(1.0f);
                }
            }

            if (flag) {
                String act = action.getCode();
                String msg = null;

                /**
                 * 0- "move"
                 */
                /* @TODO: Detect Stop Conditions for the algorithm */
                switch (language.getActionIndex(act)) {
                    case 0: // move
                        GraphElements.MyVertex v = (GraphElements.MyVertex) ac.getAttribute("location");

                        a.setPrevLocation(a.getLocation());
                        visitedNodes.add(currentNode);

                        a.setLocation(v);
                        getLocationAgents().put(a.getId(), a.getLocation());

                        String[] msgnoder = new String[3];
                        msgnoder[0] = "freeresp";
                        msgnoder[1] = String.valueOf(a.getId());
                        NetworkNodeMessageBuffer.getInstance().putMessage(a.getPrevLocation().getName(), msgnoder);

                        String[] msgnode = new String[3];
                        msgnode[0] = "arrived";
                        msgnode[1] = String.valueOf(a.getId());
                        NetworkNodeMessageBuffer.getInstance().putMessage(a.getLocation().getName(), msgnode);

                        currentNode = v;

                        //System.out.println("a despues" + a.getLocation());
                        a.setPheromone((float) (a.getPheromone() + 0.01f * (0.5f - a.getPheromone())));
                        a.getLocation().setPh(a.getLocation().getPh() + 0.01f * (a.getPheromone() - a.getLocation().getPh()));
                        a.setRound(a.getRound() + 1);

                        boolean complete = false;
                        if (a.getData().size() == topology.getVertexCount()) {
                            complete = true;
                        }

                        if (getRoundComplete() == -1 && complete) {
                            System.out.println("complete! round" + a.getRound());
                            setRoundComplete(a.getRound());
                            setIdBest(a.getId());
                            updateWorldAge();
                        }
                        break;
                    case 1: //die
                        System.out.println("Agent " + a.getId() + "has failed");
                        a.die();
                        increaseAgentsDie();
                        getLocationAgents().put(a.getId(), null);
                        a.setLocation(null);
                        setChanged();
                        notifyObservers();
                        return false;
                    default:
                        msg = "[Unknown action " + act
                                + ". Action not executed]";
                        System.out.println(msg);
                        break;
                }
            }

            updateWorldAge();
            setChanged();
            notifyObservers();
            //System.out.println("wat" + a.getId());
            return flag;
        }
        if (agent instanceof Node) {
            Node n = (Node) agent;
            n.incRounds();

            /* A node process messages */
            String[] inbox;
            while ((inbox = NetworkNodeMessageBuffer.getInstance().getMessage(n.getVertex().getName())) != null) {
                //inbox: node | agent
                if (inbox[0].equals("arrived")) {
                    int agentId = Integer.valueOf(inbox[1]);
                    n.setLastAgentArrival(agentId, n.getRounds());
                    n.incMsgRecv();
                    //System.out.println("Node " + n.getVertex().getName() + " recv message: " + inbox[0]);
                    n.getResponsibleAgents().put(agentId, n.getRounds());
                    n.calculateTimeout();
                    //System.out.println("node " + n.getVertex().getName() + " is responsible for agents:" + n.getResponsibleAgents());
                }
                if (inbox[0].equals("freeresp")) {
                    n.incMsgRecv();
                    //System.out.println("Node " + n.getVertex().getName() + " recv message: " + inbox[0] + "," + n.getRounds());
                    int agentId = Integer.valueOf(inbox[1]);
                    n.setLastMessageArrival(agentId, n.getRounds());
                    n.calculateTimeout();
                    n.getResponsibleAgents().remove(agentId);
                    //System.out.println("node " + n.getVertex().getName() + " is no more responsible for " + n.getResponsibleAgents() + "," + n.getRounds());
                }
            }
            n.calculateTimeout();
            evaluateAgentCreation(n);
        }
        return false;
    }

    public void evaporatePheromone() {
        for (GraphElements.MyVertex v : topology.getVertices()) {
            //System.out.println(v.toString() + "before:" + v.getPh());
            v.setPh(v.getPh() - v.getPh() * 0.001f);
            //System.out.println(v.toString() + "after:" + v.getPh());
        }
    }

    //Example: It is better handshake protocol. J. Gomez
    public void evaluateAgentCreation(Node n) {
        synchronized (NetworkEnvironmentPheromoneReplication.class) {
            Iterator<Map.Entry<Integer, Integer>> iter = n.getResponsibleAgents().entrySet().iterator();
            /*if (!n.getResponsibleAgents().isEmpty()) {
                System.out.println(n.getVertex().getName() + " hashmap " + n.getResponsibleAgents());
            }*/
            while (iter.hasNext()) {
                //Key: agentId|roundNumber
                Map.Entry<Integer, Integer> Key = iter.next();
                int k = Key.getKey();
                int estimatedTimeout = n.estimateTimeout();

                System.out.println("node: " + n.getVertex().getName() + ", timeout " + estimatedTimeout);

                if (estimatedTimeout > 1 && n.getLastAgentArrival().containsKey(k) && ((n.getRounds() - n.getLastAgentArrival(k)) > estimatedTimeout)) { //this is not the expresion
                    System.out.println("entra n rounds:" + (n.getRounds() - n.getLastAgentArrival().get(k)) + " > " + estimatedTimeout);

                    //if (Math.random() < n.getPfCreate()) {
                    System.out.println("create new agent instance..." + n.getVertex().getName());
                    AgentProgram program = MotionProgramSimpleFactory.createMotionProgram(SimulationParameters.pf, SimulationParameters.motionAlg);

                    int newAgentID = agents.size();
                    MobileAgent a = new MobileAgent(program, newAgentID);

                    System.out.println("creating agent id" + newAgentID);
                    NetworkMessageBuffer.getInstance().createBuffer(newAgentID);

                    //getLocationAgents().add(new GraphElements.MyVertex("null"));
                    a.setId(newAgentID);
                    a.setData(new ArrayList(n.getVertex().getData()));

                    a.setIdFather(n.getResponsibleAgents().get(k));
                    a.setRound(super.getAge());
                    this.agents.add(a);

                    a.live();
                    Thread t = new Thread(a);
                    a.setThread(t);
                    a.setLocation(n.getVertex());
                    a.setPrevLocation(n.getVertex());
                    a.setArchitecture(this);
                    setTotalAgents(getTotalAgents() + 1);

                    String[] msgnode = new String[3];
                    msgnode[0] = "arrived";
                    msgnode[1] = String.valueOf(a.getId());
                    NetworkNodeMessageBuffer.getInstance().putMessage(a.getLocation().getName(), msgnode);
                    t.start();

                    System.out.println("node before: " + n.getVertex().getName() + " - " + n.getResponsibleAgents());
                    iter.remove();
                    System.out.println("node after: " + n.getVertex().getName() + " - " + n.getResponsibleAgents());
                    System.out.println("end creation of agent" + newAgentID);
                }

            }

        }
    }
}
