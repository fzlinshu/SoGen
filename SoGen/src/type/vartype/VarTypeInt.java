package type.vartype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;

import type.ExpTreeNode;
import type.ExpTreeNode.Operator;
import type.VarType;

public class VarTypeInt implements VarType {

	private BigInteger lowerBound, upperBound;

	public VarTypeInt() {
	}

	public VarTypeInt(VarTypeInt t) {
		lowerBound = t.lowerBound;
		upperBound = t.upperBound;
	}

	public VarTypeInt(VarTypeReal t) {
		BigDecimal lb = t.getLowerBound();
		if (lb != null)
			lowerBound = lb.setScale(0, RoundingMode.UP).toBigInteger();
		BigDecimal ub = t.getUpperBound();
		if (ub != null)
			upperBound = ub.toBigInteger();
	}

	public VarTypeInt(String str) {
		lowerBound = new BigInteger(str);
		upperBound = new BigInteger(str);
	}

	public VarTypeInt(BigInteger lowerBound, BigInteger upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public VarTypeInt(ArrayList<ExpTreeNode> sons) {
		VarType lb = null;
		if (sons.get(0) != null)
			lb = sons.get(0).getVarType();
		VarType ub = null;
		if (sons.get(1) != null)
			ub = sons.get(1).getVarType();
		if (lb != null && lb.getLowerBound() != null)
			lowerBound = lb.getLowerBound().toBigInteger();
		if (ub != null && ub.getUpperBound() != null)
			upperBound = ub.getUpperBound().toBigInteger();
	}

	@Override
	public String getName() {
		return "int";
	}

	@Override
	public String getDef(String varName) {
		return "int " + varName;
	}

	@Override
	public VarType rawType() {
		return new VarTypeInt();
	}

	@Override
	public VarType clone() {
		return new VarTypeInt(this);
	}

	@Override
	public String toString() {
		String range = "";
		if (lowerBound == null)
			range = "?";
		else
			range = lowerBound.toString();
		range += ",";
		if (upperBound == null)
			range += "?";
		else
			range += upperBound;
		return "INT[" + range + "]";
	}

	@Override
	public BigDecimal getLowerBound() {
		if (lowerBound == null)
			return null;
		return new BigDecimal(lowerBound);
	}

	@Override
	public BigDecimal getUpperBound() {
		if (upperBound == null)
			return null;
		return new BigDecimal(upperBound);
	}

	@Override
	public ExpTreeNode getLowerBoundExp() {
		return new ExpTreeNode(new VarTypeInt(), lowerBound.toString());
	}

	@Override
	public ExpTreeNode getUpperBoundExp() {
		return new ExpTreeNode(new VarTypeInt(), upperBound.toString());
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
		return BigInteger.valueOf(4);
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
		if (upperBound == null || lowerBound == null)
			return null;
		return upperBound.subtract(lowerBound).add(BigInteger.ONE);
	}

	@Override
	public VarType aggregate(Operator op) throws Exception {
		throw new Exception();
	}

	@Override
	public VarType calculate(Operator op, VarType varType, int pos) throws Exception {
		VarTypeInt t;
		BigInteger lb, ub, tmp;
		Boolean linf, uinf;
		switch (op) {
		case EQUAL:
		case NOTEQUAL:
		case GREATER:
		case LESS:
		case GREATEREQUAL:
		case LESSEQUAL:
			return new VarTypeReal(this).calculate(op, varType, pos);
		case ADD:
			if (varType == null)
				return new VarTypeReal();
			if (pos != 0)
				return calculate(Operator.SUBTRACT, varType, 0);
			if (varType instanceof VarTypeReal)
				return new VarTypeReal(this).calculate(op, varType, pos);
			t = (VarTypeInt) varType;
			if (lowerBound == null || t.lowerBound == null)
				lb = null;
			else
				lb = lowerBound.add(t.lowerBound);
			if (upperBound == null || t.upperBound == null)
				ub = null;
			else
				ub = upperBound.add(t.upperBound);
			return new VarTypeInt(lb, ub);
		case SUBTRACT:
			if (varType == null)
				return new VarTypeReal();
			if (pos == 1)
				return calculate(Operator.ADD, varType, 0);
			if (pos == 2)
				return varType.calculate(Operator.SUBTRACT, this, 0);
			if (varType instanceof VarTypeReal)
				return new VarTypeReal(this).calculate(op, varType, pos);
			t = (VarTypeInt) varType;
			if (lowerBound == null || t.upperBound == null)
				lb = null;
			else
				lb = lowerBound.subtract(t.upperBound);
			if (upperBound == null || t.lowerBound == null)
				ub = null;
			else
				ub = upperBound.subtract(t.lowerBound);
			return new VarTypeInt(lb, ub);
		case MULTIPLY:
			if (varType == null)
				return new VarTypeReal();
			if (pos != 0)
				return calculate(Operator.DIVIDE, varType, 0);
			if (varType instanceof VarTypeReal)
				return new VarTypeReal(this).calculate(op, varType, pos);
			t = (VarTypeInt) varType;
			linf = false;
			uinf = false;
			lb = null;
			ub = null;
			if (lowerBound != null && lowerBound.signum() == 0 || upperBound != null && upperBound.signum() == 0
					|| t.lowerBound != null && t.lowerBound.signum() == 0
					|| t.upperBound != null && t.upperBound.signum() == 0) {
				lb = BigInteger.ZERO;
				ub = BigInteger.ZERO;
			}
			if (lowerBound == null) {
				if (t.lowerBound == null || t.lowerBound.signum() == -1)
					uinf = true;
				else
					linf = true;
				if (t.upperBound == null || t.upperBound.signum() == 1)
					linf = true;
				else
					uinf = true;
			} else {
				if (t.lowerBound == null)
					if (lowerBound.signum() == -1)
						uinf = true;
					else
						linf = true;
				else {
					tmp = lowerBound.multiply(t.lowerBound);
					if (!linf && (lb == null || lb.compareTo(tmp) > 0))
						lb = tmp;
					if (!uinf && (ub == null || ub.compareTo(tmp) < 0))
						ub = tmp;
				}
				if (t.upperBound == null)
					if (lowerBound.signum() == -1)
						linf = true;
					else
						uinf = true;
				else {
					tmp = lowerBound.multiply(t.upperBound);
					if (!linf && (lb == null || lb.compareTo(tmp) > 0))
						lb = tmp;
					if (!uinf && (ub == null || ub.compareTo(tmp) < 0))
						ub = tmp;
				}
			}
			if (upperBound == null) {
				if (t.lowerBound == null || t.lowerBound.signum() == -1)
					linf = true;
				else
					uinf = true;
				if (t.upperBound == null || t.upperBound.signum() == 1)
					uinf = true;
				else
					linf = true;
			} else {
				if (t.lowerBound == null)
					if (lowerBound.signum() == -1)
						linf = true;
					else
						uinf = true;
				else {
					tmp = upperBound.multiply(t.lowerBound);
					if (!linf && (lb == null || lb.compareTo(tmp) > 0))
						lb = tmp;
					if (!uinf && (ub == null || ub.compareTo(tmp) < 0))
						ub = tmp;
				}
				if (t.upperBound == null)
					if (lowerBound.signum() == -1)
						linf = true;
					else
						uinf = true;
				else {
					tmp = upperBound.multiply(t.upperBound);
					if (!linf && (lb == null || lb.compareTo(tmp) > 0))
						lb = tmp;
					if (!uinf && (ub == null || ub.compareTo(tmp) < 0))
						ub = tmp;
				}
			}
			if (linf)
				lb = null;
			if (uinf)
				ub = null;
			return new VarTypeInt(lb, ub);
		case DIVIDE:
			if (varType == null)
				return new VarTypeReal();
			if (pos == 1)
				return calculate(Operator.MULTIPLY, varType, 0);
			if (pos == 2)
				return varType.calculate(Operator.DIVIDE, this, 0);
			return new VarTypeReal(this).calculate(op, varType, pos);
		case MODULO:
			if (pos != 0)
				return new VarTypeInt();
			if (varType == null)
				return new VarTypeInt(BigInteger.ZERO, null);
			t = (VarTypeInt) varType;
			if (t.lowerBound == null || t.upperBound == null)
				return new VarTypeInt(BigInteger.ZERO, null);
			ub = t.lowerBound.abs().max(t.upperBound.abs());
			if (ub.signum() == 0)
				throw new Exception();
			return new VarTypeInt(BigInteger.ZERO, ub.subtract(BigInteger.ONE));
		case INTDIVIDE:
			switch (pos) {
			case 0:
				VarTypeReal tr = (VarTypeReal) calculate(Operator.DIVIDE, varType, 0);
				if (tr.getLowerBound() == null)
					return new VarTypeInt(tr);
				lb = tr.getLowerBound().toBigInteger();
				if (tr.getUpperBound() == null)
					return new VarTypeInt(lb, null);
				return new VarTypeInt(lb, tr.getUpperBound().toBigInteger());
			case 1:
				return new VarTypeInt(((VarTypeInt) calculate(Operator.MULTIPLY, varType, 0)).lowerBound,
						((VarTypeInt) calculate(Operator.ADD, new VarTypeInt("1"), 0)
								.calculate(Operator.MULTIPLY, varType, 0)
								.calculate(Operator.SUBTRACT, new VarTypeInt("1"), 0)).upperBound);
			case 2:
				return new VarTypeInt();
			}
		case POWER:
			if (pos != 0 || lowerBound == null || lowerBound.signum() < 0)
				return new VarTypeReal();
			int l, u;
			try {
				l = varType.getLowerBound().intValue();
				u = varType.getUpperBound().intValue();
				if (upperBound == null)
					ub = null;
				else
					ub = upperBound.pow(u);
				return new VarTypeInt(lowerBound.pow(l), ub);
			} catch (Exception e) {
				return new VarTypeReal();
			}
		default:
			throw new Exception();
		}
	}

	@Override
	public VarType extend(VarType varType) throws Exception {
		if (varType instanceof VarTypeReal)
			return varType.extend(this);
		return new VarTypeInt((VarTypeReal) new VarTypeReal(this).extend(varType));
	}

	@Override
	public boolean contain(VarType t) throws Exception {
		if (t == null)
			return false;
		if (!(t instanceof VarTypeInt)) {
			if (!(t instanceof VarTypeReal))
				throw new Exception();
			return false;
		}
		VarTypeInt tt = (VarTypeInt) t;
		return (lowerBound == null || tt.lowerBound != null && lowerBound.compareTo(tt.lowerBound) <= 0)
				&& (upperBound == null || tt.upperBound != null && upperBound.compareTo(tt.upperBound) >= 0);
	}

	@Override
	public VarType combine(VarType t) throws Exception {
		if (t == null)
			return this.clone();
		VarTypeInt tt;
		if (t instanceof VarTypeReal)
			tt = new VarTypeInt((VarTypeReal) t);
		else
			tt = (VarTypeInt) t.clone();
		if (tt.lowerBound == null || lowerBound != null && tt.lowerBound.compareTo(lowerBound) < 0)
			tt.lowerBound = lowerBound;
		if (tt.upperBound == null || upperBound != null && tt.upperBound.compareTo(upperBound) > 0)
			tt.upperBound = upperBound;
		if (tt.lowerBound != null && tt.upperBound != null && tt.lowerBound.compareTo(tt.upperBound) > 0)
			throw new Exception();
		return tt;
	}

}
