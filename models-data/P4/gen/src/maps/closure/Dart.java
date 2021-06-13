package maps.closure;

import java.util.Stack;
import java.util.Random;
import maps.BinaryTree;
import maps.DemiArete;

// An instance of this class corresponds to an half-edge of a 
// planar map. The planar map can be a 3-connected map as well as an irreducible dissection
// or a quadrangulation. Notice that there is another class DemiArete for half-edges, having less parameters.
// To do the closure, we have to use more parameters, like for instance the parameter numberEdgesAfter that is
// useful to do the local closures. At the end of the closure, when getting the final 3-connected planar map, 
// we can copy the Map as a chain of Darts pointing to each other into a chain of DemiArete pointing to each other,
// which is better for the memory
public class Dart {
	public Dart opp=null; 			// denotes the other dart of the edge
	public Dart next=null; 		// denotes the succeeding dart in the counter-clockwise
	                		// order around the origin of the dart 
	public Dart prec=null; 		// denotes the succeeding dart in the clockwise
	                		// order around the origin of the dart
	int numberEdgesAfter=0;  // useful in the method "partial closure". For each edge whose attribute 
	                         // opp is "null" (this corresponds to a stem), this attribute denotes the number
	                         // of consecutive inner edges traversed after this stem in a counter-clockwise traversal
	                         // around the figure. Once this attribute becomes equal to 3, a local closure has to
	                         // be performed.
	Dart coupledDart=null;  // this attribute is only useful in the method "calculateMapFromQuadrangulation". For each
							// dart d of a quadrangulation, this attribute denotes the associated dart d' of the map associated
							// to the quadrangulation by Tutte's bijection: d' is the dart succeeding d in the clockwise order
							// around the origin of d.
        
        public DemiArete associatedDemiArete=null; // the class DemiArete also corresponds to an half-edge, but it has the strict minimum of attributes, i.e. next, prec and opp. 
        // The attribute associatedDemiArete is useful to make a copy of the map with DemiArete instead of Dart, which represents a gain of memory.
        
       
	
	public int index=0;
	public static int currentIndex=1;        // this attribute is only useful in the method makeindexes attributing a different index to 
                                                 // each dart of a planar map. Then using these indexes, the map can be printed in the form 
                                                 // of its two permutations, see the class mapPermutations
	
	public Dart () {}

	// This method takes a binary tree as argument and returns the 
	// structure of planar map corresponding to this binary tree.
	// However, this planar map has the particularity that stems
	// have no opposite dart (they will get one during the closure).
	public static Dart constrMap(BinaryTree binaryTree) {
            Dart gardeDart=new Dart();
            makeBids(binaryTree,gardeDart);
            gardeDart.opp.opp=null;
            return gardeDart.opp;
        }

	// This is the adjoint recursive method of constrMap
	public static void makeBids(BinaryTree binaryTree, Dart bid){
            Dart Dart1=new Dart();
            Dart Dart2=new Dart();
            Dart Dart3=new Dart();
            Dart1.opp=bid;
            bid.opp=Dart1;
            Dart1.next=Dart2; Dart2.prec=Dart1;
            Dart2.next=Dart3; Dart3.prec=Dart2;
            Dart3.next=Dart1; Dart1.prec=Dart3;
            if (!(binaryTree.leftSon==null)) makeBids(binaryTree.leftSon,Dart2);
            if (!(binaryTree.rightSon==null)) makeBids(binaryTree.rightSon,Dart3);
        }

	// performs the partial closure of the map-structure of a binary tree
	// and place the unmatched darts in the Stack stack
	public static void partialClosure(Dart dart, Stack stack){
            Dart breakTestDart=dart;
            stack.push(dart);
            while(true){
                dart=dart.next;
                if(dart.opp==null) {
                    if(stack.empty()) breakTestDart=dart;
                    else {
                        if(dart==breakTestDart) break;
                    }
                    stack.push(dart);
                } else {
                    dart=dart.opp;
                    if (!stack.empty()) {
                        Dart topDart=(Dart)stack.peek();
                        ++topDart.numberEdgesAfter;
                        if (topDart.numberEdgesAfter==3) {
                            Dart topDartOpp=new Dart();
                            topDart.opp=topDartOpp;
                            topDartOpp.opp=topDart;
                            topDartOpp.next=dart.next;
                            topDartOpp.prec=dart;
                            dart.next.prec=topDartOpp;
                            dart.next=topDartOpp;
                            stack.pop();
                            dart=topDart.prec;
                        }
                    }
                }
            }
        }

