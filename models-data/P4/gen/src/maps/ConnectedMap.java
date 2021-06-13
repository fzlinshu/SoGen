package maps;

import java.util.Random;
import randomChoose.ChooseVector;

public class ConnectedMap extends PlanarMap{
    
    public static ChooseVector poisson_dB=new ChooseVector(17);
    public static ChooseVector ch_dC_or_ddC=new ChooseVector(2);
    public static ChooseVector ch_2ddC_or_dddC=new ChooseVector(2);
    public static ChooseVector choose_vector_dddC=new ChooseVector(3);
    
    
    /** Creates a new instance of ConnectedMap */
    public ConnectedMap() {
    }
    
    public static ConnectedMap draw_dC(Random r){
        int c=poisson_dB.choose(r);
        if (c==0) return null;
        else{
            //if(c>1)System.out.println("peut etre probleme");
            TwoConnectedMap[] arrayBlocks=new TwoConnectedMap[c];
            for (int i=0;i<c;++i){
                TwoConnectedMap twoConnectedMap=TwoConnectedMap.draw_dB(r);
                arrayBlocks[i]=twoConnectedMap;
                int nr_vertices=twoConnectedMap.vertexList.size();
                int j=r.nextInt(nr_vertices);
                for (int k=0;k<nr_vertices;k++){
                    DemiArete e=twoConnectedMap.vertexList.getDemiArete(k);
                    if (k!=j) twoConnectedMap.replaceVertexByConnectedMap(e, draw_dC(r),r);
                }
                twoConnectedMap.rootDemiArete=twoConnectedMap.vertexList.getDemiArete(j);
                twoConnectedMap.vertexList.remove(j);
            }
            return mergeBlocksAroundRootVertex(arrayBlocks);
        }
    }
    
    public static ConnectedMap mergeBlocksAroundRootVertex(TwoConnectedMap[] arrayBlocks){
        TwoConnectedMap result=arrayBlocks[0];
        DemiArete resultRootPrec=result.rootDemiArete.prec;
        for (int i=1;i<arrayBlocks.length;++i){
            result.vertexList.addAll(arrayBlocks[i].vertexList);
            DemiArete e=arrayBlocks[i-1].rootDemiArete;
            DemiArete f=arrayBlocks[i].rootDemiArete.prec;
            e.prec=f;
            f.next=e;
            result.nr_edges=result.nr_edges+arrayBlocks[i].nr_edges;
        }
        DemiArete e=arrayBlocks[arrayBlocks.length-1].rootDemiArete;
        DemiArete f=resultRootPrec;
        e.prec=f;
        f.next=e;
        result.vertexList.add(result.rootDemiArete);
        return result;
    }
    
    public static ConnectedMap draw_ddC(Random r){
        TwoConnectedMap twoConnectedMap=TwoConnectedMap.draw_ddB(r);
        int j=r.nextInt(twoConnectedMap.vertexList.size());
        for (int i=0; i<twoConnectedMap.vertexList.size(); ++i){
            if(i!=j) twoConnectedMap.replaceVertexByConnectedMap(twoConnectedMap.vertexList.getDemiArete(i), draw_dC(r),r);
            else{
                int c=ch_dC_or_ddC.choose(r);
                ConnectedMap connectedMap=new ConnectedMap();
                if (c==0) connectedMap=draw_dC(r); else connectedMap=draw_ddC(r);
                twoConnectedMap.replaceVertexByConnectedMap(twoConnectedMap.vertexList.getDemiArete(i), connectedMap,r);
            }
        }
        return twoConnectedMap;
    }
    
    public static ConnectedMap draw_dddC(Random r){
        int c=choose_vector_dddC.choose(r);
        if (c==0){
            TwoConnectedMap twoConnectedMap=TwoConnectedMap.draw_ddB(r);
            int j=r.nextInt(twoConnectedMap.vertexList.size());
            for (int i=0; i<twoConnectedMap.vertexList.size(); ++i){
                if(i!=j) twoConnectedMap.replaceVertexByConnectedMap(twoConnectedMap.vertexList.getDemiArete(i), draw_dC(r),r);
                else{
                    int d=ch_2ddC_or_dddC.choose(r);
                    ConnectedMap connectedMap=new ConnectedMap();
                    if (d==0) connectedMap=draw_ddC(r); else connectedMap=draw_dddC(r);
                    twoConnectedMap.replaceVertexByConnectedMap(twoConnectedMap.vertexList.getDemiArete(i), connectedMap,r);
                }
            }
            return twoConnectedMap;
        }
        
        else if (c==1){
            TwoConnectedMap twoConnectedMap=TwoConnectedMap.draw_dddB(r);
            // choice of two random different vertices of twoConnectedMap
            int k=r.nextInt(twoConnectedMap.vertexList.size());
            int p=r.nextInt(twoConnectedMap.vertexList.size()-1);if(p>=k) ++p;
            for (int i=0; i<twoConnectedMap.vertexList.size(); ++i){
                if((i!=k)&&(i!=p)) twoConnectedMap.replaceVertexByConnectedMap(twoConnectedMap.vertexList.getDemiArete(i), draw_dC(r),r);
                else{
                    int d=ch_dC_or_ddC.choose(r);
                    ConnectedMap connectedMap=new ConnectedMap();
                    if (d==0) connectedMap=draw_dC(r); else connectedMap=draw_ddC(r);
                    twoConnectedMap.replaceVertexByConnectedMap(twoConnectedMap.vertexList.getDemiArete(i), connectedMap,r);
                }
            }
            return twoConnectedMap;
        }
        
        else if(c==2){
            TwoConnectedMap twoConnectedMap=TwoConnectedMap.draw_ddB(r);
            // choice of two random different vertices of twoConnectedMap
            int k=r.nextInt(twoConnectedMap.vertexList.size());
            int p=r.nextInt(twoConnectedMap.vertexList.size()-1);if(p>=k) ++p;
            for (int i=0; i<twoConnectedMap.vertexList.size(); ++i){
                if((i!=k)&&(i!=p)) twoConnectedMap.replaceVertexByConnectedMap(twoConnectedMap.vertexList.getDemiArete(i), draw_dC(r),r);
                else{
                    if(i==k){
                        int d=ch_dC_or_ddC.choose(r);
                        ConnectedMap connectedMap=new ConnectedMap();
                        if (d==0) connectedMap=draw_dC(r); else if (d==1) connectedMap=draw_ddC(r); else{System.out.println("pb ch_dC_or_ddC");}
                        twoConnectedMap.replaceVertexByConnectedMap(twoConnectedMap.vertexList.getDemiArete(i), connectedMap,r);
                    } else twoConnectedMap.replaceVertexByConnectedMap(twoConnectedMap.vertexList.getDemiArete(i), draw_ddC(r),r);
                }
            }
            return twoConnectedMap;
        } else{System.out.println("pb choose_vector dddC"); return null;}
    }
    
    
    
}
