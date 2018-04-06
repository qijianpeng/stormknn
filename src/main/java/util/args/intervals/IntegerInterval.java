/*
 * Copyright © 2015 Bobulous <http://www.bobulous.org.uk/>.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package util.args.intervals;

import java.util.Objects;

/**
 * An immutable <code>NumericInterval</code> whose endpoints have type
 * <code>Integer</code>.
 *
 * <p>
 * <strong>WARNING:</strong> development and testing are still in early stages.
 * Consider this class to be in its alpha testing phase. Use with caution, and
 * do not rely on its structure remaining in exactly its current form!</p>
 *
 * @author Bobulous <http://www.bobulous.org.uk/>
 */
public final class IntegerInterval implements NumericInterval<Integer> {

	private final Integer lower;
	private final Integer upper;

	private final EndpointMode lowerMode;
	private final EndpointMode upperMode;

	/**
	 * An interval which permits no values: the empty set. Note that the
	 * endpoint values are arbitrary and that any <code>IntegerInterval</code>
	 * which is empty is considered equal to this instance of the empty set.
	 */
	public static final IntegerInterval EMPTY_SET = new IntegerInterval(
			EndpointMode.OPEN, 0, 0, EndpointMode.OPEN);

	/**
	 * An interval which includes all <code>Integer</code> values.
	 */
	public static final IntegerInterval UNBOUNDED = new IntegerInterval(
			EndpointMode.OPEN, null, null, EndpointMode.OPEN);

	/**
	 * An interval which includes only the integers zero and one.
	 */
	public static final IntegerInterval ZERO_OR_ONE = new IntegerInterval(
			EndpointMode.CLOSED, 0, 1, EndpointMode.CLOSED);

	/**
	 * An interval which includes all non-negative <code>Integer</code> values.
	 * Any <code>Integer</code> value greater-than-or-equal-to zero is included.
	 */
	public static final IntegerInterval ZERO_OR_MORE = new IntegerInterval(
			EndpointMode.CLOSED, 0, null, EndpointMode.OPEN);

	/**
	 * An interval which includes all positive, non-zero <code>Integer</code>
	 * values. Any <code>Integer</code> value greater-than-or-equal-to one is
	 * included.
	 */
	public static final IntegerInterval ONE_OR_MORE = new IntegerInterval(
			EndpointMode.CLOSED, 1, null, EndpointMode.OPEN);

	/*
	Private constructor because static methods are provided for the creation of
	argsutil.intervals with different endpoint modes. (This also allows for the option of
	adding caching of common instances in future.)
	*/
	private IntegerInterval(EndpointMode lowerMode, Integer lower, Integer upper,
			EndpointMode upperMode) {
		this.lower = lower;
		this.upper = upper;
		this.lowerMode = lowerMode;
		this.upperMode = upperMode;
	}

	/**
	 * Constructs a closed interval with the given integer endpoint values.
	 *
	 * @param lower the value of the lower endpoint or <code>null</code> if the
	 * lower endpoint is unbounded (infinite).
	 * @param upper the value of the upper endpoint or <code>null</code> if the
	 * upper endpoint is unbounded (infinite).
	 * @return an <code>IntegerInterval</code> with the given endpoint values
	 * and both endpoint modes set to <code>EndpointMode.CLOSED</code>.
	 */
	public static final IntegerInterval closed(Integer lower, Integer upper) {
		return new IntegerInterval(EndpointMode.CLOSED, lower, upper,
				EndpointMode.CLOSED);
	}

	/**
	 * Constructs an open interval with the given integer endpoint values.
	 *
	 * @param lower the value of the lower endpoint or <code>null</code> if the
	 * lower endpoint is unbounded (infinite).
	 * @param upper the value of the upper endpoint or <code>null</code> if the
	 * upper endpoint is unbounded (infinite).
	 * @return an <code>IntegerInterval</code> with the given endpoint values
	 * and both endpoint modes set to <code>EndpointMode.OPEN</code>.
	 */
	public static final IntegerInterval open(Integer lower, Integer upper) {
		return new IntegerInterval(EndpointMode.OPEN, lower, upper,
				EndpointMode.OPEN);
	}

