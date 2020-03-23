package io.gr1d.ic.usage.validation;

import io.gr1d.spring.keycloak.Keycloak;
import io.gr1d.spring.keycloak.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * Validates if an User ID exists
 * 
 * @author Rafael M. Lins
 *
 */
@Slf4j
public class UserExistsValidator implements ConstraintValidator<UserExists, String> {
	private final Keycloak keycloak;
	private final boolean testEnvironment;
	
	@Autowired
	public UserExistsValidator(final Keycloak keycloak, final Environment env) {
		this.keycloak = keycloak;
		testEnvironment = Arrays.asList(env.getActiveProfiles()).contains("test");
	}
	
	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (testEnvironment) {
			log.debug("Test Environment Detected. Returning true.");
			return true;
		}
		else {
			try {
				final User user = keycloak.user(value);
				return value != null && user != null && user.isEnabled();
			} catch (final Exception e) {
				log.error("Error trying to validate if user exists", e);
				return false;
			}
		}
	}
	
}
