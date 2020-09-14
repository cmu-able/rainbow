package org.sa.rainbow.configuration;
/*
Copyright 2020 Carnegie Mellon University

Permission is hereby granted, free of charge, to any person obtaining a copy of this 
software and associated documentation files (the "Software"), to deal in the Software 
without restriction, including without limitation the rights to use, copy, modify, merge,
 publish, distribute, sublicense, and/or sell copies of the Software, and to permit 
 persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE.
 */
import java.math.BigDecimal;
import java.util.Set;

import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractNullSafeConverter;
import org.eclipse.xtext.conversion.impl.STRINGValueConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

public class RclValueConverter extends DefaultTerminalConverters {
	
	public class FQNConverter extends AbstractNullSafeConverter<String> {
		private Set<String> allKeywords = ImmutableSet.copyOf(GrammarUtil.getAllKeywords(getGrammar()));
		
		@Override
		protected String internalToValue(String string, INode node) throws ValueConverterException {
			return string.replaceAll("[\\^\\s]", "");
		}
		
		@Override
		protected String internalToString(String value) {
			String[] segments = value.split("\\.");
			StringBuilder builder = new StringBuilder(value.length());
			boolean first = true;
			for (String segment: segments) {
				if (!first)
					builder.append('.');
				if (allKeywords.contains(segment))
					builder.append("^");
				builder.append(segment);
				first = false;
			}
			return builder.toString();
		}
	}
	
	@ValueConverter(rule="ImportedFQN")
	public IValueConverter<String> ImportedFQN() {
		return new FQNConverter();
	}
	
	@ValueConverter(rule="FQN")
	public IValueConverter<String> FQN() {
		return new FQNConverter();
	}
	
	@ValueConverter(rule="DoubleValue")
	public IValueConverter<Double> DoubleValue() {
		return new AbstractNullSafeConverter<Double>() {
			@Override
			protected Double internalToValue(String string, INode node) throws ValueConverterException {
				try {
					return Double.parseDouble(string);
				}
				catch (NumberFormatException e) {
					throw new ValueConverterException(e.getMessage(), node, e);
				}
			}
			
			@Override
			protected String internalToString(Double value) {
				return BigDecimal.valueOf(value).toPlainString();
			}
		};
	}
	
	@ValueConverter(rule="ConstantValue")
	public IValueConverter<String> ConstantValue() {
		return new AbstractNullSafeConverter<String>() {

			@Override
			protected String internalToValue(String string, INode node) {
				try {
					string = string.replace("\\${", "${");
					return Strings.convertFromJavaString(string, false);
				} catch(IllegalArgumentException e) {
					throw new ValueConverterException(e.getMessage(), node, e);
				}
			}

			@Override
			protected String internalToString(String value) {
				String result = Strings.convertToJavaString(value, false);
				result = result.replace("${", "\\${");
				return result;
			}
		};
	}
	
	@Inject 
	STRINGValueConverter stringValueConverter;
	
	@ValueConverter(rule="RICH_TEXT_DQ")
	public IValueConverter<String> RICH_TEXT_DQ() {
		return stringValueConverter;
	}
	
	@ValueConverter(rule="RICH_TEXT_SQ")
	public IValueConverter<String> RICH_TEXT_SQ() {
		return stringValueConverter;
	}
}