	/**
	 * Constructs a left-closed interval with the given integer endpoint values.
	 *
	 * @param lower the value of the lower endpoint or <code>null</code> if the
	 * lower endpoint is unbounded (infinite).
	 * @param upper the value of the upper endpoint or <code>null</code> if the
	 * upper endpoint is unbounded (infinite).
	 * @return an <code>IntegerInterval</code> with the given endpoint values
	 * and the lower endpoint mode set to <code>EndpointMode.CLOSED</code> and
	 * the upper endpoint mode set to <code>EndpointMode.OPEN</code>.
	 */
	public static final IntegerInterval leftClosed(Integer lower, Integer upper) {
		return new IntegerInterval(EndpointMode.CLOSED, lower, upper,
				EndpointMode.OPEN);
	}

	/**
	 * Constructs a right-closed interval with the given integer endpoint
	 * values.
	 *
	 * @param lower the value of the lower endpoint or <code>null</code> if the
	 * lower endpoint is unbounded (infinite).
	 * @param upper the value of the upper endpoint or <code>null</code> if the
	 * upper endpoint is unbounded (infinite).
	 * @return an <code>IntegerInterval</code> with the given endpoint values
	 * and the lower endpoint mode set to <code>EndpointMode.OPEN</code> and the
	 * upper endpoint mode set to <code>EndpointMode.CLOSED</code>.
	 */
	public static final IntegerInterval rightClosed(Integer lower, Integer upper) {
		return new IntegerInterval(EndpointMode.OPEN, lower, upper,
				EndpointMode.CLOSED);
	}

	@Override
	public Integer width() {
		if (this.isEmpty()) {
			return 0;
		}
		if (upper == null || lower == null) {
			return null;
		}
		return upper - lower;
	}

	@Override
	public boolean isEmpty() {
		if (upper == null || lower == null) {
			// Unbounded argsutil.intervals are never empty.
			return false;
		}
		if (upper < lower) {
			// If the upper endpoint is less than the lower endpoint then all
			// integers are excluded, so the interval is empty.
			return true;
		}
		if (upper.equals(lower)) {
			// If the endpoint values are equal then both endpoints must be
			// closed to permit that value. If either is open then even that
			// value is excluded and so the interval is empty.
			return getLowerEndpointMode().equals(EndpointMode.OPEN)
					|| getUpperEndpointMode().equals(EndpointMode.OPEN);
		}
		if (upper - lower == 1) {
			// If the difference between the endpoints is exactly one and both
			// endpoints are open then neither of the endpoint values is
			// included by the interval and there are no other values between
			// the two, making the interval empty. If either endpoint is closed
			// then that value is permitted by the interval and so it is not
			// empty.
			return getLowerEndpointMode().equals(EndpointMode.OPEN)
					&& getUpperEndpointMode().equals(EndpointMode.OPEN);
		}
		// To reach this point, the interval is bounded and its upper endpoint
		// is at least two greater than its lower endpoint, so at least one
		// integer is included by this interval and thus it is not empty.
		return false;
	}

