package darkyenuscommand.command;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;

/**
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cmd {

	/** Name of the command. Uses method name by default. */
	String value() default "";

	/** Order in which this command binding should be tried.
	 * Lower values are tried first. */
	int order() default Integer.MAX_VALUE;

	/** Brief description about this overload */
	String description() default "";

	/** For this argument to get matched,
	 * it must be prefixed with {@link #value}. */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@interface Prefix {
		String value();
	}

	/** This argument is optional, use implicit value for this type when missing.
	 * Implicit value is implemented per type and not always exists. For example implicit {@link World}
	 * is the world in which the command sender is in, but does not exist when the command is sent from the console. */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@interface UseImplicit {}

	/** This argument is optional, use default value for this type when missing.
	 * Default value is whatever java considers a default value for that type, i.e. null, 0, false. */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@interface UseDefault {}

	/** Valid only for parameters, whose type is an enum, String.
	 * When on Enum:
	 * This argument may match only if it is assigned any of the enum value names specified by {@link #value}.
	 * It is an error when specified enum constant names don't match exactly defined enum constants.
	 * When on String:
	 * Matches ony literal string with this value, case insensitive. Matched value is stored in the string.
	 * */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@interface OneOf{
		String[] value();
	}

	/** This argument will consume all remaining parameters.
	 * Currently supported only on {@link String} parameters which are last. */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@interface VarArg {
		String separator() default " ";
	}

	class Util {
		/** Retrieve which enum constants does given parameter expect.
		 * Considers {@link OneOf}. When parameter does not take an enum, returns null. */
		@SuppressWarnings("unchecked")
		@Nullable
		static <T extends Enum<T>> Enum<T>[] viableEnumConstants(@NotNull Parameter parameter) {
			final Class<T> paramType = (Class<T>) parameter.getType();
			if (!paramType.isEnum()) {
				return null;
			}

			final T[] enumConstants = paramType.getEnumConstants();
			final OneOf oneOf = parameter.getDeclaredAnnotation(OneOf.class);
			if (oneOf == null) {
				return enumConstants;
			} else {
				final T[] constants = (T[]) new Enum[oneOf.value().length];
				int i = 0;
				for (String s : oneOf.value()) {
					T foundConstant = null;
					for (T constant : enumConstants) {
						if (s.equalsIgnoreCase(constant.name())) {
							foundConstant = constant;
							break;
						}
					}
					if (foundConstant == null) {
						throw new IllegalArgumentException("Can't find viable enum constants for "+parameter+": no constant of "+paramType+" is called "+s);
					}
					constants[i++] = foundConstant;
				}

				return constants;
			}
		}
	}
}
