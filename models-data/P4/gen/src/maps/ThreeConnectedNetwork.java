package maps;

import java.util.Random;
import maps.closure.Dart;
import randomChoose.ChooseVector;

/**
 *
 * @author fusy
 */
public class ThreeConnectedNetwork extends Network{
    
    public static ChooseVector ch_K_in_dyK=new ChooseVector(2);
    public static ChooseVector ch_dxK_in_dxyK=new ChooseVector(2);
    public static ChooseVector ch_b_or_dxb=new ChooseVector(2);
    public static ChooseVector ch_3b_or_dyb=new ChooseVector(2);
    
    
    public DemiArete root=null;
    public DemiAreteList edgeList=new DemiAreteList();; //contains for each non-root edge one of its incident demiArete
    
    /**
     * Creates a new instance of ThreeConnectedNetwork 
     */
    public ThreeConnectedNetwork(Dart rootDart) {
        makeCopy(rootDart,rootDart);
        this.rootDemiArete=rootDart.associatedDemiArete;
        this.nr_edges=this.edgeList.size();
    }
    
    public void replaceEdgeByNetwork(DemiArete e, Network network){
        
        
        DemiArete e2=e.opp;
        DemiArete f=network.rootDemiArete.next;
        DemiArete f2=network.rootDemiArete.opp.next;
        DemiArete f3=network.rootDemiArete.prec;
        DemiArete f4=network.rootDemiArete.opp.prec;
        
        if (f==f3){
            e.opp=f.opp;
            f.opp.opp=e;
        } else{
            DemiArete eNextold=e.next;
            e.next=f.next;
            f.next.prec=e;
            e.opp=f.opp;
            f.opp.opp=e;
            f3.next=eNextold;
            eNextold.prec=f3;
        }
        
        
        
        
        
        
        if(f2==f4){
            e2.opp=f2.opp;
            f2.opp.opp=e2;
        } else{
            DemiArete e2Nextold=e2.next;
            e2.next=f2.next;
            f2.next.prec=e2;
            e2.opp=f2.opp;
            f2.opp.opp=e2;
            f4.next=e2Nextold;
            e2Nextold.prec=f4;
        }

        this.vertexList.addAll(network.vertexList);
        this.nr_edges+=(network.nr_edges-1);
    }
    
    public void makeCopy(Dart firstDart, Dart rootDart){
        if (firstDart.associatedDemiArete==null){
            Dart dart=firstDart;
            do
            {
		DemiArete demiArete=new DemiArete();
		dart.associatedDemiArete=demiArete;
		//demiArete.index=dart.index;
		dart=dart.next;
            }
            while(dart!=firstDart);
            if((dart!=rootDart)&&(dart.opp!=rootDart)) this.vertexList.add(dart.associatedDemiArete);
			
            do
            {
		dart.associatedDemiArete.prec=dart.prec.associatedDemiArete;
		dart.associatedDemiArete.next=dart.next.associatedDemiArete;
		if(dart.opp.associatedDemiArete!=null){
                    dart.associatedDemiArete.opp=dart.opp.associatedDemiArete;
                    dart.opp.associatedDemiArete.opp=dart.associatedDemiArete;
		}
                else {
                    if(dart!=rootDart) this.edgeList.add(dart.associatedDemiArete);
                }
		dart=dart.next;
            }
            while(dart!=firstDart);
			
            do
            {
		makeCopy(dart.opp, rootDart);
		dart=dart.next;
            }
            while(dart!=firstDart);
	}
    }
    
    public void toScreen(){
        System.out.print("indices of demi-aretes in vertexList: "); this.vertexList.toScreen();
        System.out.println("");
        System.out.print("indices of demi-aretes in edgeList: "); this.edgeList.toScreen();
        System.out.println("");
        this.vertexList.addAll(this.edgeList);
        System.out.print("indices of merge: "); this.vertexList.toScreen();
        System.out.println("");
    }
    
