package dataWithEvaluations;

import randomChoose.ChooseVector;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import maps.*;

public class ReaderOfEvaluations {
    public static String FILE_NETWORKS="../../src/dataWithEvaluations/values_networks";
    public static String FILE_PLANAR_GRAPHS="../../src/dataWithEvaluations/values_planar";
 
    /** Creates a new instance of ReaderOfEvaluations */
    public ReaderOfEvaluations() {
    }
    
    public static void loadNetworkGeneratingFunctionsValues(String size){
        try{
         BufferedReader reader=new BufferedReader(new FileReader(FILE_NETWORKS));
         findSize(size, reader);
         readBigNumberBinaryTrees(reader);
         readBigNumber3connected(reader);
         readBigNumberNetworks(reader);
        }
        catch(FileNotFoundException e)
        {System.out.println(e);}
    }
    
    public static void loadAllGeneratingFunctionsValues(String size){
        try{
         BufferedReader reader=new BufferedReader(new FileReader(FILE_PLANAR_GRAPHS));
         findSize(size, reader);
         readBigNumberBinaryTrees(reader);
         readBigNumber3connected(reader);
         readBigNumberNetworks(reader);
         readBigNumber2Connected(reader);
         readBigNumberConnected(reader);
         readBigNumberPlanar(reader);
        }
        catch(FileNotFoundException e)
        {System.out.println(e);}
    }
    
    public static void readBigNumber3connected(BufferedReader reader){
        affectChooseVector(ThreeConnectedNetwork.ch_K_in_dyK,reader);
        affectChooseVector(ThreeConnectedNetwork.ch_dxK_in_dxyK,reader);
        affectChooseVector(ThreeConnectedNetwork.ch_b_or_dxb,reader);
        affectChooseVector(ThreeConnectedNetwork.ch_3b_or_dyb,reader);
    }
    
    public static void readBigNumber2Connected(BufferedReader reader){
        affectChooseVector(TwoConnectedMap.ch_xy_in_dB,reader);
        affectChooseVector(TwoConnectedMap.ch_y_in_ddB,reader);
        affectChooseVector(TwoConnectedMap.ch_nontrivialD_or_dD,reader);
        affectChooseVector(TwoConnectedMap.ch_dD_or_ddD,reader);
    }
    
    public static void readBigNumberBinaryTrees(BufferedReader reader){
        affectChooseVector(BinaryTree.ch_1_or_u,reader);
        affectChooseVector(BinaryTree.ch_1_or_v,reader);
        affectChooseVector(BinaryTree.ch_u_or_v,reader);
        affectChooseVector(BinaryTree.ch_dxu_or_dxv,reader);
        affectChooseVector(BinaryTree.choose_vector_dxu,reader);
        affectChooseVector(BinaryTree.choose_vector_dxv,reader);
        affectChooseVector(BinaryTree.ch_dyu_or_dyv,reader);
        affectChooseVector(BinaryTree.choose_vector_dyv,reader);
        affectChooseVector(BinaryTree.choose_vector_dyu,reader);
    }
    
    public static void readBigNumberConnected(BufferedReader reader){
        affectChooseVector(ConnectedMap.poisson_dB,reader);
        affectChooseVector(ConnectedMap.ch_dC_or_ddC,reader);
        affectChooseVector(ConnectedMap.ch_2ddC_or_dddC,reader);
        affectChooseVector(ConnectedMap.choose_vector_dddC,reader);
    }

    public static void readBigNumberNetworks(BufferedReader reader){
        
         affectChooseVector(Network.choose_vector_non_trivial_D,reader);
         affectChooseVector(Network.choose_vector_D,reader);
         affectChooseVector(Network.choose_vector_P,reader);
         affectChooseVector(Network.ch_y_or_P_or_H,reader);
         affectChooseVector(Network.ch_S_or_H,reader);
         affectChooseVector(Network.poisson_S_plus_H,reader);
         affectChooseVector(Network.poisson_at_least1_S_plus_H,reader);
         affectChooseVector(Network.poisson_at_least2_S_plus_H,reader);
         affectChooseVector(Network.choose_vector_dD,reader);
         affectChooseVector(Network.choose_vector_dS,reader);
         affectChooseVector(Network.choose_vector_dP,reader);
         affectChooseVector(Network.choose_vector_dH,reader);
         affectChooseVector(Network.ch_dP_or_dH,reader);
         affectChooseVector(Network.ch_dS_or_dH,reader);
         affectChooseVector(Network.choose_vector_ddD,reader);
         affectChooseVector(Network.choose_vector_ddS,reader);
         affectChooseVector(Network.choose_vector_ddP,reader);
         affectChooseVector(Network.choose_vector_ddH,reader);
         affectChooseVector(Network.ch_ddP_or_ddH,reader);
         affectChooseVector(Network.ch_ddS_or_ddH,reader);
    }
    
