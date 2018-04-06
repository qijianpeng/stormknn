/*
 * Copyright © 2014 Bobulous <http://www.bobulous.org.uk/>.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package util.args.intervals;

import java.util.Comparator;
import util.args.intervals.Interval.EndpointMode;

/**
 * A <code>Comparator</code> which compares <code>Interval</code> objects by
 * their lower endpoint and then by their upper endpoint.
 * <p>
 * The compare method is evaluated thusly:</p>
 * <ol>
 * <li>If the argsutil.intervals have differing lower endpoint values then the interval
 * with the smallest lower endpoint value is considered the lesser interval.
 * Because a lower endpoint value of <code>null</code> represents negative
 * infinity it is considered equal to <code>null</code> but lower than any other
 * value.</li>
 * <li>If the argsutil.intervals have identical lower endpoint values but differing lower
 * endpoint modes then the interval with the <code>CLOSED</code> lower endpoint
 * is considered the lesser interval. Note that endpoint mode is irrelevant if
 * the value is <code>null</code>. Two lower endpoints with a <code>null</code>
 * value are equal regardless of their modes.</li>
 * <li>If the lower endpoints of the two argsutil.intervals are equal, then the interval
 * with the smallest upper endpoint value is considered the lesser interval.
 * Because an upper endpoint value of <code>null</code> represents positive
 * infinity it is considered equal to <code>null</code> and greater than any
 * other value.</li>
 * <li>If the lower endpoints of the two argsutil.intervals are equal, and both have
 * identical upper endpoint values, but differing upper endpoint modes then the
 * interval with the <code>OPEN</code> upper endpoint is considered the lesser
 * interval. Note that endpoint mode is irrelevant if the value is
 * <code>null</code>. Two upper endpoints with a <code>null</code> value are
 * equal regardless of mode.</li>
 * <li>If the lower endpoints of the argsutil.intervals are equal, and the upper
 * endpoints of the argsutil.intervals are equal, then the two argsutil.intervals are considered
 * comparatively equal.</li>
 * </ol>
 * <p>
 * For example, for <var>a</var> &lt; <var>b</var> &lt; <var>c</var> then:</p>
 * <ul>
 * <li>[<var>a</var>,<var>b</var>] is lesser than [<var>b</var>, <var>c</var>]
 * (lower endpoint values differ)</li>
 * <li>[<var>a</var>,<var>b</var>] is lesser than (<var>a</var>, <var>c</var>]
 * (lower endpoint modes differ)</li>
 * <li>[<var>a</var>,<var>b</var>] is lesser than [<var>a</var>, <var>c</var>]
 * (upper endpoint values differ)</li>
 * <li>[<var>a</var>,<var>b</var>) is lesser than [<var>a</var>, <var>b</var>]
 * (upper endpoint modes differ)</li>
 * <li>(<var>-∞</var>, <var>∞</var>) is equal to [<var>-∞</var>, <var>∞</var>]
 * (endpoint mode irrelevant for infinite/unbounded/<code>null</code>
 * value)</li>
 * </ul>
 *
 * @author Bobulous <http://www.bobulous.org.uk/>
 * @param <T> the type which forms the basis of the <code>Interval</code>
 * objects to be compared.
 * @see Interval
 */