	@Override
	public boolean intersectsWith(NumericInterval<Integer> interval) {
		if (interval == null) {
			throw new NullPointerException("Cannot pass a null value to "
					+ "intersects(Interval<T>).");
		}

		// If either interval is empty then there can be no intersection
		if (this.isEmpty() || interval.isEmpty()) {
			return false;
		}

		IntervalComparator<Integer> intervalComparator = IntervalComparator.
				<Integer>getInstance();

		// Use a comparator to order the two argsutil.intervals, so that we can reduce
		// the problem space and reduce the number of conditional tests
		Interval<Integer> first, second;
		int intervalComparison = intervalComparator.compare(this,
				interval);
		if (intervalComparison > 0) {
			// This interval is comparatively greater than the specified interval
			// so call the specified interval "first" and this interval "second".
			first = interval;
			second = this;
		} else if (intervalComparison < 0) {
			// If this interval is comparitively lesser than the specified
			// interval then call this interval "first" and the specified
			// interval "second"
			first = this;
			second = interval;
		} else {
			// Intervals are identical, and we already know that neither
			// interval is empty, so these identical, non-empty argsutil.intervals must
			// intersect
			return true;
		}

		// At this point we know that the two argsutil.intervals are not identical, that
		// neither of them is empty, and that the first interval is lesser than
		// the second interval according to the IntervalComparator.
		// Check to see whether the second interval starts before the first ends
		int gapComparison = endpointValuesLowerUpperCompare(second.
				getLowerEndpoint(),
				first.getUpperEndpoint());
		if (gapComparison > 0) {
			// The start value of the second interval is after than the end
			// value of the first interval. There can be no overlap.
			return false;
		}
		if (gapComparison == 0) {
			// The end of the first interval is the same value as the start of
			// the second interval. There can only be overlap if both of these
			// endpoints are CLOSED.
			return (first.getUpperEndpointMode().equals(EndpointMode.CLOSED)
					&& second.getLowerEndpointMode().equals(EndpointMode.CLOSED));
		}
		// At this point we know that the two argsutil.intervals overlap by some amount.
		// If the overlap has a width of exactly one then one of the endpoints
		// must be closed, otherwise neither of the endpoints values are common
		// to both argsutil.intervals and thus the intersection is empty.
		if (first.getUpperEndpoint() - second.getLowerEndpoint() == 1) {
			return first.getUpperEndpointMode().equals(EndpointMode.CLOSED)
					|| second.getLowerEndpointMode().equals(EndpointMode.CLOSED);
		}

		// At this point we know that the start of the second interval is not
		// greater than or equal to the end of the first interval. And by virtue
		// of being the "second" interval its start cannot be lower than the
		// start of the first. So the second interval starts within the first
		// interval and there must be an intersection.
		return true;
	}

	/**
	 * Compare a lower endpoint value with an upper endpoint value.
	 *
	 * @param lower the lower endpoint.
	 * @param upper the upper endpoint.
	 * @return a negative integer if either lower or upper endpoint is
	 * <code>null</code>, or if the value of the lower endpoint is lesser than
	 * the value of the upper endpoint; zero if both values are non-null and
	 * identical; a positive integer if both values are non-null and the lower
	 * endpoint value is greater than the upper endpoint value.
	 */
	private static int endpointValuesLowerUpperCompare(Integer lower,
			Integer upper) {
		if (lower == null || upper == null) {
			return -1;
		}
		return lower.compareTo(upper);
	}

