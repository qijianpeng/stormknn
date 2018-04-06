package util.args;

import util.args.intervals.GenericInterval;
import util.args.intervals.Interval;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes command-line arguments supplied to a Java application's
 * <code>main</code> method.
 * <p>
 * Provides methods for specifying which flags and variables are permitted as
 * arguments to the main method of an application, and methods for processing
 * supplied arguments to check for compliance and retrieve variable values and
 * determine the presence of flags.</p>
 * <p>
 * To use <code>argsutil.MainArgsHandler</code> follow these steps in order:</p>
 * <ol>
 * <li>Acquire the sole instance of <code>argsutil.MainArgsHandler</code> by calling the
 * static method {@link #getHandler()}.</li>
 * <li>Specify which flag names, if any, are permitted in the command-line
 * arguments for your application by calling the
 * {@link #permitFlag(String, String)} method for each flag.</li>
 * <li>Specify which variables, if any, are permitted in the command-line
 * arguments, and how many times they must or may appear, by calling the
 * {@link #permitVariable(String, Interval, String)} for each variable.</li>
 * <li>Call the {@link #processMainArgs(String[])} method to process the String
 * array received by your application's <code>main(String[])</code> method. Be
 * prepared for an <code>IllegalArgumentException</code> to be thrown if the
 * user calls your application with unrecognised command-line flags or variables
 * or incorrect quantities of permitted variables.</li>
 * <li>Check for the presence of a permitted flag in the command-line arguments
 * by calling {@link #foundFlag(String)}.</li>
 * <li>Check for the presence of a permitted variable in the command-line
 * arguments by calling {@link #foundVariable(String)}.</li>
 * <li>If a variable is found on the command-line, get a
 * <code>List&lt;String&gt;</code> of its values by calling
 * {@link #getValuesFromVariable(String)} for that variable name.</li>
 * </ol>
 * <p>
 * Each command-line argument can be either a <dfn>flag</dfn> (which has no
 * value) or a <dfn>variable</dfn> (which does have a value).</p>
 * <p>
 * A command-line flag has the form <kbd>--flag-name</kbd> (two hyphens then the
 * flag-name) where the flag-name begins with lowercase a-z and can then contain
 * lowercase a-z sequences separated by single hyphens (consecutive hyphens
 * cannot appear within flag names, so <kbd>--flag--name</kbd> would be illegal
 * for instance).</p>
 * <p>
 * A command-line variable has the form <kbd>--variable-name=value</kbd> (two
 * hyphens then the variable-name then equals-sign then value) where the
 * variable-name must follow the same rules as a flag-name. The value can
 * contain any characters (though this can be constrained by specifying a
 * <code>Pattern</code> in the call to <code>permitVariable</code>). (Bear in
 * mind that to pass Java a value which contains spaces the command-line
 * argument must wrap the value in double-quotes, e.g.
 * <kbd>--window-title="Nightly Build v1.740"</kbd> and also note that the
 * double-quotes are automatically stripped away by Java before they reach the
 * main method.)</p>
 * <p>
 * Note that the initial double-hyphen is only used on the command-line, and
 * does not form part of the flag or variable name. The initial double-hyphen
 * must not be included when calling methods such as {@link #permitFlag(String)}
 * or {@link #getValuesFromVariable(String)}.</p>
 * <p>
 * If a flag or variable is encountered which does not fit the permitted syntax
 * then an <code>IllegalArgumentException</code> will be thrown.</p>
 * <p>
 * It is permitted to have a command-line call which provides the same variable
 * name more than once, to provide multiple values for that variable. For
 * instance the following command-line arguments are permitted in a single call:
 * <kbd>--path=/usr/bin --path=/usr/local/bin</kbd> and both of these values
 * will be stored in the map for this variable name "path".</p>
 * <p>
 * It is not possible to use the same name for both a flag and a variable.</p>
 *
 * @author Bobulous
 */
public final class MainArgsHandler {

	public static final Interval<Integer> ZERO_OR_ONE = new GenericInterval<Integer>(0, 1);
	public static final Interval<Integer> ONE_EXACTLY = new GenericInterval<Integer>(1, 1);
	public static final Interval<Integer> ONE_OR_MORE = new GenericInterval<Integer>(1, null);

	/**
	 * Keeps track of the state of this argsutil.MainArgsHandler object. Most public
	 * methods require the object to be in a certain state in order to run, and
	 * calling them at the wrong time will lead to an IllegalStateException
	 * being thrown.
	 */
	private static enum Status {

		/**
		 * Pre-processing state, before processMainArgs has been called.
		 */
		PREPRO,
		/**
		 * Processing state, during processMainArgs method execution.
		 */
		PROCESSING,
		/**
		 * Post-processing state, after processMainArgs has finished
		 * successfully.
		 */
		POPRO
	}

	/**
	 * Represents a flag permitted as a command-line argument.
	 */
	private static class Flag {

		/**
		 * The name of this command-line flag.
		 */
		String name;
		/**
		 * The description intended to advise the application user about this
		 * command-line flag and the effect it has on the application.
		 */
		String usageDescription;
		/**
		 * Will be true if this flag should not be included in the list of
		 * command-line flags supported by the Java application, false
		 * otherwise.
		 */
		boolean forTestUseOnly;

		public Flag(String name, String usageDescription, boolean testUseOnly) {
			this.name = name;
			this.usageDescription = usageDescription;
			this.forTestUseOnly = testUseOnly;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Flag)) {
				return false;
			}
			Flag that = (Flag) obj;
			return this.name.equals(that.name);
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
			return hash;
		}
	}

	/**
	 * Represents a variable permitted as a command-line argument.
	 */
	private static class Variable {

		/**
		 * The name of this command-line variable.
		 */
		String name;
		/**
		 * The number of times this variable must appear on the command-line.
		 */
		Interval<Integer> permittedCountInterval;
		/**
		 * The description intended to advise the application user about this
		 * command-line variable and the sort of values it can take.
		 */
		String usageDescription;
		/**
		 * A Pattern which defines what is a valid value for this command-line
		 * variable.
		 */
		Pattern valuePattern;
		/**
		 * Will be true if this variable should not be included in the list of
		 * command-line variables supported by the Java application, false
		 * otherwise.
		 */
		boolean forTestUseOnly;

		public Variable(String name,
				Interval<Integer> permittedCountInterval,
				String usageDescription, Pattern valuePattern,
				boolean testUseOnly) {
			this.name = name;
			this.permittedCountInterval = permittedCountInterval;
			this.usageDescription = usageDescription;
			this.valuePattern = valuePattern;
			this.forTestUseOnly = testUseOnly;
		}

		/* The command-line variable name is the primary and sole key, so this
		 equals method considers only the variable name when judging equality.
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Variable)) {
				return false;
			}
			Variable that = (Variable) obj;
			return this.name.equals(that.name);
		}

		/* The command-line variable name is the primary and sole key, so this
		 hashCode method conisders only the variable name when calculating a hash
		 value.
		 */
		@Override
		public int hashCode() {
			int hash = 7;
			hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
			return hash;
		}
	}

	/**
	 * The current Status of this argsutil.MainArgsHandler.
	 */
	private Status objectStatus;
	/**
	 * The set of flags which the client has declared acceptable.
	 */
	private final Map<String, Flag> permittedFlags;
	/**
	 * The set of variables which the client has declared acceptable.
	 */
	private final Map<String, Variable> permittedVariables;
	/**
	 * The set of flags found in the command-line arguments (supplied by the
	 * user).
	 */
	private Set<String> commandLineFlagSet;
	/**
	 * The set of variables found in the command-line arguments (supplied by the
	 * user).
	 */
	private Map<String, List<String>> commandLineVariableValues;
	/**
	 * The sole instance of this class.
	 */
	private static MainArgsHandler singletonInstance;
	// For reasons beyond me, this first pattern was working absolutely fine in
	// over eighty tests until I added test case
	// "quickbrownfoxjumpsoverthelazyfoxZ" (notice the capital Z at the end
	// which causes the String to become an invalid name. Suddenly the Java
	// Matcher method matcher() was hanging every time it reached this test
	// even though it was fine on every other case, including other simple a-z
	// strings which ended with "Z". Very weird. The alternative regex seems
	// to work fine in all cases, but who knows?
//	private static final String NAME_REGEX = "([a-z](?:-?[a-z]+)*)";
	/**
	 * The Pattern which defines a valid name for a flag or variable.
	 */
	private final Pattern NAME_PATTERN = Pattern.compile(
			"([a-zA-Z](?:-[a-zA-Z\\u002E]|[a-zA-Z\\u002E])*)");

	private MainArgsHandler() {
		this.objectStatus = MainArgsHandler.Status.PREPRO;
//		this.permittedFlagSet = new HashSet<String>();
//		this.permittedVariableNameCounts =
//				new HashMap<String, Interval<Integer>>();
		this.permittedFlags = new HashMap<>();
		this.permittedVariables = new HashMap<>();
	}

	/**
	 * Returns the singleton instance object of the argsutil.MainArgsHandler class.
	 *
	 * @return the sole argsutil.MainArgsHandler object.
	 */
	public static MainArgsHandler getHandler() {
		if (MainArgsHandler.singletonInstance == null) {
			MainArgsHandler.singletonInstance = new MainArgsHandler();
		}
		return MainArgsHandler.singletonInstance;
	}

	/**
	 * Package-private method for JUnit testing. Allows the singleton to be
	 * wiped away so that a new handler can be instantiated and tested in JUnit.
	 * Without this method being available to the test classes within this
	 * package, every JUnit test case would be working on the same handler,
	 * which would only allow one use before hurling exceptions like cluster
	 * bombs. Which would be undesirable.
	 */
	static void nukeHandler() {
		MainArgsHandler.singletonInstance = null;
	}

	/**
	 * Reports on whether the specified string would be accepted by
	 * argsutil.MainArgsHandler as a valid name for a command-line flag or variable.
	 *
	 * @param name the string to test.
	 * @return true if the specified string would be a valid name for a flag or
	 * variable.
	 */
	private boolean validName(String name) {
		return this.NAME_PATTERN.matcher(name).matches();
	}

	/**
	 * Instruct this handler to accept a command-line flag with the specified
	 * name. The specified flag can occur zero or one times in the command-line
	 * arguments for them to be accepted as valid by this handler.
	 * <p>
	 * <strong>Note:</strong> do not include the initial double-hyphen in the
	 * flag name when calling <code>permitFlag</code>.</p>
	 * <p>
	 * A flag name must begin with lowercase a-z and can then contain lowercase
	 * a-z terms separated by single hyphens. Consecutive hyphens cannot appear
	 * within flag names, so <kbd>flag--name</kbd> would be illegal, for
	 * example.</p>
	 *
	 * @param flagName the name of a flag permitted by this handler.
	 * @throws IllegalStateException if this method is called after the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if this method is provided with a flag
	 * name which does not fit the permitted pattern.
	 */
	public void permitFlag(String flagName) {
		this.permitFlag(flagName, null, false);
	}

	/**
	 * Instruct this handler to accept a command-line flag with the specified
	 * name and usage description text. The specified flag can occur zero or one
	 * times in the command-line arguments for them to be accepted as valid by
	 * this handler.
	 * <p>
	 * <strong>Note:</strong> do not include the initial double-hyphen in the
	 * flag name when calling <code>permitFlag</code>.</p>
	 * <p>
	 * A flag name must begin with lowercase a-z and can then contain lowercase
	 * a-z terms separated by single hyphens. Consecutive hyphens cannot appear
	 * within flag names, so <kbd>flag--name</kbd> would be illegal, for
	 * example.</p>
	 *
	 * @param flagName the name of a flag permitted by this handler.
	 * @param usageDescription a brief description of when to use this flag, for
	 * display to a user who needs help on calling the client application from
	 * the command-line.
	 * @throws IllegalStateException if this method is called after the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if this method is provided with a flag
	 * name which does not fit the permitted pattern.
	 */
	public void permitFlag(String flagName, String usageDescription) {
		this.permitFlag(flagName, usageDescription, false);
	}

	/**
	 * Instruct this handler to accept a command-line flag which is intended
	 * only for test purposes. This works in exactly the same way as the
	 * {@link #permitFlag(String, String)} method, but marks the flag as being
	 * for test use only, so that this flag is not displayed to end users if
	 * they are shown a list of permitted command-line flags. This method should
	 * be used to permit flags which should only ever be used by the developer
	 * of an application, and should not be commonly known to end users.
	 *
	 * @param flagName the name of the test flag to permit.
	 * @throws IllegalStateException if this method is called after the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if this method is provided with a flag
	 * name which does not fit the permitted pattern.
	 */
	public void permitTestFlag(String flagName) {
		this.permitFlag(flagName, null, true);
	}

	private void permitFlag(String flagName, String usageDescription,
			boolean testUseOnly) {
		if (this.objectStatus != Status.PREPRO) {
			throw new IllegalStateException("Cannot add to the set of "
					+ "permitted flag names once processing of main arguments "
					+ "has already begun.");
		}
		if (!this.validName(flagName)) {
			throw new IllegalArgumentException("Cannot permit flag with name ["
					+ flagName + "] because this name does not fit the "
					+ "accepted pattern.");
		}
		if (this.permittedVariables.containsKey(flagName)
				|| this.permittedFlags.containsKey(flagName)) {
			throw new IllegalArgumentException("Cannot permit flag with name ["
					+ flagName + "] because this is already a permitted flag "
					+ "or a permitted or required variable name.");
		}
		Flag newFlag = new Flag(flagName, usageDescription, testUseOnly);
		this.permittedFlags.put(flagName, newFlag);
	}

	/**
	 * Instruct this handler to accept command-line variables with the specified
	 * name, in any quantity which is permitted by the specified interval. Any
	 * String is permitted for the variable value.
	 * <p>
	 * <strong>Note:</strong> do not include the initial double-hyphen in the
	 * variable name when calling <code>permitVariable</code>.</p>
	 * <p>
	 * A variable name must begin with lowercase a-z and can then contain
	 * lowercase a-z terms separated by single hyphens. Consecutive hyphens
	 * cannot appear within variable names, so <kbd>variable--name</kbd> would
	 * be illegal, for example.</p>
	 *
	 * @param variableName the name of a variable permitted by this handler.
	 * @param permittedCountInterval an <code>Interval</code> of
	 * <code>Integer</code> values whose lower endpoint must be at least zero,
	 * and whose upper endpoint must be at least one. The lower endpoint cannot
	 * be <code>null</code> (equivalent to negative infinity) but the upper
	 * endpoint can be <code>null</code> (equivalent to positive infinity).
	 * @throws IllegalStateException if this method is called after the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if this method is provided with a
	 * variable name which does not fit the permitted pattern, or if the
	 * provided <code>Interval</code> has a <code>null</code> or negative value
	 * for its lower endpoint, or a value of less than one for its upper
	 * endpoint.
	 */
	public void permitVariable(String variableName,
			Interval<Integer> permittedCountInterval) {
		this.permitVariable(variableName, permittedCountInterval, null, null,
				false);
	}

	/**
	 * Instruct this handler to accept command-line variables with the specified
	 * name, in any quantity permitted by the specified interval, and attach the
	 * provided usage description text. Any String is accepted as the value of
	 * the variable.
	 * <p>
	 * <strong>Note:</strong> do not include the initial double-hyphen in the
	 * variable name when calling <code>permitVariable</code>.</p>
	 * <p>
	 * A variable name must begin with lowercase a-z and can then contain
	 * lowercase a-z terms separated by single hyphens. Consecutive hyphens
	 * cannot appear within variable names, so <kbd>variable--name</kbd> would
	 * be illegal, for example.</p>
	 *
	 * @param variableName the name of a variable permitted by this handler.
	 * @param permittedCountInterval an <code>Interval</code> of
	 * <code>Integer</code> values whose lower endpoint must be at least zero,
	 * and whose upper endpoint must be at least one. The lower endpoint cannot
	 * be <code>null</code> (equivalent to negative infinity) but the upper
	 * endpoint can be <code>null</code> (equivalent to positive infinity).
	 * @param usageDescription a brief description of how to use this variable,
	 * for display to a user who needs help on calling the client application
	 * from the command-line.
	 * @throws IllegalStateException if this method is called after the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if this method is provided with a
	 * variable name which does not fit the permitted pattern, or if the
	 * provided <code>Interval</code> has a <code>null</code> or negative value
	 * for its lower endpoint, or a value of less than one for its upper
	 * endpoint.
	 */
	public void permitVariable(String variableName,
			Interval<Integer> permittedCountInterval, String usageDescription) {
		this.permitVariable(variableName, permittedCountInterval,
				usageDescription, null, false);
	}

	/**
	 * Instruct this handler to accept command-line variables with the specified
	 * name, in any quantity permitted by the supplied interval, attach the
	 * provided usage description text, and instruct the variable to reject any
	 * value which does not entirely match the specified <code>Pattern</code>.
	 * <p>
	 * <strong>Note:</strong> do not include the initial double-hyphen in the
	 * variable name when calling <code>permitVariable</code>.</p>
	 * <p>
	 * A variable name must begin with lowercase a-z and can then contain
	 * lowercase a-z terms separated by single hyphens. Consecutive hyphens
	 * cannot appear within variable names, so <kbd>variable--name</kbd> would
	 * be illegal, for example.</p>
	 *
	 * @param variableName the name of a variable permitted by this handler.
	 * @param permittedCountInterval an <code>Interval</code> of
	 * <code>Integer</code> values whose lower endpoint must be at least zero,
	 * and whose upper endpoint must be at least one. The lower endpoint cannot
	 * be <code>null</code> (equivalent to negative infinity) but the upper
	 * endpoint can be <code>null</code> (equivalent to positive infinity).
	 * @param usageDescription a brief description of how to use this variable,
	 * for display to a user who needs help on calling the client application
	 * from the command-line.
	 * @param valuePattern a Pattern which determines whether or not a value
	 * provided to this command-line variable is valid. Can be <code>null</code>
	 * if any String value is acceptable as a value for this command-line
	 * variable.
	 * @throws IllegalStateException if this method is called after the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if this method is provided with a
	 * variable name which does not fit the permitted pattern, or if the
	 * provided <code>Interval</code> has a <code>null</code> or negative value
	 * for its lower endpoint, or a value of less than one for its upper
	 * endpoint.
	 */
	public void permitVariable(String variableName,
			Interval<Integer> permittedCountInterval, String usageDescription,
			Pattern valuePattern) {
		this.permitVariable(variableName, permittedCountInterval,
				usageDescription, valuePattern, false);
	}

	/**
	 * Instruct this handler to accept a command-line variable which is intended
	 * only for test purposes. This works in exactly the same way as the
	 * {@link #permitVariable(String, Interval)} method, but marks the variable
	 * as being for test use only, so that this command-line variable is not
	 * displayed to end-users if they are shown a list of permitted command-line
	 * variables. This method should be used to permit variables which should
	 * only ever be used by the developer of an application, and should not be
	 * commonly known to end users.
	 *
	 * @param variableName the name of a test variable permitted by this
	 * handler.
	 * @param permittedCountInterval an <code>Interval</code> of
	 * <code>Integer</code> values whose lower endpoint must be zero (because
	 * test variables must be optional), and whose upper endpoint must be at
	 * least one and can be <code>null</code> (equivalent to positive infinity).
	 * @throws IllegalStateException if this method is called after the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if this method is provided with a
	 * variable name which does not fit the permitted pattern, or if the
	 * provided interval has a non-zero value for its lower endpoint, or a value
	 * of less than one for its upper endpoint.
	 */
	public void permitTestVariable(String variableName,
			Interval<Integer> permittedCountInterval) {
		if (permittedCountInterval.getLowerEndpoint() != 0) {
			throw new IllegalArgumentException("The lower endpoint of the "
					+ "permitted count interval for a test variable must be "
					+ "zero, because a test variable must be optional.");
		}
		this.permitVariable(variableName, permittedCountInterval, null, null,
				true);
	}

	/**
	 * Instruct this handler to accept command-line variables intended for test
	 * purposes, which accept only values that match the specified
	 * <code>Pattern</code>. This works in exactly the same way as the
	 * {@link #permitVariable(String, Interval)} method, but marks the variable
	 * as being for test use only, so that this command-line variable is not
	 * displayed to end-users if they are shown a list of permitted command-line
	 * variables. This method should be used to permit variables which should
	 * only ever be used by the developer of an application, and should not be
	 * commonly known to end users.
	 *
	 * @param variableName the name of a test variable permitted by this
	 * handler.
	 * @param permittedCountInterval an <code>Interval</code> of
	 * <code>Integer</code> values whose lower endpoint must be zero (because
	 * test variables must be optional), and whose upper endpoint must be at
	 * least one and can be <code>null</code> (equivalent to positive infinity).
	 * @param valuePattern a <code>Pattern</code> which determines whether or
	 * not a value provided to this command-line variable is valid. Can be
	 * <code>null</code> if this command-line variable should accept any
	 * <code>String</code> as a valid value.
	 * @throws IllegalStateException if this method is called after the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if this method is provided with a
	 * variable name which does not fit the permitted pattern, or if the
	 * provided interval has a non-zero value for its lower endpoint, or a value
	 * of less than one for its upper endpoint.
	 */
	public void permitTestVariable(String variableName,
			Interval<Integer> permittedCountInterval, Pattern valuePattern) {
		if (permittedCountInterval.getLowerEndpoint() != 0) {
			throw new IllegalArgumentException("The lower endpoint of the "
					+ "permitted count interval for a test variable must be "
					+ "zero, because a test variable must be optional.");
		}
		this.permitVariable(variableName, permittedCountInterval, null,
				valuePattern,
				true);
	}

	/**
	 * Declares a permitted command-line variable with the specified name,
	 * quantity, and usage description, which accepts values of the specified
	 * Pattern, and specifies whether or not the variable is for test usage.
	 *
	 * @param variableName the name of the command-line variable.
	 * @param permittedCountInterval an <code>Interval</code> which specifies
	 * the number of times this command-line variable must occur when starting
	 * the client Java application.
	 * @param usageDescription the text which should be used to describe this
	 * command-line variable, or <code>null</code> if no description is to be
	 * provided to the application user. Note that the value of
	 * <code>usageDescription</code> is ignored if <code>testUseOnly</code> is
	 * set to <code>true</code>.
	 * @param valuePattern a Pattern which determines whether or not a value
	 * provided to this command-line variable is valid. Can be <code>null</code>
	 * if any String value is acceptable as a value for this command-line
	 * variable.
	 * @param testUseOnly should be <code>true</code> if this command-line
	 * variable is for developer test purposes only, and should not be listed in
	 * the usage description text for the Java application; <code>false</code>
	 * otherwise.
	 */
	private void permitVariable(String variableName,
			Interval<Integer> permittedCountInterval, String usageDescription,
			Pattern valuePattern, boolean testUseOnly) {
		if (this.objectStatus != Status.PREPRO) {
			throw new IllegalStateException("Cannot add to the list of "
					+ "permitted variables once processing of main arguments "
					+ "has already begun.");
		}
		if (permittedCountInterval.getLowerEndpoint() == null
				|| permittedCountInterval.getLowerEndpoint() < 0) {
			throw new IllegalArgumentException("Cannot specify a minimum "
					+ "variable count of less than zero.");
		}
		if (permittedCountInterval.getUpperEndpoint() != null
				&& permittedCountInterval.getUpperEndpoint() < 1) {
			throw new IllegalArgumentException("Cannot specify a maximum "
					+ "variable count of less than one.");
		}
		if (!this.validName(variableName)) {
			throw new IllegalArgumentException("Cannot permit variable with "
					+ "name [" + variableName + "] because this name does not "
					+ "fit the accepted pattern.");
		}
		if (this.permittedFlags.containsKey(variableName)
				|| this.permittedVariables.containsKey(variableName)) {
			throw new IllegalArgumentException("Cannot permit variable with "
					+ "name [" + variableName + "] because this is already a "
					+ "permitted flag name or permitted or required variable "
					+ "name.");
		}
		Variable newVariable
				= new Variable(variableName, permittedCountInterval,
						usageDescription, valuePattern, testUseOnly);
		this.permittedVariables.put(variableName, newVariable);
	}

	/**
	 * Process the command-line arguments passed to the <code>main</code> method
	 * of your Java application.
	 * <p>
	 * Call this method only after you have specified all permitted flag names
	 * by calling <code>permitFlag</code>, and all permitted variable names and
	 * counts by calling <code>permitVariable</code>.</p>
	 * <p>
	 * If any of the arguments found on the command-line are not found in the
	 * set of permitted flags or the set of permitted variable names then an
	 * IllegalArgumentException will be thrown.</p>
	 *
	 * @param args the array of strings passed to the main method of your
	 * application.
	 * @throws IllegalStateException if this method is called more than once.
	 * @throws IllegalArgumentException if a command-line argument is found
	 * which is a flag not found in the set of permitted flag names, or which is
	 * a variable not found in the set of permitted variable names.
	 */
	public void processMainArgs(String[] args) {
		Objects.requireNonNull(args, "The args parameter cannot be null.");
		if (this.objectStatus != Status.PREPRO) {
			throw new IllegalStateException("Cannot process main arguments "
					+ "twice.");
		}
		this.objectStatus = Status.PROCESSING; //switch to processing state

		// Dice the command-line arguments up into a set of flags and a set of
		// variables.
		this.diceArgs(args);

		// Check that variable counts are all within permitted argsutil.intervals.
		this.checkVariableCounts();

		this.objectStatus = Status.POPRO; //switch to post-processing state
	}

	/* 
	 * Gather the command-line arguments into a set of flag names and a Map
	 * which maps each variable name to a List of value strings.
	 */
	private void diceArgs(String[] args) {
		this.commandLineFlagSet = new HashSet<>();
		this.commandLineVariableValues = new HashMap<>();
		Pattern nameValuePattern = Pattern.compile(
				"^--" + this.NAME_PATTERN.pattern() + "(?:=(.+))?$");
		for (String arg : args) {
			Matcher nameValueMatcher = nameValuePattern.matcher(arg);
			if (nameValueMatcher.matches()) {
				int groupCount = nameValueMatcher.groupCount();
				if (groupCount != 2) {
					throw new RuntimeException("Regex pattern should return "
							+ "two subcapture groups, but another quantity "
							+ "was found.");
				}
				String key = nameValueMatcher.group(1);
				String value = nameValueMatcher.group(2);
				if (value == null) {
					// No value was found by the regex matcher, so treat this as
					// a simple command-line flag.
					if (!this.permittedFlags.containsKey(key)) {
						// This flag name does not appear in the list of
						// permitted flag names specified by the client.
						throw new IllegalArgumentException("Command-line flag ["
								+ key + "] is not permitted by this handler.");
					}
					if (commandLineFlagSet.contains(key)) {
						// This flag has already been provided on the args list
						// but it does not make sense for a flag to occur twice
						// in the same command-line call.
						throw new IllegalArgumentException("Command-line flag ["
								+ key
								+ "] appears twice. Each type of flag can "
								+ "only appear once in a set of command-line "
								+ "arguments.");
					}
					// This flag does not already exist in the map, so add it
					// and map its name to an empty List.
					commandLineFlagSet.add(key);
				} else {
					// A value was found by the regex matcher, so treat this as
					// a command-line variable which maps a variable-name to a
					// value.
					if (!this.permittedVariables.containsKey(key)) {
						// This flag name does not appear in the list of
						// permitted flag names specified by the client.
						throw new IllegalArgumentException("Command-line "
								+ "variable name [" + key + "] is not "
								+ "permitted by this application.");
					}
					// Check that the supplied value satisfies the regex Pattern
					// specified (if any) for this command-line variable.
					if (this.permittedVariables.get(key).valuePattern != null) {
						Matcher valueMatcher
								= this.permittedVariables.get(key).valuePattern.
								matcher(value);
						if (!valueMatcher.matches()) {
							throw new IllegalArgumentException(
									"The command-line variable [" + key
									+ "] does not permit the value \"" + value
									+ "\".");
						}
					}
					if (this.commandLineVariableValues.containsKey(key)) {
						// Mapping already exists, so there must be other values
						// already assigned to this variable argument.
						List<String> valueList = this.commandLineVariableValues.
								get(key);
						// A list of values already exists, so add this
						// newest value to the list.
						valueList.add(value);
					} else {
						// This variable name is not already found in the map, so
						// create a mapping from this variable name to a list of
						// values and add this first value.
						List<String> newValueList = new ArrayList<>();
						newValueList.add(value);
						this.commandLineVariableValues.put(key, newValueList);
					}
				}
			} else {
				throw new IllegalArgumentException("Command-line argument `"
						+ arg + "` is not correctly formatted.");
			}
		}
	}

	/*
	 * Check that variables occur as few and as many times as permitted by this
	 * handler, and throw an exception if the actual count is outside of the
	 * permitted interval.
	 */
	private void checkVariableCounts() {
		// By this point, all of the variable names acting as keys in
		// the commandLineVariableValues map must be permitted variable names
		// (because this check is performed by the argsDicer method when each
		// variable name is found in the command-line arguments)
		// but now we need to check that the occurrence count of each variable
		// falls within the permitted interval.
		for (String variableName : permittedVariables.keySet()) {
			// For each variable name in the permitted set . . .
			// Retrieve the permitted interval
			Interval<Integer> permittedCountInterval = permittedVariables.get(
					variableName).permittedCountInterval;
			// Determine the actual count provided on the command-line
			List<String> actualVariableValues = commandLineVariableValues.get(
					variableName);
			int actualVariableCount = (actualVariableValues == null ? 0
					: actualVariableValues.size());
			// Check whether the actual count is permitted by the interval.
			// If the actual count is outside of the permitted interval then
			// throw an exception.
			if (!permittedCountInterval.includes(actualVariableCount)) {
				StringBuilder sb = new StringBuilder("Variable \"");
				sb.append(variableName);
				sb.append("\" appears too ");
				sb.append((actualVariableCount < permittedCountInterval.
						getLowerEndpoint() ? "few" : "many"));
				sb.append(" times in command-line arguments. Permitted count ");
				sb.append("must be in ");
				sb.append(permittedCountInterval);
				sb.append(" but actual count was ");
				sb.append(actualVariableCount);
				sb.append('.');
				throw new IllegalArgumentException(sb.toString());
			}
		}
	}

	/**
	 * Reports on whether a specified flag name was found in the command-line
	 * arguments.
	 *
	 * @param flagName the flag name to check.
	 * @return true if the specified flag name was specified in the command-line
	 * arguments.
	 * @throws IllegalStateException if this method is called before the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if the specified flag name is not a name
	 * recognised by this handler instance.
	 */
	public boolean foundFlag(String flagName) {
		if (this.objectStatus != Status.POPRO) {
			throw new IllegalStateException("Cannot query the set of flags "
					+ "found on the command-line before processing has "
					+ "been completed by calling the processMainArgs method.");
		}
		if (!this.permittedFlags.containsKey(flagName)) {
			throw new IllegalArgumentException("Flag name " + flagName
					+ " is not "
					+ "in the set of permitted flag names, so it does not make "
					+ "sense to check for it in the set of flag names found on "
					+ "the command-line.");
		}
		return this.commandLineFlagSet.contains(flagName);
	}

	/**
	 * Reports on whether a specified variable name was found in the command
	 * line arguments.
	 * <p>
	 * Note that this method does not return any of the values attached to the
	 * variable name. It serves only to confirm the presence of a variable in
	 * the command-line arguments.</p>
	 *
	 * @param variableName the variable name to check.
	 * @return true if the specified variable name was found in the command-line
	 * arguments.
	 * @throws IllegalStateException if this method is called before the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if the specified variable name is not a
	 * name recognised by this handler instance.
	 */
	public boolean foundVariable(String variableName) {
		if (this.objectStatus != Status.POPRO) {
			throw new IllegalStateException("Cannot query the set of variables "
					+ "found on the command-line before processing has "
					+ "been completed by calling the processMainArgs method.");
		}
		if (!this.permittedVariables.containsKey(variableName)) {
			throw new IllegalArgumentException("Variable name " + variableName
					+ " is not in the set of permitted variable names, so it "
					+ "does not make sense to check for it in the set of "
					+ "variable names found on the command-line.");
		}
		return this.commandLineVariableValues.containsKey(variableName);
	}

	/**
	 * Returns the list of command-line values associated with the specified
	 * variable name.
	 *
	 * @param variableName the name of the command-line variable whose values
	 * you want.
	 * @return a <code>List&lt;String&gt;</code> object which contains the
	 * values found with the specified variable name on the command-line. The
	 * <code>List</code> will be empty if the command-line variable is optional
	 * and the application user did not provide a value for it.
	 * @throws IllegalStateException if this method is called before the
	 * {@link #processMainArgs(String[])} method.
	 * @throws IllegalArgumentException if the specified variable name is not a
	 * name recognised by this handler instance.
	 */
	public List<String> getValuesFromVariable(String variableName) {
		if (this.objectStatus != Status.POPRO) {
			throw new IllegalStateException("Cannot query the set of variable "
					+ "values found on the command-line before processing has "
					+ "been completed by calling the processMainArgs method.");
		}
		if (!this.permittedVariables.containsKey(variableName)) {
			throw new IllegalArgumentException("Variable name " + variableName
					+ " is not in the set of permitted variables, so it does "
					+ "not make sense to check for it in the set of variables "
					+ "found on the command-line.");
		}
		List<String> valueList = this.commandLineVariableValues.
				get(variableName);
		if (valueList == null) {
			return new ArrayList<>();
		}
		return valueList;
	}

	/**
	 * Generates a <code>String</code> which describes the list of command-line
	 * flags and variables which are permitted or required by the
	 * <code>argsutil.MainArgsHandler</code> for the running application. It is
	 * recommended to display the content of this <code>String</code> to the
	 * application user if they attempt to call the Java application with
	 * invalid command-line flags or variables.
	 * <p>
	 * Note that the returned <code>String</code> does not contain any mention
	 * of test flags or variables, as those are only intended for use by
	 * developers and the content of this <code>String</code> is intended for
	 * display to application end-users.</p>
	 * <p>
	 * This method cannot be called before the
	 * {@link #processMainArgs(String[])} method has been called (otherwise it
	 * is not guaranteed that all permitted flags and variables have been
	 * declared).</p>
	 *
	 * @throws IllegalStateException if this method is called before the
	 * {@link #processMainArgs(String[])} method.
	 * @return a <code>String</code> which describes the list of flags and
	 * variables which can be supplied to the running application.
	 */
	public String getUsageSummary() {
		if (this.objectStatus == Status.PREPRO) {
			throw new IllegalStateException("Cannot request usage summary text "
					+ "before processing has begun.");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Valid command-line options:\n\n");
		StringBuilder flagUsage = new StringBuilder();
		flagUsage.append("Flags\nA flag has the form --flag-name (two hyphens "
				+ "and then the flag name in lowercase). A flag cannot take a "
				+ "value. "
				+ "Flags are separated from other flags and variables by a "
				+ "space. "
				+ "Flags are always optional, and each flag can be supplied "
				+ "at most once.\n\n");

		flagUsage.append(
				"The following command-line flags are accepted by this "
				+ "application:\n\n");
		int nonTestFlagCount = 0;
		for (Flag flag : this.permittedFlags.values()) {
			if (flag.forTestUseOnly) {
				// Do not show test flags as part of the end-user usage guide.
				continue;
			}
			++nonTestFlagCount;
			flagUsage.append("\t--");
			flagUsage.append(flag.name);
			if (flag.usageDescription != null
					&& !flag.usageDescription.isEmpty()) {
				flagUsage.append("\n\t\t");
				flagUsage.append(flag.usageDescription);
			}
			flagUsage.append("\n\n");
		}
		if (nonTestFlagCount == 0) {
			sb.append("Flags\nNo command-line flags are required or accepted "
					+ "by this application.\n\n");
		} else {
			sb.append(flagUsage.toString());
		}
		StringBuilder variableUsage = new StringBuilder();
		variableUsage.append("Variables\nA variable must have the form "
				+ "--variable-name=VALUE (two hyphens, then the variable name "
				+ "in lowercase, then an equals symbol, then the value you "
				+ "want to provide for the variable). "
				+ "Variables are separated from other variables and flags "
				+ "by a space, so if you want to provide a value which "
				+ "contains a space you must wrap the whole value in "
				+ "double-quote symbols. A variable may be supplied more "
				+ "than once (see the usage information for each variable to "
				+ "check how many times it can occur).\n\n");
		variableUsage.append("The following command-line variables "
				+ "are accepted by this application:\n\n");
		int nonTestVariableCount = 0;
		for (Variable variable : this.permittedVariables.values()) {
			if (variable.forTestUseOnly) {
				// Do not show test variables as part of end-user usage guide.
				continue;
			}
			++nonTestVariableCount;
			variableUsage.append("\t--");
			variableUsage.append(variable.name);
			variableUsage.append("\n\t");
			variableUsage.
					append(intervalAsText(variable.permittedCountInterval));
			if (variable.usageDescription != null && !variable.usageDescription.
					isEmpty()) {
				variableUsage.append("\n\t\t");
				variableUsage.append(variable.usageDescription);
			}
			variableUsage.append("\n\n");
		}
		if (nonTestVariableCount == 0) {
			sb.append("Variables\nNo command-line variables are required or "
					+ "accepted by this application.\n\n");
		} else {
			sb.append(variableUsage.toString());
		}
		return sb.toString();
	}

	private String intervalAsText(Interval<Integer> interval) {
		int lower = interval.getLowerEndpoint();
		if (interval.getUpperEndpoint() == null) {
			if (lower == 0) {
				return "Optional, can occur ANY number of times.";
			}
			return "Must occur at least " + (lower == 1 ? "once." : lower
					+ " times.");
		}
		int upper = interval.getUpperEndpoint();
		if (lower == 0) {
			if (upper == 1) {
				return "Optional, can occur at most once.";
			}
			return "Optional, can occur up to " + upper
					+ " times.";
		}
		// Lower endpoint greater than zero.
		if (lower == upper) {
			return "Must occur exactly " + (upper == 1 ? "once." : upper
					+ " times.");
		}
		// Endpoints not equal.
		return "Must occur at least " + (lower == 1 ? "once" : lower + " times")
				+ " and at most " + upper + " times.";
	}
}
