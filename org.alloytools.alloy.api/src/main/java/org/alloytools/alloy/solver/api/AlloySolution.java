package org.alloytools.alloy.solver.api;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A solution is the answer of a solver. It can either be satisfied or not. If
 * it is satisfied then you can ask for the _instances_.
 * 
 */
public interface AlloySolution extends Iterable<AlloyInstance> {

	/**
	 * Answer if this solution is satisfied.
	 */
	boolean isSatisfied();

	/**
	 * Provides access to a root tupleset which can be used to create new
	 * tuplesets.
	 * 
	 * @return a tuple set representing none.
	 */
	ITupleSet none();
	
	
	default Stream<AlloyInstance> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}
}
