package generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import type.ExpTreeNode;
import type.VarRecord;
import type.VarType;
import type.ExpTreeNode.Operator;
import type.vartype.VarTypeArray;
import type.vartype.VarTypeInt;

public class ExpGenerator {

	private int tmpCount;
	private String prefix = "";
	private ArrayList<String> calcs;
	private LinkedList<Boolean> braces = new LinkedList<Boolean>();
	private HashSet<String> bestVars = new HashSet<String>();
	private boolean isIO;
	private String breakStr;
	private String arrname;
	private String appendStr;
	private boolean forallCheck = false;

	public ExpGenerator(HashSet<VarRecord> vars) {
		if (vars != null)
			for (VarRecord v : vars)
				bestVars.add(v.getName());
	}

	private int priority(Operator o) {
		switch (o) {
		case EQUAL:
		case NOTEQUAL:
		case LESS:
		case LESSEQUAL:
		case GREATER:
		case GREATEREQUAL:
			return 1;
		case IF:
			return 2;
		case OR:
			return 3;
		case AND:
			return 4;
		case XOR:
			return 5;
		case ADD:
		case SUBTRACT:
			return 6;
		case MULTIPLY:
		case DIVIDE:
		case INTDIVIDE:
		case MODULO:
			return 7;
		case NOT:
			return 8;
		default:
			return 0;
		}
	}

	public void resetTmpCount() {
		tmpCount = 0;
	}

	public String getTmpName() {
		String tmp = "_tmp" + tmpCount;
		tmpCount++;
		return tmp;
	}

	public String offset(BigInteger i) {
		int sign = i.signum();
		if (sign == 0)
			return "";
		else if (sign > 0)
			return " - " + i;
		else
			return " + " + i.negate();
	}