	@Override
	public NumericInterval<Integer> intersection(
			NumericInterval<Integer> interval) {
		Objects.requireNonNull(interval);
		if (this.isEmpty() || interval.isEmpty()) {
			// If one or both argsutil.intervals are empty then there can be no
			// intersection, so return an empty set.
			return EMPTY_SET;
		}

		IntervalComparator<Integer> comparator = IntervalComparator.
				<Integer>getInstance();
		int comparison = comparator.compare(this, interval);

		// Sort the argsutil.intervals using IntervalComparator, so that the first
		// interval is the one whose lower endpoint permits the lowest value,
		// or in case of identical lower endpoints, the first interval is the
		// one whose upper endpoint permits the lowest value.
		Interval<Integer> first, second;
		if (comparison == 0) {
			// Both argsutil.intervals are identical, so return a reference to this
			// interval.
			return this;
		} else if (comparison < 0) {
			first = this;
			second = interval;
		} else {
			first = interval;
			second = this;
		}

		// Check that an intersection does exist.
		int secondLowerFirstUpperComparison = endpointValuesLowerUpperCompare(
				second.getLowerEndpoint(), first.getUpperEndpoint());
		if (secondLowerFirstUpperComparison > 0) {
			// The lower endpoint of the second interval is greater than the
			// upper endpoint of the first interval, so there can be no
			// intersection. Return the empty set.
			return EMPTY_SET;
		} else if (secondLowerFirstUpperComparison == 0) {
			// The lower endpoint of the second interval has the same value as
			// the upper endpoint of the first interval. So an intersection can
			// only occur if both endpoint modes are CLOSED.
			if (first.getUpperEndpointMode().equals(EndpointMode.OPEN)
					|| second.getLowerEndpointMode().equals(EndpointMode.OPEN)) {
				// Intervals are adjacent but do not include their shared
				// boundary, so there is no intersection. Return the empty set.
				return EMPTY_SET;
			}
		}

		// Lower endpoint value and mode for the intersection have to be equal
		// to the lower endpoint of the second interval, because of the way
		// that IntervalComparator has sorted them into order.
		Integer intersectionLowerEndpoint = second.getLowerEndpoint();
		EndpointMode intersectionLowerMode = second.getLowerEndpointMode();

		// Now determine which is lower (inclusive of the lowest values), the
		// upper endpoint of the first interval, or the upper endpoint of the
		// second interval.
		Integer intersectionUpperEndpoint;
		EndpointMode intersectionUpperMode;
		int upperComparison = comparator.
				upperEndpointValueCompare(first, second);
		if (upperComparison < 0) {
			// The upper endpoint of the first interval is the least inclusive,
			// so the intersection ends with it.
			intersectionUpperEndpoint = first.getUpperEndpoint();
			intersectionUpperMode = first.getUpperEndpointMode();
		} else {
			// Either the upper endpoint of the second interval is the least
			// inclusive, or both argsutil.intervals have an identical upper endpoint.
			// So take the upper endpoint of the second interval for the
			// intersection.
			intersectionUpperEndpoint = second.getUpperEndpoint();
			intersectionUpperMode = second.getUpperEndpointMode();
		}

		return new IntegerInterval(intersectionLowerMode,
				intersectionLowerEndpoint, intersectionUpperEndpoint,
				intersectionUpperMode);
	}

	@Override
	public boolean unitesWith(NumericInterval<Integer> interval) {
		Objects.requireNonNull(interval);
		// If either interval is empty then there can be no union.
		if (this.isEmpty() || interval.isEmpty()) {
			return false;
		}
		if (this.intersectsWith(interval)) {
			return true;
		}
		IntervalComparator<Integer> comparator = IntervalComparator.
				<Integer>getInstance();
		NumericInterval<Integer> first, second;
		int comparison = comparator.compare(this,
				interval);
		if (comparison < 0) {
			// This interval comes first according to IntervalComparator.
			first = this;
			second = interval;
		} else {
			first = interval;
			second = this;
		}
		// The two argsutil.intervals don't intersect, but they can still form a union if
		// they adjoin (share an endpoint value as a common boundary) and one or
		// both of the adjoining endpoints has a mode of CLOSED.
		if (first.getUpperEndpoint().compareTo(second.getLowerEndpoint()) == 0) {
			// The two argsutil.intervals adjoin, so check that at least one of them has
			// an endpoint mode of CLOSED at the adjoining boundary.
			if (first.getUpperEndpointMode().equals(EndpointMode.CLOSED)
					|| second.getLowerEndpointMode().equals(EndpointMode.CLOSED)) {
				return true;
			}
		}
		// The two argsutil.intervals neither intersect nor do they adjoin at an endpoint
		// which is included by one or both of the argsutil.intervals. There can be no
		// union, so return null.
		return false;
	}

