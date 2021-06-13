package generator;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import type.ExpTreeNode;
import type.VarRecord;
import type.VarRecord.VarClass;
import type.VarTable;
import type.VarTable.Objective;
import type.VarType;
import type.ExpTreeNode.Operator;
import type.vartype.VarTypeArray;
import type.vartype.VarTypeSet;

public class CodeGenerator {

	private CodePrinter printer;
	private ArrayList<ExpTreeNode> expTrees;
	private LinkedList<ArrayList<ExpTreeNode>> checkers;
	private LinkedList<ArrayList<ExpTreeNode>> accCheckers;
	private HashMap<ExpTreeNode, String> setname;
	private HashSet<VarRecord> used;
	private ExpGenerator cgen;

	private void getCheckers(VarRecord v) throws IOException {
		ArrayList<ExpTreeNode> checker = new ArrayList<ExpTreeNode>();
		ArrayList<ExpTreeNode> accChecker = new ArrayList<ExpTreeNode>();
		for (ExpTreeNode e : expTrees)
			if (e.isReady(v))
				if (e.getAccV(v) != null)
					accChecker.add(e);
				else
					checker.add(e);
		expTrees.removeIf(e -> e.isReady(v));
		checkers.push(checker);
		accCheckers.push(accChecker);
	}

	private void genChecks(ArrayList<ExpTreeNode> checker, String breakStr, VarRecord v) throws IOException {
		for (ExpTreeNode e : checker)
			for (String calc : cgen.getCheck(e, breakStr, v))
				printer.print(calc + "\n");
	}

	private ArrayList<String> calcDepVars(ArrayList<VarRecord> dependVars, HashSet<VarRecord> indeps, VarRecord var,
			String breakStr) throws Exception {
		ArrayList<String> calcs = new ArrayList<String>();
		boolean flag = true;
		while (flag) {
			flag = false;
			for (VarRecord v : dependVars)
				if (!used.contains(v))
					if (v.ready(used, indeps, var)) {
						if (!v.getName().startsWith("_sum"))
							calcs.addAll(cgen.getCalcs(new ExpTreeNode(v), v.getDependExp(), breakStr));
						else if (!(var.getVarType() instanceof VarTypeArray)
								&& !(var.getVarType() instanceof VarTypeSet)) {
							calcs.addAll(cgen.getCalcs(new ExpTreeNode(v), v.getDependExp(), breakStr));
							int m = calcs.size() - 1;
							calcs.set(m, "int " + v.getName() + " " + calcs.get(m).substring(1) + ";");
						}
						used.add(v);
						flag = true;
					}
		}
		return calcs;
	}

	private void initDPTable(VarRecord v, ArrayList<Integer> DPTable, String initVal) throws IOException {
		if (DPTable != null) {
			int indent = printer.getIndent();
			ArrayList<String> tmpnames = new ArrayList<String>();
			String tmp = cgen.getTmpName();
			tmpnames.add(tmp);
			printer.print("for (int " + tmp + " = 0; " + tmp + " < " + v.getVarType().getSize() + "; " + tmp + "++)\n");
			printer.incIndent();
			for (int d : DPTable) {
				tmp = cgen.getTmpName();
				tmpnames.add(tmp);
				printer.print("for (int " + tmp + " = 0; " + tmp + " < " + d + "; " + tmp + "++)\n");
				printer.incIndent();
			}
			printer.print("_DP_" + v.getName());
			for (String t : tmpnames)
				printer.append("[" + t + "]");
			printer.append(" = " + initVal + ";\n");
			printer.setIndent(indent);
		}
	}

	private String DPEntry(VarRecord v, ArrayList<VarRecord> dim, String prefix) {
		String s = "_DP_" + v.getName() + "[_step]";
		for (int i = 0; i < dim.size(); i++) {
			VarRecord d = dim.get(i);
			if (d != v)
				s += "[" + prefix + d.getName() + cgen.offset(d.getVarType().getLowerBound().toBigInteger()) + "]";
			else
				s += "[" + v.getName() + "[_step - " + (dim.size() - i) + "]"
						+ cgen.offset(d.getVarType().getLowerBound().toBigInteger()) + "]";
		}
		return s;
	}