	private String genExp(ExpTreeNode node, Operator o) {
		ExpTreeNode son0 = null, son1 = null, son2 = null;
		ArrayList<ExpTreeNode> sons = node.getSons();
		switch (sons.size()) {
		case 3:
			son2 = sons.get(2);
		case 2:
			son1 = sons.get(1);
		case 1:
			son0 = sons.get(0);
		}
		String s;
		String tmp, tmp2;
		String backup = prefix;
		VarType t;
		int m;
		Operator op = node.getOp();
		switch (op) {
		case NONE:
			tmp = node.getStr();
			if (!isIO || !bestVars.contains(tmp))
				return tmp;
			return "_best_" + tmp;
		case NOT:
			s = genExp(son0, op);
			break;
		case AND:
			s = genExp(son0, op) + " && " + genExp(son1, op);
			break;
		case OR:
			s = genExp(son0, op) + " || " + genExp(son1, op);
			break;
		case XOR:
			s = genExp(son0, op) + " ^ " + genExp(son1, op);
			break;
		case ADD:
			s = genExp(son0, op) + " + " + genExp(son1, op);
			break;
		case SUBTRACT:
			s = genExp(son0, op) + " - " + genExp(son1, op);
			break;
		case MULTIPLY:
			s = genExp(son0, op) + " * " + genExp(son1, op);
			break;
		case DIVIDE:
			s = genExp(son1, op);
			calcs.add("if (" + s + " == 0)");
			calcs.add("\t" + breakStr + ";");
			tmp = genExp(son0, op);
			if (son0.getVarType() instanceof VarTypeInt && son1.getVarType() instanceof VarTypeInt
					&& node.getVarType() instanceof VarTypeInt) {
				calcs.add("if (" + tmp + " % " + s + " != 0)");
				calcs.add("\t" + breakStr + ";");
			}
			s = tmp + " / " + s;
			break;
		case INTDIVIDE:
			s = genExp(son1, op);
			calcs.add("if (" + s + " == 0)");
			calcs.add("\t" + breakStr + ";");
			s = genExp(son0, op) + " / " + s;
			break;
		case MODULO:
			s = genExp(son0, op) + " % " + genExp(son1, op);
			break;
		case POWER:
			return null;
		case EQUAL:
			s = genExp(son0, op) + " == " + genExp(son1, op);
			break;
		case NOTEQUAL:
			s = genExp(son0, op) + " != " + genExp(son1, op);
			break;
		case LESS:
			s = genExp(son0, op) + " < " + genExp(son1, op);
			break;
		case LESSEQUAL:
			s = genExp(son0, op) + " <= " + genExp(son1, op);
			break;
		case GREATER:
			s = genExp(son0, op) + " > " + genExp(son1, op);
			break;
		case GREATEREQUAL:
			s = genExp(son0, op) + " >= " + genExp(son1, op);
			break;
		case MAXIMIZE:
			return genExp(son0, op);
		case MINIMIZE:
			return genExp(son0, op);
		case IN:
			s = genExp(son1, op);
			tmp = genExp(son0, op) + offset(son1.getVarType().getLowerBound().toBigInteger());
			s += "[" + tmp + "]";
			if (s == arrname)
				appendStr += "_step > " + tmp + " && ";
			break;
		case INDEX:
			s = genExp(son0, op);
			tmp = genExp(son1, op);
			if (tmp.contains("[") || forallCheck) {
				String bs = breakStr;
				if (forallCheck)
					breakStr = "continue";
				boolean left = son1.getVarType().getLowerBound().compareTo(
						((VarTypeArray) son0.getVarType()).getLowerIndexExp().getVarType().getUpperBound()) < 0;
				boolean right = son1.getVarType().getUpperBound().compareTo(
						((VarTypeArray) son0.getVarType()).getUpperIndexExp().getVarType().getLowerBound()) > 0;
				if (left || right) {
					String st = tmp;
					tmp = getTmpName();
					calcs.add(prefix + "int " + tmp + " = " + st + ";");
					if (left) {
						calcs.add(prefix + "if (" + tmp + " < "
								+ genExp(((VarTypeArray) son0.getVarType()).getLowerIndexExp(), Operator.LESS) + ")");
						calcs.add(prefix + "\t" + breakStr + ";");
					}
					if (right) {
						calcs.add(prefix + "if (" + tmp + " > "
								+ genExp(((VarTypeArray) son0.getVarType()).getUpperIndexExp(), Operator.GREATER)
								+ ")");
						calcs.add(prefix + "\t" + breakStr + ";");
					}
				}
				breakStr = bs;
			}
			tmp += offset(son0.getVarType().getLowerIndex());
			if (s == arrname)
				appendStr += "_step > " + tmp + " && ";
			s += "[" + tmp + "]";
			break;
		case IF:
			s = genExp(son0, op) + " ? " + genExp(son1, op) + " : " + genExp(son2, op);
			break;
		case TUPLE:
			for (ExpTreeNode n : sons) {
				tmp = n.getStr();
				t = n.getVarType();
				calcs.add(prefix + "for (int " + tmp + " = " + genExp(t.getLowerBoundExp(), Operator.EQUAL) + "; " + tmp
						+ " <= " + genExp(t.getUpperBoundExp(), Operator.LESSEQUAL) + "; " + tmp + "++)");
				prefix += "\t";
			}
			return null;
		case EXISTS:
			tmp = getTmpName();
			calcs.add(prefix + "bool " + tmp + " = false;");
			genExp(son0, op);
			calcs.add(prefix + "if (" + genExp(son1, op) + ")");
			calcs.add(prefix + "\t" + tmp + " = true;");
			calcs.add(prefix + "break;");
			calcs.add(prefix.substring(1) + "}");
			prefix = backup;
			return tmp;
		case FORALL:
			if (o == Operator.NOT) {
				forallCheck = true;
				tmp = getTmpName();
				calcs.add(prefix + "bool " + tmp + " = true;");
				genExp(son0, op);
				m = calcs.size() - 1;
				s = "";
				if (son1.getOp() != Operator.NONE && son1.getOp() != Operator.OF)
					s = genExp(son1, Operator.AND) + " && ";
				calcs.add(prefix + "if (" + s + "!(" + genExp(son2, op) + ")) {");
				calcs.add(prefix + "\t" + tmp + " = false;");
				calcs.add(prefix + "\tbreak;");
				calcs.add(prefix + "}");
				if (calcs.size() > m + 5) {
					calcs.set(m, calcs.get(m) + " {");
					calcs.add(prefix.substring(1) + "}");
				}
				prefix = backup;
				forallCheck = false;
				return tmp;
			}
			genExp(son0, op);
			m = calcs.size() - 1;
			boolean flag = false;
			if (son1.getOp() != Operator.NONE) {
				String st = genExp(son1, op);
				if (st != null) {
					calcs.add(prefix + "if (" + st + ")");
					if (calcs.size() > m + 2) {
						calcs.set(m, calcs.get(m) + " {");
						braces.push(Boolean.TRUE);
					} else
						braces.push(Boolean.FALSE);
					prefix += "\t";
					m = calcs.size() - 1;
				}
			}
			braces.push(flag);
			s = genExp(son2, o);
			if (calcs.size() > m + 1) {
				calcs.set(m, calcs.get(m) + " {");
				braces.push(Boolean.TRUE);
			} else
				braces.push(Boolean.FALSE);
			return s;
		case COUNT:
			tmp = getTmpName();
			calcs.add(prefix + "int " + tmp + " = 0;");
			s = genExp(son0.getSons().get(0), op);
			calcs.add(prefix + "if (" + s + ")");
			calcs.add(prefix + "\t" + tmp + "++;");
			if (braces.pop())
				calcs.add(prefix.substring(1) + "}");
			if (braces.pop())
				calcs.add(prefix.substring(2) + "}");
			prefix = backup;
			return tmp;
		case MAX:
			son0 = son0.getSons().get(0);
			tmp = getTmpName();
			t = son0.getSons().get(2).getVarType();
			calcs.add(prefix + t.getDef(tmp) + " = " + t.getLowerBound() + ";");
			tmp2 = getTmpName();
			s = genExp(son0, Operator.EQUAL);
			calcs.add(prefix + t.rawType() + " " + tmp2 + " = " + s + ";");
			calcs.add(prefix + "if (" + tmp + " < " + tmp2 + ")");
			calcs.add(prefix + "\t" + tmp + " = " + tmp2 + ";");
			prefix = backup;
			return tmp;
		case MIN:
			son0 = son0.getSons().get(0);
			tmp = getTmpName();
			t = son0.getSons().get(2).getVarType();
			calcs.add(prefix + t.getDef(tmp) + " = " + t.getUpperBound() + ";");
			tmp2 = getTmpName();
			s = genExp(son0, Operator.EQUAL);
			calcs.add(prefix + t.rawType() + " " + tmp2 + " = " + s + ";");
			calcs.add(prefix + "if (" + tmp + " < " + tmp2 + ")");
			calcs.add(prefix + "\t" + tmp + " = " + tmp2 + ";");
			prefix = backup;
			return tmp;
		case SUMMATION:
			son0 = son0.getSons().get(0);
			tmp = getTmpName();
			calcs.add(prefix + son0.getSons().get(2).getVarType().getDef(tmp) + " = 0;");
			s = genExp(son0, Operator.EQUAL);
			calcs.add(prefix + tmp + " += " + s + ";");
			if (braces.pop())
				calcs.add(prefix.substring(1) + "}");
			if (braces.pop())
				calcs.add(prefix.substring(2) + "}");
			prefix = backup;
			return tmp;
		case PRODUCT:
			son0 = son0.getSons().get(0);
			tmp = getTmpName();
			s = genExp(son0, Operator.EQUAL);
			calcs.add(prefix + son0.getSons().get(2).getVarType().getDef(tmp) + " = 1;");
			calcs.add(prefix + tmp + " *= " + s + ";");
			if (braces.pop())
				calcs.add(prefix.substring(1) + "}");
			if (braces.pop())
				calcs.add(prefix.substring(2) + "}");
			prefix = backup;
			return tmp;
		default:
			return null;
		}
		int pr = priority(op);
		if (pr > 0 && priority(o) >= pr)
			return "(" + s + ")";
		return s;
	}

