package main;

import java.util.ArrayList;
import java.util.HashSet;

import type.ExpTreeNode;
import type.VarRecord;

public class DepExtracter {

	public void extract(ArrayList<ExpTreeNode> expTrees) {
		for (ExpTreeNode expTree : expTrees)
			expTree.extractDep(new HashSet<VarRecord>(), expTree);
	}

}
