package type.vartype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import type.ExpTreeNode;
import type.ExpTreeNode.Operator;
import type.VarType;

public class VarTypeChar implements VarType {

	private char lowerBound = 0;
	private char upperBound = 255;

	public VarTypeChar() {
	}

	public VarTypeChar(VarTypeChar t) {
		lowerBound = t.lowerBound;
		upperBound = t.upperBound;
	}

	public VarTypeChar(String str) {
		lowerBound = str.charAt(0);
		upperBound = str.charAt(0);
	}

	public VarTypeChar(ArrayList<ExpTreeNode> sons) {
		VarType lb = sons.get(0).getVarType();
		VarType ub = sons.get(1).getVarType();
		if (lb != null && lb.getLowerBound() != null)
			lowerBound = (char) lb.getLowerBound().intValue();
		if (ub != null && ub.getUpperBound() != null)
			upperBound = (char) ub.getUpperBound().intValue();
	}

	@Override
	public String getName() {
		return "char";
	}

	@Override
	public String getDef(String varName) {
		return "char " + varName;
	}

	@Override
	public VarType rawType() {
		return new VarTypeChar();
	}

	@Override
	public VarType clone() {
		return new VarTypeChar(this);
	}

	@Override
	public String toString() {
		return "CHAR";
	}

	@Override
	public BigDecimal getLowerBound() {
		return null;
	}

	@Override
	public BigDecimal getUpperBound() {
		return null;
	}

	@Override
	public ExpTreeNode getLowerBoundExp() {
		return new ExpTreeNode(new VarTypeChar(), Integer.toString(lowerBound));
	}

	@Override
	public ExpTreeNode getUpperBoundExp() {
		return new ExpTreeNode(new VarTypeChar(), Integer.toString(upperBound));
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
		return null;
	}

	@Override
	public VarType aggregate(Operator op) throws Exception {
		throw new Exception();
	}

	@Override
	public VarType calculate(Operator op, VarType varType, int pos) throws Exception {
		switch (op) {
		default:
			throw new Exception();
		}
	}

	@Override
	public VarType extend(VarType varType) throws Exception {
		VarTypeChar t = (VarTypeChar) varType;
		return new VarTypeBool(Math.min(lowerBound, t.lowerBound), Math.max(upperBound, t.upperBound));
	}

	@Override
	public boolean contain(VarType t) throws Exception {
		if (t == null)
			return false;
		if (!(t instanceof VarTypeChar))
			throw new Exception();
		VarTypeChar tt = (VarTypeChar) t;
		return lowerBound <= tt.lowerBound && upperBound >= tt.upperBound;
	}

	@Override
	public VarType combine(VarType t) throws Exception {
		if (t == null)
			return this.clone();
		VarTypeChar tt = (VarTypeChar) t.clone();
		if (tt.lowerBound < lowerBound)
			tt.lowerBound = lowerBound;
		if (tt.upperBound > upperBound)
			tt.upperBound = upperBound;
		if (tt.lowerBound > tt.upperBound)
			throw new Exception();
		return tt;
	}

}
