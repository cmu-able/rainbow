package incubator.scb.sdl.generators;

import incubator.jcodegen.JavaClass;
import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaField;
import incubator.jcodegen.JavaMethod;
import incubator.jcodegen.JavaPackage;
import incubator.jcodegen.JavaType;
import incubator.pval.Ensure;
import incubator.scb.MergeableIdScb;
import incubator.scb.MergeableScb;
import incubator.scb.delta.ScbDelta;
import incubator.scb.sdl.GenerationInfo;
import incubator.scb.sdl.GenerationResult;
import incubator.scb.sdl.SdlAttribute;
import incubator.scb.sdl.SdlBean;
import incubator.scb.sdl.SdlBeanGenerator;
import incubator.scb.sdl.SdlBeanType;
import incubator.scb.sdl.SdlGenerationException;
import incubator.scb.sdl.SdlType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bean generator that makes the SCB a mergeable SCB.
 */
public class MergeableScbGenerator implements SdlBeanGenerator {
	/**
	 * Suggested generator name.
	 */
	public static final String NAME = "mergeable_scb";
	
	/**
	 * Property set in the bean when the ID has been added to the simple
	 * constructor.
	 */
	public static final String SDL_PROP_ID_ADDED_TO_SC = "id_added_sc";
	
	/**
	 * Property set in the bean when the ID has been added to the copy
	 * constructor.
	 */
	public static final String SDL_PROP_ID_ADDED_TO_CC = "id_added_cc";
	
	/**
	 * Property set in the bean with the ID field.
	 */
	public static final String SDL_PROP_ID = "id";
	
	/**
	 * Property set in the bean with the merge method.
	 */
	public static final String SDL_PROP_MERGE_METHOD = "merge_method";
	
	/**
	 * Property set in the bean with the diff method.
	 */
	public static final String SDL_PROP_DIFF_METHOD = "diff_method";
	
	/**
	 * Property set in the bean to state that the mergeable interface has
	 * already been added.
	 */
	public static final String SDL_PROP_INTERFACE_ADDED = "mergeable_added";
	
	/**
	 * Generator property stating that no ID should be generated.
	 */
	public static final String GEN_PROP_NO_ID = "no_id";
	
	/**
	 * Has the ID been added into the hashcode methods?
	 */
	public static final String SDL_PROP_ID_IN_HASHCODE =
			"mergeable_id_in_hashcode";
	
	/**
	 * Has the ID been added into the equals methods?
	 */
	public static final String SDL_PROP_ID_IN_EQUALS =
			"mergeable_id_in_equals";
	
	/**
	 * Constructor.
	 */
	public MergeableScbGenerator() {
		/*
		 * Nothing to do.
		 */
	}
	
