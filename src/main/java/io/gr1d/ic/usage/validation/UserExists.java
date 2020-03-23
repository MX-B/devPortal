package io.gr1d.ic.usage.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates if an User exists
 * 
 * @author Rafael M. Lins
 *
 */
@Documented
@Constraint(validatedBy = UserExistsValidator.class)
@Target({ METHOD, FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface UserExists {
	String message() default "{gr1d.portal.billing.errors.userDoNotExist}";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