	// The version of partial closure taking the bicoloration of vertices
	// into account
	public static boolean bicoloredPartialClosure(Dart dart, Stack stack, boolean originOfRootDartBlack){
            boolean isOriginOfCurrentDartBlack=originOfRootDartBlack;
            Dart breakTestDart=dart;
            stack.push(dart);
            while(true){
                dart=dart.next;
                if(dart.opp==null) {
                    if(stack.empty()) breakTestDart=dart;
                    else {
                        if(dart==breakTestDart) break;
                    }
                    stack.push(dart);
                } else {
                    dart=dart.opp;
                    isOriginOfCurrentDartBlack=!isOriginOfCurrentDartBlack;
                    if (!stack.empty()) {
                        Dart topDart=(Dart)stack.peek();
                        ++topDart.numberEdgesAfter;
                        if (topDart.numberEdgesAfter==3) {
                            Dart topDartOpp=new Dart();
                            topDart.opp=topDartOpp;
                            topDartOpp.opp=topDart;
                            topDartOpp.next=dart.next;
                            topDartOpp.prec=dart;
                            dart.next.prec=topDartOpp;
                            dart.next=topDartOpp;
                            stack.pop();
                            dart=topDart.prec;
                            isOriginOfCurrentDartBlack=!isOriginOfCurrentDartBlack;
                        }
                    }
                }
            }
            return isOriginOfCurrentDartBlack;
        }


	// performs the complete closure of the map-structure of a binary tree
	public static Dart completeClosure(Dart hexagonDart) {
            Stack stack=new Stack();
            Dart[] hexagonDarts=new Dart[12];
            int hexagonDartsIndex=0;
            partialClosure(hexagonDart,stack);
            Dart firstDart=(Dart)stack.pop();
            Dart firstDartOpp=new Dart();
            firstDart.opp=firstDartOpp;
            firstDartOpp.opp=firstDart;
            Dart precDart=firstDartOpp;
            boolean firstDartNotTreated=true;
            
            while((!stack.empty())||firstDartNotTreated) {
                if(stack.empty()) {
                    stack.push(firstDart);
                    firstDartNotTreated=false;
                }
                Dart topDart=(Dart)stack.pop();
                Dart newDart=null;
                if (firstDartNotTreated) newDart=new Dart();
                else newDart=firstDartOpp;
                if (topDart.numberEdgesAfter==0){
                    Dart d1=new Dart();Dart d2=new Dart();Dart d3=new Dart();Dart d4=new Dart();
                    topDart.opp=newDart;
                    newDart.opp=topDart;
                    newDart.prec=d1;
                    d1.next=newDart;
                    d1.opp=d2;
                    d2.opp=d1;
                    d2.next=d3;
                    d2.prec=d3;
                    d3.prec=d2;
                    d3.next=d2;
                    d3.opp=d4;
                    d4.opp=d3;
                    d4.prec=precDart;
                    precDart.next=d4;
                    precDart=newDart;
                    hexagonDarts[hexagonDartsIndex++]=d4;
                    hexagonDarts[hexagonDartsIndex++]=d3;
                    hexagonDarts[hexagonDartsIndex++]=d2;
                    hexagonDarts[hexagonDartsIndex++]=d1;
                }
                if (topDart.numberEdgesAfter==1){
                    Dart d1=new Dart();
                    Dart d2=new Dart();
                    topDart.opp=newDart;
                    newDart.opp=topDart;
                    newDart.prec=d1;
                    d1.next=newDart;
                    d1.opp=d2;
                    d2.opp=d1;
                    d2.prec=precDart;
                    precDart.next=d2;
                    precDart=newDart;
                    hexagonDarts[hexagonDartsIndex++]=d2;
                    hexagonDarts[hexagonDartsIndex++]=d1;
                }
                if (topDart.numberEdgesAfter==2){
                    topDart.opp=newDart;
                    newDart.opp=topDart;
                    newDart.prec=precDart;
                    precDart.next=newDart;
                    precDart=newDart;
                }
            }
            for (int i=2;i<12;i=i+2) {
                hexagonDarts[i].next=hexagonDarts[i-1];
                hexagonDarts[i-1].prec=hexagonDarts[i];
            }
            hexagonDarts[0].next=hexagonDarts[11];
            hexagonDarts[11].prec=hexagonDarts[0];
            Random random=new Random(System.currentTimeMillis());
            return hexagonDarts[random.nextInt(6)*2+1];
        }
	
