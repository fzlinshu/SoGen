package type.vartype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import type.ExpTreeNode;
import type.ExpTreeNode.Operator;
import type.VarType;

public class VarTypeTuple implements VarType {

	private VarType[] subTypes;

	public VarTypeTuple(VarTypeTuple t) {
		int len = t.subTypes.length;
		subTypes = new VarType[len];
		for (int i = 0; i < len; i++)
			if (t.subTypes[i] != null)
				subTypes[i] = t.subTypes[i].clone();
	}

	public VarTypeTuple(VarType[] subTypes) {
		this.subTypes = subTypes.clone();
	}

	public VarTypeTuple(ArrayList<ExpTreeNode> sons) {
		int len = sons.size();
		subTypes = new VarType[len];
		for (int i = 0; i < len; i++)
			subTypes[i] = sons.get(i).getVarType();
	}

	@Override
	public String getName() {
		// TODO add struct for TupleType
		return null;
	}

	@Override
	public String getDef(String varName) {
		// TODO add struct for TupleType
		return null;
	}

	@Override
	public VarType rawType() {
		int len = subTypes.length;
		VarType[] raws = new VarType[len];
		for (int i = 0; i < len; i++)
			raws[i] = subTypes[i].rawType();
		return new VarTypeTuple(raws);
	}

	@Override
	public VarType clone() {
		return new VarTypeTuple(this);
	}

	@Override
	public String toString() {
		String range = "( ";
		for (VarType subType : subTypes)
			if (subType == null)
				range += "? ";
			else
				range += subType + " ";
		return range + ")";
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
		return null;
	}

	@Override
	public ExpTreeNode getUpperBoundExp() {
		return null;
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
		// TODO tuple size
		return null;
	}

	@Override
	public BigInteger getSize() {
		return null;
	}

	@Override
	public String getTotStr() {
		return null;
	}

	@Override
	public VarType calculate(Operator op, VarType varType, int pos) throws Exception {
		switch (op) {
		case INDEX:
			int l = 1, u = subTypes.length;
			if (varType != null) {
				VarTypeInt ti = (VarTypeInt) varType;
				if (ti.getLowerBound() != null)
					l = ti.getLowerBound().intValue();
				if (ti.getUpperBound() != null)
					u = ti.getUpperBound().intValue();
			}
			VarType t = subTypes[l - 1];
			for (int i = l; i < u - 1; i++)
				try {
					t = t.extend(subTypes[i]);
				} catch (Exception e) {
					return null;
				}
			return t;
		default:
			throw new Exception();
		}
	}

	@Override
	public VarType extend(VarType varType) throws Exception {
		VarTypeTuple t = (VarTypeTuple) varType;
		int len = subTypes.length;
		if (t.subTypes.length != len)
			throw new Exception();
		VarType[] ts = new VarType[len];
		for (int i = 0; i < len; i++)
			ts[i] = VarType.extend(subTypes[i], t.subTypes[i]);
		return new VarTypeTuple(ts);
	}

	@Override
	public BigInteger range() {
		BigInteger ret = BigInteger.ONE;
		for (VarType t : subTypes) {
			if (t == null)
				return null;
			BigInteger r = t.range();
			if (r == null)
				return null;
			ret = ret.multiply(r);
		}
		return ret;
	}

	@Override
	public VarType aggregate(Operator op) throws Exception {
		throw new Exception();
	}

	@Override
	public boolean contain(VarType t) throws Exception {
		if (t == null)
			return false;
		if (!(t instanceof VarTypeTuple))
			throw new Exception();
		VarTypeTuple tt = (VarTypeTuple) t;
		if (subTypes.length != tt.subTypes.length)
			throw new Exception();
		for (int i = 0; i < subTypes.length; i++)
			if (!VarType.contain(subTypes[i], tt.subTypes[i]))
				return false;
		return true;
	}

	@Override
	public VarType combine(VarType t) throws Exception {
		if (t == null)
			return this.clone();
		VarTypeTuple tt = (VarTypeTuple) t.clone();
		for (int i = 0; i < subTypes.length; i++)
			tt.subTypes[i] = VarType.combine(tt.subTypes[i], subTypes[i]);
		return tt;
	}

	public VarType getSubType(int i) {
		return subTypes[i];
	}

	public int getNum() {
		return subTypes.length;
	}

}
