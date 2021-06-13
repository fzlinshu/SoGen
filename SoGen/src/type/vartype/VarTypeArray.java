package type.vartype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import generator.ExpGenerator;
import type.ExpTreeNode;
import type.ExpTreeNode.Operator;
import type.VarType;

public class VarTypeArray implements VarType {

	private VarType subType;
	private BigInteger lowerIndex;
	private BigInteger upperIndex;
	private BigInteger num;
	private ExpTreeNode lowerIndexExp;
	private ExpTreeNode upperIndexExp;

	public VarTypeArray(VarTypeArray t) {
		if (t.subType != null)
			subType = t.subType.clone();
		lowerIndex = t.lowerIndex;
		upperIndex = t.upperIndex;
		num = t.num;
		lowerIndexExp = t.lowerIndexExp;
		upperIndexExp = t.upperIndexExp;
	}

	public VarTypeArray(VarType subType, BigInteger lowerIndex, BigInteger upperIndex) {
		this.subType = subType;
		this.lowerIndex = lowerIndex;
		this.upperIndex = upperIndex;
	}

	public VarTypeArray(String str) {
		subType = new VarTypeChar();
		lowerIndex = BigInteger.ONE;
		upperIndex = BigInteger.valueOf(str.length());
	}

	public VarTypeArray(VarType subType) {
		this.subType = subType;
	}

	public VarTypeArray(ArrayList<ExpTreeNode> sons) {
		subType = null;
		if (sons.get(0) != null)
			subType = sons.get(0).getVarType();
		if (sons.size() == 1)
			num = sons.get(0).getSons().get(0).getVarType().range();
		else {
			VarType li = null;
			lowerIndexExp = sons.get(1);
			if (lowerIndexExp != null)
				li = lowerIndexExp.getVarType();
			VarType ui = null;
			upperIndexExp = sons.get(2);
			if (upperIndexExp != null)
				ui = upperIndexExp.getVarType();
			if (li != null && li.getLowerBound() != null)
				lowerIndex = li.getLowerBound().toBigInteger();
			if (ui != null && ui.getUpperBound() != null)
				upperIndex = ui.getUpperBound().toBigInteger();
		}
	}

	@Override
	public String getName() {
		return subType.getName() + "*";
	}

	@Override
	public String getDef(String varName) {
		BigInteger size = num;
		if (size == null)
			size = upperIndex.subtract(lowerIndex).add(BigInteger.ONE);
		return subType.getDef(varName + "[" + size + "]");
	}

	@Override
	public VarType rawType() {
		return new VarTypeArray(subType.rawType());
	}

	@Override
	public VarType clone() {
		return new VarTypeArray(this);
	}

	@Override
	public String toString() {
		if (num != null)
			return "ARRAY[ <=" + num + "] of " + subType;
		String range = "";
		if (lowerIndex == null)
			range += "?";
		else
			range += lowerIndex;
		range += "~";
		if (upperIndex == null)
			range += "?";
		else
			range += upperIndex;
		return "ARRAY[" + range + "] of " + subType;
	}

	@Override
	public BigDecimal getLowerBound() {
		return subType.getLowerBound();
	}

	@Override
	public BigDecimal getUpperBound() {
		return subType.getUpperBound();
	}

	@Override
	public ExpTreeNode getLowerBoundExp() {
		return subType.getLowerBoundExp();
	}

	@Override
	public ExpTreeNode getUpperBoundExp() {
		return subType.getUpperBoundExp();
	}

	@Override
	public BigInteger getLowerIndex() {
		if (lowerIndex == null)
			return null;
		return lowerIndex;
	}

	@Override
	public BigInteger getUpperIndex() {
		if (upperIndex == null)
			return null;
		return upperIndex;
	}

	@Override
	public BigInteger getMemSize() {
		return subType.getMemSize().multiply(upperIndex.subtract(lowerIndex).add(BigInteger.ONE));
	}

	@Override
	public BigInteger getSize() {
		return subType.getSize().multiply(upperIndex.subtract(lowerIndex).add(BigInteger.ONE));
	}

	@Override
	public String getTotStr() throws Exception {
		return new ExpGenerator(null).getCalcs(null, upperIndexExp, null).get(0) + " - "
				+ new ExpGenerator(null).getCalcs(null, lowerIndexExp, null).get(0) + " + 1";
	}

	@Override
	public BigInteger range() {
		if (subType == null || lowerIndex == null || upperIndex == null)
			return null;
		BigInteger range = subType.range();
		if (range == null)
			return null;
		return subType.range().multiply(getLength());
	}

