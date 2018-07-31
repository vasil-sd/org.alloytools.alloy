package org.alloytools.alloy.core.api;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.alloytools.alloy.solver.api.AlloySolver;

/**
 * Primary interface into Alloy.
 */
public interface Alloy {
	/**
	 * Get a list of available solvers
	 * 
	 * @return a list of available solvers
	 */
	List<AlloySolver> getSolvers();

	/**
	 * Get a solver with a specific name
	 * 
	 * @param id
	 *            the name of the solver
	 * @return and optional AlloySolver
	 */
	Optional<AlloySolver> getSolver(String id);

	/**
	 * Return the compiler but providing a resolver for abstracting where the
	 * content is coming from.
	 * 
	 * @param resolver
	 *            abstracts the file system
	 * @return an Alloy compiler
	 */
	AlloyCompiler compiler(SourceResolver resolver);

	/**
	 * Return a default compiler based on the current file system
	 * 
	 * @return a compiler
	 */
	AlloyCompiler compiler();

	/**
	 * Get a file in the Alloy private directory. The intention for this path is
	 * to be used by solvers or visualizers for caches and preferences.
	 * 
	 * @param pathWithSlashes
	 *            a path separated with slashes also on windows
	 * @return a Path to a file on the file system.
	 */

	Path getFile(String pathWithSlashes);
}
