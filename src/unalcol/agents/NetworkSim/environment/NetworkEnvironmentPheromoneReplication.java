package unalcol.agents.NetworkSim.environment;

import unalcol.agents.simulate.util.*;
import unalcol.agents.*;

import java.util.Vector;

import edu.uci.ics.jung.graph.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import unalcol.agents.NetworkSim.ActionParameters;
import unalcol.agents.NetworkSim.GraphElements;
import unalcol.agents.NetworkSim.MobileAgent;
import unalcol.agents.NetworkSim.MotionProgramSimpleFactory;
import unalcol.agents.NetworkSim.Node;
import unalcol.agents.NetworkSim.SimulationParameters;
import static unalcol.agents.NetworkSim.environment.NetworkEnvironmentReplication.setTotalAgents;
import unalcol.agents.NetworkSim.util.StatsCreation;

public class NetworkEnvironmentPheromoneReplication extends NetworkEnvironmentReplication {

    private static int falsePossitives = 0;
    private static int agentMovements = 0;
    private static int ACKAmount = 0;
    int hops = 1;
    
    public static synchronized void incrementFalsePossitives() {
        falsePossitives ++;
    }

    /**
     * @return the falsePossitives
     */
    public static int getFalsePossitives() {
        return falsePossitives;
    }

    public static synchronized void incrementAgentMovements() {
        agentMovements++;
    }

    /**
     * @return the falsePossitives
     */
    public static int getAgentMovements() {
        return agentMovements;
    }

    public static synchronized void incrementACKAmount() {
        ACKAmount++;
    }

    /**
     * @return the falsePossitives
     */
    public static int getACKAmount() {
        return ACKAmount;
    }

   
    public NetworkEnvironmentPheromoneReplication(Vector<Agent> _agents, SimpleLanguage _language, Graph<GraphElements.MyVertex, String> gr) {
        super(_agents, _language, gr);
    }

