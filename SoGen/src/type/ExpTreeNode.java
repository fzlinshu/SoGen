package type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

import type.VarRecord.VarClass;
import type.vartype.VarTypeArray;
import type.vartype.VarTypeBool;
import type.vartype.VarTypeChar;
import type.vartype.VarTypeFunction;
import type.vartype.VarTypeInt;
import type.vartype.VarTypeReal;
import type.vartype.VarTypeSet;
import type.vartype.VarTypeTuple;

public class ExpTreeNode {

	public static enum Operator {
		NONE, OF, INT, REAL, BOOL, CHAR, ARRAY, SET, TUPLE, FUNCTION, MINIMIZE, MAXIMIZE, SUMMATION, PRODUCT, COUNT,
		MAX, MIN, FORALL, EXISTS, IF, ALLDIFF, NOT, AND, OR, XOR, EQUAL, NOTEQUAL, GREATER, LESS, GREATEREQUAL,
		LESSEQUAL, IN, ADD, SUBTRACT, MULTIPLY, DIVIDE, INTDIVIDE, MODULO, POWER, INDEX
	}

	private static boolean modified;

	public static void clear() {
		modified = false;
	}

	public static boolean isModified() {
		return modified;
	}

	private VarType varType;
	private Operator op;
	private ArrayList<ExpTreeNode> sons = new ArrayList<ExpTreeNode>();
	private HashSet<VarRecord> varSet = new HashSet<VarRecord>();
	private boolean constant;
	private String str;
	private boolean ignore;
	private VarRecord accV = null;

	public ExpTreeNode(Operator op, ExpTreeNode... sons) {
		this.op = op;
		for (ExpTreeNode son : sons)
			add(son);
	}

	public ExpTreeNode(VarRecord var) {
		this.op = Operator.NONE;
		varType = var.getVarType();
		varSet.add(var);
		str = var.getName();
	}

	public ExpTreeNode(VarType varType, String s) {
		this.op = Operator.NONE;
		this.varType = varType;
		constant = true;
		str = s;
	}

	public String toString() {
		if (op == Operator.NONE)
			return str;
		String s = op.name() + " ( ";
		for (ExpTreeNode node : sons)
			s += node + " ";
		s += ")";
		return s;
	}

	public void add(ExpTreeNode son) {
		if (son == null)
			son = new ExpTreeNode(Operator.NONE);
		sons.add(son);
		varSet.addAll(son.varSet);
	}

	public Operator getOp() {
		return op;
	}

	public ArrayList<ExpTreeNode> getSons() {
		return sons;
	}

	public VarType getVarType() {
		return varType;
	}

	public HashSet<VarRecord> getVarSet() {
		return varSet;
	}

	public String getStr() {
		return str;
	}

	public boolean useless() {
		return constant || op == Operator.NONE;
	}

	public void combine(VarType t) throws Exception {
		try {
			if (!VarType.contain(t, varType)) {
				modified = true;
				varType = VarType.combine(t, varType);
			}
		} catch (Exception e) {
			throw new Exception(
					"Expression tree " + this + " should be of type \"" + t + "\" instead of \"" + varType + "\"");
		}
	}

	public VarType calculate(Operator op, VarType t1, VarType t2, int pos) throws Exception {
		String pos1, pos2;
		if (pos == 0)
			pos1 = "ARGU1";
		else
			pos1 = "RET";
		if (pos == 2)
			pos2 = "ARGU1";
		else
			pos2 = "ARGU2";
		if (t1 == null)
			switch (op) {
			case NOT:
			case AND:
			case OR:
			case XOR:
				return new VarTypeBool().calculate(op, t2, pos);
			case EQUAL:
			case NOTEQUAL:
			case GREATER:
			case LESS:
			case GREATEREQUAL:
			case LESSEQUAL:
				if (pos == 0)
					return new VarTypeBool();
				else if (t2 == null)
					return null;
				else if (t2 instanceof VarTypeSet)
					return t2.rawType().calculate(op, t2, pos);
				else
					return new VarTypeReal().calculate(op, t2, pos);
			case IN:
				switch (pos) {
				case 0:
					return new VarTypeBool();
				case 1:
					if (t2 == null)
						return null;
					return ((VarTypeSet) t2).getSubType().rawType();
				case 2:
					return new VarTypeSet(t2).rawType();
				}
			case ADD:
			case SUBTRACT:
				if (t2 == null)
					return null;
				if (t2 instanceof VarTypeSet)
					return t2.rawType();
				return new VarTypeReal();
			case MULTIPLY:
			case DIVIDE:
			case POWER:
				return new VarTypeReal();
			case MODULO:
			case INTDIVIDE:
				return new VarTypeInt();
			case INDEX:
				if (pos != 2)
					return null;
				break;
			default:
				return null;
			}
		try {
			if (op == Operator.INDEX)
				if (pos == 1)
					return null;
				else if (pos == 2) {
					if (t2 == null)
						return null;
					if (t2 instanceof VarTypeArray)
						return new VarTypeInt(((VarTypeArray) t2).getLowerIndex(), ((VarTypeArray) t2).getUpperIndex());
					if (t2 instanceof VarTypeTuple)
						return new VarTypeInt(BigInteger.ONE, BigInteger.valueOf(((VarTypeTuple) t2).getNum()));
					return ((VarTypeFunction) t2).getArgs();
				}
			return t1.calculate(op, t2, pos);
		} catch (Exception e) {
			e.printStackTrace();
			String t2name = "null";
			if (t2 != null)
				t2name = t2.toString();
			throw new Exception("Operator " + op + " cannot be applied for " + pos1 + " of \"" + t1 + "\" and " + pos2
					+ " of \"" + t2name + "\" in expression tree " + this);
		}
	}