        // performs the complete closure of the map-structure of a rooted bicolored binary tree
	public static Dart bicoloredCompleteClosure(Dart hexagonBid, boolean originOfRootDartBlack ) {
            Stack stack=new Stack();
            Dart[] hexagonDarts=new Dart[12];
            int hexagonDartsIndex=0;
            boolean isOriginOfDartAtBottomOfStackBlack=bicoloredPartialClosure(hexagonBid,stack,originOfRootDartBlack);
            Dart firstDart=(Dart)stack.pop();
            boolean isOriginOfFirstHexagonDartBlack=true;
            if (firstDart.numberEdgesAfter==1) isOriginOfFirstHexagonDartBlack=isOriginOfDartAtBottomOfStackBlack;
            else isOriginOfFirstHexagonDartBlack=!isOriginOfDartAtBottomOfStackBlack;
            Dart firstDartOpp=new Dart();
            firstDart.opp=firstDartOpp;
            firstDartOpp.opp=firstDart;
            Dart precDart=firstDartOpp;
            boolean firstDartNotTreated=true;
            
            while((!stack.empty())||firstDartNotTreated) {
                if(stack.empty()) {
                    stack.push(firstDart);
                    firstDartNotTreated=false;
                }
                Dart topDart=(Dart)stack.pop();
                Dart newDart=null;
                if (firstDartNotTreated) newDart=new Dart();
                else newDart=firstDartOpp;
                if (topDart.numberEdgesAfter==0){
                    Dart d1=new Dart();Dart d2=new Dart();Dart d3=new Dart();Dart d4=new Dart();
                    topDart.opp=newDart;
                    newDart.opp=topDart;
                    newDart.prec=d1;
                    d1.next=newDart;
                    d1.opp=d2;
                    d2.opp=d1;
                    d2.next=d3;
                    d2.prec=d3;
                    d3.prec=d2;
                    d3.next=d2;
                    d3.opp=d4;
                    d4.opp=d3;
                    d4.prec=precDart;
                    precDart.next=d4;
                    precDart=newDart;
                    hexagonDarts[hexagonDartsIndex++]=d4;
                    hexagonDarts[hexagonDartsIndex++]=d3;
                    hexagonDarts[hexagonDartsIndex++]=d2;
                    hexagonDarts[hexagonDartsIndex++]=d1;
                }
                if (topDart.numberEdgesAfter==1){
                    Dart d1=new Dart();
                    Dart d2=new Dart();
                    topDart.opp=newDart;
                    newDart.opp=topDart;
                    newDart.prec=d1;
                    d1.next=newDart;
                    d1.opp=d2;
                    d2.opp=d1;
                    d2.prec=precDart;
                    precDart.next=d2;
                    precDart=newDart;
                    hexagonDarts[hexagonDartsIndex++]=d2;
                    hexagonDarts[hexagonDartsIndex++]=d1;
                }
                if (topDart.numberEdgesAfter==2){
                    topDart.opp=newDart;
                    newDart.opp=topDart;
                    newDart.prec=precDart;
                    precDart.next=newDart;
                    precDart=newDart;
                }
            }
            for (int i=2;i<12;i=i+2) {
                hexagonDarts[i].next=hexagonDarts[i-1];
                hexagonDarts[i-1].prec=hexagonDarts[i];
            }
            hexagonDarts[0].next=hexagonDarts[11];
            hexagonDarts[11].prec=hexagonDarts[0];
            Random random=new Random();
            if (isOriginOfFirstHexagonDartBlack) return hexagonDarts[random.nextInt(3)*4+3];
            else return hexagonDarts[random.nextInt(3)*4+1];
        }

	// returns true if there exists a path of length 3 passing by an interior
	// vertex between the root vertex and the opposite vertex in the hexagon 	
	public static boolean reject(Dart dart){
            Dart oppDartInHexagon=dart.opp.next.opp.next.opp.next;
            Dart dart1=dart.next;
            while(dart1!=dart.prec){
                if (belongToNodesAtDistanceAtMostTwo(dart1.opp,oppDartInHexagon)) return true;
                dart1=dart1.next;
            }
            dart1=oppDartInHexagon.next;
            while(dart1!=oppDartInHexagon.prec){
                if (belongToNodesAtDistanceAtMostTwo(dart1.opp,dart)) return true;
                dart1=dart1.next;
            }
            return false;
        }
	
        // adjoint procedure of the procedure reject
	public static boolean belongToSameNode(Dart dart1, Dart dart2){
            Dart dart=dart1.next;
            if (dart1==dart2) return true;
            while(dart!=dart1){
                if(dart==dart2) return true;
                dart=dart.next;
            }
            return false;
        }
        
        // adjoint procedure of the procedure reject
	public static boolean belongToAdjacentNodes(Dart dart1, Dart dart2){
            Dart dart=dart1.next;
            if(belongToSameNode(dart1.opp,dart2)) return true;
            while(dart!=dart1){
                if(belongToSameNode(dart.opp,dart2)) return true;
                dart=dart.next;
            }
            return false;
        }
        
