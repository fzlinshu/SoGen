package main;

import java.util.ArrayList;

import type.ExpTreeNode;
import type.ExpTreeNode.Operator;
import type.vartype.VarTypeBool;

public class BoundTightener {

	private ArrayList<ExpTreeNode> expTrees;

	public BoundTightener(ArrayList<ExpTreeNode> expTrees) {
		this.expTrees = expTrees;
	}

	public boolean tighten() throws Exception {
		ExpTreeNode.clear();
		for (ExpTreeNode expTree : expTrees)
			if (expTree.getOp() == Operator.OF)
				expTree.restrict(null);
			else
				expTree.restrict(new VarTypeBool("true"));
		expTrees.removeIf(expTree -> expTree.useless());
		return ExpTreeNode.isModified();
	}

}
