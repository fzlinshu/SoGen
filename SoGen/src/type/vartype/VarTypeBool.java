package type.vartype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import type.ExpTreeNode;
import type.ExpTreeNode.Operator;
import type.VarType;

public class VarTypeBool implements VarType {

	private int lowerBound = 0;
	private int upperBound = 1;

	public VarTypeBool() {
	}

	public VarTypeBool(VarTypeBool t) {
		lowerBound = t.lowerBound;
		upperBound = t.upperBound;
	}

	public VarTypeBool(String str) {
		if (str == "false")
			lowerBound = 0;
		else
			lowerBound = 1;
		upperBound = lowerBound;
	}

	public VarTypeBool(int lowerBound, int upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public VarTypeBool(ArrayList<ExpTreeNode> sons) {
		VarType lb = sons.get(0).getVarType();
		VarType ub = sons.get(1).getVarType();
		if (lb != null && lb.getLowerBound() != null)
			lowerBound = lb.getLowerBound().intValue();
		if (ub != null && ub.getUpperBound() != null)
			upperBound = ub.getUpperBound().intValue();
	}

	@Override
	public String getName() {
		return "bool";
	}

	@Override
	public String getDef(String varName) {
		return "bool " + varName;
	}

	@Override
	public VarType rawType() {
		return new VarTypeBool();
	}

	@Override
	public VarType clone() {
		return new VarTypeBool(this);
	}

	@Override
	public String toString() {
		if (lowerBound == 1)
			return "BOOL(true)";
		if (upperBound == 0)
			return "BOOL(false)";
		return "BOOL";
	}

	@Override
	public BigDecimal getLowerBound() {
		return BigDecimal.valueOf(lowerBound);
	}

	@Override
	public BigDecimal getUpperBound() {
		return BigDecimal.valueOf(upperBound);
	}

	@Override
	public ExpTreeNode getLowerBoundExp() {
		return new ExpTreeNode(new VarTypeBool(), Integer.toString(lowerBound));
	}

	@Override
	public ExpTreeNode getUpperBoundExp() {
		return new ExpTreeNode(new VarTypeBool(), Integer.toString(upperBound));
	}

	@Override
	public BigInteger getLowerIndex() {
		return null;
	}

	@Override
	public BigInteger getUpperIndex() {
		return null;
	}

	@Override
	public BigInteger getMemSize() {
		return BigInteger.ONE;
	}

	@Override
	public BigInteger getSize() {
		return BigInteger.ONE;
	}

	@Override
	public String getTotStr() {
		return null;
	}

	@Override
	public BigInteger range() {
		return BigInteger.valueOf(upperBound - lowerBound);
	}

	@Override
	public VarType aggregate(Operator op) throws Exception {
		throw new Exception();
	}

	@Override
	public VarType calculate(Operator op, VarType varType, int pos) throws Exception {
		switch (op) {
		case NOT:
			return new VarTypeBool(1 - upperBound, 1 - lowerBound);
		case AND:
			if (varType == null)
				varType = new VarTypeBool();
			if (pos == 0)
				return new VarTypeBool(Math.min(lowerBound, varType.getLowerBound().intValue()),
						Math.min(upperBound, varType.getUpperBound().intValue()));
			else
				return new VarTypeBool(lowerBound, Math.max(upperBound, 1 - varType.getLowerBound().intValue()));
		case OR:
			if (varType == null)
				varType = new VarTypeBool();
			if (pos == 0)
				return new VarTypeBool(Math.max(lowerBound, varType.getLowerBound().intValue()),
						Math.max(upperBound, varType.getUpperBound().intValue()));
			else
				return new VarTypeBool(Math.min(lowerBound, 1 - varType.getUpperBound().intValue()), upperBound);
		case XOR:
			return new VarTypeBool();
		case EQUAL:
			if (pos == 0) {
				if (varType == null || (varType instanceof VarTypeBool))
					return new VarTypeBool();
				throw new Exception();
			}
			if (lowerBound == 0 || varType == null)
				return null;
			return varType.clone();
		case NOTEQUAL:
			if (pos == 0)
				throw new Exception();
			if (upperBound == 1 || varType == null)
				return null;
			return varType.clone();
		case GREATER:
		case GREATEREQUAL:
			if (pos == 0)
				throw new Exception();
			if (varType == null)
				return null;
			if (lowerBound == 0 && upperBound == 1)
				return varType.rawType();
			if (upperBound == 0)
				return calculate(Operator.NOT, null, 0).calculate(Operator.LESS, varType, pos);
			if (pos == 2)
				return calculate(Operator.LESS, varType, 1);
			if (varType instanceof VarTypeSet)
				return varType.rawType();
			return new VarTypeReal(varType.getLowerBound(), null);
		case LESS:
		case LESSEQUAL:
			if (pos == 0)
				throw new Exception();
			if (varType == null)
				return null;
			if (lowerBound == 0 && upperBound == 1)
				return varType.rawType();
			if (upperBound == 0)
				return calculate(Operator.NOT, null, 0).calculate(Operator.GREATER, varType, pos);
			if (pos == 2)
				return calculate(Operator.GREATER, varType, 1);
			if (varType instanceof VarTypeSet)
				return new VarTypeSet(((VarTypeSet) varType).getSubType());
			return new VarTypeReal(null, varType.getUpperBound());
		case IN:
			if (pos == 0)
				throw new Exception();
			if (lowerBound == 1)
				if (pos == 2)
					return new VarTypeSet(varType).rawType();
				else if (pos == 1 && varType instanceof VarTypeSet)
					return ((VarTypeSet) varType).getSubType();
			return null;
		default:
			throw new Exception();
		}
	}

	@Override
	public VarType extend(VarType varType) throws Exception {
		VarTypeBool t = (VarTypeBool) varType;
		return new VarTypeBool(Math.min(lowerBound, t.lowerBound), Math.max(upperBound, t.upperBound));
	}

	@Override
	public boolean contain(VarType t) throws Exception {
		if (t == null)
			return false;
		if (!(t instanceof VarTypeBool))
			throw new Exception();
		VarTypeBool tt = (VarTypeBool) t;
		return lowerBound <= tt.lowerBound && upperBound >= tt.upperBound;
	}

	@Override
	public VarType combine(VarType t) throws Exception {
		if (t == null)
			return this.clone();
		VarTypeBool tt = (VarTypeBool) t.clone();
		if (tt.lowerBound < lowerBound)
			tt.lowerBound = lowerBound;
		if (tt.upperBound > upperBound)
			tt.upperBound = upperBound;
		if (tt.lowerBound > tt.upperBound)
			throw new Exception();
		return tt;
	}

}
