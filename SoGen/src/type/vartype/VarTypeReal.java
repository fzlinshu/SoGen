package type.vartype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;

import type.ExpTreeNode;
import type.ExpTreeNode.Operator;
import type.VarType;

public class VarTypeReal implements VarType {

	private BigDecimal lowerBound, upperBound;

	public VarTypeReal() {
	}

	public VarTypeReal(VarTypeReal t) {
		lowerBound = t.lowerBound;
		upperBound = t.upperBound;
	}

	public VarTypeReal(String str) {
		lowerBound = new BigDecimal(str);
		upperBound = new BigDecimal(str);
	}

	public VarTypeReal(BigDecimal lowerBound, BigDecimal upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public VarTypeReal(VarTypeInt t) {
		lowerBound = t.getLowerBound();
		upperBound = t.getUpperBound();
	}

	public VarTypeReal(ArrayList<ExpTreeNode> sons) {
		VarType lb = null;
		if (sons.get(0) != null)
			lb = sons.get(0).getVarType();
		VarType ub = null;
		if (sons.get(1) != null)
			ub = sons.get(1).getVarType();
		if (lb != null)
			lowerBound = lb.getLowerBound();
		if (ub != null)
			upperBound = ub.getUpperBound();
	}

	@Override
	public String getName() {
		return "double";
	}

	@Override
	public String getDef(String varName) {
		return "double " + varName;
	}

	@Override
	public VarType rawType() {
		return new VarTypeReal();
	}

	@Override
	public VarType clone() {
		return new VarTypeReal(this);
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
		return "REAL[" + range + "]";
	}

	@Override
	public BigDecimal getLowerBound() {
		return this.lowerBound;
	}

	@Override
	public BigDecimal getUpperBound() {
		return this.upperBound;
	}

	@Override
	public ExpTreeNode getLowerBoundExp() {
		return new ExpTreeNode(new VarTypeReal(), lowerBound.toString());
	}

	@Override
	public ExpTreeNode getUpperBoundExp() {
		return new ExpTreeNode(new VarTypeReal(), upperBound.toString());
	}

	@Override
	public BigInteger getLowerIndex() {
		return null;
	}

	@Override
	public BigInteger getMemSize() {
		return BigInteger.valueOf(8);
	}

	@Override
	public BigInteger getUpperIndex() {
		return null;
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
	public VarType calculate(Operator op, VarType varType, int pos) throws Exception {
		VarTypeReal t;
		BigDecimal lb, ub, tmp;
		Boolean linf, uinf;
		switch (op) {
		case EQUAL:
			return calculate(Operator.NOTEQUAL, varType, pos).calculate(Operator.NOT, null, 0);
		case NOTEQUAL:
			return calculate(Operator.GREATER, varType, pos).calculate(Operator.OR,
					calculate(Operator.LESS, varType, pos), 0);
		case GREATER:
			if (pos != 0)
				throw new Exception();
			if (varType == null)
				varType = new VarTypeReal();
			ub = varType.getUpperBound();
			if (lowerBound != null && ub != null && lowerBound.compareTo(ub) > 0)
				return new VarTypeBool("true");
			lb = varType.getLowerBound();
			if (upperBound == null || lb == null || upperBound.compareTo(lb) >= 0)
				return new VarTypeBool();
			return new VarTypeBool("false");
		case LESS:
			if (pos != 0)
				throw new Exception();
			if (varType == null)
				varType = new VarTypeReal();
			lb = varType.getUpperBound();
			if (upperBound != null && lb != null && upperBound.compareTo(lb) < 0)
				return new VarTypeBool("true");
			ub = varType.getLowerBound();
			if (lowerBound == null || ub == null || lowerBound.compareTo(ub) <= 0)
				return new VarTypeBool();
			return new VarTypeBool("false");
		case GREATEREQUAL:
			return calculate(Operator.LESS, varType, pos).calculate(Operator.NOT, null, 0);
		case LESSEQUAL:
			return calculate(Operator.GREATER, varType, pos).calculate(Operator.NOT, null, 0);
		case ADD:
			if (varType == null)
				return new VarTypeReal();
			if (pos != 0)
				return calculate(Operator.SUBTRACT, varType, 0);
			if (varType instanceof VarTypeReal)
				t = (VarTypeReal) varType;
			else
				t = new VarTypeReal((VarTypeInt) varType);
			if (lowerBound == null || t.lowerBound == null)
				lb = null;
			else
				lb = lowerBound.add(t.lowerBound);
			if (upperBound == null || t.upperBound == null)
				ub = null;
			else
				ub = upperBound.add(t.upperBound);
			return new VarTypeReal(lb, ub);
		case SUBTRACT:
			if (varType == null)
				return new VarTypeReal();
			if (pos == 1)
				return calculate(Operator.ADD, varType, 0);
			if (pos == 2)
				return varType.calculate(Operator.SUBTRACT, this, 0);
			if (varType instanceof VarTypeReal)
				t = (VarTypeReal) varType;
			else
				t = new VarTypeReal((VarTypeInt) varType);
			if (lowerBound == null || t.upperBound == null)
				lb = null;
			else
				lb = lowerBound.subtract(t.upperBound);
			if (upperBound == null || t.lowerBound == null)
				ub = null;
			else
				ub = upperBound.subtract(t.lowerBound);
			return new VarTypeReal(lb, ub);
		case MULTIPLY:
			if (varType == null)
				return new VarTypeReal();
			if (pos != 0)
				return calculate(Operator.DIVIDE, varType, 0);
			if (varType instanceof VarTypeReal)
				t = (VarTypeReal) varType;
			else
				t = new VarTypeReal((VarTypeInt) varType);
			linf = false;
			uinf = false;
			lb = null;
			ub = null;
			if (lowerBound != null && lowerBound.signum() == 0 || upperBound != null && upperBound.signum() == 0
					|| t.lowerBound != null && t.lowerBound.signum() == 0
					|| t.upperBound != null && t.upperBound.signum() == 0) {
				lb = BigDecimal.ZERO;
				ub = BigDecimal.ZERO;
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
			return new VarTypeReal(lb, ub);
		case DIVIDE:
			if (varType == null)
				return new VarTypeReal();
			if (pos == 1)
				return calculate(Operator.MULTIPLY, varType, 0);
			if (pos == 2)
				return varType.calculate(Operator.DIVIDE, this, 0);
			if (varType instanceof VarTypeReal)
				t = (VarTypeReal) varType;
			else
				t = new VarTypeReal((VarTypeInt) varType);
			if (t.lowerBound == null || t.lowerBound.signum() == -1)
				if (t.upperBound == null || t.upperBound.signum() >= 0)
					if (varType instanceof VarTypeReal)
						lb = null;
					else
						lb = BigDecimal.ONE.negate();
				else
					lb = BigDecimal.ONE.divide(t.upperBound, 10, RoundingMode.DOWN);
			else if (t.upperBound == null)
				lb = BigDecimal.ZERO;
			else
				lb = BigDecimal.ONE.divide(t.upperBound, 10, RoundingMode.DOWN);
			if (t.upperBound == null || t.upperBound.signum() == 1)
				if (t.lowerBound == null || t.lowerBound.signum() <= 0)
					if (varType instanceof VarTypeReal)
						ub = null;
					else
						ub = BigDecimal.ONE;
				else
					ub = BigDecimal.ONE.divide(t.lowerBound, 10, RoundingMode.UP);
			else if (t.lowerBound == null)
				ub = BigDecimal.ZERO;
			else
				ub = BigDecimal.ONE.divide(t.lowerBound, 10, RoundingMode.UP);
			return calculate(Operator.MULTIPLY, new VarTypeReal(lb, ub), 0);
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
				return new VarTypeReal(lowerBound.pow(l), ub);
			} catch (Exception e) {
				return new VarTypeReal();
			}
		default:
			throw new Exception();
		}
	}

	@Override
	public VarType extend(VarType varType) throws Exception {
		VarTypeReal t;
		if (varType instanceof VarTypeReal)
			t = (VarTypeReal) varType;
		else
			t = new VarTypeReal((VarTypeInt) varType);
		BigDecimal lb, ub;
		if (lowerBound == null || t.lowerBound == null)
			lb = null;
		else
			lb = lowerBound.min(t.lowerBound);
		if (upperBound == null || t.upperBound == null)
			ub = null;
		else
			ub = upperBound.max(t.upperBound);
		return new VarTypeReal(lb, ub);
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
	public boolean contain(VarType t) throws Exception {
		if (t == null)
			return false;
		VarTypeReal tt;
		if (t instanceof VarTypeReal)
			tt = (VarTypeReal) t;
		else if (t instanceof VarTypeInt)
			tt = new VarTypeReal((VarTypeInt) t);
		else
			throw new Exception();
		return (lowerBound == null || tt.lowerBound != null && lowerBound.compareTo(tt.lowerBound) <= 0)
				&& (upperBound == null || tt.upperBound != null && upperBound.compareTo(tt.upperBound) >= 0);
	}

	@Override
	public VarType combine(VarType t) throws Exception {
		if (t == null)
			return this.clone();
		if (t instanceof VarTypeInt)
			return t.combine(this);
		VarTypeReal tt = (VarTypeReal) t.clone();
		if (tt.lowerBound == null || lowerBound != null && tt.lowerBound.compareTo(lowerBound) < 0)
			tt.lowerBound = lowerBound;
		if (tt.upperBound == null || upperBound != null && tt.upperBound.compareTo(upperBound) > 0)
			tt.upperBound = upperBound;
		if (tt.lowerBound != null && tt.upperBound != null && tt.lowerBound.compareTo(tt.upperBound) > 0)
			throw new Exception();
		return tt;
	}

}
