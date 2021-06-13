package maps;

import randomChoose.ChooseVector;
import java.util.Random;
import java.util.Stack;
import maps.*;
import maps.closure.DyckWord;

public class BinaryTree {
    
    public static ChooseVector ch_1_or_u=new ChooseVector(2); 
    public static ChooseVector ch_1_or_v=new ChooseVector(2); 
    public static ChooseVector ch_u_or_v=new ChooseVector(2);  
    public static ChooseVector ch_dxu_or_dxv=new ChooseVector(2);  
    public static ChooseVector choose_vector_dxu=new ChooseVector(3); 
    public static ChooseVector choose_vector_dxv=new ChooseVector(2); 
    public static ChooseVector ch_dyu_or_dyv=new ChooseVector(2);  
    public static ChooseVector choose_vector_dyv=new ChooseVector(3); 
    public static ChooseVector choose_vector_dyu=new ChooseVector(3); 
    
    public BinaryTree leftSon=null;
    public BinaryTree rightSon=null;
    boolean leftSonExplored=false; // useful for the construction from a Dyck word
    public static boolean rootNodeOfLastGeneratedTreeBlack=true;
    
    public static int countnodes=0; public static int countblacknodes=0; public static int countwhitenodes=0;
    
    /**
     * Creates a new instance of BinaryTree 
     */
    
   
    public BinaryTree(){
        
    }
    
    public BinaryTree(BinaryTree leftSon, BinaryTree rightSon){
        this.leftSon=leftSon;
	this.rightSon=rightSon;
    }
    
    
    
    public static String toString (BinaryTree binaryTree){
	if(binaryTree==null) return "()";
	else return "("+toString(binaryTree.leftSon)+toString(binaryTree.rightSon)+")";
    }
    
    public static void init_static_parameters(){
        countnodes=0;
        countblacknodes=0;
        countwhitenodes=0;
    }

    public static void toScreen(BinaryTree binaryTree){
	System.out.println("parenthesis word corresponding to the binary tree");
	System.out.println(toString(binaryTree));
    }
    
    public static void printParameters(BinaryTree binaryTree){
        System.out.println("nr_nodes: "+binaryTree.countnodes);
        System.out.println("nr_black_nodes: "+binaryTree.countblacknodes);
        System.out.println("nr_white_nodes: "+binaryTree.countwhitenodes);
        float ratio=(float)binaryTree.countblacknodes/(float)binaryTree.countnodes;
        System.out.println("ratio_blackNodes_nodes: "+ratio);
    }
    
    public static BinaryTree draw_dxb(Random r){
        init_static_parameters();
        r.nextDouble();
        int c=ch_dxu_or_dxv.choose(r);
        if (c==0){
            //System.out.println("black"); 
            BinaryTree tree=draw_dxu(r); BinaryTree.rootNodeOfLastGeneratedTreeBlack=true; return tree; }
        else{
            //System.out.println("white"); 
            BinaryTree tree=draw_dxv(r); BinaryTree.rootNodeOfLastGeneratedTreeBlack=false; return tree;}
    }
    
    public static BinaryTree draw_dxu(Random r){
        r.nextDouble();
        int c=choose_vector_dxu.choose(r);

        if (c==0)
            return draw_u(r);
        else
        {
            //we add the black root
            countnodes++;countblacknodes++;
            BinaryTree leftSon=new BinaryTree();
            BinaryTree rightSon=new BinaryTree();
      
            if (c==1)
            {
                leftSon=draw_1_or_v(r);
                rightSon=draw_dxv(r);
            }
            else
            {
                leftSon=draw_dxv(r);
                rightSon=draw_1_or_v(r);
            }
            return new BinaryTree(leftSon,rightSon);
        }
    }
    
    public static BinaryTree draw_dxv(Random r){
        r.nextDouble();
        int c=choose_vector_dxv.choose(r);

        
        //we add the black root
        countnodes++;countwhitenodes++;
        BinaryTree leftSon=new BinaryTree();
        BinaryTree rightSon=new BinaryTree();
      
        if (c==0)
        {
             leftSon=draw_1_or_u(r);
             rightSon=draw_dxu(r);
        }
        else
        {
             leftSon=draw_dxu(r);
             rightSon=draw_1_or_u(r);
        }
        return new BinaryTree(leftSon,rightSon);
    }
    
    public static BinaryTree draw_dyb(Random r){
        init_static_parameters();
        r.nextDouble();
        int c=ch_dyu_or_dyv.choose(r);
        if (c==0){
            // System.out.println("black"); 
            BinaryTree tree=draw_dyu(r); BinaryTree.rootNodeOfLastGeneratedTreeBlack=true; return tree; }
        else{
            // System.out.println("white"); 
            BinaryTree tree=draw_dyv(r); BinaryTree.rootNodeOfLastGeneratedTreeBlack=false; return tree;}
    }
    
