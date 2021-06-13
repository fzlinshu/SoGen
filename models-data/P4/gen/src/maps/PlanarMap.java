package maps;

import java.io.FileWriter;
import java.io.IOException;
import randomChoose.ChooseVector;

/**
 *
 * @author fusy
 */
public class PlanarMap {
    public static int NR_BLOCKS=0;
    public static int NR_BRICKS=0;
    
    public static ChooseVector poisson_C=new ChooseVector(17);
    public static ChooseVector choose_vector_ddG=new ChooseVector(2);
    public static ChooseVector choose_vector_dddG=new ChooseVector(3);
    
    public DemiAreteList vertexList= new DemiAreteList(); //contains for each vertex, non incident to the root-edge, one of its incident demiArete
    public int nr_edges=0;
    public DemiArete rootDemiArete=null;
    
    /** Creates a new instance of PlanarMap */
    public PlanarMap() {
    }
    
    public void toScreenParameters(){
        System.out.print("number of vertices: "+this.vertexList.size());
        System.out.println("");
        System.out.print("number of edges: "+this.nr_edges);
        System.out.println("");
        int i=this.vertexList.size(); int j=this.nr_edges;
        float ratio=(float)j/(float)i;
        System.out.print("ratio edges-vertices: "+ratio);
        System.out.println("");
    }
    
    public static void initStaticParameters(){
        NR_BLOCKS=0;
        NR_BRICKS=0;
    }
    
    public static void printStatisticParameters(){
        System.out.println("nr blocks: "+NR_BLOCKS);
        System.out.println("nr bricks: "+NR_BRICKS);
    }
    
    public int[] degreeVertices(){
        int[] degrees=new int[this.vertexList.size()+1];
        int indexRightMostNonZero=-1;
        for(int j=0;j<degrees.length;++j) degrees[j]=0;
        for (int i=0;i<this.vertexList.size();++i){
            DemiArete aStart=this.vertexList.getDemiArete(i);
            DemiArete a=this.vertexList.getDemiArete(i);
            int degree=0;
            do{
                degree++;
                a=a.next;
            }
            while(a!=aStart);
            degrees[degree]++;
            if(degree>indexRightMostNonZero) indexRightMostNonZero=degree;
        }
        degrees[0]=indexRightMostNonZero;//degrees[0] contains the maximal degree of the planar graph
        return degrees;
    }
    
    public void printDegreeVertices(){
        int[] degrees=this.degreeVertices();
        System.out.print("degrees: ");
        for (int i=1;i<=degrees[0];++i){
            System.out.print(degrees[i]+",");
        }
        System.out.println("");
        int nr_vertices=this.vertexList.size();
        for (int i=1;i<=degrees[0];++i){
            System.out.println((float)degrees[i]/(float)nr_vertices+",");
        }
        System.out.println("");
    }
    
    public void printHalfEdges(){
        int problem=0;
        int current_index=0; 
        for (int i=0;i<vertexList.size();++i){
            //System.out.println(i);
            DemiArete demiArete=vertexList.getDemiArete(i);
            int chrono=1000;
            do {
                if(demiArete.opp.index==-1) demiArete.index=++current_index;
                else demiArete.index=-demiArete.opp.index;
                demiArete=demiArete.next;
                chrono--;if(chrono==0)problem++;
            }
            while(demiArete!=vertexList.getDemiArete(i)&&chrono>0);
        }
        if(problem>0) System.out.println("There were "+problem+" problems");
        //if (0==0) return;
        StringBuffer stringCycles=new StringBuffer("");
        for (int i=0;i<vertexList.size();++i){
            DemiArete demiArete=vertexList.getDemiArete(i);
            stringCycles.append('(');
            int chrono=1000;
            do {
                stringCycles.append(demiArete.index+" ");
                demiArete=demiArete.next;
                chrono--;if(chrono==0)problem++;
            }
            while(demiArete!=vertexList.getDemiArete(i));
            stringCycles.append(')');
        }
        System.out.println(stringCycles);
        if(problem>0) System.out.println("There were "+problem+" problems");
        
        
    }
    
    public void printForPigale () throws IOException{
        FileWriter graphForPigale=new FileWriter("../../ListEdges.txt");
        graphForPigale.write("PIG:0 Graph\n");
        int problem=0;
        int current_index=0; 
        for (int i=0;i<vertexList.size();++i){
            //System.out.println(i);
            DemiArete demiArete=vertexList.getDemiArete(i);
            int chrono=1000;
            do {
                demiArete.indexOfVertex=i+1;
                if(demiArete.opp.index==-1) demiArete.index=++current_index;
                else {
                    demiArete.index=-demiArete.opp.index;
                    graphForPigale.write(demiArete.indexOfVertex+" "+demiArete.opp.indexOfVertex+"\n");
                }
                demiArete=demiArete.next;
                chrono--;if(chrono==0)problem++;
            }
            while(demiArete!=vertexList.getDemiArete(i)&&chrono>0);
        }
        if(problem>0) System.out.println("There were "+problem+" problems");
        graphForPigale.write("0 0\n");
        graphForPigale.close();
    }
    
    
    
}
