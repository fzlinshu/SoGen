package type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import type.VarRecord.VarClass;

public class VarTable {

	public static enum Objective {
		MAX, MIN, SAT
	};

	private HashMap<String, VarRecord> varList = new HashMap<String, VarRecord>();
	private String errorMsg;
	private ArrayList<VarRecord> inputs = new ArrayList<VarRecord>();
	private ArrayList<VarRecord> outputs = new ArrayList<VarRecord>();
	private Objective objective = Objective.SAT;

	public VarRecord newVar(String varName, VarRecord.VarClass varClass) {
		if (varList.get(varName) == null) {
			varList.put(varName, new VarRecord(varName, varClass));
			if (varClass == VarClass.INPUT)
				inputs.add(varList.get(varName));
		}
		return varList.get(varName);
	}

	public void addOutput(String varName) {
		outputs.add(varList.get(varName));
	}

	public ArrayList<VarRecord> getInputs() {
		return inputs;
	}

	public ArrayList<VarRecord> getOutputs() {
		return outputs;
	}

	public void combine(VarTable ret) {
		varList.putAll(ret.varList);
	}

	public VarRecord get(String name) {
		return varList.get(name);
	}

	public ArrayList<VarRecord> getVars(VarRecord.VarClass c) {
		ArrayList<VarRecord> vars = new ArrayList<VarRecord>();
		for (VarRecord v : varList.values())
			if (c == null && v.getVarClass() != VarClass.LOCAL || v.getVarClass() == c)
				vars.add(v);
		return vars;
	}

	public void printDepend() {
		for (VarRecord v : varList.values())
			v.printDepend();
	}

	public String toString() {
		if (errorMsg != null)
			return errorMsg;
		String input = "";
		String function = "";
		String independent = "";
		String dependent = "";
		String output = "";
		String local = "";
		for (VarRecord rec : varList.values())
			switch (rec.getVarClass()) {
			case INPUT:
				input += "\t" + rec + "\n";
				break;
			case FUNCTION:
				function += "\t" + rec + "\n";
				break;
			case INDEPENDENT:
				independent += "\t" + rec + "\n";
				break;
			case DEPENDENT:
				dependent += "\t" + rec + "\n";
				break;
			case LOCAL:
				local += "\t" + rec + "\n";
				break;
			}
		String msg = "";
		if (input != "")
			msg += "#INPUT\n" + input;
		if (function != "")
			msg += "#FUNCTION\n" + function;
		if (independent != "")
			msg += "#INDEPENDENT\n" + independent;
		if (dependent != "")
			msg += "#DEPENDENT\n" + dependent;
		if (output != "")
			msg += "#OUTPUT\n" + output;
		if (local != "")
			msg += "#LOCAL\n" + local;
		return msg;
	}

	private BigInteger best = null;
	private ArrayList<VarRecord> vars = new ArrayList<VarRecord>();
	private HashSet<VarRecord> independ = new HashSet<VarRecord>();
	private HashSet<VarRecord> bestIndepend;
	private ArrayList<VarRecord> calcOrder = new ArrayList<VarRecord>();

	private void find(int x, BigInteger range) {
		if (best != null && best.compareTo(range) <= 0)
			return;
		if (x == vars.size()) {
			HashSet<VarRecord> vs = new HashSet<VarRecord>(vars);
			ArrayList<VarRecord> inds = new ArrayList<VarRecord>(independ);
			boolean flag = true;
			while (flag) {
				vs.removeAll(inds);
				flag = false;
				for (VarRecord var : vs) {
					if (var.checkDepend(inds)) {
						flag = true;
						inds.add(var);
					}
				}
			}
			if (!vs.isEmpty())
				return;
			best = new BigInteger(range.toString());
			bestIndepend = new HashSet<VarRecord>(independ);
			return;
		}
		find(x + 1, range);
		VarRecord v = vars.get(x);
		if (v.getVarType() != null && v.getVarType().range() != null) {
			independ.add(v);
			find(x + 1, range.multiply(v.getVarType().range()));
			independ.remove(v);
		}
	}

	public boolean chooseIndependent() {
		for (VarRecord v : varList.values())
			if (v.getVarClass() == VarClass.INDEPENDENT)
				vars.add(v);
		find(0, BigInteger.ONE);
		if (best != null) {
			for (VarRecord v : vars)
				if (bestIndepend.contains(v))
					v.removeDepends(null);
				else
					v.setVarClass(VarClass.DEPENDENT);
			HashSet<VarRecord> vs = new HashSet<VarRecord>(vars);
			ArrayList<VarRecord> inds = new ArrayList<VarRecord>(bestIndepend);
			while (!vs.isEmpty()) {
				vs.removeAll(inds);
				for (VarRecord v : vs)
					if (v.removeDepends(inds)) {
						calcOrder.add(v);
						inds.add(v);
					}
			}
		}
		return best != null;
	}

	public String getInfiniteVars() {
		String s = null;
		for (String v : varList.keySet())
			if (!v.startsWith("_")) {
				VarType t = varList.get(v).getVarType();
				if (t == null || t.range() == null)
					if (s == null)
						s = v;
					else
						s += ", " + v;
			}
		return s;
	}

	public void setObjective(Objective _objective) {
		objective = _objective;
	}

	public Objective getObjective() {
		return objective;
	}

}