	public ArrayList<String> getCheck(ExpTreeNode node, String _breakStr, VarRecord v) {
		breakStr = _breakStr;
		if (node.isIgnore())
			return new ArrayList<String>();
		calcs = new ArrayList<String>();
		if (v == null)
			arrname = null;
		else
			arrname = v.getName();
		appendStr = "";
		String current = genExp(node, Operator.NOT);
		if (current == null)
			return new ArrayList<String>();
		if (v != null && current.contains("_sum"))
			calcs.clear();
		else {
			calcs.add("if (" + appendStr + "!" + current + ")");
			calcs.add("\t" + breakStr + ";");
		}
		return calcs;
	}

	private String addBrac(String s, int p, Operator op) {
		int pr = priority(op);
		if (pr > 0 && p > pr)
			return "(" + s + ")";
		return s;
	}

	public ArrayList<String> getAccCalcs(ExpTreeNode root, VarType t, BigInteger total, String _breakStr) {
		breakStr = _breakStr;
		root = root.getSons().get(0).getSons().get(0);
		calcs = new ArrayList<String>();
		ArrayList<ExpTreeNode> sons = root.getSons();
		ExpTreeNode son1 = sons.get(1);
		ExpTreeNode son2 = sons.get(2);
		Operator op = root.getOp();
		ExpTreeNode it = sons.get(0).getSons().get(0);
		String off = null;
		if (total.subtract(it.getVarType().range()).signum() > 0) {
			off = offset(total.subtract(it.getVarType().range()));
			if (off != null) {
				calcs.add("if (_step" + off + " >= 0) {");
				prefix += "\t";
			}
		}
		int m = calcs.size();
		boolean flag = false;
		if (son1.getOp() != Operator.NONE) {
			String st = genExp(son1, op);
			if (st != null) {
				flag = true;
				calcs.add(prefix + "if (" + st + ")");
				prefix += "\t";
			}
		}
		String s = genExp(son2, Operator.ADD);
		calcs.add(prefix + s);
		for (int i = 0; i < calcs.size(); i++)
			calcs.set(i, calcs.get(i).replaceAll(it.getStr(), "_step" + offset(
					total.subtract(it.getVarType().range()).subtract(it.getVarType().getLowerBound().toBigInteger()))));
		if (flag && calcs.size() > m + 2) {
			calcs.set(m, calcs.get(m) + " {");
			calcs.add(prefix.substring(1) + "}");
		}
		prefix = "";
		if (off != null)
			calcs.add("}");
		return calcs;
	}

