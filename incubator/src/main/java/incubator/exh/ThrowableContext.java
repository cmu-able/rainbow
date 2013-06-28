package incubator.exh;

import incubator.pval.Ensure;
import incubator.scb.ScbDateField;
import incubator.scb.ScbField;
import incubator.scb.ScbTextField;
import incubator.scb.SerializableScb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class representing the context in which a throwable was caught.
 */
public class ThrowableContext extends SerializableScb<ThrowableContext> {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The throwable caught.
	 */
	private Throwable m_throwable;
	
	/**
	 * The throwable location.
	 */
	private String m_location;
	
	/**
	 * When was the throwable caught.
	 */
	private Date m_when;
	
	/**
	 * Creates a new throwable context.
	 * @param t the throwable
	 * @param location an optional location describing where the throwable
	 * has been caught 
	 */
	public ThrowableContext(Throwable t, String location) {
		Ensure.notNull(t);
		m_throwable = t;
		m_location = location;
		m_when = new Date();
	}
	
	/**
	 * Obtains the throwable.
	 * @return the throwable
	 */
	public Throwable throwable() {
		return m_throwable;
	}
	
	/**
	 * Obtains the location.
	 * @return the location
	 */
	public String location() {
		return m_location;
	}
	
	/**
	 * Obtains when the thowable has been caught.
	 * @return the time at which the thowable was caught
	 */
	public Date when() {
		return m_when;
	}
	
	/**
	 * Obtains the class field description.
	 * @return the class field description
	 */
	public static ScbTextField<ThrowableContext> c_class() {
		return new ScbTextField<ThrowableContext>("Class", false,
				"The exception class.") {
			@Override
			public void set(ThrowableContext t, String value) {
				/*
				 * Can't write because field is read only.
				 */
			}

			@Override
			public String get(ThrowableContext t) {
				return t.throwable().getClass().getName();
			}
		};
	}
	
	/**
	 * Obtains the message field description.
	 * @return the message field description
	 */
	public static ScbTextField<ThrowableContext> c_message() {
		return new ScbTextField<ThrowableContext>("Message", false,
				"The exception message.") {
			@Override
			public void set(ThrowableContext t, String value) {
				/*
				 * Can't write because field is read only.
				 */
			}

			@Override
			public String get(ThrowableContext t) {
				String m = t.throwable().getMessage();
				if (m == null) {
					m = "(no message provided)";
				}
				
				return m;
			}
		};
	}
	
	/**
	 * Obtains the location field description.
	 * @return the location field description
	 */
	public static ScbTextField<ThrowableContext> c_location() {
		return new ScbTextField<ThrowableContext>("Location", false,
				"The location of the exception.") {
			@Override
			public void set(ThrowableContext t, String value) {
				/*
				 * Can't write because field is read only.
				 */
			}

			@Override
			public String get(ThrowableContext t) {
				String l = t.location();
				if (l == null) {
					l = "(no location provided)";
				}
				
				return l;
			}
		};
	}
	
	/**
	 * Obtains the when field description.
	 * @return the when field description
	 */
	public static ScbDateField<ThrowableContext> c_when() {
		return new ScbDateField<ThrowableContext>("Time", false,
				"The time at which the exception was thrown.") {
			@Override
			public void set(ThrowableContext t, Date value) {
				/*
				 * Can't write because field is read only.
				 */
			}

			@Override
			public Date get(ThrowableContext t) {
				return t.when();
			}
		};
	}
	
	@SuppressWarnings("javadoc")
	public static List<ScbField<ThrowableContext, ?>> c_fields() {
		List<ScbField<ThrowableContext, ?>> l = new ArrayList<>();
		l.add(c_class());
		l.add(c_message());
		l.add(c_location());
		l.add(c_when());
		return l;
	}

	@Override
	public List<ScbField<ThrowableContext, ?>> fields() {
		return c_fields();
	}

	@Override
	protected Class<ThrowableContext> my_class() {
		return ThrowableContext.class;
	}
}