	public void restrict(VarType constraint) throws Exception {
		if (op != Operator.OF)
			combine(constraint);
		if (constant)
			return;
		VarType t;
		ExpTreeNode son0 = null, son1 = null, son2 = null;
		switch (sons.size()) {
		case 3:
			son2 = sons.get(2);
		case 2:
			son1 = sons.get(1);
		case 1:
			son0 = sons.get(0);
		}
		switch (op) {
		case NONE:
			if (varSet == null)
				constant = true;
			else
				for (VarRecord var : varSet) {
					combine(var.getVarType());
					if (varType != null)
						var.setVarType(varType.clone());
				}
			break;
		case OF:
			varType = null;
			son1.restrict(null);
			combine(son0.varType);
			combine(son1.varType);
			son0.restrict(varType);
			varType = new VarTypeBool("true");
			break;
		case INT:
			if (son0 != null)
				son0.restrict(new VarTypeInt());
			if (son1 != null)
				son1.restrict(new VarTypeInt());
			combine(new VarTypeInt(sons));
			break;
		case REAL:
			if (son0 != null)
				son0.restrict(new VarTypeReal());
			if (son1 != null)
				son1.restrict(new VarTypeReal());
			combine(new VarTypeReal(sons));
			break;
		case BOOL:
			if (son0 != null)
				son0.restrict(new VarTypeBool());
			if (son1 != null)
				son1.restrict(new VarTypeBool());
			combine(new VarTypeBool(sons));
			break;
		case CHAR:
			if (son0 != null)
				son0.restrict(new VarTypeChar());
			if (son1 != null)
				son1.restrict(new VarTypeChar());
			combine(new VarTypeChar(sons));
			break;
		case ARRAY:
			if (son0 != null)
				if (varType != null)
					son0.restrict(((VarTypeArray) varType).getSubType());
				else
					son0.restrict(null);
			if (son1 != null)
				son1.restrict(new VarTypeInt());
			if (son2 != null)
				son2.restrict(new VarTypeInt());
			combine(new VarTypeArray(sons));
			break;
		case SET:
			if (son0 != null)
				if (varType != null)
					son0.restrict(((VarTypeSet) varType).getSubType());
				else
					son0.restrict(null);
			combine(new VarTypeSet(sons));
			break;
		case TUPLE:
			int len = sons.size();
			for (int i = 0; i < len; i++)
				if (varType != null)
					sons.get(i).restrict(((VarTypeTuple) varType).getSubType(i));
				else
					sons.get(i).restrict(null);
			combine(new VarTypeTuple(sons));
			break;
		case FUNCTION:
			if (varType != null)
				son0.restrict(((VarTypeFunction) varType).getArgs());
			else
				son0.restrict(null);
			if (varType != null) {
				VarType ret = ((VarTypeFunction) varType).getRet();
				son1.restrict(ret);
				son2.restrict(ret);
			} else {
				son1.restrict(null);
				son2.restrict(null);
			}
			combine(new VarTypeFunction(sons));
			break;
		case MINIMIZE:
		case MAXIMIZE:
			son0.restrict(null);
			combine(son0.varType);
			break;
		case SUMMATION:
		case PRODUCT:
		case COUNT:
		case MAX:
		case MIN:
			t = son0.varType;
			if (t == null || varType == null)
				son0.restrict(null);
			else if (t instanceof VarTypeArray)
				son0.restrict(new VarTypeArray(varType.rawType()));
			else if (t instanceof VarTypeSet)
				son0.restrict(new VarTypeSet(varType.rawType()));
			VarType aggr = null;
			try {
				aggr = son0.varType.aggregate(op);
			} catch (Exception e) {
				String msg = "Expression tree " + son0 + " should be of type \" ARRAY/SET of INT/REAL \"";
				if (son0.varType != null)
					msg = msg + " instead of \" " + son0.varType + " \"";
				throw new Exception(msg);
			}
			combine(aggr);
			break;
		case FORALL:
			son0.restrict(null);
			son1.restrict(new VarTypeBool("true"));
			son2.restrict(varType);
			combine(son2.varType);
			break;
		case EXISTS:
			son0.restrict(null);
			son1.restrict(new VarTypeBool("true"));
			combine(new VarTypeBool());
			break;
		case IF:
			son0.restrict(new VarTypeBool());
			son1.restrict(varType);
			combine(son1.varType);
			if (son2 != null) {
				son2.restrict(varType);
				combine(son2.varType);
			}
			break;
		case ALLDIFF:
			t = son0.varType;
			if (t == null || t instanceof VarTypeArray)
				son0.restrict(null);
			else {
				String msg = "Expression tree " + son0 + " should be of type \" ARRAY/SET \"";
				if (son0.varType != null)
					msg = msg + " instead of \" " + son0.varType + " \"";
				throw new Exception(msg);
			}
			combine(new VarTypeBool());
			break;
		case NOT:
			son0.restrict(calculate(op, varType, null, 1));
			combine(calculate(op, son0.varType, null, 0));
			break;
		case AND:
		case OR:
		case XOR:
		case EQUAL:
		case NOTEQUAL:
		case GREATER:
		case LESS:
		case GREATEREQUAL:
		case LESSEQUAL:
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case DIVIDE:
		case INTDIVIDE:
		case MODULO:
		case POWER:
		case INDEX:
			if (son0.getStr() == null || !son0.getStr().startsWith("_sum") || son1.getOp() == Operator.SUMMATION) {
				son0.restrict(calculate(op, varType, son1.varType, 1));
				son1.restrict(calculate(op, varType, son0.varType, 2));
				combine(calculate(op, son0.varType, son1.varType, 0));
			}
			break;
		case IN:
			son0.restrict(calculate(op, varType, son1.varType, 1));
			son1.restrict(calculate(op, varType, son0.varType, 2));
			combine(calculate(op, son1.varType, son0.varType, 0));
			break;
		}
		if (op != Operator.NONE && op != Operator.OF) {
			constant = true;
			for (ExpTreeNode son : sons)
				constant &= son.constant;
		}
	}

