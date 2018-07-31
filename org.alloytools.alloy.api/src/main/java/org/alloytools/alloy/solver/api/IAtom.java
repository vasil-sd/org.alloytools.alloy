package org.alloytools.alloy.solver.api;

import org.alloytools.alloy.core.api.TSig;

/**
 * An atom is an immutable object that has no meaning. Atoms always belong to a
 * single solution.
 */
public interface IAtom extends Comparable<IAtom> {

	/**
	 * Return a human readble name for the atom that is unique for all atoms of
	 * the solution.
	 * 
	 * @return the unique name of the atom
	 */
	String getName();

	/**
	 * Get the owning solution
	 * 
	 * @return the owner
	 */
	AlloySolution getSolution();

	/**
	 * Get the signature of this atom
	 * 
	 * @return the signature
	 */
	TSig getSig();

	/**
	 * Convert the atom to an ITupleSet
	 * 
	 * @return a tupleset with one atom
	 */
	ITupleSet asTupleSet();

	/**
	 * Each atom has a unique index int the universe.
	 * 
	 * TODO necessary?
	 * 
	 * @return the universe index
	 */
	int getIndex();

	/**
	 * Join this atom with a tuple set
	 * 
	 * @param right
	 *            the tuple set to join with
	 * @return a new tuple set that is the Alloy join of this and the right
	 */
	default ITupleSet join(ITupleSet right) {
		return asTupleSet().join(right);
	}

	/**
	 * Create the product atom with a tuple set
	 * 
	 * @param right
	 *            the tuple set to create the product with
	 * @return a new tuple set that is the Alloy product of this and the right
	 */
	default ITupleSet product(ITupleSet right) {
		return asTupleSet().product(right);
	}

	/**
	 * Provides a new tupleset with itself
	 * 
	 * @return a tupleset of itself
	 */
	default ITupleSet head() {
		return asTupleSet();
	}

	/**
	 * An empty tuple set
	 * 
	 * @return an empty tuple set
	 */
	default ITupleSet tail() {
		return getSolution().none();
	}

	/**
	 * See {@link #equals(Object)}
	 * 
	 * @return a hashcode
	 */
	int hashCode();

	/**
	 * Equality is when the other object has identity equality
	 * 
	 * @return true if the other atom == this atom
	 */
	boolean equals(Object o);

	/**
	 * TODO needed?
	 * 
	 * @return
	 */
	default int toInt() {
		return Integer.parseInt(getName());
	}
}
