package type.vartype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import type.ExpTreeNode;
import type.ExpTreeNode.Operator;
import type.VarType;

public class VarTypeSet implements VarType {

	private VarType subType;

	public VarTypeSet() {
	}

	public VarTypeSet(VarTypeSet t) {
		if (t.subType != null)
			subType = t.subType.clone();
	}

	public VarTypeSet(VarType subType) {
		this.subType = subType;
	}

	public VarTypeSet(ArrayList<ExpTreeNode> sons) {
		subType = null;
		if (sons.get(0) != null)
			subType = sons.get(0).getVarType();
	}

	@Override
	public String getName() {
		return subType.getName() + "*";
	}

	@Override
	public String getDef(String varName) {
		return subType.getDef(varName + "[" + subType.range() + "]");
	}

	@Override
	public VarType rawType() {
		if (subType == null)
			return new VarTypeSet();
		return new VarTypeSet(subType.rawType());
	}

	@Override
	public VarType clone() {
		return new VarTypeSet(this);
	}

	@Override
	public String toString() {
		return "SET of " + subType;
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
		return subType.getLowerBound().toBigInteger();
	}

	@Override
	public BigInteger getUpperIndex() {
		return subType.getUpperBound().toBigInteger();
	}

	@Override
	public BigInteger getMemSize() {
		return subType.getMemSize().multiply(subType.range());
	}

	@Override
	public BigInteger getSize() {
		return subType.range();
	}

	@Override
	public String getTotStr() {
		return subType.getUpperBoundExp().getStr() + " - " + subType.getLowerBoundExp().getStr() + " + 1";
	}

	@Override
	public BigInteger range() {
		if (subType == null)
			return null;
		BigInteger range = subType.range();
		if (range == null)
			return null;
		try {
			return BigInteger.valueOf(2).pow(range.intValueExact());
		} catch (ArithmeticException e) {
			return null;
		}
	}

	@Override
	public VarType aggregate(Operator op) throws Exception {
		if (subType == null)
			return null;
		BigInteger length = subType.range();
		if (op == Operator.COUNT)
			return new VarTypeInt(BigInteger.ZERO, length);
		if (subType instanceof VarTypeInt) {
			if (op == Operator.MAX || op == Operator.MIN)
				return subType;
			BigDecimal lb = subType.getLowerBound();
			BigDecimal ub = subType.getUpperBound();
			BigInteger lbound = null;
			BigInteger ubound = null;
			if (op == Operator.SUMMATION) {
				if (lb != null)
					if (lb.signum() >= 0)
						lbound = BigInteger.ZERO;
					else if (length != null)
						lbound = lb.toBigInteger().multiply(length);
				if (ub != null)
					if (ub.signum() <= 0)
						ubound = BigInteger.ZERO;
					else if (length != null)
						ubound = ub.toBigInteger().multiply(length);
			} else {
				int l = 0;
				try {
					l = length.intValueExact();
				} catch (ArithmeticException e) {
					if (lb == null)
						return new VarTypeInt();
					int sign = lb.signum();
					if (sign < 0)
						return new VarTypeReal();
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
					lbound = lb.toBigInteger().pow(l);
					if (ub != null)
						ubound = ub.toBigInteger().pow(l);
				}
			}
			if (lbound != null)
				lbound = lbound.min(BigInteger.ONE);
			if (ubound != null)
				ubound = ubound.max(BigInteger.ONE);
			return new VarTypeInt(lbound, ubound);
		} else if (subType instanceof VarTypeReal) {
			if (op == Operator.MAX || op == Operator.MIN)
				return subType;
			BigDecimal len = null;
			if (length != null)
				len = new BigDecimal(length);
			BigDecimal lbound = subType.getLowerBound();
			BigDecimal ubound = subType.getUpperBound();
			if (op == Operator.SUMMATION) {
				if (lbound != null)
					if (lbound.signum() >= 0)
						lbound = BigDecimal.ZERO;
					else if (len != null)
						lbound = lbound.multiply(len);
				if (ubound != null)
					if (ubound.signum() <= 0)
						ubound = BigDecimal.ZERO;
					else if (len != null)
						ubound = ubound.multiply(len);
			} else {
				int l = 0;
				try {
					l = length.intValueExact();
				} catch (ArithmeticException e) {
					if (lbound == null)
						return new VarTypeReal();
					int sign = lbound.signum();
					if (sign < 0)
						return new VarTypeReal();
					if (sign == 0)
						return new VarTypeReal(BigDecimal.ZERO, null);
					return new VarTypeReal(BigDecimal.ONE, null);
				}
				if (lbound == null)
					return new VarTypeReal();
				else if (lbound.signum() < 0)
					if (ubound == null)
						return new VarTypeReal();
					else {
						ubound = lbound.abs().max(ubound.abs()).pow(l);
						lbound = ubound.negate();
					}
				else {
					lbound = lbound.pow(l);
					if (ubound != null)
						ubound = ubound.pow(l);
				}
			}
			if (lbound != null)
				lbound = lbound.min(BigDecimal.ONE);
			if (ubound != null)
				ubound = ubound.max(BigDecimal.ONE);
			return new VarTypeReal(lbound, ubound);
		}
		throw new Exception();
	}

	@Override
	public VarType calculate(Operator op, VarType varType, int pos) throws Exception {
		switch (op) {
		case EQUAL:
		case NOTEQUAL:
		case GREATER:
		case LESS:
		case GREATEREQUAL:
		case LESSEQUAL:
			if (pos != 0)
				throw new Exception();
			return new VarTypeBool();
		case ADD:
			if (pos != 0 || subType == null)
				return this.clone();
			return new VarTypeSet(VarType.extend(subType, ((VarTypeSet) varType).subType));
		case SUBTRACT:
			if (pos != 1 || subType == null)
				return this.clone();
			return new VarTypeSet(VarType.extend(subType, ((VarTypeSet) varType).subType));
		case IN:
			if (pos != 0)
				throw new Exception();
			if (VarType.contain(subType, varType))
				return new VarTypeBool("true");
			return new VarTypeBool();
		default:
			throw new Exception();
		}
	}

	@Override
	public VarType extend(VarType varType) throws Exception {
		return new VarTypeSet(VarType.extend(subType, ((VarTypeSet) varType).subType));
	}

	@Override
	public boolean contain(VarType t) throws Exception {
		if (t == null)
			return false;
		if (!(t instanceof VarTypeSet))
			throw new Exception();
		VarTypeSet tt = (VarTypeSet) t;
		return (VarType.contain(subType, tt.subType));
	}

	@Override
	public VarType combine(VarType t) throws Exception {
		if (t == null)
			return this.clone();
		VarTypeSet tt = (VarTypeSet) t.clone();
		tt.subType = VarType.combine(tt.subType, subType);
		return tt;
	}

	public VarType getSubType() {
		return subType;
	}

}
