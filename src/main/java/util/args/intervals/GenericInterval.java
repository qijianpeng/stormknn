/*
 * Copyright © 2014 Bobulous <http://www.bobulous.org.uk/>.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package util.args.intervals;

import util.args.intervals.Interval.EndpointMode;

/**
 * A concrete implementation of an <code>Interval</code> over any naturally
 * ordered type.
 * <p>
 * Each <code>GenericInterval</code> contains a lower endpoint value and an
 * upper endpoint value such that the result of
 * <code>upper.compareTo(lower)</code> must be zero or greater. In other words,
 * the upper endpoint must be equal to or greater than the lower endpoint
 * according to the natural ordering of the interval basis type.</p>
 * <p>
 * For example, if an <code>GenericInterval&lt;Integer&gt;</code> object (an
 * interval of integers) has a lower endpoint value of zero then its upper
 * endpoint value must be zero or greater. If an
 * <code>GenericInterval&lt;Character&gt;</code> (an interval of character
 * values) had a lower endpoint value of <code>'a'</code> then its upper
 * endpoint value would have to be <code>'a'</code> or <code>'b'</code> or any
 * other character which causes <code>upper.compareTo(lower)</code> to return
 * zero or greater.</p>
 * <p>
 * If an endpoint is <code>null</code> then it means that the endpoint is
 * unbounded and there is no limit to what is included in that direction. So a
 * <code>null</code> lower endpoint means that the lower endpoint is unbounded
 * and the interval includes every value which is less than the upper endpoint
 * value; a <code>null</code> upper endpoint means that the upper endpoint is
 * unbounded and the interval includes every value greater than the lower
 * endpoint value; and if both endpoints are <code>null</code> then all values
 * are included in this interval. In effect, a lower endpoint of
 * <code>null</code> equates to a lower endpoint having the value negative
 * infinity, and an upper endpoint of <code>null</code> equates to an upper
 * endpoint having the value positive infinity. Note that <code>null</code>
 * itself is <strong>never</strong> included in an interval, as
 * <code>null</code> represents the lack of a value. Be aware that
 * <code>null</code> endpoints will allow any value of the permitted type,
 * including values such as <code>Double.NaN</code> (which is supposed to
 * represent a non-value value).</p>
 * <p>
 * Also note that a type constant which is intended to represent infinity, such
 * as <code>Double.POSITIVE_INFINITY</code>, is just another numeric value so
 * far as the <code>Comparable</code> interface is concerned, and is therefore
 * just another member of the naturally ordered set Double so far as
 * <code>Interval&lt;Double&gt;</code> is concerned. Specifying such a value for
 * an endpoint is not the same as specifying <code>null</code> because
 * <code>null</code> will admit any value belonging to the interval basis type,
 * whereas a pseudo-infinite value such as <code>Double.POSITIVE_INFINITY</code>
 * will exclude any values which are beyond it in the natural order of the basis
 * type. For example, <code>Double.NaN</code> is considered greater than
 * <code>Double.NEGATIVE_INFINITY</code> and also greater than
 * <code>Double.POSITIVE_INFINITY</code> according to the <code>compareTo</code>
 * method of <code>Double</code>.</p>
 * <p>
 * An interval can include zero, one or both of its endpoint values. This is
 * specified by the mode of each endpoint.
 * {@link Interval.EndpointMode#CLOSED EndpointMode.CLOSED} means that the
 * endpoint value itself is included in the interval, while
 * {@link Interval.EndpointMode#OPEN EndpointMode.OPEN} means that the endpoint
 * value itself is not included in the interval. For instance, an
 * <code>Interval&lt;Double&gt;</code> might have a lower endpoint value of zero
 * and a lower endpoint mode of <code>EndpointMode.OPEN</code> which means that
 * zero is the lower limit of the interval but is not included in the interval.
 * The endpoint mode is irrelevant for a null endpoint.</p>
 * <p>
 * <strong>Warning:</strong> Because an <code>Interval</code> relies on the
 * <code>Comparable</code> interface, everything depends on the result returned
 * by the <code>compareTo</code> method. So be aware that a <code>Double</code>
 * object with value <code>-0.0</code> (negative zero) is considered by
 * compareTo to be less in value than a <code>Double</code> object with value
 * <code>0.0</code> (positive zero) even though almost all applications would
 * consider these two values to be identical. Be careful not to allow a negative
 * zero to be used as an endpoint value without understanding that it will be
 * treated differently.</p>
 * <p>
 * Objects of type <code>GenericInterval</code> cannot be mutated, but it is
 * only safe to consider them truly immutable if they are based on an immutable
 * type. So a <code>GenericInterval&lt;String&gt;</code> is truly immutable
 * because neither the <code>GenericInterval</code> nor the <code>String</code>
 * objects acting as endpoints can be mutated. But a
 * <code>GenericInterval&lt;Date&gt;</code> cannot be considered immutable
 * because even though the <code>GenericInterval</code> cannot be mutated, the
 * <code>Date</code> objects acting as endpoints can be mutated. This also means
 * that a <code>GenericInterval</code> of a mutable type cannot be guaranteed to
 * maintain the order of its endpoints, so the lower endpoint may suddenly be
 * mutated to have a greater value than the upper endpoint. For this reason it
 * is strongly recommended that <code>GenericInterval</code> is only used with
 * immutable types. If a <code>GenericInterval</code> is created over a mutable
 * type, then both the <code>GenericInterval</code> and its endpoint objects
 * must be kept safe, and must not be shared.</p>
 *
 * @author Bobulous <http://www.bobulous.org.uk/>
 * @param <T> the basis type of this <code>GenericInterval</code>. The basis
 * type must implement <code>Comparable&lt;T&gt;</code> so that each instance of
 * the type can be compared with other instances of the same type, thus being a
 * type which has a natural order.
 * @see Interval
 * @see IntervalComparator
 * @see IntervalSet
 */