    @Override
    public boolean act(Agent agent, Action action) {
        agent.sleep(30);
        if (agent instanceof MobileAgent) {
            boolean flag = (action != null);
            MobileAgent a = (MobileAgent) agent;

            if (a.status == Action.DIE || a.getLocation() == null) {
                return false;
            }
            currentNode = a.getLocation();
            visitedNodes.add(currentNode);
            getLocationAgents().put(a, a.getLocation());
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
                        //System.out.println("a despues" + a.getLocation());
                        boolean complete = false;
                        if (a.getData().size() == getTopology().getVertexCount()) {
                            complete = true;
                        }

                        if (getRoundComplete() == -1 && complete) {
                            System.out.println("complete! round" + a.getRound());
                            setRoundComplete(a.getRound());
                            setIdBest(a.getId());
                            //updateWorldAge();
                        }

                        //get new location
                        GraphElements.MyVertex v = (GraphElements.MyVertex) ac.getAttribute("location");
                        float pf = (float) ac.getAttribute("pf");
                        //Send a message to current node before moving to new destination v
                        //msgnode: "departing"|agentId|FatherId|newDest

                        if (SimulationParameters.activateReplication.equals("replalgon")) {
                            String[] msgnode = new String[4];
                            msgnode[0] = "departing";
                            msgnode[1] = String.valueOf(a.getId());
                            msgnode[2] = String.valueOf(a.getIdFather());
                            msgnode[3] = v.getName();
                            if (a.getLocation() != null) {
                                NetworkNodeMessageBuffer.getInstance().putMessage(a.getLocation().getName(), msgnode);
                            }
                        }

                        //Agent Fail when moving
                        if (Math.random() < pf) {
                            //System.out.println("Agent " + a.getId() + "has failed");
                            a.die();
                            increaseAgentsDie();
                            getLocationAgents().put(a, null);
                            a.setLocation(null);
                            setChanged();
                            notifyObservers();
                            return false;
                        }

                        if (!SimulationParameters.nodeDelay.equals("NODELAY")) {
                            int nodeDelay = Integer.valueOf(SimulationParameters.nodeDelay);
                            a.sleep(nodeDelay);
                        }

                        a.setPrevLocation(a.getLocation());
                        a.setLocation(v);
                        getLocationAgents().put(a, a.getLocation());
                        a.setPheromone((float) (a.getPheromone() + 0.01f * (0.5f - a.getPheromone())));
                        a.getLocation().setPh(a.getLocation().getPh() + 0.01f * (a.getPheromone() - a.getLocation().getPh()));
                        a.setRound(a.getRound() + 1);
                        incrementAgentMovements();

                        if (SimulationParameters.activateReplication.equals("replalgon")) {
                            if (a.getPrevLocation() != null) {
                                String[] msgnoder = new String[3];
                                msgnoder[0] = "freeresp";
                                msgnoder[1] = String.valueOf(a.getId());
                                msgnoder[2] = a.getLocation().getName();
                                NetworkNodeMessageBuffer.getInstance().putMessage(a.getPrevLocation().getName(), msgnoder);
                            }
                        }
                        currentNode = v;
                        visitedNodes.add(currentNode);
                        break;
                    case 1: //die
                        //System.out.println("Agent " + a.getId() + "has failed");
                        a.die();
                        increaseAgentsDie();
                        getLocationAgents().put(a, null);
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

            //updateWorldAge();
            setChanged();
            notifyObservers();
            //System.out.println("wat" + a.getId());
            return flag;
        }
        if (agent instanceof Node) {
            Node n = (Node) agent;
            n.incRounds();
            if (SimulationParameters.activateReplication.equals("replalgon")) {
                /* A node process messages */
                String[] inbox;
                while ((inbox = NetworkNodeMessageBuffer.getInstance().getMessage(n.getVertex().getName())) != null) {
                    //inbox: node | agent
                    /*if (inbox[0].equals("arrived")) {
                    int agentId = Integer.valueOf(inbox[1]);
                    n.setLastAgentArrival(agentId, n.getRounds());
                    n.incMsgRecv();
                    //System.out.println("Node " + n.getVertex().getName() + " recv message: " + inbox[0]);
                    n.getResponsibleAgents().put(agentId, Integer.valueOf(inbox[2]));
                    n.getResponsibleAgentsLocation().put(agentId, inbox[3]);
                    n.calculateTimeout();
                    //System.out.println("node " + n.getVertex().getName() + " is responsible for agents:" + n.getResponsibleAgents());
                }*/

                    //Send a message to current node before moving to new destination v
                    //msgnode: "departing"|agentId|FatherId|newDest
                    if (inbox[0].equals("departing")) {
                        int agentId = Integer.valueOf(inbox[1]);
                        n.setLastAgentDeparting(agentId, n.getRounds(), hops);
                        n.incMsgRecv();
                        //System.out.println("Node " + n.getVertex().getName() + " recv message: " + inbox[0]);
                        n.getFollowedAgents(hops).put(agentId, Integer.valueOf(inbox[2]));
                        //System.out.println("n" + n.getResponsibleAgents());
                        n.getFollowedAgentsLocation(hops).put(agentId, inbox[3]);
                        n.calculateTimeout(hops);
                        //System.out.println("departing node age" + n.getRounds());
                        //System.out.println("node " + n.getVertex().getName() + " is responsible for agents:" + n.getResponsibleAgents());
                    }

                    if (inbox[0].equals("freeresp")) {
                        incrementACKAmount();
                        n.incMsgRecv();
                        //System.out.println("Node " + n.getVertex().getName() + " recv message: " + inbox[0] + "," + n.getRounds());
                        int agentId = Integer.valueOf(inbox[1]);
                        String newLocation = inbox[2];
                        n.setLastMessageFreeResp(agentId, n.getRounds(), newLocation, hops);
                        n.calculateTimeout(hops);
                        if (n.getFollowedAgents(hops).containsKey(agentId)) {
                            n.getFollowedAgents(hops).remove(agentId);
                        } else {
                            //System.out.println("Delete replica!!!!!!");
                            incrementFalsePossitives();
                            deleteNextReplica(n);
                        }
                        //System.out.println("freeresp node age" + n.getRounds());
                        //System.out.println("node " + n.getVertex().getName() + " is no more responsible for " + n.getResponsibleAgents() + "," + n.getRounds());
                    }
                }
                n.calculateTimeout(hops);
                evaluateAgentCreation(n);
            }
            //setChanged();
            //notifyObservers();
        }
        return false;
    }

    public void evaporatePheromone() {
        for (GraphElements.MyVertex v : getTopology().getVertices()) {
            //System.out.println(v.toString() + "before:" + v.getPh());
            v.setPh(v.getPh() - v.getPh() * 0.001f);
            //System.out.println(v.toString() + "after:" + v.getPh());
        }
    }

