package maps;

import java.util.Random;
import randomChoose.ChooseVector;

/**
 *
 * @author fusy
 */
public class Network extends TwoConnectedMap{
    public static ChooseVector choose_vector_non_trivial_D=new ChooseVector(3);
    public static ChooseVector choose_vector_D=new ChooseVector(4);  
    public static ChooseVector choose_vector_P=new ChooseVector(2);  
    public static ChooseVector ch_y_or_P_or_H=new ChooseVector(3);  
    public static ChooseVector ch_S_or_H=new ChooseVector(2);  
    public static ChooseVector poisson_S_plus_H=new ChooseVector(17); 
    public static ChooseVector poisson_at_least1_S_plus_H=new ChooseVector(17);  
    public static ChooseVector poisson_at_least2_S_plus_H=new ChooseVector(17);  
    public static ChooseVector choose_vector_dD=new ChooseVector(3);  
    public static ChooseVector choose_vector_dS=new ChooseVector(3);  
    public static ChooseVector choose_vector_dP=new ChooseVector(2);  
    public static ChooseVector choose_vector_dH=new ChooseVector(2);  
    public static ChooseVector ch_dP_or_dH=new ChooseVector(2);  
    public static ChooseVector ch_dS_or_dH=new ChooseVector(2);  
    public static ChooseVector choose_vector_ddD=new ChooseVector(3);  
    public static ChooseVector choose_vector_ddS=new ChooseVector(5); 
    public static ChooseVector choose_vector_ddP=new ChooseVector(4); 
    public static ChooseVector choose_vector_ddH=new ChooseVector(4); 
    public static ChooseVector ch_ddP_or_ddH=new ChooseVector(2); 
    public static ChooseVector ch_ddS_or_ddH=new ChooseVector(2); 
    
    
    /** Creates a new instance of Network */
    public Network() {
    }
    
    
    
    public static Network merge2parallel(Network n1, Network n2){
        if(n1==null) return n2;
        if(n2==null) return n1;
        DemiArete r1=n1.rootDemiArete;
        DemiArete r2=n2.rootDemiArete;
        
        DemiArete r1precOld=r1.prec;
        r1.prec=r2.prec;
        r2.prec.next=r1;
        r2.next.prec=r1precOld;
        r1precOld.next=r2.next;
       
        DemiArete r1oppNextOld=r1.opp.next;
        r1.opp.next=r2.opp.next;
        r2.opp.next.prec=r1.opp;
        r2.opp.prec.next=r1oppNextOld;
        r1oppNextOld.prec=r2.opp.prec;
        
        n1.vertexList.addAll(n2.vertexList);
        n1.nr_edges+=n2.nr_edges;
        return n1;
    }
    
    public static Network mergeParallel(Network[] n){
        Network result=n[0];
        for (int i=1;i<n.length;++i){
            result=Network.merge2parallel(n[i-1],n[i]);
        }
        return result;
    }
    
    public static Network merge2series(Network n1, Network n2){
        DemiArete r1=n1.rootDemiArete;
        DemiArete r2=n2.rootDemiArete;
        
        DemiArete h2=r2.next;
        DemiArete h1=r1.opp.prec;
        h1.next=h2;
        h2.prec=h1;
        
        DemiArete l2=r2.prec;
        DemiArete l1=r1.opp.next;
        l1.prec=l2;
        l2.next=l1;
        
        r1.opp=r2.opp;
        r2.opp.opp=r1;
        
        n1.vertexList.addAll(n2.vertexList);
        n1.vertexList.add(h2);
        n1.nr_edges+=n2.nr_edges;
        return n1;
    }
    
    public static Network mergeSeries(Network[] n){
        Network result=n[0];
        for (int i=1;i<n.length;++i){
            result=Network.merge2series(n[i-1],n[i]);
        }
        return result;
    }
    
