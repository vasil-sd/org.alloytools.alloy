package org.alloytools.alloy.core.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an Alloy Module
 */
public interface AlloyModule {

	/**
	 * The source path of this module. In certain cases
	 * 
	 * @return
	 */
	Optional<String> getPath();

	/**
	 * Get the defined sigs in this module
	 * 
	 * @return a list of sigs
	 */
	Set<TSig> getSigs();

	/**
	 * Get a sig by name
	 * 
	 * @param name
	 *            the name of the request sig
	 * @return an optional TSig
	 */
	Optional<TSig> getSig(String name);

	/**
	 * Get any run commands defined in the module
	 * 
	 * @return the list of available run commands
	 */
	List<TRun> getRuns();

	/**
	 * Get any check commands defined in the module
	 * 
	 * @return the list of available check commands
	 */
	List<TCheck> getChecks();

	/**
	 * Get compiler warnings. Warnings are messages that should be corrected but
	 * do not stop the module from becoming unusable.
	 * 
	 * @return compiler warnings
	 */
	List<CompilerMessage> getWarnings();

	/**
	 * Get fatal compiler errors
	 * 
	 * @return compiler errors
	 */
	List<CompilerMessage> getErrors();

	/**
	 * Return true if this module had no fatal errors.
	 * 
	 * @return true if no fatal errors
	 */
	boolean isValid();

	/**
	 * Get the options in the source for the given command. A source option is
	 * specified with {@code--option[suffix] option}. The suffix is by default
	 * {@code *} which implies all.
	 * 
	 * @param command
	 * @return
	 */
	Map<String, String> getSourceOptions(TCommand command);

	/**
	 * TODO mark an option as used for required
	 * 
	 * @param optionKey
	 */
	void usedOption(String optionKey);

	/**
	 * Return the compiler that compiled this module.
	 * 
	 * @return the compiler
	 */
	AlloyCompiler getCompiler();
}