    //Example: It is better handshake protocol. J. Gomez
    public void evaluateAgentCreation(Node n) {

        synchronized (NetworkEnvironmentPheromoneReplication.class) {
            Iterator<Map.Entry<Integer, Integer>> iter = n.getFollowedAgents(hops).entrySet().iterator();
            Iterator<Map.Entry<Integer, String>> iterLoc = n.getFollowedAgentsLocation(hops).entrySet().iterator();
            ///if (!n.getResponsibleAgents().isEmpty()) {
            // System.out.println(n.getVertex().getName() + " hashmap " + n.getResponsibleAgents());
            /*}*/

            //System.out.println(n.getVertex().getName() + " hashmap " + n.getResponsibleAgents());
            int estimatedTimeout = 0;
            int stdDevTimeout = 0;
            while (iter.hasNext()) {
                //Key: agentId|roundNumber
                Map.Entry<Integer, Integer> Key = iter.next();
                int k = Key.getKey();
                estimatedTimeout = n.estimateExpectedTime(n.getFollowedAgentsLocation(hops).get(k), hops);
                stdDevTimeout = (int) n.getStdDevTimeout(n.getFollowedAgentsLocation(hops).get(k), hops);

                if (n.getLastAgentDeparting(hops).containsKey(k) && Math.abs((n.getRounds() - n.getLastAgentDeparting(k, hops))) > (estimatedTimeout + 3 * stdDevTimeout)) { //this is not the expresion
                    /*if (n.getResponsibleAgentsLocation().containsKey(k) && n.getNodeTimeouts().containsKey(n.getResponsibleAgentsLocation().get(k))) {
                        n.getNodeTimeouts().get(n.getResponsibleAgentsLocation().get(k)).add(estimatedTimeout);
                        //n.addTimeout(estimatedTimeout);
                    }*/
                    //System.out.println("node" + n.getVertex().getName() + "," + estimatedTimeout);
                    //System.out.println("create new agent instance..." + n.getVertex().getName());
                    AgentProgram program = MotionProgramSimpleFactory.createMotionProgram(SimulationParameters.pf, SimulationParameters.motionAlg);

                    int newAgentID = agents.size();
                    MobileAgent a = new MobileAgent(program, newAgentID);

                    //System.out.println("creating agent id" + newAgentID);
                    NetworkMessageBuffer.getInstance().createBuffer(newAgentID);

                    //getLocationAgents().add(new GraphElements.MyVertex("null"));
                    a.setId(newAgentID);
                    a.setData(new ArrayList(n.getVertex().getData()));

                    if (n.getFollowedAgents(hops).get(k) == -1) {
                        a.setIdFather(k);
                    } else {
                        a.setIdFather(n.getFollowedAgents(hops).get(k));
                    }
                    a.setRound(super.getAge());
                    this.agents.add(a);

                    a.live();
                    Thread t = new Thread(a);
                    a.setThread(t);
                    a.setLocation(n.getVertex());
                    a.setPrevLocation(n.getVertex());
                    a.setArchitecture(this);
                    setTotalAgents(getTotalAgents() + 1);

                    String[] msgnode = new String[4];
                    msgnode[0] = "arrived";
                    msgnode[1] = String.valueOf(a.getId());
                    msgnode[2] = String.valueOf(a.getIdFather());
                    msgnode[3] = String.valueOf(-1);
                    NetworkNodeMessageBuffer.getInstance().putMessage(a.getLocation().getName(), msgnode);
                    t.start();
                    //System.out.println("replica created:" + a.getId());
                    //System.out.println("add creation time" + (n.getRounds() - n.getLastAgentArrival(k)));
                    //n.addCreationTime(n.getRounds() - n.getLastAgentArrival(k));
                    //System.out.println("node before: " + n.getVertex().getName() + " - " + n.getResponsibleAgents());
                    iter.remove();
                    //System.out.println("node after: " + n.getVertex().getName() + " - " + n.getResponsibleAgents());
                    //System.out.println("end creation of agent" + newAgentID);
                }
            }
        }
    }

    private void deleteNextReplica(Node n) {
        Vector<Agent> copy = (Vector) agents.clone();
        for (Agent a : copy) {
            if (a instanceof MobileAgent) {
                MobileAgent t = (MobileAgent) a;
                if (t.getLocation() != null && t.getLocation().getName().equals(n.getVertex().getName())) {
                    if (t.getPrevLocation() != null) {
                        String[] msgnoder = new String[3];
                        msgnoder[0] = "freeresp";
                        msgnoder[1] = String.valueOf(t.getId());
                        msgnoder[2] = t.getLocation().getName();
                        NetworkNodeMessageBuffer.getInstance().putMessage(t.getPrevLocation().getName(), msgnoder);
                    }
                    a.die();
                    increaseAgentsDie();
                    getLocationAgents().put(t, null);

                    t.setLocation(null);

                    setChanged();
                    notifyObservers();
                    //System.out.println("delete replica!");
                    return;
                }
            }
        }
    }

    @Override
    public boolean isOccuped(GraphElements.MyVertex v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void validateNodesAlive() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getNodesAlive() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Node> getNodes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StatsCreation getStatAgentCreation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
