package type;

import java.util.ArrayList;
import java.util.HashSet;

public class VarRecord {

	public static enum VarClass {
		INPUT, FUNCTION, INDEPENDENT, DEPENDENT, LOCAL
	}

	private class Depend {
		ArrayList<VarRecord> vars;
		ExpTreeNode exp;

		public Depend(HashSet<VarRecord> _vars, ExpTreeNode _exp) {
			vars = new ArrayList<VarRecord>(_vars);
			exp = _exp;
		}
	};

	private String name;
	private VarClass varClass;
	private VarType varType;
	private ArrayList<Depend> depends = new ArrayList<Depend>();
	private ExpTreeNode dependExp;
	private ExpTreeNode defExp;

	public void printDepend() {
		if (varClass == VarClass.INDEPENDENT) {
			System.out.print(name + " depends on: ");
			boolean first = true;
			for (Depend d : depends) {
				if (first)
					first = false;
				else
					System.out.print("| ");
				for (VarRecord v : d.vars)
					System.out.print(v.name + " ");
			}
			System.out.println();
		}
	}

	public VarRecord(String name, VarClass varClass) {
		this.name = name;
		this.varClass = varClass;
	}

	public void setDefExp(ExpTreeNode _defExp) {
		defExp = _defExp;
	}

	public ExpTreeNode getDefExp() {
		return defExp;
	}

	public ExpTreeNode getDependExp() {
		return dependExp;
	}

	public void addDepend(HashSet<VarRecord> s, ExpTreeNode exp) {
		if (s.size() == 0 || s.contains(this))
			return;
		depends.add(new Depend(s, exp));
	}

	public boolean checkDepend(ArrayList<VarRecord> independ) {
		for (Depend d : depends) {
			boolean flag = true;
			for (VarRecord v : d.vars)
				if (!independ.contains(v)) {
					flag = false;
					break;
				}
			if (flag)
				return true;
		}
		return false;
	}

	public boolean ready(HashSet<VarRecord> used, HashSet<VarRecord> indeps, VarRecord var) {
		HashSet<VarRecord> vs = dependExp.getVarSet();
		boolean flag = false;
		for (VarRecord v : vs) {
			VarClass vc = v.getVarClass();
			if (v != this && vc != VarClass.INPUT && vc != VarClass.LOCAL && !used.contains(v) && !indeps.contains(v))
				return false;
			if (v == var || v.varClass == VarClass.DEPENDENT)
				flag = true;
		}
		return flag;
	}

	public boolean removeDepends(ArrayList<VarRecord> inds) {
		if (varClass == VarClass.DEPENDENT) {
			for (Depend d : depends) {
				boolean flag = true;
				for (VarRecord v : d.vars)
					if (!inds.contains(v)) {
						flag = false;
						break;
					}
				if (flag) {
					dependExp = d.exp;
					dependExp.setIgnore();
					break;
				}
			}
			if (dependExp == null)
				return false;
		}
		depends.clear();
		return true;
	}

	public String toString() {
		String str = name + ": " + varType;
		return str;
	}

	public String getName() {
		return name;
	}

	public void setVarClass(VarClass varClass) {
		this.varClass = varClass;
	}

	public void setVarType(VarType varType) {
		this.varType = varType;
	}

	public VarType getVarType() {
		return varType;
	}

	public VarClass getVarClass() {
		return varClass;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public String getDef() {
		return varType.getDef(name);
	}

	public String getBestDef() {
		return varType.getDef("_best_" + name);
	}

}