	public void extractDep(HashSet<VarRecord> vars, ExpTreeNode exp) {
		vars.removeIf(var -> var.getVarClass() != VarClass.INDEPENDENT);
		ExpTreeNode son0 = null, son1 = null;
		switch (sons.size()) {
		case 3:
		case 2:
			son1 = sons.get(1);
		case 1:
			son0 = sons.get(0);
		}
		HashSet<VarRecord> tmp;
		switch (op) {
		case NONE:
			for (VarRecord var : varSet)
				var.addDepend(vars, exp);
			break;
		case NOT:
			son0.extractDep(vars, exp);
			break;
		case EQUAL:
			if (varType.getLowerBound() == BigDecimal.ONE) {
				son0.extractDep(son1.varSet, this);
				son1.extractDep(son0.varSet, this);
			}
			break;
		case NOTEQUAL:
			if (varType.getUpperBound() == BigDecimal.ZERO) {
				son0.extractDep(son1.varSet, this);
				son1.extractDep(son0.varSet, this);
			}
			break;
		case XOR:
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case DIVIDE:
		case POWER:
			tmp = new HashSet<VarRecord>(vars);
			tmp.addAll(son1.varSet);
			son0.extractDep(tmp, exp);
			tmp = new HashSet<VarRecord>(vars);
			tmp.addAll(son0.varSet);
			son1.extractDep(tmp, exp);
			break;
		default:
			break;
		}
	}

	public boolean isReady(VarRecord v) {
		if (varSet.contains(v))
			return true;
		for (VarRecord var : varSet)
			if (var.getVarClass() == VarClass.DEPENDENT && var.getDependExp().varSet.contains(v))
				return true;
		return false;
	}

	public void setAccV(VarRecord v) {
		accV = v;
	}

	public VarRecord getAccV(VarRecord v) {
		if (v == null || varSet.contains(v))
			return accV;
		return null;
	}

	public void setIgnore() {
		ignore = true;
	}

	public boolean isIgnore() {
		return ignore;
	}

}