	@Override
	public VarType aggregate(Operator op) throws Exception {
		BigInteger length = null;
		if (lowerIndex != null && upperIndex != null)
			length = upperIndex.subtract(lowerIndex).add(BigInteger.ONE);
		if (op == Operator.COUNT) {
			if (num != null)
				return new VarTypeInt(BigInteger.ZERO, num);
			return new VarTypeInt(length, length);
		}
		if (subType == null)
			return null;
		if (subType instanceof VarTypeInt) {
			if (op == Operator.MAX || op == Operator.MIN)
				return subType;
			BigDecimal lb = subType.getLowerBound();
			BigDecimal ub = subType.getUpperBound();
			BigInteger lbound = null;
			BigInteger ubound = null;
			if (op == Operator.SUMMATION) {
				if (lb != null)
					if (length != null)
						lbound = lb.toBigInteger().multiply(length);
					else if (lb.signum() >= 0)
						lbound = BigInteger.ZERO;
					else if (num != null)
						lbound = lb.toBigInteger().multiply(num);
				if (ub != null)
					if (length != null)
						ubound = ub.toBigInteger().multiply(length);
					else if (ub.signum() <= 0)
						ubound = BigInteger.ZERO;
					else if (num != null)
						ubound = ub.toBigInteger().multiply(num);
			} else {
				int l = 0;
				try {
					if (length != null)
						l = length.intValueExact();
					else if (num != null)
						l = num.intValueExact();
				} catch (ArithmeticException e) {
				}
				if (l == 0) {
					if (lb == null)
						return new VarTypeInt();
					int sign = lb.signum();
					if (sign < 0)
						return new VarTypeInt();
					if (sign == 0)
						return new VarTypeInt(BigInteger.ZERO, null);
					return new VarTypeInt(BigInteger.ONE, null);
				}
				if (lb == null)
					return new VarTypeInt();
				else if (lb.signum() < 0)
					if (ub == null)
						return new VarTypeInt();
					else {
						ubound = lb.abs().max(ub.abs()).toBigInteger().pow(l);
						lbound = ubound.negate();
					}
				else {
					if (num != null)
						lbound = BigInteger.ONE;
					else
						lbound = lb.toBigInteger().pow(l);
					if (ub != null)
						ubound = ub.toBigInteger().pow(l);
				}
			}
			return new VarTypeInt(lbound, ubound);
		} else if (subType instanceof VarTypeReal) {
			if (op == Operator.MAX || op == Operator.MIN)
				return subType;
			BigDecimal lb = subType.getLowerBound();
			BigDecimal ub = subType.getUpperBound();
			BigDecimal lbound = null;
			BigDecimal ubound = null;
			if (op == Operator.SUMMATION) {
				if (lb != null)
					if (length != null)
						lbound = lb.multiply(new BigDecimal(length));
					else if (lb.signum() >= 0)
						lbound = BigDecimal.ZERO;
					else if (num != null)
						lbound = lb.multiply(new BigDecimal(num));
				if (ub != null)
					if (length != null)
						ubound = ub.multiply(new BigDecimal(length));
					else if (ub.signum() <= 0)
						ubound = BigDecimal.ZERO;
					else if (num != null)
						ubound = ub.multiply(new BigDecimal(num));
			} else {
				int l = 0;
				try {
					if (length != null)
						l = length.intValueExact();
					else if (num != null)
						l = num.intValueExact();
				} catch (ArithmeticException e) {
				}
				if (l == 0) {
					if (lb == null)
						return new VarTypeReal();
					int sign = lb.signum();
					if (sign < 0)
						return new VarTypeReal();
					return new VarTypeReal(BigDecimal.ZERO, null);
				}
				if (lb == null)
					return new VarTypeReal();
				else if (lb.signum() < 0)
					if (ub == null)
						return new VarTypeReal();
					else {
						ubound = lb.abs().max(ub.abs()).pow(l);
						lbound = ubound.negate();
					}
				else {
					if (num != null)
						lbound = BigDecimal.ONE;
					else
						lbound = lb.pow(l);
					if (ub != null)
						ubound = ub.pow(l);
				}
			}
			return new VarTypeReal(lbound, ubound);
		}
		throw new Exception();
	}

	@Override
	public VarType calculate(Operator op, VarType varType, int pos) throws Exception {
		switch (op) {
		case INDEX:
			return subType.clone();
		default:
			throw new Exception();
		}
	}

	@Override
	public VarType extend(VarType varType) throws Exception {
		VarTypeArray t = (VarTypeArray) varType;
		BigInteger li, ui;
		if (lowerIndex == null || t.lowerIndex == null)
			li = null;
		else
			li = lowerIndex.min(t.lowerIndex);
		if (upperIndex == null || t.upperIndex == null)
			ui = null;
		else
			ui = upperIndex.max(t.upperIndex);
		return new VarTypeArray(VarType.extend(subType, t.subType), li, ui);
	}

	@Override
	public boolean contain(VarType t) throws Exception {
		if (t == null)
			return false;
		if (!(t instanceof VarTypeArray))
			throw new Exception();
		VarTypeArray tt = (VarTypeArray) t;
		return (num == null && tt.num == null
				&& (lowerIndex == null || tt.lowerIndex != null && lowerIndex.compareTo(tt.lowerIndex) <= 0)
				&& (upperIndex == null || tt.upperIndex != null && upperIndex.compareTo(tt.upperIndex) >= 0)
				|| num != null && tt.num != null && num.compareTo(tt.num) >= 0)
				&& (VarType.contain(subType, tt.subType));
	}

	@Override
	public VarType combine(VarType t) throws Exception {
		if (t == null)
			return this.clone();
		VarTypeArray tt = (VarTypeArray) t.clone();
		if (num != null && (tt.num == null || tt.num.compareTo(num) > 0))
			tt.num = num;
		else {
			if (tt.lowerIndex == null || lowerIndex != null && tt.lowerIndex.compareTo(lowerIndex) <= 0)
				tt.lowerIndex = lowerIndex;
			if (tt.upperIndex == null || upperIndex != null && tt.upperIndex.compareTo(upperIndex) >= 0)
				tt.upperIndex = upperIndex;
			if (tt.lowerIndex != null && tt.upperIndex != null && tt.lowerIndex.compareTo(tt.upperIndex) > 0)
				throw new Exception();
		}
		tt.subType = VarType.combine(tt.subType, subType);
		return tt;
	}

	public VarType getSubType() {
		return subType;
	}

	public BigInteger getLength() {
		return upperIndex.subtract(lowerIndex).add(BigInteger.ONE);
	}

	public ExpTreeNode getLowerIndexExp() {
		return lowerIndexExp;
	}

	public ExpTreeNode getUpperIndexExp() {
		return upperIndexExp;
	}

}
