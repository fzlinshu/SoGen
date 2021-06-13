package type;

import java.math.BigDecimal;
import java.math.BigInteger;

import type.ExpTreeNode.Operator;

public interface VarType extends Cloneable {

	public static boolean contain(VarType t1, VarType t2) throws Exception {
		if (t1 == null)
			return true;
		return t1.contain(t2);
	}

	public static VarType combine(VarType t1, VarType t2) throws Exception {
		if (t1 == null) {
			if (t2 == null)
				return null;
			return t2.clone();
		}
		return t1.combine(t2);
	}

	public static VarType extend(VarType t1, VarType t2) throws Exception {
		if (t1 == null || t2 == null)
			return null;
		return t1.extend(t2);
	}

	public String getName();

	public String getDef(String varName);

	public VarType rawType();

	public VarType clone();

	public String toString();

	public BigDecimal getLowerBound();

	public BigDecimal getUpperBound();

	public ExpTreeNode getLowerBoundExp();

	public ExpTreeNode getUpperBoundExp();

	public BigInteger getLowerIndex();

	public BigInteger getUpperIndex();

	public BigInteger getSize();

	public String getTotStr() throws Exception;

	public BigInteger getMemSize();

	public VarType calculate(Operator op, VarType varType, int pos) throws Exception;

	public VarType extend(VarType varType) throws Exception;

	public BigInteger range();

	public VarType aggregate(Operator op) throws Exception;

	public boolean contain(VarType t) throws Exception;

	public VarType combine(VarType t) throws Exception;

}
