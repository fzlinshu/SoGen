package maps;

import java.util.Random;
import randomChoose.ChooseVector;

/**
 *
 * @author fusy
 */
public class TwoConnectedMap extends ConnectedMap{
        
    public static ChooseVector ch_xy_in_dB=new ChooseVector(2);
    public static ChooseVector ch_y_in_ddB=new ChooseVector(2);
    public static ChooseVector ch_nontrivialD_or_dD=new ChooseVector(2);
    public static ChooseVector ch_dD_or_ddD=new ChooseVector(2);
    
    /** Creates a new instance of TwoConnectedMap */
    public TwoConnectedMap() {
    }
    
    public static TwoConnectedMap edgeMap(){
        TwoConnectedMap result=new TwoConnectedMap();
        DemiArete r1=new DemiArete();
        DemiArete r2=new DemiArete();
        
        
        r1.opp=r2; r2.opp=r1;
        r1.next=r1; r1.prec=r1;
        r2.next=r2; r2.prec=r2;
        
        
        result.rootDemiArete=r1;
        result.vertexList.add(r1);
        result.vertexList.add(r2);
        result.nr_edges=1;
        return result;
    }
    
    public void replaceVertexByConnectedMap(DemiArete e, ConnectedMap connectedMap, Random r){
        // if the connectedMap consists of a single vertex, then we have nothing to do.
        if(connectedMap!=null){
            //System.out.println("replace");
            // choice of a random vertex in connectedMap, more precisely of an half-edge incident to this vertex
            int i=r.nextInt(connectedMap.vertexList.size());
            DemiArete f=connectedMap.vertexList.getDemiArete(i); if (f==null) System.out.println("root of replaced network is null");
            connectedMap.vertexList.remove(i);
            
            DemiArete eprec=e.prec; DemiArete fprec=f.prec;
            e.prec=fprec;
            fprec.next=e;
            f.prec=e.prec;
            eprec.next=f;
            f.prec=eprec;
            
            this.vertexList.addAll(connectedMap.vertexList);
            this.nr_edges+=(connectedMap.nr_edges);
        }
    }
    
    public static TwoConnectedMap draw_dB(Random r){
        
        int c=ch_xy_in_dB.choose(r);
        if (c==0) return edgeMap();
        
        else{
            PlanarMap.NR_BLOCKS++;
            while(true){
                Network network=Network.draw_non_trivial_D(r);
                network.toTwoConnectedMap();
                int i=network.vertexList.size();
                int j=network.nr_edges;
                double rejet=(double)(i)/(double)(j);//System.out.println(i+" "+j+" "+"reject 2connected: "+rejet);
                if(r.nextDouble()<=rejet) return network; //else System.out.println("2-connected is rejected");
            }
        }
    }
    
    public static TwoConnectedMap draw_ddB(Random r){
        
        int c=ch_y_in_ddB.choose(r);
        if (c==0) return edgeMap();
        else{
            PlanarMap.NR_BLOCKS++;
            Network network=new Network();
            while(true){
                int d=ch_nontrivialD_or_dD.choose(r);
                if(d==0) network=Network.draw_non_trivial_D(r);
                else network=Network.draw_dD(r);
                network.toTwoConnectedMap();
                int i=network.vertexList.size();
                int j=network.nr_edges;
                double rejet=(double)(i)/(double)(j); //System.out.println(i+" "+j+" "+"reject 2connected: "+rejet);
                if(r.nextDouble()<=rejet) return network;//else System.out.println("2-connected is rejected");
            }
        }
    }
    
    public static TwoConnectedMap draw_dddB(Random r){
        PlanarMap.NR_BLOCKS++;
        Network network=new Network();
        while(true){
            int d=ch_dD_or_ddD.choose(r);
            if(d==0) network=Network.draw_dD(r);
            else network=Network.draw_ddD(r);
            network.toTwoConnectedMap();
            int i=network.vertexList.size();
            int j=network.nr_edges;
            double rejet=(double)(i)/(double)(j); //System.out.println(i+" "+j+" "+"reject 2connected: "+rejet);
            if(r.nextDouble()<=rejet) return network;//else System.out.println("2-connected is rejected");
        }
        
    }
    
}