        // adjoint procedure of the procedure reject
	public static boolean belongToNodesAtDistanceAtMostTwo(Dart dart1, Dart dart2){
            Dart dart=dart1.next;
            if(belongToAdjacentNodes(dart1.opp,dart2)) return true;
            while(dart!=dart1){
                if(belongToAdjacentNodes(dart.opp,dart2)) return true;
                dart=dart.next;
            }
            return false;
        }

	// returns the map-structure of the quadrangulation obtained
	// from the dissection by adding an edge in the outer face:
	// this edge goes, with the infinite face on its right, from the root vertex
	// to its opposite vertex in the hexagon
	public static Dart toQuadrangulation(Dart dart){
            Dart dart1=new Dart();
            Dart dart2=new Dart();
            dart1.opp=dart2;
            dart2.opp=dart1;
            Dart oppDartInHexagon=dart.opp.next.opp.next.opp.next;
            dart1.next=dart;
            dart1.prec=dart.prec;
            dart.prec=dart1;
            dart1.prec.next=dart1;
            dart2.next=oppDartInHexagon;
            dart2.prec=oppDartInHexagon.prec;
            oppDartInHexagon.prec=dart2;
            dart2.prec.next=dart2;
            return dart1;
        }
	
	
	// performs the angular bijection and returns the 3-connected map associated to
	// an irreducible quadrangulation by the bijection	
	public static Dart calculateMapFromQuadrangulation(Dart quadrangulationDart){
		makeCouplingDarts(quadrangulationDart);
		return quadrangulationDart.next.coupledDart;
	}

	// This is the adjoint recursive method of calculateMapFromQuadrangulation	
	public static void makeCouplingDarts(Dart startDart) {
            startDart.coupledDart=new Dart();
            Dart dart=startDart.next;
            while(dart!=startDart){
                dart.coupledDart=new Dart();
                dart.coupledDart.prec=dart.prec.coupledDart;
                dart.prec.coupledDart.next=dart.coupledDart;
                dart=dart.next;
            }
            startDart.coupledDart.prec=startDart.prec.coupledDart;
            startDart.prec.coupledDart.next=startDart.coupledDart;
            boolean startDartTreated=false; dart=startDart;
            while((dart!=startDart)||(!startDartTreated)){
                if(dart==startDart) startDartTreated=true;
                Dart oppositeDartInFace=dart.opp.next.opp.next;
                if (oppositeDartInFace.coupledDart==null) makeCouplingDarts(oppositeDartInFace);
                dart.coupledDart.opp=oppositeDartInFace.coupledDart;
                oppositeDartInFace.coupledDart.opp=dart.coupledDart;
                dart=dart.next;
            }
        }

	// returns the two permutations of half-edges (one for cycles of vertices, the other for opposite
	// half-edges
	public static mapPermutations calculateMapPermutations(Dart startDart){
            currentIndex=1;
            StringBuffer alphaCycles=new StringBuffer("");
            StringBuffer sigmaCycles=new StringBuffer("");
            makeIndexes(startDart,alphaCycles, sigmaCycles);
            return new mapPermutations(alphaCycles.toString(), sigmaCycles.toString());
        }

	// gives a different index to each half-edge of the planar map	
	public static void makeIndexes(Dart startDart, StringBuffer alphaCycles, StringBuffer sigmaCycles){
            boolean startDartTreated=false;
            Dart dart=startDart;
            while(dart!=startDart||(!startDartTreated)){
                if(dart==startDart) {
                    startDartTreated=true;
                    dart.index=currentIndex++;
                    sigmaCycles.append("("+String.valueOf(dart.index));
                } else{
                    dart.index=currentIndex++;
                    sigmaCycles.append(" "+String.valueOf(dart.index));
                }
                dart=dart.next;
            }
            sigmaCycles.append(")");
            startDartTreated=false;
            dart=startDart;
            while((dart!=startDart)||(!startDartTreated)){
                if(dart==startDart) startDartTreated=true;
                if (dart.opp!=null){
                    if(dart.opp.index==0) makeIndexes(dart.opp,alphaCycles,sigmaCycles);
                    else {
                        if (dart.index>dart.opp.index)
                            alphaCycles.append("("+String.valueOf(dart.index)+" "+String.valueOf(dart.opp.index)+")");
                    }
                }
                dart=dart.next;
            }
        }
        
        // does the closure of a rooted bicolored binary tree and returns the obtained map if it is 3-connected,
        // otherwise returns null
        public static Dart closure(BinaryTree tree){
            Dart dart=Dart.constrMap(tree);
            dart=Dart.bicoloredCompleteClosure(dart, BinaryTree.rootNodeOfLastGeneratedTreeBlack);
            if (Dart.reject(dart)) return null;
            dart=Dart.toQuadrangulation(dart);
            dart=Dart.calculateMapFromQuadrangulation(dart);
            return dart;
        }
	
}