    public static Network trivialNetwork(){
        Network result=new Network();
        DemiArete r1=new DemiArete();
        DemiArete r2=new DemiArete();
        DemiArete h1=new DemiArete();
        DemiArete h2=new DemiArete();
        
        r1.opp=r2; r2.opp=r1;
        h1.opp=h2; h2.opp=h1;
        h2.next=r2; r2.prec=h2; h2.prec=r2; r2.next=h2;
        h1.next=r1; r1.prec=h1; h1.prec=r1; r1.next=h1;
        
        result.rootDemiArete=r1;
        result.nr_edges=1;
        return result;
    }
    
    public void toTwoConnectedMap(){
        DemiArete e=this.polesAdjacent();
        if (this.polesAdjacent()!=null) this.removePoleConnectingEdge(e);
        this.vertexList.add(rootDemiArete);
        this.vertexList.add(rootDemiArete.opp);
        this.nr_edges++;
    }   
    
    // if the poles are adjacent, returns the half-edge incident to the root-vertex and belonging to
    // the non-root edge connecting the two poles 
    public DemiArete polesAdjacent(){
        DemiArete e=this.rootDemiArete.next;
        while(e!=this.rootDemiArete)
        {
            if (DemiArete.belongToSameVertex(e.opp, rootDemiArete.opp)) return e;
            e=e.next;
        }
        return null;
    }
    
    // The parameter e is the DemiArete incident to the root vertex and belonging 
    // to the non-root edge connecting the two poles
    public void removePoleConnectingEdge(DemiArete e){
        
        DemiArete enext=e.next; DemiArete eprec=e.prec;
        eprec.next=enext; enext.prec=eprec;
        
        DemiArete eoppnext=e.opp.next; DemiArete eoppprec=e.opp.prec;
        eoppprec.next=eoppnext; eoppnext.prec=eoppprec;
                
        this.nr_edges--;
    }
    
    
    
    public static Network draw_non_trivial_D(Random r){
        // nontrivialD  = S+P+H
        int i=choose_vector_non_trivial_D.choose(r);
        if (i==0) return draw_S(r);
        if (i==1) return draw_P(r);
        if (i==2) return draw_H(r);
        else {System.out.println("error in choose vector non trivial D: index= "+i); return null;}
    }
    
    public static Network draw_D(Random r){
        // D = y+S+P+H
        int i=choose_vector_D.choose(r);
        if (i==0) return trivialNetwork();
        if (i==1) return draw_S(r);
        if (i==2) return draw_P(r);
        if (i==3) return draw_H(r);
        else {System.out.println("error in choose vector D: index= "+i); return null;}
    }
    
    public static Network draw_S(Random r){
        Network n1=draw_y_or_P_or_H(r);
        Network n2=draw_D(r);
        return merge2series(n1,n2);
    }
    
    public static Network draw_P(Random r){
        // P = y*(exp(S+H)-1)+(exp(S+H)-S-H-1)
        int i=choose_vector_P.choose(r);
        if (i==0) {
            Network n1=draw_poisson_at_least1_S_plus_H(r);
            return merge2parallel(trivialNetwork(), n1);
        }
        if (i==1) return draw_poisson_at_least2_S_plus_H(r);
        else {System.out.println("error in choose vector P: index= "+i); return null;}
    }
    
