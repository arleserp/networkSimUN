/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unalcol.agents.NetworkSim.programs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import unalcol.agents.Action;
import unalcol.agents.AgentProgram;
import unalcol.agents.NetworkSim.ActionParameters;
import unalcol.agents.NetworkSim.GraphElements;
import unalcol.agents.Percept;
import unalcol.random.RandomUtil;

/**
 *
 * @author My-Macintosh
 */
public class FirstNeighborVisitedPh implements AgentProgram {
    
    float pf;

    public FirstNeighborVisitedPh(float pf) {
        this.pf = pf;
        //System.out.println("random motion program");
        // System.out.println("pf: " + pf);
    }

    @Override
    public Action compute(Percept p) {
        ActionParameters act = new ActionParameters("move");
        int pos;

        if (Math.random() < pf) {
            return new ActionParameters("die");
        }

        Collection<GraphElements.MyVertex> empty = (Collection<GraphElements.MyVertex>) p.getAttribute("neighbors");
        Iterator<GraphElements.MyVertex> it = empty.iterator();
        ArrayList<GraphElements.MyVertex> vs = new ArrayList<>();
        ArrayList<GraphElements.MyVertex> tt = new ArrayList<>();      

        while(it.hasNext()){
            GraphElements.MyVertex v = it.next();
            tt.add(v);
            if(v!=null){
                if(v.getStatus() != "v"){
                    vs.add(v);
                }
            }
        }
        
        if (vs.size() == 0){
            vs = tt;
            pos = (int) (Math.random() * vs.size());
        }else{
            pos = carry(vs);
        }
        
        
        try {
            boolean isSet = false;
            do {
                if (((GraphElements.MyVertex) vs.toArray()[pos]) != null) {
                    act.setAttribute("location", vs.toArray()[pos]);
                    act.setAttribute("pf", pf);
                    isSet = true;
                }
            } while (!isSet);
            //System.out.println("location" + vs.toArray()[pos]);
        } catch (Exception ex) {
            // System.out.println("this cannot happen!!! agent fail because node is not running or was killed determining new movement." + vs);
            //return new ActionParameters("die");
            // System.out.println("Inform node that possibly a node is death: " + ex.getLocalizedMessage());
            return new ActionParameters("informfailure");
        }
        //System.out.println("act:" + act.getCode());
        return act;
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    int Roulette(float[] pheromone) {
        //System.out.println("roulette");
        float sum = 0;
        for (int k = 0; k < pheromone.length; k++) {
            sum += pheromone[k];
        }
        double rand = (double) (Math.random() * sum);
        sum = 0;
        int mov = 0;
        for (int k = 0; k < pheromone.length; k++) {
            sum += pheromone[k];
            if (rand < sum) {
                mov = k;
                break;
            }
        }
        return mov;
    }

    private int carry(Collection<GraphElements.MyVertex> vs) {
        int dirPos = 0;
        float q0 = 0.9f;

        ArrayList<Integer> temp = new ArrayList();
        if (Math.random() <= q0) {
            for (int k = 0; k < vs.size(); k++) {
                if (((GraphElements.MyVertex) vs.toArray()[k]).getPh() != -1) {
                    dirPos = k;
                    break;
                }
            }

            for (int k = dirPos + 1; k < vs.size(); k++) {
                if (((GraphElements.MyVertex) vs.toArray()[k]).getPh() != -1 && ((GraphElements.MyVertex) vs.toArray()[dirPos]).getPh() > ((GraphElements.MyVertex) vs.toArray()[k]).getPh()) {
                    dirPos = k;
                }
            }

            //store location with the min amount of pheromone
            float min = ((GraphElements.MyVertex) vs.toArray()[dirPos]).getPh();
            temp.add(dirPos);
            for (int k = 0; k < vs.size(); k++) {
                if (((GraphElements.MyVertex) vs.toArray()[k]).getPh() == min) {
                    temp.add(k);
                }
            }
            //dirPos = LevyWalk(proximitySensor, termitesNeighbor);
            //if (!temp.contains(dirPos)) {

            dirPos = temp.get(RandomUtil.nextInt(temp.size()));
            //}
        } else {
            //Idea is choose direction with the less amount of pheromone
            float[] phinv = new float[vs.size()];
            for (int i = 0; i < vs.size(); i++) {
                phinv[i] = 1 - ((GraphElements.MyVertex) vs.toArray()[i]).getPh();
            }
            dirPos = Roulette(phinv);
            //dirPos = LevyWalk(proximitySensor, termitesNeighbor);
        }
        return dirPos;
    }
}