	@Override
	public NumericInterval<Integer> union(NumericInterval<Integer> interval) {
		Objects.requireNonNull(interval);
		// If either interval is empty then there can be no union.
		if (this.isEmpty() || interval.isEmpty()) {
			return null;
		}
		IntervalComparator<Integer> comparator = IntervalComparator.
				<Integer>getInstance();
		NumericInterval<Integer> first, second;
		Integer unionLowerEndpoint, unionUpperEndpoint;
		EndpointMode unionLowerEndpointMode, unionUpperEndpointMode;
		int comparison = comparator.compare(this,
				interval);
		if (comparison < 0) {
			// This interval comes first according to IntervalComparator.
			first = this;
			second = interval;
		} else {
			first = interval;
			second = this;
		}
		if (first.intersectsWith(second)) {
			// The two interval intersect, so the union will be the lowest and
			// highest of the endpoints involved (and their modes).
			// We already know (thanks to the IntervalComparator check above)
			// that the first interval must have the endpoint which includes the
			// lowest values, so the union will take that endpoint value and
			// mode.
			unionLowerEndpoint = first.getLowerEndpoint();
			unionLowerEndpointMode = first.getLowerEndpointMode();
			// Now determine which endpoint includes the highest values and make
			// that the upper endpoint of the union.
			int upperComparison = comparator.upperEndpointValueCompare(first,
					second);
			if (upperComparison > 0) {
				// First interval has the greater upper endpoint.
				unionUpperEndpoint = first.getUpperEndpoint();
				unionUpperEndpointMode = first.getUpperEndpointMode();
			} else {
				// Second interval has the greater upper endpoint, or both
				// argsutil.intervals have an identical upper endpoint.
				unionUpperEndpoint = second.getUpperEndpoint();
				unionUpperEndpointMode = second.getUpperEndpointMode();
			}
			return new IntegerInterval(unionLowerEndpointMode,
					unionLowerEndpoint, unionUpperEndpoint,
					unionUpperEndpointMode);
		}
		// The two argsutil.intervals don't intersect, but they can still form a union if
		// they adjoin (share an endpoint value as a common boundary) and one or
		// both of the adjoining endpoints has a mode of CLOSED.
		if (first.getUpperEndpoint().compareTo(second.getLowerEndpoint()) == 0) {
			// The two argsutil.intervals adjoin, so check that at least one of them has
			// an endpoint mode of CLOSED at the adjoining boundary.
			if (first.getUpperEndpointMode().equals(EndpointMode.CLOSED)
					|| second.getLowerEndpointMode().equals(EndpointMode.CLOSED)) {
				return new IntegerInterval(first.getLowerEndpointMode(),
						first.getLowerEndpoint(),
						second.getUpperEndpoint(), second.getUpperEndpointMode());
			}
		}
		// The two argsutil.intervals neither intersect nor do they adjoin at an endpoint
		// which is included by one or both of the argsutil.intervals. There can be no
		// union, so return null.
		return null;
	}

	@Override
	public Integer getLowerEndpoint() {
		return lower;
	}

	@Override
	public Integer getUpperEndpoint() {
		return upper;
	}

	@Override
	public EndpointMode getLowerEndpointMode() {
		return lowerMode;
	}

	@Override
	public EndpointMode getUpperEndpointMode() {
		return upperMode;
	}

	/**
	 * Reports on whether the lower endpoint of this interval permits the given
	 * integer value.
	 *
	 * @param value the value to test against the lower endpoint of this
	 * interval.
	 * @return <code>true</code> if the lower endpoint of this interval permits
	 * the given value; <code>false</code> if the lower endpoint excludes the
	 * given value.
	 */
	private boolean lowerAdmits(Integer value) {
		if (lower == null) {
			return true;
		}
		if (lowerMode.equals(EndpointMode.CLOSED)) {
			return lower <= value;
		} else {
			return lower < value;
		}
	}

	/**
	 * Reports on whether the upper endpoint of this interval permits the given
	 * integer value.
	 *
	 * @param value the value to test against the upper endpoint of this
	 * interval.
	 * @return <code>true</code> if the upper endpoint of this interval permits
	 * the given value; <code>false</code> if the upper endpoint excludes the
	 * given value.
	 */
	private boolean upperEndpointAdmits(Integer value) {
		if (upper == null) {
			return true;
		}
		if (upperMode.equals(EndpointMode.CLOSED)) {
			return value <= upper;
		} else {
			return value < upper;
		}
	}