    public static Network draw_H(Random r){
        //System.out.println("brick");
        ThreeConnectedNetwork core=ThreeConnectedNetwork.draw_K(r);
        //for (int i=0;i<core.edgeList.size();++i) core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_D(r));
        for (int i=0;i<core.edgeList.size();++i) core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),trivialNetwork());
        return core;
    }
    
    public static Network draw_y_or_P_or_H(Random r){
        int i=ch_y_or_P_or_H.choose(r);
        if (i==0) return trivialNetwork();
        if (i==1) return draw_P(r);
        if (i==2) return draw_H(r);
        else {System.out.println("error in choose vector y_or_P_or_H: index= "+i); return null;}
    }
    
    public static Network draw_S_or_H(Random r){
        int i=ch_S_or_H.choose(r);
        if (i==0) return draw_S(r);
        if (i==1) return draw_H(r);
        else {System.out.println("error in choose vector S_or_H: index= "+i); return null;}
    }
    
    public static Network draw_poisson_S_plus_H(Random r){
        int i=poisson_S_plus_H.choose(r);
        if (i==0) return null;
        Network[] n=new Network[i];
        for (int j=0;j<i;++j) n[j]=draw_S_or_H(r);
        return mergeParallel(n);
    }
    
    public static Network draw_poisson_at_least1_S_plus_H(Random r){
        int i=poisson_at_least1_S_plus_H.choose(r);
        Network[] n=new Network[i+1];
        for (int j=0;j<=i;++j) n[j]=draw_S_or_H(r);
        return mergeParallel(n);
    }
    
    public static Network draw_poisson_at_least2_S_plus_H(Random r){
        int i=poisson_at_least2_S_plus_H.choose(r);
        Network[] n=new Network[i+2];
        for (int j=0;j<=i+1;++j) n[j]=draw_S_or_H(r);
        return mergeParallel(n);
    }
    
    public static Network draw_dD(Random r){
        // dD = dS+dP+dH
        int i=choose_vector_dD.choose(r);
        if (i==0) return draw_dS(r);
        if (i==1) return draw_dP(r);
        if (i==2) return draw_dH(r);
        else {System.out.println("error in choose vector dD: index= "+i); return null;}
    }
    
    public static Network draw_dS(Random r){
        // dS = (dP+dH)*x*D+(y+P+H)*D+(y+P+H)*x*dD
        int i=choose_vector_dS.choose(r);
        if (i==0) return merge2series(draw_dP_or_dH(r),draw_D(r));
        if (i==1) return merge2series(draw_y_or_P_or_H(r),draw_D(r));
        if (i==2) return merge2series(draw_y_or_P_or_H(r),draw_dD(r));
        else {System.out.println("error in choose vector dS: index= "+i); return null;}
    }
    
    public static Network draw_dP(Random r){
        // dP = y*(dS+dH)*exp(S+H)+(dS+dH)*(exp(S+H)-1)
        int i=choose_vector_dP.choose(r);
        if (i==0) return merge2parallel(trivialNetwork(), merge2parallel(draw_dS_or_dH(r),draw_poisson_S_plus_H(r)));
        if (i==1) return merge2parallel(draw_dS_or_dH(r),draw_poisson_at_least1_S_plus_H(r));
        else {System.out.println("error in choose vector dP: index= "+i); return null;}
    }

    public static Network draw_dH(Random r){
        //System.out.println("brick");
    // dH = dxK+dD*dyK
        int c=choose_vector_dH.choose(r);
        if(c==0){
            ThreeConnectedNetwork core=ThreeConnectedNetwork.draw_dxK(r);
            for (int i=0;i<core.edgeList.size();++i) core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_D(r));
            return core;
        }
        if(c==1){
            ThreeConnectedNetwork core=ThreeConnectedNetwork.draw_dyK(r);
            int j=r.nextInt(core.edgeList.size());
            for (int i=0;i<core.edgeList.size();++i){
                  if (i!=j) core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_D(r));
                  else core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_dD(r));
            }
            return core;
            
        }
        else {System.out.println("error in choose vector dH: index= "+c); return null;}
    }
    
    public static Network draw_dP_or_dH(Random r){
        int i=ch_dP_or_dH.choose(r);
        if (i==0) return draw_dP(r);
        if (i==1) return draw_dH(r);
        else {System.out.println("error in choose vector dP_or_dH: index= "+i); return null;}
    }
    
    public static Network draw_dS_or_dH(Random r){
        int i=ch_dS_or_dH.choose(r);
        if (i==0) return draw_dS(r);
        if (i==1) return draw_dH(r);
        else {System.out.println("error in choose vector dS_or_dH: index= "+i); return null;}
    }
    
    public static Network draw_ddD(Random r){
    // ddD = ddS+ddP+ddH
        int i=choose_vector_ddD.choose(r);
        if (i==0) return draw_ddS(r);
        if (i==1) return draw_ddP(r);
        if (i==2) return draw_ddH(r);
        else {System.out.println("error in choose vector ddD: index= "+i); return null;}
    }
    
    public static Network draw_ddS(Random r){
    // ddS =(ddP+ddH)*x*D+2*(dP+dH)*D+2*(dP+dH)*x*dD+2*(y+P+H)*dD+(y+P+H)*x*ddD
        int i=choose_vector_ddS.choose(r);
        if (i==0) return merge2series(draw_ddP_or_ddH(r),draw_D(r));
        if (i==1) return merge2series(draw_dP_or_dH(r),draw_D(r));
        if (i==2) return merge2series(draw_dP_or_dH(r),draw_dD(r));  
        if (i==3) return merge2series(draw_y_or_P_or_H(r),draw_dD(r));   
        if (i==4) return merge2series(draw_y_or_P_or_H(r),draw_ddD(r));  
        else {System.out.println("error in choose vector ddS: index= "+i); return null;}
    }
    
    public static Network draw_ddP(Random r){
    // ddP=y*(ddS+ddH)*exp(S+H)+y*(dS+dH)^2*exp(S+H)+(ddS+ddH)*(exp(S+H)-1)+(dS+dH)^2*exp(S+H)
        int i=choose_vector_ddP.choose(r);
        if (i==0) return merge2parallel(trivialNetwork(),merge2parallel(draw_ddS_or_ddH(r),draw_poisson_S_plus_H(r)));
        if (i==1) return merge2parallel(trivialNetwork(),merge2parallel(draw_dS_or_dH(r),merge2parallel(draw_dS_or_dH(r),draw_poisson_S_plus_H(r))));
        if (i==2) return merge2parallel(draw_ddS_or_ddH(r),draw_poisson_at_least1_S_plus_H(r));  
        if (i==3) return merge2parallel(draw_dS_or_dH(r),merge2parallel(draw_dS_or_dH(r),draw_poisson_S_plus_H(r)));   
        else {System.out.println("error in choose vector ddP: index= "+i); return null;}
    }
    
    public static Network draw_ddH(Random r){
        //System.out.println("brick");
    // ddH=dxxK+2*dD*dxyK+ddD*dyK+dD^2*dyyK    
        int c=choose_vector_ddH.choose(r);
        if(c==0){
            ThreeConnectedNetwork core=ThreeConnectedNetwork.draw_dxxK(r);
            for (int i=0;i<core.edgeList.size();++i) core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_D(r));
            return core;
        }
        if (c==1){
            ThreeConnectedNetwork core=ThreeConnectedNetwork.draw_dxyK(r);
            int j=r.nextInt(core.edgeList.size());
            for (int i=0;i<core.edgeList.size();++i){
                  if (i!=j) core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_D(r));
                  else core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_dD(r));
            }
            return core;
        }
        
        if (c==2){
            ThreeConnectedNetwork core=ThreeConnectedNetwork.draw_dxyK(r);
            int j=r.nextInt(core.edgeList.size());
            for (int i=0;i<core.edgeList.size();++i){
                  if (i!=j) core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_D(r));
                  else core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_dD(r));
            }
            return core;
        }
        
        if (c==3) {
            ThreeConnectedNetwork core=ThreeConnectedNetwork.draw_dyyK(r);
            // j and m are two different random integers in [0,size-1] in order to pick up to different 
            // edges of the core at random
            int j=r.nextInt(core.edgeList.size());
            int m=r.nextInt(core.edgeList.size()-1); if (m>=j) ++m;
            for (int i=0;i<core.edgeList.size();++i){
                  if ((i!=j)&&(i!=m)) core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_D(r));
                  else core.replaceEdgeByNetwork(core.edgeList.getDemiArete(i),draw_dD(r));
            }
            return core;
        
        }
        else {System.out.println("error in choose vector ddH: index= "+c); return null;}
    }
    
    public static Network draw_ddP_or_ddH(Random r){
        int i=ch_ddP_or_ddH.choose(r);
        if (i==0) return draw_ddP(r);
        if (i==1) return draw_ddH(r);
        else {System.out.println("error in choose vector ddP_or_ddH: index= "+i); return null;}
    }
    
    public static Network draw_ddS_or_ddH(Random r){
        int i=ch_ddS_or_ddH.choose(r);
        if (i==0) return draw_ddS(r);
        if (i==1) return draw_ddH(r);
        else {System.out.println("error in choose vector ddS_or_ddH: index= "+i); return null;}
    }
    
}