    public static BinaryTree draw_dyv(Random r){
        r.nextDouble();
        int c=choose_vector_dyv.choose(r);

        if (c==0)
            return draw_v(r);
        else
        {
            //we add the black root
            countnodes++;countwhitenodes++;
            BinaryTree leftSon=new BinaryTree();
            BinaryTree rightSon=new BinaryTree();
      
            if (c==1)
            {
                leftSon=draw_1_or_u(r);
                rightSon=draw_dyu(r);
            }
            else
            {
                leftSon=draw_dyu(r);
                rightSon=draw_1_or_u(r);
            }
            return new BinaryTree(leftSon,rightSon);
        }
    }
    
    public static BinaryTree draw_dyu(Random r){
        r.nextDouble();
        int c=choose_vector_dyu.choose(r);

        if (c==0)
            return draw_u(r);
        else
        {
            //we add the black root
            countnodes++;countblacknodes++;
            BinaryTree leftSon=new BinaryTree();
            BinaryTree rightSon=new BinaryTree();
      
            if (c==1)
            {
                leftSon=draw_1_or_v(r);
                rightSon=draw_dyv(r);
            }
            else
            {
                leftSon=draw_dyv(r);
                rightSon=draw_1_or_v(r);
            }
            return new BinaryTree(leftSon,rightSon);
        }
        
    }
    
    public static BinaryTree draw_b(Random r){
        init_static_parameters();
        r.nextDouble();
        int c=ch_u_or_v.choose(r);
        if (c==0){
            // System.out.println("black"); 
            BinaryTree tree=draw_u(r); BinaryTree.rootNodeOfLastGeneratedTreeBlack=true; return tree; }
        else{
            // System.out.println("white"); 
            BinaryTree tree=draw_v(r); BinaryTree.rootNodeOfLastGeneratedTreeBlack=false; return tree;}
    }
    
    public static BinaryTree draw_u(Random r){
        r.nextDouble();
        
        //we add the black root
        countnodes++;countblacknodes++;
        BinaryTree leftSon=new BinaryTree();
        BinaryTree rightSon=new BinaryTree();
      
        leftSon=draw_1_or_v(r);
        rightSon=draw_1_or_v(r);
        return new BinaryTree(leftSon,rightSon);
    }
    
    public static BinaryTree draw_v(Random r){
        r.nextDouble();
        
        //we add the black root
        countnodes++;countwhitenodes++;
        BinaryTree leftSon=new BinaryTree();
        BinaryTree rightSon=new BinaryTree();
      
        leftSon=draw_1_or_u(r);
        rightSon=draw_1_or_u(r);
        return new BinaryTree(leftSon,rightSon);
    }
    
    public static BinaryTree draw_1_or_u(Random r){
        r.nextDouble();
        if (ch_1_or_u.choose(r)==0) return null;
        else return draw_u(r);
    }
    
    public static BinaryTree draw_1_or_v(Random r){
        r.nextDouble();
        if (ch_1_or_v.choose(r)==0) return null;
        else return draw_v(r);
    }
    
    // the following random generators draw binary tree having a maximal size. They 
    // stop as soon as the size gets greater than the maximal size. They are useful
    // for draw_K (3-connected networks), that demands a rejection step. 
    public static BinaryTree draw_b(int maxSize, Random r){
        init_static_parameters();
        r.nextDouble();
        int c=ch_u_or_v.choose(r);
        BinaryTree tree=new BinaryTree();
        if (c==0){
            // System.out.println("black"); 
            tree=draw_u(r); BinaryTree.rootNodeOfLastGeneratedTreeBlack=true;  }
        else{
            // System.out.println("white"); 
            tree=draw_v(r); BinaryTree.rootNodeOfLastGeneratedTreeBlack=false; }
        if(countnodes>maxSize) return null;
        else return tree;
    }
    
    public static BinaryTree draw_u(int maxSize, Random r){
        if(countnodes>maxSize) return null;
        r.nextDouble();
        
        //we add the black root
        countnodes++;countblacknodes++;
        BinaryTree leftSon=new BinaryTree();
        BinaryTree rightSon=new BinaryTree();
      
        leftSon=draw_1_or_v(r);
        rightSon=draw_1_or_v(r);
        return new BinaryTree(leftSon,rightSon);
    }
    
    public static BinaryTree draw_v(int maxSize, Random r){
        if(countnodes>maxSize) return null;
        r.nextDouble();
        
        //we add the black root
        countnodes++;countwhitenodes++;
        BinaryTree leftSon=new BinaryTree();
        BinaryTree rightSon=new BinaryTree();
      
        leftSon=draw_1_or_u(r);
        rightSon=draw_1_or_u(r);
        return new BinaryTree(leftSon,rightSon);
    }
    
    public static BinaryTree draw_1_or_u(int maxSize, Random r){
        if(countnodes>maxSize) return null;
        r.nextDouble();
        if (ch_1_or_u.choose(r)==0) return null;
        else return draw_u(r);
    }
    
    public static BinaryTree draw_1_or_v(int maxSize, Random r){
        if(countnodes>maxSize) return null;
        r.nextDouble();
        if (ch_1_or_v.choose(r)==0) return null;
        else return draw_v(r);
    }
    
    
}



    