	@Override
	public boolean includes(Integer value) {
		Objects.requireNonNull(value);
		if (lower < value && value < upper) {
			return true;
		}

		return lowerAdmits(value) && upperEndpointAdmits(value);
	}

	@Override
	public boolean includes(Interval<Integer> interval) {
		Objects.requireNonNull(interval);
		if (lower == null && upper == null) {
			// An infinite interval contains all possible values, regardless of
			// the mode of its endpoints.
			return true;
		}

		boolean lowerAdmitted = false, upperAdmitted = false;

		if (lower == null) {
			lowerAdmitted = true;  // null endpoint admits all whatever its mode
		} else if (interval.getLowerEndpoint() == null) {
			// lowerAdmitted = false;
		} else if (lower.compareTo(interval.getLowerEndpoint()) < 0) {
			lowerAdmitted = true;
		} else if (lower.compareTo(interval.getLowerEndpoint())
				== 0) {
			if (lowerMode.equals(EndpointMode.CLOSED)) {
				lowerAdmitted = true;
			} else if (interval.getLowerEndpointMode().equals(EndpointMode.OPEN)) {
				lowerAdmitted = true;
			}
		}

		if (upper == null) {
			upperAdmitted = true;  // null endpoint admits all whatever its mode
		} else if (interval.getUpperEndpoint() == null) {
			// upperAdmitted = false;
		} else if (upper.compareTo(interval.getUpperEndpoint()) > 0) {
			upperAdmitted = true;
		} else if (upper.compareTo(interval.getUpperEndpoint())
				== 0) {
			if (this.upperMode.equals(EndpointMode.CLOSED)) {
				upperAdmitted = true;
			} else if (interval.getUpperEndpointMode().equals(EndpointMode.OPEN)) {
				upperAdmitted = true;
			}
		}

		return (lowerAdmitted && upperAdmitted);
	}

	/**
	 * Returns a normalized version of this interval, such that finite endpoints
	 * will be adjusted to give a closed endpoint, and unbounded endpoints will
	 * be open.
	 * <p>
	 * A lower endpoint which is open with an integer value of n will, in the
	 * returned interval, become a closed endpoint with an integer value of (n +
	 * 1). An upper endpoint which is open with an integer value of n will
	 * become a closed endpoint with an integer value of (n - 1).</p>
	 * <p>
	 * For example, the result of this method will be the interval [1, 5] for
	 * all of the following argsutil.intervals: (0, 5], (0, 6), [1, 5], [1, 6). And the
	 * interval (−∞, +∞) will always be returned for all of the following
	 * argsutil.intervals: [−∞, +∞], (−∞, +∞], [−∞, +∞), (−∞, +∞) (where
	 * <code>null</code> is used to represent infinity).</p>
	 *
	 * @return an <code>IntegerInterval</code> normalised so that finite
	 * endpoints are closed and unbounded endpoints are open, such that the
	 * result represents exactly the same set of integers as are represented by
	 * this interval.
	 */
	private IntegerInterval normalized() {
		if (this.isEmpty()) {
			return EMPTY_SET;
		}
		Integer newLower, newUpper;
		EndpointMode newLowerMode, newUpperMode;

		if (lower == null) {
			newLower = null;
			newLowerMode = EndpointMode.OPEN;
		} else {
			newLowerMode = EndpointMode.CLOSED;
			if (lowerMode.equals(EndpointMode.CLOSED)) {
				newLower = lower;
			} else {
				newLower = lower + 1;
			}
		}

		if (upper == null) {
			newUpper = null;
			newUpperMode = EndpointMode.OPEN;
		} else {
			newUpperMode = EndpointMode.CLOSED;
			if (upperMode.equals(EndpointMode.CLOSED)) {
				newUpper = upper;
			} else {
				newUpper = upper - 1;
			}
		}

		if (Objects.equals(newLower, lower) && Objects.equals(newUpper, upper)
				&& newLowerMode.equals(lowerMode)
				&& newUpperMode.equals(upperMode)) {
			return this;
		}
		return new IntegerInterval(newLowerMode, newLower, newUpper,
				newUpperMode);
	}