	public void generate(File file, VarTable varTable, ArrayList<ExpTreeNode> _expTrees, String memlimit)
			throws Exception {
		printer = new CodePrinter(file);
		printer.print("#include<stdio.h>\n");
		printer.print("\n");
		ArrayList<VarRecord> independVars = varTable.getVars(VarClass.INDEPENDENT);
		ArrayList<VarRecord> dependVars = varTable.getVars(VarClass.DEPENDENT);
		used = new HashSet<VarRecord>();
		ArrayList<VarRecord> inputVars = varTable.getInputs();
		ArrayList<VarRecord> outputVars = varTable.getOutputs();
		for (VarRecord v : inputVars)
			printer.print(v.getDef() + ";\n");
		for (VarRecord v : independVars)
			printer.print(v.getDef() + ";\n");
		for (VarRecord v : dependVars)
			if (!v.getName().startsWith("_sum"))
				printer.print(v.getDef() + ";\n");
		Objective obj = varTable.getObjective();
		HashSet<VarRecord> bestVars = new HashSet<VarRecord>(outputVars);
		if (obj != Objective.SAT) {
			bestVars.add(varTable.get("_result"));
			for (VarRecord v : outputVars) {
				ExpTreeNode e = v.getDefExp();
				if (e != null)
					bestVars.addAll(e.getVarSet());
			}
			bestVars.removeAll(inputVars);
			for (VarRecord v : bestVars)
				printer.print(v.getBestDef() + ";\n");
		}
		printer.print("\n");
		cgen = new ExpGenerator(bestVars);
		if (!inputVars.isEmpty()) {
			printer.print("void _input() {\n");
			for (VarRecord v : inputVars) {
				ArrayList<String> calcs = cgen.generateIO(v.getDefExp());
				int m = calcs.size() - 1;
				calcs.set(m, "scanf(\"%d\", " + v.getName() + calcs.get(m) + ");");
				for (String s : calcs) {
					printer.incIndent();
					printer.print(s + "\n");
				}
				printer.resetIndent();
			}
			printer.print("}\n");
			printer.print("\n");
		}
		printer.print("void _output() {\n");
		for (VarRecord v : outputVars) {
			ArrayList<String> calcs;
			ExpTreeNode defExp = v.getDefExp();
			if (defExp == null) {
				calcs = new ArrayList<String>();
				String prefix = "";
				if (obj != Objective.SAT)
					prefix = "_best_";
				calcs.add("printf(\"%d\n\", " + prefix + v.getName() + ");");
			} else {
				calcs = cgen.generateIO(defExp);
				int m = calcs.size() - 1;
				String prefix = "";
				if (obj != Objective.SAT)
					prefix = "_best_";
				if (m == 0)
					calcs.set(m, "printf(\"%d\n\", " + prefix + v.getName() + calcs.get(m) + ");");
				else if (defExp.getOp() != Operator.SET)
					calcs.set(m, "printf(\"%d \", " + prefix + v.getName() + calcs.get(m) + ");");
				else
					calcs.set(m - 1, "if (" + prefix + v.getName() + calcs.get(m - 1) + ")");
			}
			for (String s : calcs) {
				printer.incIndent();
				printer.print(s + "\n");
			}
			printer.resetIndent();
			if (calcs.size() > 1) {
				printer.incIndent();
				printer.print("printf(\"\\n\");\n");
				printer.decIndent();
			}
		}
		if (obj == Objective.SAT) {
			printer.incIndent();
			printer.print("exit(0);\n");
			printer.decIndent();
		}
		printer.print("}\n");
		printer.print("\n");
		ArrayList<VarRecord> independArrays = new ArrayList<VarRecord>();
		for (VarRecord v : independVars) {
			VarType t = v.getVarType();
			if (t instanceof VarTypeArray || t instanceof VarTypeSet)
				independArrays.add(v);
		}
		HashSet<VarRecord> indeps = new HashSet<VarRecord>(independVars);
		independVars.removeAll(independArrays);
		expTrees = _expTrees;
		setname = new HashMap<ExpTreeNode, String>();
		int seti = 0;
		for (ExpTreeNode e : expTrees)
			if (e.getOp() == Operator.ALLDIFF) {
				setname.put(e, "_set" + seti);
				printer.print("std::set<int> " + "_set" + seti + ";\n");
				seti++;
			}
		if (!setname.isEmpty())
			printer.print("\n");
		checkers = new LinkedList<ArrayList<ExpTreeNode>>();
		accCheckers = new LinkedList<ArrayList<ExpTreeNode>>();
		for (VarRecord v : independArrays)
			getCheckers(v);
		for (int i = independVars.size() - 1; i >= 0; i--)
			getCheckers(independVars.get(i));
		cgen.resetTmpCount();
		if (obj != Objective.SAT) {
			printer.print("void _update() {\n");
			printer.incIndent();
			if (obj == Objective.MAX)
				printer.print("if (_result <= _best__result)\n");
			else
				printer.print("if (_result >= _best__result)\n");
			printer.print("\treturn;\n");
			for (VarRecord v : bestVars) {
				VarType t = v.getVarType();
				if (t instanceof VarTypeArray || t instanceof VarTypeSet)
					printer.print("memcpy(_best_" + v.getName() + ", " + v.getName() + ", " + t.getMemSize() + ");\n");
				else
					printer.print("_best_" + v.getName() + " = " + v.getName() + ";\n");
			}
			printer.decIndent();
			printer.print("}\n");
			printer.print("\n");
		}
		VarRecord next = null;
		int preCount = 0;
		ArrayList<Integer> preDPTable = null;
		String preInitVal = null;
		if (!independArrays.isEmpty())
			for (int i = independArrays.size() - 1; i >= 0; i--) {
				VarRecord v = independArrays.get(i);
				ArrayList<String> tmpnames = new ArrayList<String>();
				ArrayList<ExpTreeNode> accChecker = accCheckers.removeLast();
				ArrayList<ExpTreeNode> checker = checkers.removeLast();
				VarType t = v.getVarType();
				BigInteger total = t.getSize();
				ArrayList<VarRecord> dim = new ArrayList<VarRecord>();
				VarRecord targetV = null;
				BigInteger side = BigInteger.ZERO;
				boolean useDP = obj != Objective.SAT && !outputVars.contains(v);
				if (useDP)
					for (ExpTreeNode e : checker)
						if (e.getOp() == Operator.FORALL || setname.containsKey(e)) {
							useDP = false;
							break;
						}
				for (ExpTreeNode e : accChecker) {
					VarRecord accV = e.getAccV(null);
					if (useDP) {
						VarType it = e.getSons().get(1).getSons().get(0).getSons().get(0).getSons().get(0).getSons()
								.get(0).getVarType();
						side = side.max(it.getLowerBound().toBigInteger().subtract(v.getVarType().getLowerIndex()))
								.max(v.getVarType().getUpperIndex().subtract(it.getUpperBound().toBigInteger()));
						boolean flag = false;
						for (ExpTreeNode e1 : checker)
							if (e1.getVarSet().contains(accV) && e1.getSons().get(0).getStr() != "_result") {
								flag = true;
								break;
							}
						if (flag)
							dim.add(accV);
						else
							targetV = accV;
					}
					tmpnames.add(accV.getName());
				}
				ArrayList<Integer> DPTable = new ArrayList<Integer>();
				useDP &= targetV != null;
				if (useDP) {
					for (int j = side.intValue(); j > 0; j--)
						dim.add(v);
					BigInteger mem = total;
					for (VarRecord var : dim) {
						BigInteger bi = var.getVarType().range();
						if (bi == null) {
							useDP = false;
							break;
						}
						DPTable.add(bi.intValue());
						mem = mem.multiply(bi);
					}
					useDP &= mem.compareTo(new BigInteger(memlimit)) <= 0;
				}
				String initVal = null;
				if (useDP)
					if (obj == Objective.MAX)
						initVal = targetV.getVarType().getLowerBound().toBigInteger().subtract(BigInteger.ONE)
								.toString();
					else
						initVal = targetV.getVarType().getUpperBound().toBigInteger().add(BigInteger.ONE).toString();
				if (useDP) {
					printer.print(targetV.getVarType().getName() + " _DP_" + v.getName() + "[" + total + "]");
					for (int val : DPTable)
						printer.append("[" + val + "]");
					printer.append(";\n");
					printer.print("\n");
				}
				String fname = "_find_" + v.getName();
				cgen.resetTmpCount();
				if (useDP)
					printer.print(targetV.getVarType().getName());
				else
					printer.print("void");
				printer.append(" " + fname + "(int _step");
				for (String tmp : tmpnames)
					printer.append(", int " + tmp);
				printer.append(") {\n");
				printer.incIndent();
				String totStr = t.getTotStr();
				if (t instanceof VarTypeSet)
					totStr = v.getDefExp().getSons().get(0).getSons().get(1).getStr() + " - "
							+ v.getDefExp().getSons().get(0).getSons().get(0).getStr() + " + 1";
				printer.print("if (_step == " + totStr + ") {\n");
				printer.incIndent();
				String breakStr = "return";
				if (useDP)
					breakStr = "return " + initVal;
				ArrayList<String> depCalcs = calcDepVars(dependVars, indeps, v, breakStr);
				for (String calc : depCalcs)
					printer.print(calc + "\n");
				genChecks(checker, breakStr, null);
				if (next == null)
					if (obj == Objective.SAT)
						printer.print("_output();\n");
					else
						printer.print("_update();\n");
				else {
					initDPTable(next, preDPTable, preInitVal);
					printer.print("_find_" + next.getName() + "(0");
					for (int c = 0; c < preCount; c++)
						printer.append(", 0");
					printer.append(");\n");
				}
				if (useDP)
					printer.print("return " + targetV.getName() + ";\n");
				else
					printer.print("return;\n");
				printer.decIndent();
				printer.print("}\n");
				if (useDP) {
					printer.print("if (");
					if (side.signum() > 0)
						printer.append("_step >= " + side + " && ");
					printer.append(DPEntry(v, dim, "") + " != " + initVal + ") {\n");
					printer.incIndent();
					printer.print(targetV.getName() + " += " + DPEntry(v, dim, "") + ";\n");
					breakStr = "return " + initVal;
					for (String calc : depCalcs)
						printer.print(calc + "\n");
					genChecks(checker, breakStr, v);
					if (next == null)
						printer.print("_update();\n");
					else {
						initDPTable(next, preDPTable, preInitVal);
						printer.print("_find_" + next.getName() + "(0");
						for (int c = 0; c < preCount; c++)
							printer.append(", 0");
						printer.append(");\n");
					}
					printer.print("return " + targetV.getName() + ";\n");
					printer.decIndent();
					printer.print("}\n");
				}
				indeps.remove(v);
				preCount = tmpnames.size();
				for (String tmp : tmpnames)
					printer.print("int _" + tmp + " = " + tmp + ";\n");
				if (t instanceof VarTypeSet)
					printer.print("for (" + v.getName() + "[_step] = 0; " + v.getName() + "[_step] <= 1; " + v.getName()
							+ "[_step]++)");
				else {
					BigInteger length = ((VarTypeArray) t).getLength();
					String addr = v.getName() + "[_step]";
					if (length.compareTo(total) < 0) {
						total = total.divide(length);
						addr = v.getName() + "[_step / " + total + "]";
						VarTypeArray type = (VarTypeArray) t;
						while (true) {
							type = (VarTypeArray) type.getSubType();
							length = type.getLength();
							if (length.compareTo(total) == 0)
								break;
							BigInteger nextTotal = total.divide(length);
							addr += "[_step % " + total + " / " + nextTotal + "]";
							total = nextTotal;
						}
						addr += "[_step % " + total + "]";
					}
					String lb = cgen.getCalcs(null, t.getLowerBoundExp(), null).get(0);
					if (lb == null)
						lb = Integer.toString(t.getLowerBound().intValue());
					String ub = cgen.getCalcs(null, t.getUpperBoundExp(), null).get(0);
					if (ub == null)
						ub = Integer.toString(t.getUpperBound().intValue());
					printer.print("for (" + addr + " = " + lb + "; " + addr + " <= " + ub + "; " + addr + "++)");
				}
				boolean flag = tmpnames.size() > 0;
				if (!flag)
					for (ExpTreeNode e : checker)
						if (e.getOp() == Operator.ALLDIFF) {
							flag = true;
							break;
						}
				if (flag)
					printer.append(" {");
				printer.append("\n");
				printer.incIndent();
				LinkedList<String> setInsert = new LinkedList<String>();
				LinkedList<String> setErase = new LinkedList<String>();
				for (ExpTreeNode e : checker)
					if (e.getOp() == Operator.ALLDIFF) {
						ArrayList<String> calcs = new ArrayList<String>();
						ExpTreeNode exp = e.getSons().get(0);
						if (exp.getOp() == Operator.NONE)
							calcs.add(exp.getStr() + "[_step]");
						else {
							exp = exp.getSons().get(0);
							calcs = cgen.getCalcs(null, exp.getSons().get(2), "continue");
							ExpTreeNode it = exp.getSons().get(0).getSons().get(0);
							for (int j = 0; j < calcs.size(); j++)
								calcs.set(j, calcs.get(j).replaceAll(it.getStr(),
										"_step" + cgen.offset(v.getVarType().getLowerIndex().negate())));
						}
						String tmp = cgen.getTmpName();
						calcs.set(calcs.size() - 1, "int " + tmp + " = " + calcs.get(calcs.size() - 1) + ";");
						for (String s : calcs)
							printer.print(s + "\n");
						String sname = setname.get(e);
						printer.print("if (" + sname + ".find(" + tmp + ") != " + sname + ".end())\n");
						printer.print("\tcontinue;\n");
						setInsert.add(sname + ".insert(" + tmp + ");");
						setErase.add(sname + ".erase(" + tmp + ");");
					}
				for (String tmp : tmpnames)
					printer.print(tmp + " = _" + tmp + ";\n");
				for (ExpTreeNode e : accChecker) {
					String tmp = e.getAccV(null).getName();
					ArrayList<String> calcs = cgen.getAccCalcs(e.getSons().get(1), v.getVarType(), total, "continue");
					int m = calcs.size() - 1;
					while (calcs.get(m).contains("}"))
						m--;
					String st = calcs.get(m);
					String tt = tmp;
					while (st.startsWith("\t")) {
						tt = "\t" + tt;
						st = st.substring(1);
					}
					calcs.set(m, tt + " += " + st + ";");
					for (String s : calcs)
						printer.print(s + "\n");
				}
				for (String vname : tmpnames) {
					VarRecord acc = varTable.get(vname);
					for (ExpTreeNode e : checker)
						if (e.getVarSet().contains(acc)) {
							ArrayList<String> calcs = cgen.getCalcs(new ExpTreeNode(acc), e, breakStr);
							String suffix = calcs.get(calcs.size() - 1);
							if (useDP && suffix.endsWith("_best__result"))
								continue;
							String bound;
							String tmp1 = null;
							if (suffix.startsWith("<") || suffix.startsWith("=")) {
								bound = acc.getDependExp().getSons().get(1).getSons().get(0).getVarType()
										.getLowerBound().toString();
								if (bound.startsWith("-"))
									bound += " * (" + total + " - _step) ";
								else
									bound = "";
								tmp1 = vname + " " + bound + suffix;
								if (suffix.startsWith("="))
									tmp1 = vname + " " + bound + "<" + suffix.substring(1);
							}
							String tmp2 = null;
							if (suffix.startsWith(">") || suffix.startsWith("=")) {
								bound = acc.getDependExp().getSons().get(1).getSons().get(0).getVarType()
										.getUpperBound().toString();
								if (bound != "")
									bound = "+ " + bound + " * (" + total + " - _step) ";
								tmp2 = vname + " " + bound + suffix;
								if (suffix.startsWith("="))
									tmp2 = vname + " " + bound + ">" + suffix.substring(1);
							}
							if (tmp1 != null && tmp2 == null)
								calcs.set(calcs.size() - 1, "if (!(" + tmp1 + "))");
							if (tmp1 == null && tmp2 != null)
								calcs.set(calcs.size() - 1, "if (!(" + tmp2 + "))");
							if (tmp1 != null && tmp2 != null)
								calcs.set(calcs.size() - 1, "if (!(" + tmp1 + ") || !(" + tmp2 + "))");
							for (String s : calcs)
								printer.print(s + "\n");
							printer.print("\tcontinue;\n");
						}
				}
				for (String s : setInsert)
					printer.print(s + "\n");
				String dptmp = null;
				if (useDP) {
					dptmp = cgen.getTmpName();
					printer.print("int " + dptmp + " = ");
				} else
					printer.print("");
				printer.append("_find_" + v.getName() + "(_step + 1");
				for (String tmp : tmpnames)
					printer.append(", " + tmp);
				printer.append(")");
				if (useDP)
					printer.append(" - _" + targetV.getName());
				printer.append(";\n");
				for (String s : setErase)
					printer.print(s + "\n");
				if (useDP) {
					printer.print("if (" + dptmp);
					if (obj == Objective.MAX)
						printer.append(" > ");
					else
						printer.append(" < ");
					printer.append(DPEntry(v, dim, "_") + ")\n");
					printer.incIndent();
					printer.print(DPEntry(v, dim, "_") + " = " + dptmp + ";\n");
					printer.decIndent();
				}
				printer.decIndent();
				if (flag)
					printer.print("}\n");
				if (useDP)
					printer.print("return _" + targetV.getName() + " + " + DPEntry(v, dim, "_") + ";\n");
				printer.resetIndent();
				printer.print("}\n");
				printer.print("\n");
				if (useDP) {
					preDPTable = DPTable;
					preInitVal = initVal;
				} else
					preDPTable = null;
				next = v;
			}
		cgen.resetTmpCount();
		printer.print("void _solve() {\n");
		printer.incIndent();
		switch (obj) {
		case MAX:
			printer.print("_best__result = "
					+ varTable.get("_result").getVarType().getLowerBound().subtract(BigDecimal.ONE) + ";\n");
			break;
		case MIN:
			printer.print("_best__result = " + varTable.get("_result").getVarType().getUpperBound().add(BigDecimal.ONE)
					+ ";\n");
			break;
		case SAT:
			break;
		}
		LinkedList<Boolean> braces = new LinkedList<Boolean>();
		indeps = new HashSet<VarRecord>();
		for (VarRecord v : independVars) {
			String vname = v.getName();
			VarType t = v.getVarType();
			String lb = null;
			if (v.getDefExp() != null)
				lb = cgen.getCalcs(null, v.getDefExp().getSons().get(0), null).get(0);
			if (lb == null)
				lb = Integer.toString(t.getLowerBound().intValue());
			String ub = null;
			if (v.getDefExp() != null)
				ub = cgen.getCalcs(null, v.getDefExp().getSons().get(1), null).get(0);
			if (ub == null)
				ub = Integer.toString(t.getUpperBound().intValue());
			printer.print("for (" + vname + " = " + lb + "; " + vname + " <= " + ub + "; " + vname + "++)");
			indeps.add(v);
			ArrayList<String> calcs = calcDepVars(dependVars, indeps, v, "continue");
			ArrayList<ExpTreeNode> checker = checkers.removeFirst();
			braces.push(!checker.isEmpty() || !calcs.isEmpty());
			if (braces.getFirst())
				printer.append(" {");
			printer.append("\n");
			printer.incIndent();
			for (String calc : calcs)
				printer.print(calc + "\n");
			genChecks(checker, "continue", null);
		}
		if (next == null)
			if (obj == Objective.SAT)
				printer.print("_output();\n");
			else
				printer.print("_update();\n");
		else {
			initDPTable(next, preDPTable, preInitVal);
			printer.print("_find_" + next.getName() + "(0");
			for (int c = 0; c < preCount; c++)
				printer.append(", 0");
			printer.append(");\n");
		}
		while (!braces.isEmpty()) {
			printer.decIndent();
			if (braces.pop())
				printer.print("}\n");
		}
		printer.decIndent();
		printer.print("}\n");
		printer.print("\n");
		printer.print("int main() {\n");
		printer.incIndent();
		if (!inputVars.isEmpty())
			printer.print("_input();\n");
		printer.print("_solve();\n");
		if (obj == Objective.SAT)
			printer.print("printf(\"No Solution!\\n\");\n");
		else
			printer.print("_output();\n");
		printer.print("return 0;\n");
		printer.decIndent();
		printer.print("}\n");
		printer.close();
	}

}