public final class IntervalComparator<T extends Comparable<T>> implements
		Comparator<Interval<T>> {

	/*
	 * Private constructor because only one instance of this Comparator ever
	 * needs to exist. (Because it behaves exactly the same regardless of the
	 * parameterized type.) Use the static getInstance method to get the
	 * singleton object.
	 */
	private IntervalComparator() {
	}

	/**
	 * Singleton instance of <code>IntervalComparator</code> which is used in
	 * all cases.
	 */
	private static final IntervalComparator singleton = new IntervalComparator();

	/**
	 * Returns an instance of <code>IntervalComparator</code> which compares
	 * argsutil.intervals of the specified basis type.
	 * <p>
	 * To get an <code>IntervalComparator</code> which compares argsutil.intervals of
	 * basis type <code>K</code> you need to call this method like so:</p>
	 * <p>
	 * <code>IntervalComparator&lt;K&gt; comparator =
	 * IntervalComparator.&lt;K&gt;getInstance();</code></p>
	 *
	 * @param <K> the basis type of <code>Interval</code> objects which can be
	 * compared by this <code>Comparator</code>.
	 * @return a <code>Comparator</code> which compares two
	 * <code>Interval</code> objects having the specified basis type.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Comparable<K>> IntervalComparator<K> getInstance() {
		return (IntervalComparator<K>) singleton;
	}

	/**
	 * Compares two <code>Interval</code> objects of the same basis type. Read
	 * the description of this class for detail on how the comparison is
	 * evaluated.
	 *
	 * @param o1 an <code>Interval</code> object of basis type <code>T</code>.
	 * @param o2 another <code>Interval</code> object with the same basis type.
	 * @return a negative integer if <var>o1</var> is a lesser interval than
	 * <var>o2</var>; or zero if the two argsutil.intervals are comparatively identical;
	 * or a positive integer if
	 * <var>o1</var> is a greater interval than <var>o2</var>.
	 */
	@Override
	public int compare(Interval<T> o1, Interval<T> o2) {

		// Compare the lower endpoints first
		int lowerComparison = this.lowerEndpointValueCompare(o1, o2);
		if (lowerComparison != 0) {
			// One lower endpoint is lesser than the other, so return the
			// result of the comparison
			return lowerComparison;
		}
		// Lower endpoints are equivalent, so it comes down to a comparison of
		// the upper endpoints
		return this.upperEndpointValueCompare(o1, o2);
	}

	/**
	 * Compare the lower endpoints of two <code>Interval</code> objects of the
	 * same type.
	 * <p>
	 * If the first interval has a lesser value for its lower endpoint than the
	 * second interval then a negative integer is returned. If the first
	 * interval has a greater value for its lower endpoint than the second
	 * interval then a positive integer is returned.</p>
	 * <p>
	 * If both argsutil.intervals have identical values and modes for their lower
	 * endpoint then zero is returned.</p>
	 * <p>
	 * If both argsutil.intervals have identical values for their lower endpoint but
	 * different modes for their lower endpoint, then the endpoint which is
	 * <code>CLOSED</code> will be considered comparatively lesser (because it
	 * includes values closer to negative infinity than the other does). In this
	 * case, if the first interval has a <code>CLOSED</code> lower endpoint then
	 * a negative integer is returned. If the second interval is the one with a
	 * <code>CLOSED</code> lower endpoint then a positive integer is
	 * returned.</p>
	 * <p>
	 * Note that a <code>null</code> lower endpoint is considered to have a
	 * value of negative infinity. Only <code>null</code> is equal to
	 * <code>null</code>, and no non-null value can be less-than-or-equal-to
	 * <code>null</code>. Even if an actual value calls itself negative infinity
	 * (such as <code>Double.NEGATIVE_INFINITY</code>) <code>null</code> will
	 * still be considered lesser in value. For a <code>null</code> endpoint the
	 * endpoint mode is irrelevant.</p>
	 *
	 * @param first an interval.
	 * @param second another interval of the same basis type as the first.
	 * @return A negative value if the lower endpoint of the first interval has
	 * a comparatively lesser value than that of the other interval; zero if the
	 * lower endpoint of the first interval has value equivalent to that of the
	 * second interval; or a positive value if the lower endpoint of the first
	 * interval has a comparatively greater value than that of the second
	 * interval.
	 */
	int lowerEndpointValueCompare(Interval<T> first, Interval<T> second) {
		T alpha = first.getLowerEndpoint();
		T beta = second.getLowerEndpoint();
		if (alpha == null) {
			// alpha is as low as it gets
			// beta is either equal if it is also null, or larger otherwise
			// so alpha is either equal (return 0) or smaller (return -1)
			return (beta == null ? 0 : -1);
		}
		// alpha has an actual value
		if (beta == null) {
			// beta is as low as it gets, so alpha must be larger
			return +1;
		}
		// Both alpha and beta have actual values so compare them
		int valueComparison = alpha.compareTo(beta);
		if (valueComparison == 0) {
			// Lower endpoint values are the same, so it comes down to the mode
			// of the lower endpoints
			if (first.getLowerEndpointMode().equals(
					second.getLowerEndpointMode())) {
				return 0;
			} else if (first.getLowerEndpointMode().equals(EndpointMode.CLOSED)) {
				return -1;
			} else {
				return 1;
			}
		}
		// Values are different so return valueComparison result
		return valueComparison;
	}

	/**
	 * Compare the upper endpoints of two <code>Interval</code> objects of the
	 * same type.
	 * <p>
	 * If the first interval has a lesser value for its upper endpoint than the
	 * second interval then a negative integer is returned. If the first
	 * interval has a greater value for its upper endpoint than the second
	 * interval then a positive integer is returned.</p>
	 * <p>
	 * If both argsutil.intervals have identical values and modes for their upper
	 * endpoint then zero is returned.</p>
	 * <p>
	 * If both argsutil.intervals have identical values for their upper endpoint but
	 * different modes for their upper endpoint, then the endpoint which is
	 * <code>CLOSED</code> will be considered comparatively greater (because it
	 * includes values closer to positive infinity than the other does). In this
	 * case, if the first interval has an <code>CLOSED</code> upper endpoint
	 * then a positive integer is returned. If the second interval is the one
	 * with a <code>CLOSED</code> upper endpoint then a negative integer is
	 * returned.</p>
	 * <p>
	 * Note that a <code>null</code> upper endpoint is considered to have a
	 * value of positive infinity. Only <code>null</code> is equal to
	 * <code>null</code>, and no non-null value can be greater-than-or-equal-to
	 * <code>null</code>. Even if an actual value calls itself positive infinity
	 * (such as <code>Double.POSITIVE_INFINITY</code>) <code>null</code> will
	 * still be considered greater in value. For a <code>null</code> endpoint
	 * the endpoint mode is irrelevant.</p>
	 *
	 * @param first an interval.
	 * @param second another interval of the same basis type as the first.
	 * @return A negative value if the first interval has an upper endpoint
	 * comparatively lesser than that of the other interval; zero if the first
	 * interval has an upper endpoint which is equivalent to the upper endpoint
	 * of the second interval; or a positive value if the first interval has an
	 * upper endpoint comparatively greater than that of the second interval.
	 */
	int upperEndpointValueCompare(Interval<T> first, Interval<T> second) {
		T alpha = first.getUpperEndpoint();
		T beta = second.getUpperEndpoint();
		if (alpha == null) {
			// alpha is as high as it gets
			// beta is either equal if it is also null, or smaller otherwise
			// so alpha is either equal (return 0) or larger (return +1)
			return (beta == null ? 0 : +1);
		}
		// alpha has an actual value
		if (beta == null) {
			// beta is as high as it gets, so alpha must be smaller
			return -1;
		}
		// both alpha and beta have actual values so let compareTo handle it
		int valueComparison = alpha.compareTo(beta);
		if (valueComparison == 0) {
			// Upper endpoint values are the same, so it comes down to the mode
			// of the lower endpoints
			if (first.getUpperEndpointMode().equals(
					second.getUpperEndpointMode())) {
				return 0;
			} else if (first.getUpperEndpointMode().equals(EndpointMode.OPEN)) {
				return -1;
			} else {
				return 1;
			}
		}
		// Values are different so return valueComparison result
		return valueComparison;
	}

}