	private String opStr(Operator op, boolean left) {
		if (left)
			switch (op) {
			case EQUAL:
				return "==";
			case LESS:
				return "<";
			case LESSEQUAL:
				return "<=";
			case GREATER:
				return ">";
			case GREATEREQUAL:
				return ">=";
			default:
				return null;
			}
		else
			switch (op) {
			case EQUAL:
				return "==";
			case LESS:
				return ">";
			case LESSEQUAL:
				return ">=";
			case GREATER:
				return "<";
			case GREATEREQUAL:
				return "<=";
			default:
				return null;
			}
	}

	public ArrayList<String> getCalcs(ExpTreeNode v, ExpTreeNode root, String _breakStr) throws Exception {
		breakStr = _breakStr;
		String o = null;
		if (root == null)
			return null;
		calcs = new ArrayList<String>();
		String current = null;
		if (v == null) {
			current = genExp(root, Operator.EQUAL);
			if (calcs.isEmpty())
				calcs.add(current);
			else
				calcs.set(0, null);
			return calcs;
		}
		int p = 0;
		while (root.getOp() != Operator.NONE && root != v) {
			Operator op = root.getOp();
			ExpTreeNode son0 = null, son1 = null;
			ArrayList<ExpTreeNode> sons = root.getSons();
			switch (sons.size()) {
			case 3:
			case 2:
				son1 = sons.get(1);
			case 1:
				son0 = sons.get(0);
			}
			boolean left = son0.getVarSet().contains(v.getVarSet().toArray()[0]);
			String s, t;
			switch (op) {
			case NOT:
				current = "!(" + current + ")";
				root = son0;
				break;
			case EQUAL:
			case NOTEQUAL:
			case LESS:
			case LESSEQUAL:
			case GREATER:
			case GREATEREQUAL:
				o = opStr(op, left);
				if (left) {
					current = genExp(son1, op);
					root = son0;
				} else {
					current = genExp(son0, op);
					root = son1;
				}
				break;
			case MAXIMIZE:
				o = ">";
				current = "_best__result";
				root = son0;
				break;
			case MINIMIZE:
				o = "<";
				current = "_best__result";
				root = son0;
				break;
			case XOR:
				if (left) {
					current = addBrac(current, p, op) + " ^ " + genExp(son1, op);
					root = son0;
				} else {
					current = addBrac(current, p, op) + " ^ " + genExp(son0, op);
					root = son1;
				}
				break;
			case ADD:
				if (left) {
					current = addBrac(current, p, op) + " - " + genExp(son1, op);
					root = son0;
				} else {
					current = addBrac(current, p, op) + " - " + genExp(son0, op);
					root = son1;
				}
				break;
			case SUBTRACT:
				if (left) {
					current = addBrac(current, p, op) + " + " + genExp(son1, op);
					root = son0;
				} else {
					current = genExp(son0, op) + " - " + addBrac(current, p, op);
					root = son1;
				}
				break;
			case MULTIPLY:
				if (left) {
					s = genExp(son1, op);
					calcs.add("if (" + s + " == 0)");
					calcs.add("\t" + breakStr + ";");
					t = addBrac(current, p, op);
					if (root.getVarType() instanceof VarTypeInt && son0.getVarType() instanceof VarTypeInt
							&& son1.getVarType() instanceof VarTypeInt) {
						calcs.add("if (" + t + " % " + s + " != 0)");
						calcs.add("\t" + breakStr + ";");
					}
					current = t + " / " + s;
					root = son0;
				} else {
					s = genExp(son0, op);
					calcs.add("if (" + s + " == 0)");
					calcs.add("\t" + breakStr + ";");
					t = addBrac(current, p, op);
					if (root.getVarType() instanceof VarTypeInt && son0.getVarType() instanceof VarTypeInt
							&& son1.getVarType() instanceof VarTypeInt) {
						calcs.add("if (" + t + " % " + s + " != 0)");
						calcs.add("\t" + breakStr + ";");
					}
					current = t + " / " + s;
					root = son1;
				}
				break;
			case DIVIDE:
				if (left) {
					current = addBrac(current, p, op) + " * " + genExp(son1, op);
					root = son0;
				} else {
					s = addBrac(current, p, op);
					calcs.add("if (" + s + " == 0)");
					calcs.add("\t" + breakStr + ";");
					t = genExp(son0, op);
					if (root.getVarType() instanceof VarTypeInt && son0.getVarType() instanceof VarTypeInt
							&& son1.getVarType() instanceof VarTypeInt) {
						calcs.add("if (" + t + " % " + s + " != 0)");
						calcs.add("\t" + breakStr + ";");
					}
					current = t + " / " + s;
					root = son1;
				}
				break;
			case POWER:
				break;
			default:
				throw new Exception();
			}
			p = priority(op);
		}
		if (v.getOp() == Operator.NONE && !v.getStr().startsWith("_sum"))
			calcs.add(v.getStr() + " = " + current + ";");
		else if (o != null)
			calcs.add(o + " " + current);
		else
			calcs.add(current);
		return calcs;
	}