    public static void readBigNumberPlanar(BufferedReader reader){
        affectChooseVector(PlanarMap.poisson_C,reader);
        affectChooseVector(PlanarMap.choose_vector_ddG,reader);
        affectChooseVector(PlanarMap.choose_vector_dddG,reader);
    }
    
    public static void affectChooseVector(ChooseVector vector, BufferedReader reader){
        int i=0; String s="";
        try{
            s=reader.readLine();
            s=reader.readLine();// System.out.println(s);
            StringTokenizer st=new StringTokenizer(s);
            while(st.hasMoreTokens()){
                double x=0;
                try{x=Double.parseDouble(st.nextToken());} catch(NumberFormatException e){System.out.println("problem when loading choose_vector, not a number");System.out.println(x);}
                vector.setDoubleAt(i++, x);
            }
        } 
        catch(IOException e) {System.out.println(e);}// we pass the line indicating the beginning of the vector 
        
    }
    
    public static void findSize(String size, BufferedReader reader){
        String toCompare="case"+size; // System.out.println(toCompare);
        String s="";
        while(true){
            try{s=reader.readLine();} catch(IOException e) {System.out.println(e);}
            if((s!=null)&&(s.compareTo(toCompare)==0)) {
                return;
            }
        }
    }
    
    public static void printVectors(){
        BinaryTree.ch_1_or_u.toScreen();
        BinaryTree.ch_1_or_v.toScreen();
        BinaryTree.ch_u_or_v.toScreen();
        BinaryTree.ch_dxu_or_dxv.toScreen();
        BinaryTree.choose_vector_dxu.toScreen();
        BinaryTree.choose_vector_dxv.toScreen();
        BinaryTree.ch_dyu_or_dyv.toScreen();
        BinaryTree.choose_vector_dyv.toScreen();
        BinaryTree.choose_vector_dyu.toScreen();
        ThreeConnectedNetwork.ch_K_in_dyK.toScreen();
        ThreeConnectedNetwork.ch_dxK_in_dxyK.toScreen();
        ThreeConnectedNetwork.ch_b_or_dxb.toScreen();
        ThreeConnectedNetwork.ch_3b_or_dyb.toScreen();
        Network.choose_vector_non_trivial_D.toScreen();
        Network.choose_vector_D.toScreen();
        Network.choose_vector_P.toScreen();
        Network.ch_y_or_P_or_H.toScreen();
        Network.ch_S_or_H.toScreen();
        Network.poisson_S_plus_H.toScreen();
        Network.poisson_at_least1_S_plus_H.toScreen();
        Network.poisson_at_least2_S_plus_H.toScreen();
        Network.choose_vector_dD.toScreen();
        Network.choose_vector_dS.toScreen();
        Network.choose_vector_dP.toScreen();
        Network.choose_vector_dH.toScreen();
        Network.ch_dP_or_dH.toScreen();
        Network.ch_dS_or_dH.toScreen();
        Network.choose_vector_ddD.toScreen();
        Network.choose_vector_ddS.toScreen();
        Network.choose_vector_ddP.toScreen();
        Network.choose_vector_ddH.toScreen();
        Network.ch_ddP_or_ddH.toScreen();
        Network.ch_ddS_or_ddH.toScreen();
        TwoConnectedMap.ch_xy_in_dB.toScreen();
        TwoConnectedMap.ch_y_in_ddB.toScreen();
        TwoConnectedMap.ch_nontrivialD_or_dD.toScreen();
        TwoConnectedMap.ch_dD_or_ddD.toScreen();
        ConnectedMap.poisson_dB.toScreen();
        ConnectedMap.ch_dC_or_ddC.toScreen();
        ConnectedMap.ch_2ddC_or_dddC.toScreen();
        ConnectedMap.choose_vector_dddC.toScreen();
        PlanarMap.poisson_C.toScreen();
        PlanarMap.choose_vector_ddG.toScreen();
        PlanarMap.choose_vector_dddG.toScreen();
    }
    
    
    
}
