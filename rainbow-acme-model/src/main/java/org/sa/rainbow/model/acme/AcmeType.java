package org.sa.rainbow.model.acme;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used as an annotation in commands to indicate the expected Acme type
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
@Retention (RetentionPolicy.RUNTIME)
public @interface AcmeType {
    String value();
}
