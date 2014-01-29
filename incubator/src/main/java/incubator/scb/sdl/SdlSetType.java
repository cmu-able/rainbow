package incubator.scb.sdl;

import incubator.jcodegen.JavaMethod;
import incubator.jcodegen.JavaSetType;
import incubator.jcodegen.JavaType;
import incubator.pval.Ensure;
import incubator.scb.MergeableScbSetComparationResult;
import incubator.scb.MergeableScbSetComparator;
import incubator.scb.delta.ScbSetSubCreateDelta;
import incubator.scb.delta.ScbSetSubDeleteDelta;
import incubator.scb.delta.ScbSetSubDelta;

/**
 * SDL type representing a set of other elements.
 */
public class SdlSetType extends SdlType {
	/**
	 * The inner type.
	 */
	private SdlType m_inner;
	
	/**
	 * Creates a new set type.
	 * @param inner the type of the set elements
	 */
	public SdlSetType(SdlType inner) {
		super("set<" + Ensure.not_null(inner, "inner == null").name() + ">");
		m_inner = inner;
	}
	
	@Override
	public JavaType generate_type() {
		return new JavaSetType(m_inner.generate_type());
	}
	
	@Override
	public String generate_delta_assign(String old_v, String new_v,
			JavaMethod field_method, String delta_var) {
		String itype = m_inner.generate_type().name();
		String mcr_name = "mcr_" + field_method.name();
		return MergeableScbSetComparationResult.class.getName() + "<"
				+ itype + "> " + mcr_name
				+ " = " + MergeableScbSetComparator.class.getName()
				+ ".compare("
				+ field_method.name() + "().get(" + old_v + "), "
				+ field_method.name() + "().get(" + new_v + "));\n"
				+ "for (" + itype + " i : " + mcr_name + "."
				+ "to_create()) {\n"
				+ delta_var + ".add(new " + ScbSetSubCreateDelta.class.getName()
				+ "(this, old, " + field_method.name() + "(), "
				+ m_inner.copy_expression("i") + "));\n"
				+ "}\n"
				+ "for (" + itype + " i : " + mcr_name + "."
				+ "to_delete()) {\n"
				+ delta_var + ".add(new " + ScbSetSubDeleteDelta.class.getName()
				+ "(this, old, " + field_method.name() + "(), "
				+ m_inner.copy_expression("i") + "));\n"
				+ "}\n"
				+ "for (incubator.Pair<" + itype + "," + itype + "> i : "
				+ mcr_name + "." + "different()) {\n"
				+ delta_var + ".add(new " + ScbSetSubDelta.class.getName()
				+ "(this, old, " + field_method.name() + "(), "
				+ "i.second().diff_from(i.first())));\n"
				+ "}";
	}
}
