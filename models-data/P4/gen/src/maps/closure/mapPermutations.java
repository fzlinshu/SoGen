package maps.closure;

// An object of this class gives the combinatorial encoding of a map
// by its two permutations (one whose cycles correspond to counter-clockwise
// sequence of darts around each vertex; the other one grouping opposite darts by cycles
// of length 2).

public class mapPermutations {
	String alphaCycles;
	String sigmaCycles;
	
	public mapPermutations (String alphaCycles, String sigmaCycles){
		this.alphaCycles=alphaCycles;
		this.sigmaCycles=sigmaCycles;
	}
	
	public void toScreen () {
		System.out.println("combinatorial encoding of the map with two permutations:");
		System.out.println("the first one for opposite darts, the second one for cycles of darts around vertices.");
		System.out.println(alphaCycles);
		System.out.println(sigmaCycles);
	}
}
