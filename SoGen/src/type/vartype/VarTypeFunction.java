package type.vartype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import type.ExpTreeNode;
import type.ExpTreeNode.Operator;
import type.VarType;

public class VarTypeFunction implements VarType {

	private VarType args;
	private VarType ret;

	public VarTypeFunction(VarTypeFunction t) {
		if (t.args != null)
			args = t.args.clone();
		if (t.ret != null)
			ret = t.ret.clone();
	}

	public VarTypeFunction(VarType args, VarType ret) {
		this.args = args;
		this.ret = ret;
	}

	public VarTypeFunction(ArrayList<ExpTreeNode> sons) throws Exception {
		args = new VarTypeTuple(sons.get(0).getSons());
		ret = VarType.combine(sons.get(1).getVarType(), sons.get(2).getVarType());
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getDef(String varName) {
		return null;
	}

	@Override
	public VarType rawType() {
		return new VarTypeFunction(args.rawType(), ret.rawType());
	}

	@Override
	public VarType clone() {
		return new VarTypeFunction(this);
	}

	@Override
	public String toString() {
		return args + " -> " + ret;
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
		case INDEX:
			return ret.clone();
		default:
			throw new Exception();
		}
	}

	@Override
	public VarType extend(VarType varType) throws Exception {
		throw new Exception();
	}

	@Override
	public boolean contain(VarType t) throws Exception {
		if (t == null)
			return false;
		if (!(t instanceof VarTypeFunction))
			throw new Exception();
		VarTypeFunction tt = (VarTypeFunction) t;
		return VarType.contain(args, tt.args) & VarType.contain(ret, tt.ret);
	}

	@Override
	public VarType combine(VarType t) throws Exception {
		if (t == null)
			return this.clone();
		VarTypeFunction tt = (VarTypeFunction) t.clone();
		tt.args = VarType.combine(tt.args, args);
		tt.ret = VarType.combine(tt.ret, ret);
		return tt;
	}

	public VarType getArgs() {
		return args;
	}

	public VarType getRet() {
		return ret;
	}

}
