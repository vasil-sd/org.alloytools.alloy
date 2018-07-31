package org.alloytools.alloy.solver.api;

import org.osgi.dto.DTO;

/**
 * Options that can be passed to Alloy and its solvers. Some solvers extend this
 * DTO with their own preferences.
 * 
 */
public class AlloyOptions extends DTO {
	/**
	 * Enable tracing
	 */
	boolean trace = false;
}