    public void toScreenParameters3connected(){
        System.out.print("number of demi-aretes in vertexList: "+this.vertexList.size());
        System.out.println("");
        System.out.print("number of demi-aretes in edgeList: "+this.edgeList.size());
        System.out.println("");
        int i=this.vertexList.size(); int j=this.edgeList.size();
        float ratio=(float)j/(float)i;
        System.out.print("ratio: "+ratio);
        System.out.println("");
    }
    
    public static ThreeConnectedNetwork draw_K(Random r){
        PlanarMap.NR_BRICKS++;
        while(true){
            double u=r.nextDouble();
            int maxSize=(int)(4/u); 
            BinaryTree binaryTree=BinaryTree.draw_b(maxSize,r);
            if(binaryTree!=null){
                Dart dart=Dart.closure(binaryTree);
                if(dart!=null) return new ThreeConnectedNetwork(dart);
            }
            //else System.out.println("reject tree");
        }
    }
    
    public static ThreeConnectedNetwork draw_dxK(Random r){
        PlanarMap.NR_BRICKS++;
        while(true){
            double u=r.nextDouble(); 
            BinaryTree binaryTree=BinaryTree.draw_b(r);
            int i=BinaryTree.countblacknodes;int j=BinaryTree.countwhitenodes;
            double reject=(double)(3.0*(i+1)/(2.0*(i+j+2))); //System.out.println("taux de rejet= "+reject);
            if(u<=reject){
                Dart dart=Dart.closure(binaryTree);
                if(dart!=null) return new ThreeConnectedNetwork(dart);
            }
        }
    }
    
    public static ThreeConnectedNetwork draw_dyK(Random r){
        PlanarMap.NR_BRICKS++;
        int c=ch_K_in_dyK.choose(r);
        if (c==0) return draw_K(r);
        else {
            while(true){
                BinaryTree binaryTree=BinaryTree.draw_b(r);
                Dart dart=Dart.closure(binaryTree);
                if(dart!=null) return new ThreeConnectedNetwork(dart);
            }
        }
    }
    
    public static ThreeConnectedNetwork draw_dxyK(Random r){
        PlanarMap.NR_BRICKS++;
        int c=ch_dxK_in_dxyK.choose(r);
        if (c==0) return draw_dxK(r);
        else {
            while(true){
                BinaryTree binaryTree=new BinaryTree();
                int d=ch_b_or_dxb.choose(r);
                if(d==0) binaryTree=BinaryTree.draw_b(r);
                else binaryTree=BinaryTree.draw_dxb(r);
                Dart dart=Dart.closure(binaryTree);
                if(dart!=null) return new ThreeConnectedNetwork(dart);
            }
        }
    }
    
    public static ThreeConnectedNetwork draw_dxxK(Random r){
        PlanarMap.NR_BRICKS++;
        while(true){
            double u=r.nextDouble(); 
            BinaryTree binaryTree=BinaryTree.draw_dxb(r);
            int i=BinaryTree.countblacknodes;int j=BinaryTree.countwhitenodes;
            double reject=(double)(3.0*(i+1)/(2.0*(i+j+2))); //System.out.println("taux de rejet= "+reject);
            if(u<=reject){
                Dart dart=Dart.closure(binaryTree);
                if(dart!=null) return new ThreeConnectedNetwork(dart);
            }
        }
    }
    
    public static ThreeConnectedNetwork draw_dyyK(Random r){
        PlanarMap.NR_BRICKS++;
        BinaryTree binaryTree=new BinaryTree();
        while(true){
            int c=ch_3b_or_dyb.choose(r);
            if(c==0) binaryTree=BinaryTree.draw_b(r);
            else binaryTree=BinaryTree.draw_dyb(r);
            Dart dart=Dart.closure(binaryTree);
            if(dart!=null) return new ThreeConnectedNetwork(dart);
        }
    }
    
    
    
    
}
