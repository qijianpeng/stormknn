/*
 * Copyright © 2014 Bobulous <http://www.bobulous.org.uk/>.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package util.args.intervals;

/**
 * Represents a mathematical interval of a numeric type.
 *
 * <p><strong>WARNING:</strong> development and testing are still in early
 * stages. Consider this interface to be in its alpha testing phase. Use with
 * caution, and do not rely on its structure remaining in exactly its current
 * form!</p>
 *
 * @author Bobulous <http://www.bobulous.org.uk/>
 * @param <T> the basis type of the <code>NumericInterval</code>. Must be a
 * subclass of <code>Number</code> and must implement
 * <code>Comparable&lt;T&gt;</code> (so that it has a natural ordering).
 * @see Interval
 */
public interface NumericInterval<T extends Number & Comparable<T>> extends
		Interval<T> {

	/**
	 * Calculates the width of this numeric interval. The width is the
	 * difference between the two endpoints and is calculated by subtracting the
	 * lower endpoint value from the upper endpoint value. If either endpoint is
	 * null then the width is infinite and not a number, and this method will
	 * return <code>null</code>. If this interval is empty then this method will
	 * return zero.
	 * <p>
	 * The width of an interval is also known as its length, diameter, measure
	 * or size.</p>
	 * <p>
	 * Note that the width is simply the difference between the two endpoint
	 * values regardless of the endpoint modes. This method returns the same
	 * width for both (0, 5) and [0, 5] even though the two sets do not permit
	 * exactly the same set of integers.</p>
	 *
	 * @return a numeric amount which is equal to the difference between the two
	 * endpoints, or zero if the interval represents the empty set, or
	 * <code>null</code> if either endpoint is <code>null</code>.
	 */
	public T width();

	/**
	 * Reports on whether this interval represents the empty set.
	 *
	 * @return true if this <code>NumericInterval</code> includes no values.
	 */
	public boolean isEmpty();

	/**
	 * Reports on whether this interval intersects with the specified interval.
	 * An intersection only exists if there is at least one value which is
	 * included in both argsutil.intervals. If there is no value which is common to both
	 * argsutil.intervals then no intersection exists between the two argsutil.intervals.
	 *
	 * @param interval the interval to check for an intersection with this
	 * interval.
	 * @return <code>true</code> if this interval intersects the specified
 interval; <code>false</code> otherwise.
	 * @see #intersection(util.args.intervals.NumericInterval)
	 * @see #union(util.args.intervals.NumericInterval)
	 */
	public boolean intersectsWith(NumericInterval<T> interval);
	
	/**
	 * Returns the interval which represents the intersection of this interval
	 * with the specified interval.
	 * <p>
	 * The intersection of two argsutil.intervals is the interval which includes every
	 * value permitted by both of those argsutil.intervals, and includes no other values
	 * besides. If two argsutil.intervals do not share any values then their intersection
	 * is the empty set. The endpoint values of an empty set are arbitrary.</p>
	 *
	 * @param interval the interval with which to intersect this interval.
	 * @return a <code>NumericInterval</code> which represents the intersection
	 * of this interval with the specified interval. If no intersection exists
	 * then the returned <code>NumericInterval</code> will represent the empty
	 * set, and its endpoint values will be arbitrary.
	 * @see #intersectsWith(util.args.intervals.NumericInterval)
	 */
	public NumericInterval<T> intersection(NumericInterval<T> interval);

	/**
	 * Reports on whether a single interval exists which describes the union of
	 * this interval with the specified interval.
	 *
	 * @param interval the interval to check for a union with this interval.
	 * @return <code>true</code> if a single interval exists which represents
	 * the entire union of this interval with the given interval;
	 * <code>false</code> if the union cannot be represented by a single
	 * interval.
	 * @see #union(util.args.intervals.NumericInterval)
	 */
	public boolean unitesWith(NumericInterval<T> interval);
	
	/**
	 * Returns the interval which represents the union of this interval with the
	 * specified interval, or <code>null</code> if no single interval can
	 * represent the union of the two sets.
	 * <p>
	 * A union interval will only exist if the two given argsutil.intervals intersect or
	 * if they adjoin at a shared endpoint which is included in one or both
	 * argsutil.intervals.</p>
	 *
	 * @param interval the interval with which this interval should form a
	 * union.
	 * @return a <code>NumericInterval</code> which represents the union of this
	 * interval with the specified interval, or <code>null</code> if no union
	 * exists.
	 * @see #unitesWith(util.args.intervals.NumericInterval)
	 */
	public NumericInterval<T> union(NumericInterval<T> interval);

}