	/**
	 * Returns a hash code which is derived from the normalized equivalent of
	 * this interval. Two <code>IntegerInterval</code> objects which are
	 * considered equal according to the <code>equals</code> method will cause
	 * this method to return an identical hash value.
	 *
	 * @return a hash code based on the normalized endpoint values and modes of
	 * this interval.
	 */
	@Override
	public int hashCode() {
		// TODO: Consider normalizing endpoint values and modes here to avoid creation of additonal object.
		IntegerInterval normal = this.normalized();
		
		int hash = 7;
		hash = 79 * hash + (normal.lower != null ? normal.lower.hashCode() : 0);
		hash = 79 * hash + (normal.upper != null ? normal.upper.hashCode() : 0);
		hash = 79 * hash + normal.lowerMode.hashCode();
		hash = 79 * hash + normal.upperMode.hashCode();
		return hash;
	}

	/**
	 * Reports on whether the specified object is an
	 * <code>IntegerInterval</code> representing exactly the same set of
	 * integers permitted by this interval.
	 * <p>
	 * By this definition the interval (0, 6) is equal to the interval [1, 5]
	 * because both include only the values 1, 2, 3, 4 and 5.</p>
	 * <p>
	 * Note that any two argsutil.intervals representing the empty set are considered
	 * equal regardless of their actual endpoint values.</p>
	 *
	 * @param obj the <code>Object</code> to test for equality.
	 * @return <code>true</code> if the supplied <code>Object</code> is an
	 * <code>IntegerInterval</code> whose normalized form is identical to the
	 * normalized form of this interval.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IntegerInterval)) {
			return false;
		}
		IntegerInterval that = (IntegerInterval) obj;
		if (this.lower == null && that.lower == null) {
			if (this.upper == null && that.upper == null) {
				// If an endpoint is null then its mode is irrelevant.
				return true;
			}
		}
		boolean thisEmpty = this.isEmpty();
		boolean thatEmpty = that.isEmpty();
		if (thisEmpty) {
			return thatEmpty;
		}
		if (thatEmpty) {
			// We already know that this is not empty.
			return false;
		}

		// TODO: Consider normalizing endpoint values and modes here to avoid creation of additonal objects.
		IntegerInterval thisNormal = this.normalized();
		IntegerInterval thatNormal = that.normalized();

		return Objects.equals(thisNormal.lower, thatNormal.lower) && Objects.
				equals(thisNormal.upper,
						thatNormal.upper) && thisNormal.lowerMode.equals(
						thatNormal.lowerMode) && thisNormal.upperMode.equals(
						thatNormal.upperMode);
	}

	/**
	 * Produces a <code>String</code> which represents this interval in
	 * mathematical notation.
	 * <p>
	 * A square bracket indicates a closed endpoint, and a parenthesis indicates
	 * an open endpoint.</p>
	 * <p>
	 * An unbounded lower endpoint will be represented by "−∞" and an unbounded
	 * upper endpoint by "+∞".</p>
	 *
	 * @return a <code>String</code> which contains the mathematical notation of
	 * this interval.
	 */
	public String inMathematicalNotation() {
		String lowerString = lower == null ? "−∞" : lower.toString();
		String upperString = upper == null ? "+∞" : upper.toString();

		int totalLength = 4 + lowerString.length() + upperString.length();
		StringBuilder sb = new StringBuilder(totalLength);

		sb.append(lowerMode.equals(EndpointMode.CLOSED) ? '[' : '(');
		sb.append(lowerString);
		sb.append(", ");
		sb.append(upperString);
		sb.append(upperMode.equals(EndpointMode.CLOSED) ? ']' : ')');

		return sb.toString();
	}

	@Override
	public String toString() {
		return "IntegerInterval: " + inMathematicalNotation();
	}
}