	public String getIO(ExpTreeNode node) {
		if (node.getOp() == Operator.ARRAY) {
			String tmp = getTmpName();
			calcs.add("for (int " + tmp + " = " + genExp(node.getSons().get(1), Operator.EQUAL) + "; " + tmp + " <= "
					+ genExp(node.getSons().get(2), Operator.LESSEQUAL) + "; " + tmp + "++)");
			return "[" + tmp + offset(node.getVarType().getLowerIndex()) + "]" + getIO(node.getSons().get(0));
		} else if (node.getOp() == Operator.SET) {
			String tmp = getTmpName();
			calcs.add("for (int " + tmp + " = " + genExp(node.getSons().get(0).getSons().get(0), Operator.EQUAL) + "; "
					+ tmp + " <= " + genExp(node.getSons().get(0).getSons().get(1), Operator.LESSEQUAL) + "; " + tmp
					+ "++)");
			calcs.add("[" + tmp + offset(node.getVarType().getLowerBound().toBigInteger()) + "]"
					+ getIO(node.getSons().get(0)));
			return "std::cout << " + tmp + " << \" \";";
		}
		return "";
	}

	public ArrayList<String> generateIO(ExpTreeNode node) {
		isIO = true;
		calcs = new ArrayList<String>();
		tmpCount = 0;
		String current = getIO(node);
		calcs.add(current);
		isIO = false;
		return calcs;
	}

}