public final class GenericInterval<T extends Comparable<T>> implements
		Interval<T> {

	private final T lowerEndpoint, upperEndpoint;
	private final EndpointMode lowerMode, upperMode;

	/**
	 * Constructs a closed <code>GenericInterval</code> with the specified
	 * endpoint values.
	 * <p>
	 * Because this is a closed interval, both endpoint values will be included
	 * within the interval.</p>
	 *
	 * @param lowerEndpoint the value of the lower endpoint of this interval, or
	 * <code>null</code> if the lower endpoint of this interval is unbounded.
	 * @param upperEndpoint the value of the upper endpoint of this interval, or
	 * <code>null</code> if the upper endpoint of this interval is unbounded.
	 * The effective value of this upper endpoint must not be lower than the
	 * effective value of the lower endpoint.
	 * @throws IllegalArgumentException if the effective value of the upper
	 * endpoint is less than the effective value of the lower endpoint.
	 */
	public GenericInterval(T lowerEndpoint, T upperEndpoint) {
		this(EndpointMode.CLOSED, lowerEndpoint, upperEndpoint,
				EndpointMode.CLOSED);
	}

	/**
	 * Constructs a <code>GenericInterval</code> with the specified endpoint
	 * values and endpoint modes.
	 *
	 * @param lowerMode the endpoint mode of the lower endpoint.
	 * @param lowerEndpoint the value of the lower endpoint of this interval, or
	 * <code>null</code> if the lower endpoint of this interval is unbounded.
	 * @param upperEndpoint the value of the upper endpoint of this interval, or
	 * <code>null</code> if the upper endpoint of this interval is unbounded.
	 * The effective value of this upper endpoint must not be lower than the
	 * effective value of the lower endpoint.
	 * @param upperMode the endpoint mode of the upper endpoint.
	 * @throws IllegalArgumentException if the effective value of the upper
	 * endpoint is less than the effective value of the lower endpoint.
	 */
	public GenericInterval(EndpointMode lowerMode, T lowerEndpoint,
			T upperEndpoint,
			EndpointMode upperMode) {
		if (lowerEndpoint != null) {
			// Bounded below
			if (upperEndpoint != null) {
				// Bounded below and above
				if (upperEndpoint.compareTo(lowerEndpoint) < 0) {
					throw new IllegalArgumentException("Cannot create an "
							+ "Interval whose upper endpoint has a value less "
							+ "than its lower endpoint value. Lower "
							+ "endpoint is: " + lowerEndpoint + ", and upper "
							+ "endpoint: " + upperEndpoint);
				}
			}
		}
		this.lowerEndpoint = lowerEndpoint;
		this.upperEndpoint = upperEndpoint;
		this.lowerMode = lowerMode;
		this.upperMode = upperMode;
	}

	@Override
	public T getLowerEndpoint() {
		return this.lowerEndpoint;
	}

	@Override
	public T getUpperEndpoint() {
		return this.upperEndpoint;
	}

	@Override
	public EndpointMode getLowerEndpointMode() {
		return this.lowerMode;
	}

	@Override
	public EndpointMode getUpperEndpointMode() {
		return this.upperMode;
	}

	/**
	 * Reports on whether the specified value is permitted by the lower endpoint
	 * of this interval (without consideration for the upper endpoint).
	 * <p>
	 * <strong>Note:</strong> this method does not consider the upper endpoint
	 * of this interval, so a result of true from this method does not
	 * necessarily mean that this interval contains the specified value.</p>
	 *
	 * @param value the value to test, which can be null if an endpoint value is
	 * being tested.
	 * @return true if the lower endpoint of this interval does not preclude the
	 * specified value from being a member of this interval; false otherwise.
	 * @see upperEndpointAdmits
	 */
	private boolean lowerEndpointAdmits(T value) {
		if (this.lowerEndpoint == null) {
			// A null lower endpoint allows all values
			return true;
		}
		if (value == null) {
			// A null value (from an interval endpoint) cannot be admitted
			// because the lower endpoint is not null
			return false;
		}
		if (this.lowerMode.equals(EndpointMode.CLOSED)) {
			// A closed lower endpoint allows values equal to or greater than itself
			return (this.lowerEndpoint.compareTo(value) <= 0);
		} else {
			// An open lower endpoint only allows values greater than itself
			return (this.lowerEndpoint.compareTo(value) < 0);
		}
	}

	/**
	 * Reports on whether the specified value is permitted by the upper endpoint
	 * of this interval (without consideration for the lower endpoint).
	 * <p>
	 * <strong>Note:</strong> this method does not consider the lower endpoint
	 * of this interval, so a result of true from this method does not
	 * necessarily mean that this interval contains the specified value.</p>
	 *
	 * @param value the value to test, which can be null if an endpoint value is
	 * being tested.
	 * @return true if the upper endpoint of this interval does not preclude the
	 * specified value from being a member of this interval; false otherwise.
	 * @see upperEndpointAdmits
	 */
	private boolean upperEndpointAdmits(T value) {
		if (this.upperEndpoint == null) {
			// A null endpoint forbids no value from membership of the interval
			return true;
		}
		if (value == null) {
			// A null value (from an interval endpoint) cannot be admitted
			// because the upper endpoint is not null
			return false;
		}
		if (this.upperMode.equals(EndpointMode.CLOSED)) {
			return (this.upperEndpoint.compareTo(value) >= 0);
		} else {
			return (this.upperEndpoint.compareTo(value) > 0);
		}

	}

	@Override
	public boolean includes(T value) {
		if (value == null) {
			throw new NullPointerException("Cannot pass a null value to "
					+ "includes(T).");
		}
		return (this.lowerEndpointAdmits(value) && this.upperEndpointAdmits(
				value));
	}

	@Override
	public boolean includes(Interval<T> interval) {
		if (interval == null) {
			throw new NullPointerException("Cannot pass a null interval to "
					+ "includes(Interval<T>).");
		}
		if (this.lowerEndpoint == null && this.upperEndpoint == null) {
			// An infinite interval contains all possible values, regardless of
			// the mode of its endpoints.
			return true;
		}

		boolean lowerAdmitted = false, upperAdmitted = false;

		if (this.lowerEndpoint == null) {
			lowerAdmitted = true;  // null endpoint admits all whatever its mode
		} else if (interval.getLowerEndpoint() == null) {
			// lowerAdmitted = false;
		} else if (this.lowerEndpoint.compareTo(interval.getLowerEndpoint()) < 0) {
			lowerAdmitted = true;
		} else if (this.lowerEndpoint.compareTo(interval.getLowerEndpoint())
				== 0) {
			if (this.lowerMode.equals(EndpointMode.CLOSED)) {
				lowerAdmitted = true;
			} else if (interval.getLowerEndpointMode().equals(EndpointMode.OPEN)) {
				lowerAdmitted = true;
			}
		}

		if (this.upperEndpoint == null) {
			upperAdmitted = true;  // null endpoint admits all whatever its mode
		} else if (interval.getUpperEndpoint() == null) {
			// upperAdmitted = false;
		} else if (this.upperEndpoint.compareTo(interval.getUpperEndpoint()) > 0) {
			upperAdmitted = true;
		} else if (this.upperEndpoint.compareTo(interval.getUpperEndpoint())
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
	private int endpointValuesLowerUpperCompare(T lower, T upper) {
		if (lower == null || upper == null) {
			return -1;
		}
		return lower.compareTo(upper);
	}

	/**
	 * Returns a <code>String</code> which represents the <code>Interval</code>
	 * using mathematical notation.
	 * <p>
	 * For example, a closed <code>Integer</code> interval might produce
	 * <samp>"[0, 1]"</samp> while an open <code>Double</code> interval might
	 * produce <samp>"(0.0, 1.0)"</samp>, and a left-closed, right-open
	 * <code>String</code> interval might produce <samp>"[a, b)"</samp>. A
	 * square bracket represents a closed endpoint; a parenthesis represents an
	 * open endpoint. A <code>null</code> lower endpoint value is replaced with
	 * negative infinity; a <code>null</code> upper endpoint value is replaced
	 * with positive infinity. This method calls the <code>toString</code>
	 * method on each endpoint of this <code>Interval</code>, so the output of
	 * this method may be meaningless if this <code>Interval</code> is based on
	 * a type whose <code>toString</code> method does not output the actual
	 * value of the endpoint objects.</p>
	 * <p>
	 * Be warned that if the endpoint values contain commas the output of this
	 * method may be confusing. For example, some numeric types might use the
	 * comma as a decimal point or a thousands-separator, and this could lead to
	 * confusing notation such as <samp>"[3,001, 3,002]"</samp>. And String
	 * objects which permit commas could lead to very confusing results such as
	 * <samp>"[There is, so it is said, a comma in here somewhere, Victor said
	 * so]"</samp></p>
	 *
	 * @return a <code>String</code> which represents this
	 * <code>GenericInterval</code> in mathematical notation.
	 */
	public String inMathematicalNotation() {
		StringBuilder sb = new StringBuilder();
		if (this.lowerMode.equals(EndpointMode.CLOSED)) {
			sb.append("[");
		} else {
			sb.append("(");
		}
		if (this.lowerEndpoint == null) {
			sb.append("−∞");
		} else {
			sb.append(this.lowerEndpoint.toString());
		}

		sb.append(", ");

		if (this.upperEndpoint == null) {
			sb.append("+∞");
		} else {
			sb.append(this.upperEndpoint.toString());
		}
		if (this.upperMode.equals(EndpointMode.CLOSED)) {
			sb.append("]");
		} else {
			sb.append(")");
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return "GenericInterval " + this.inMathematicalNotation();
	}

	/**
	 * Returns true if both objects are <code>GenericInterval</code> instances
	 * with identical basis types, endpoint values and (for non-null values)
	 * endpoint modes.
	 * <p>
	 * Note that the mode of a null endpoint is irrelevant. Because
	 * <code>null</code> represents infinity in an endpoint value, the mode is
	 * meaningless. So two lower endpoints with a value of <code>null</code> are
	 * considered equal regardless of mode, and two upper endpoints with a value
	 * of <code>null</code> are considered equal regardless of mode.</p>
	 * <p>
	 * <strong>Warning:</strong> two <code>GenericInterval</code> objects with
	 * different basis types will be declared equal if all endpoints are
	 * <code>null</code>, because it is not possible to determine the basis type
	 * from a <code>null</code> object. Be aware of this if your code will
	 * compare <code>GenericInterval</code> objects of different basis
	 * types.</p>
	 *
	 * @param obj the object to be compared for equality with this
	 * <code>GenericInterval</code>.
	 * @return <code>true</code> if both objects are
	 * <code>GenericInterval</code> objects with identical types, values and
	 * endpoint modes; <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Interval)) {
			System.out.println("Wrong object type!");
			return false;
		}
		Interval<?> that = (Interval<?>) obj;
		if (this.lowerEndpoint == null) {
			if (that.getLowerEndpoint() != null) {
				// This lower is null but other lower is not null
				return false;
			}
			// Both lower endpoints are null, so they are equal, regardless of
			// endpoint modes
		} else {
			if (that.getLowerEndpoint() == null) {
				// This lower is not null, but other lower is null
				return false;
			}
			// Neither lower endpoint is null, so compare values and modes
			if (!this.lowerEndpoint.equals(that.getLowerEndpoint())) {
				// Lower values are not equal
				return false;
			}
			if (!this.lowerMode.equals(that.getLowerEndpointMode())) {
				// Lower modes are not equal (and values are not null)
				return false;
			}
		}

		if (this.upperEndpoint == null) {
			if (that.getUpperEndpoint() != null) {
				// This upper is null but other upper is not null
				return false;
			}
			// Both upper endpoints are null, so they are equal, regardless of
			// endpoint modes
		} else {
			if (that.getUpperEndpoint() == null) {
				// This upper is not null, but other upper is null
				return false;
			}
			// Neither upper endpoint is null, so compare values and modes
			if (!this.upperEndpoint.equals(that.getUpperEndpoint())) {
				// Upper values are not equal
				return false;
			}
			if (!this.upperMode.equals(that.getUpperEndpointMode())) {
				// Upper modes are not equal (and values are not null)
				return false;
			}
		}

		return true;
	}

	/**
	 * Calculates a hash based on the values of this
	 * <code>GenericInterval</code>. Note that this method relies on there being
	 * a valid implementation of <code>hashCode</code> in the basis type (such
	 * as <code>Integer</code> or <code>String</code>). If the basis type does
	 * not correctly implement <code>hashCode</code> then the result returned by
	 * this method cannot be considered reliable.
	 *
	 * @return an <code>int</code> value based on the values of this
	 * <code>GenericInterval</code>.
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + (this.lowerEndpoint != null ? this.lowerEndpoint.
				hashCode() : 0);
		hash = 79 * hash + (this.upperEndpoint != null ? this.upperEndpoint.
				hashCode() : 0);
		hash = 79 * hash + (this.lowerMode != null ? this.lowerMode.hashCode()
				: 0);
		hash = 79 * hash + (this.upperMode != null ? this.upperMode.hashCode()
				: 0);
		return hash;
	}
}