	@Override
	public GenerationInfo generate(SdlBean b, JavaCode jc, JavaPackage jp,
			Map<String, String> properties) throws SdlGenerationException {
		Ensure.not_null(b, "b == null");
		Ensure.not_null(jc, "jc == null");
		Ensure.not_null(jp, "jp == null");
		Ensure.not_null(properties, "properties == null");
		
		boolean no_id = false;
		for (String k : properties.keySet()) {
			switch (k) {
			case GEN_PROP_NO_ID:
				no_id = true;
				break;
			default:
				throw new SdlGenerationException("Unknown generation property '"
						+ k + "'.");
			}
		}
		
		JavaClass cls = b.property(JavaClass.class,
				ClassBeanGenerator.SDL_PROP_CLASS);
		if (cls == null) {
			return new GenerationInfo(GenerationResult.CANNOT_RUN,
					MergeableScbGenerator.class.getCanonicalName()
					+ ": bean class not found");
		}
		
		boolean any = false;
		
		JavaField id_field = b.property(JavaField.class, SDL_PROP_ID);
		if (id_field == null && !no_id) {
			id_field = cls.make_field("m_id", new JavaType("int"));
			b.property(SDL_PROP_ID, id_field);
			JavaMethod m = cls.make_method("id", new JavaType("int"));
			m.append_contents("return m_id;\n");
			any = true;
		}
		
		JavaMethod cc = b.property(JavaMethod.class,
				SdlBean.SDL_PROP_COPY_CONSTRUCTOR);
		if (cc != null && !no_id) {
			Boolean added_cc = b.property(Boolean.class,
					SDL_PROP_ID_ADDED_TO_CC);
			if (added_cc == null || !added_cc) {
				cc.append_contents("this.m_id = src.m_id;\n");
				b.property(SDL_PROP_ID_ADDED_TO_CC, true);
				any = true;
			}
		}
		
		JavaMethod sc = b.property(JavaMethod.class,
				SimpleConstructorGenerator.SDL_PROP_SIMPLE_CONSTRUCTOR);
		if (sc != null && !no_id) {
			Boolean added_sc = b.property(Boolean.class,
					SDL_PROP_ID_ADDED_TO_SC);
			if (added_sc == null || !added_sc) {
				sc.make_parameter("id", new JavaType("int"));
				sc.append_contents("incubator.pval.Ensure.greater_equal(id, 0, "
						+ "\"id < 0\");\n");
				sc.append_contents("this.m_id = id;\n");
				b.property(SDL_PROP_ID_ADDED_TO_SC, true);
				any = true;
			}
		}
		
		List<SdlAttribute> attrs = new ArrayList<>();
		List<JavaField> fields = new ArrayList<>();
		List<JavaMethod> descs = new ArrayList<>();
		List<JavaMethod> setters = new ArrayList<>();
		for (String an : b.attribute_names()) {
			SdlAttribute a = b.attribute(an);
			JavaField f = a.property(JavaField.class, 
					AttributesAsFieldsGenerator.SDL_PROP_FIELD);
			if (f == null) {
				return new GenerationInfo(GenerationResult.CANNOT_RUN,
						MergeableScbGenerator.class.getCanonicalName()
						+ ": no field found for attribute '" + an + "'");
			}
			
			JavaMethod s = a.property(JavaMethod.class,
					SimpleAttributeAccessorsGenerator.SDL_PROP_SETTER);
			if (s == null) {
				return new GenerationInfo(GenerationResult.CANNOT_RUN,
						MergeableScbGenerator.class.getCanonicalName()
						+ ": no setter found for attribute '" + an + "'");
			}
			
			JavaMethod d = a.property(JavaMethod.class,
					BasicScbGenerator.SDL_PROP_SCB_FIELD_METHOD);
			if (d == null) {
				return new GenerationInfo(GenerationResult.CANNOT_RUN,
						MergeableScbGenerator.class.getCanonicalName()
						+ ": no SCB field description method found for "
						+ "attribute '" + an + "'");
			}
			
			attrs.add(a);
			fields.add(f);
			setters.add(s);
			descs.add(d);
		}
		
		JavaMethod mm = b.property(JavaMethod.class, SDL_PROP_MERGE_METHOD);
		if (mm == null) {
			mm = cls.make_method("merge", new JavaType("void"));
			b.property(SDL_PROP_MERGE_METHOD, mm);
			mm.make_parameter("v", b.type().generate_type());
			mm.append_contents("incubator.pval.Ensure.not_null(v, "
					+ "\"v == null\");\n");
			if (!no_id) {
				mm.append_contents("incubator.pval.Ensure.equals(m_id, v.m_id, "
						+ "\"Objects to merge do not have the same ID.\");\n");
			}
			
			for (int i = 0; i < attrs.size(); i++) {
				JavaField f = fields.get(i);
				SdlType ft = attrs.get(i).type();
				mm.append_contents("if (!org.apache.commons.lang."
						+ "ObjectUtils.equals(" + f.name() + ", v."
						+ f.name() + ")) {\n");
				if (ft instanceof SdlBeanType) {
					mm.append_contents("if (" + f.name() + " == null || v."
							+ f.name() + " == null) {\n");
					mm.append_contents(setters.get(i).name() + "(v." + f.name()
							+ ");\n");
					mm.append_contents("} else {\n");
					mm.append_contents(f.name() + ".merge(v." + f.name()
							+ ");\n");
					mm.append_contents("}\n");
				} else {
					mm.append_contents(setters.get(i).name() + "(v." + f.name()
							+ ");\n");
				}
				mm.append_contents("}\n");
			}
			
			any = true;
		}
		
		JavaMethod dm = b.property(JavaMethod.class, SDL_PROP_DIFF_METHOD);
		if (dm == null) {
			dm = cls.make_method("diff_from", new JavaType(
					ScbDelta.class.getName() + "<"
					+ b.type().generate_type().name() + ">"));
			b.property(SDL_PROP_DIFF_METHOD, dm);
			dm.make_parameter("old", b.type().generate_type());
			dm.append_contents("incubator.pval.Ensure.not_null(old, \""
					+ "old == null\");\n");
			dm.append_contents("java.util.List<" + ScbDelta.class.getName()
					+ "<" + b.type().generate_type().name() + ">> "
					+ "delta = new java.util.ArrayList<>();\n");
			
			for (int i = 0; i < attrs.size(); i++) {
				JavaField f = fields.get(i);
				JavaField of = new JavaField("old." + f.name(), f.type());
				
				dm.append_contents("if (!("
						+ attrs.get(i).type().generate_comparison(f, of)
						+ ")) {\n");
				dm.append_contents(attrs.get(i).type().generate_delta_assign(
						"old", "this", descs.get(i), "delta"));
				dm.append_contents("\n}\n");
			}
			
			dm.append_contents("return new "
					+ "incubator.scb.delta.ScbDeltaContainer(this, old, "
					+ "delta);\n");
		}
		
		Boolean iadded = b.property(Boolean.class, SDL_PROP_INTERFACE_ADDED);
		if (iadded == null) {
			b.property(SDL_PROP_INTERFACE_ADDED, true);
			if (no_id) {
				cls.add_implements(MergeableScb.class.getCanonicalName() + "<"
						+ b.type().generate_type().name() + ">");
			} else {
				cls.add_implements(MergeableIdScb.class.getCanonicalName() + "<"
						+ b.type().generate_type().name() + ">");
			}
		}
		
		JavaMethod hc_method = b.property(JavaMethod.class,
				HashcodeEqualsGenerator.SDL_PROP_HASHCODE_METHOD);
		Boolean id_in_hc = b.property(Boolean.class, SDL_PROP_ID_IN_HASHCODE);
		if (hc_method != null && id_in_hc == null && !no_id) {
			boolean appended = hc_method.append_contents_before(
					"result = prime * result "
					+ "+ org.apache.commons.lang.ObjectUtils.hashCode(m_id);\n",
					"^return(\\s|;).*");
			if (!appended) {
				return new GenerationInfo(GenerationResult.CANNOT_RUN,
						MergeableScbGenerator.class.getCanonicalName()
						+ ": Did not find return line in hashCode method.");
			}
			
			b.property(SDL_PROP_ID_IN_HASHCODE, true);
			any = true;
		}
		
		JavaMethod eq_method = b.property(JavaMethod.class,
				HashcodeEqualsGenerator.SDL_PROP_EQUALS_METHOD);
		Boolean id_in_eq = b.property(Boolean.class, SDL_PROP_ID_IN_EQUALS);
		if (eq_method != null && id_in_eq == null && !no_id) {
			boolean appended = eq_method.append_contents_before(
					"if (!org.apache.commons.lang.ObjectUtils.equals(m_id, "
					+ "other.m_id)) return false;\n",
					"^return true.*");
			if (!appended) {
				return new GenerationInfo(GenerationResult.CANNOT_RUN,
						MergeableScbGenerator.class.getCanonicalName()
						+ ": Did not find return line in equals method.");
			}
			
			b.property(SDL_PROP_ID_IN_EQUALS, true);
			any = true;
		}
		
		if (any) {
			return new GenerationInfo(GenerationResult.GENERATED_CODE);
		} else {
			return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
		}
	}
}
