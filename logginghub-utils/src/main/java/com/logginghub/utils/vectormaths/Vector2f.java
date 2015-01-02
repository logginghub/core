package com.logginghub.utils.vectormaths;

public class Vector2f 
{
    public float x;
    public float y;

    public Vector2f(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public Vector2f()
    {
        x = y = 0;
    }

    public Vector2f(Vector2f vector2f)
    {
        this.x = vector2f.x;
        this.y = vector2f.y;
    }

    public Vector2f set(float x, float y)
    {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2f set(Vector2f vec)
    {
        this.x = vec.x;
        this.y = vec.y;
        return this;
    }

    public Vector2f add(Vector2f vec)
    {
        if (null == vec)
        {
            return null;
        }
        return new Vector2f(x + vec.x, y + vec.y);
    }

    public Vector2f addLocal(Vector2f vec)
    {
        if (null == vec)
        {
            return null;
        }
        x += vec.x;
        y += vec.y;
        return this;
    }

    public Vector2f addLocal(float addX, float addY)
    {
        x += addX;
        y += addY;
        return this;
    }

    /**
     * <code>add</code> adds this vector by <code>vec</code> and stores the
     * result in <code>result</code>.
     * 
     * @param vec
     *            The vector to add.
     * @param result
     *            The vector to store the result in.
     * @return The result vector, after adding.
     */
    public Vector2f add(Vector2f vec, Vector2f result)
    {
        if (null == vec)
        {
            return null;
        }
        if (result == null) result = new Vector2f();
        result.x = x + vec.x;
        result.y = y + vec.y;
        return result;
    }

    /**
     * <code>dot</code> calculates the dot product of this vector with a
     * provided vector. If the provided vector is null, 0 is returned.
     * 
     * @param vec
     *            the vector to dot with this vector.
     * @return the resultant dot product of this vector and a given vector.
     */
    public float dot(Vector2f vec)
    {
        if (null == vec)
        {
            return 0;
        }
        return x * vec.x + y * vec.y;
    }

    public float determinant(Vector2f v)
    {
        return (x * v.y) - (y * v.x);
    }

    /**
     * Sets this vector to the interpolation by changeAmnt from this to the
     * finalVec this=(1-changeAmnt)*this + changeAmnt * finalVec
     * 
     * @param finalVec
     *            The final vector to interpolate towards
     * @param changeAmnt
     *            An amount between 0.0 - 1.0 representing a precentage change
     *            from this towards finalVec
     */
    public void interpolate(Vector2f finalVec, float changeAmnt)
    {
        this.x = (1 - changeAmnt) * this.x + changeAmnt * finalVec.x;
        this.y = (1 - changeAmnt) * this.y + changeAmnt * finalVec.y;
    }

    /**
     * Sets this vector to the interpolation by changeAmnt from beginVec to
     * finalVec this=(1-changeAmnt)*beginVec + changeAmnt * finalVec
     * 
     * @param beginVec
     *            The begining vector (delta=0)
     * @param finalVec
     *            The final vector to interpolate towards (delta=1)
     * @param changeAmnt
     *            An amount between 0.0 - 1.0 representing a precentage change
     *            from beginVec towards finalVec
     */
    public void interpolate(Vector2f beginVec,
                            Vector2f finalVec,
                            float changeAmnt)
    {
        this.x = (1 - changeAmnt) * beginVec.x + changeAmnt * finalVec.x;
        this.y = (1 - changeAmnt) * beginVec.y + changeAmnt * finalVec.y;
    }

    /**
     * Check a vector... if it is null or its floats are NaN or infinite, return
     * false. Else return true.
     * 
     * @param vector
     *            the vector to check
     * @return true or false as stated above.
     */
    public static boolean isValidVector(Vector2f vector)
    {
        if (vector == null) return false;
        if (Float.isNaN(vector.x) || Float.isNaN(vector.y)) return false;
        if (Float.isInfinite(vector.x) || Float.isInfinite(vector.y)) return false;
        return true;
    }

    /**
     * <code>length</code> calculates the magnitude of this vector.
     * 
     * @return the length or magnitude of the vector.
     */
    public float length()
    {
        return (float) Math.sqrt(lengthSquared());
    }

    /**
     * <code>lengthSquared</code> calculates the squared value of the magnitude
     * of the vector.
     * 
     * @return the magnitude squared of the vector.
     */
    public float lengthSquared()
    {
        return x * x + y * y;
    }

    /**
     * <code>distanceSquared</code> calculates the distance squared between this
     * vector and vector v.
     * 
     * @param v
     *            the second vector to determine the distance squared.
     * @return the distance squared between the two vectors.
     */
    public float distanceSquared(Vector2f v)
    {
        double dx = x - v.x;
        double dy = y - v.y;
        return (float) (dx * dx + dy * dy);
    }

    /**
     * <code>distanceSquared</code> calculates the distance squared between this
     * vector and vector v.
     * 
     * @param v
     *            the second vector to determine the distance squared.
     * @return the distance squared between the two vectors.
     */
    public float distanceSquared(float otherX, float otherY)
    {
        double dx = x - otherX;
        double dy = y - otherY;
        return (float) (dx * dx + dy * dy);
    }

    /**
     * <code>distance</code> calculates the distance between this vector and
     * vector v.
     * 
     * @param v
     *            the second vector to determine the distance.
     * @return the distance between the two vectors.
     */
    public float distance(Vector2f v)
    {
        return (float) Math.sqrt(distanceSquared(v));
    }

    /**
     * <code>mult</code> multiplies this vector by a scalar. The resultant
     * vector is returned.
     * 
     * @param scalar
     *            the value to multiply this vector by.
     * @return the new vector.
     */
    public Vector2f mult(float scalar)
    {
        return new Vector2f(x * scalar, y * scalar);
    }

    /**
     * <code>multLocal</code> multiplies this vector by a scalar internally, and
     * returns a handle to this vector for easy chaining of calls.
     * 
     * @param scalar
     *            the value to multiply this vector by.
     * @return this
     */
    public Vector2f multLocal(float scalar)
    {
        x *= scalar;
        y *= scalar;
        return this;
    }

    /**
     * <code>multLocal</code> multiplies a provided vector to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls. If the provided vector is null, null is returned.
     * 
     * @param vec
     *            the vector to mult to this vector.
     * @return this
     */
    public Vector2f multLocal(Vector2f vec)
    {
        if (null == vec)
        {
            return null;
        }
        x *= vec.x;
        y *= vec.y;
        return this;
    }

    /**
     * Multiplies this Vector2f's x and y by the scalar and stores the result in
     * product. The result is returned for chaining. Similar to
     * product=this*scalar;
     * 
     * @param scalar
     *            The scalar to multiply by.
     * @param product
     *            The vector2f to store the result in.
     * @return product, after multiplication.
     */
    public Vector2f mult(float scalar, Vector2f product)
    {
        if (null == product)
        {
            product = new Vector2f();
        }

        product.x = x * scalar;
        product.y = y * scalar;
        return product;
    }

    /**
     * <code>divide</code> divides the values of this vector by a scalar and
     * returns the result. The values of this vector remain untouched.
     * 
     * @param scalar
     *            the value to divide this vectors attributes by.
     * @return the result <code>Vector</code>.
     */
    public Vector2f divide(float scalar)
    {
        return new Vector2f(x / scalar, y / scalar);
    }

    /**
     * <code>divideLocal</code> divides this vector by a scalar internally, and
     * returns a handle to this vector for easy chaining of calls. Dividing by
     * zero will result in an exception.
     * 
     * @param scalar
     *            the value to divides this vector by.
     * @return this
     */
    public Vector2f divideLocal(float scalar)
    {
        x /= scalar;
        y /= scalar;
        return this;
    }

    /**
     * <code>negate</code> returns the negative of this vector. All values are
     * negated and set to a new vector.
     * 
     * @return the negated vector.
     */
    public Vector2f negate()
    {
        return new Vector2f(-x, -y);
    }

    /**
     * <code>negateLocal</code> negates the internal values of this vector.
     * 
     * @return this.
     */
    public Vector2f negateLocal()
    {
        x = -x;
        y = -y;
        return this;
    }

    /**
     * <code>subtract</code> subtracts the values of a given vector from those
     * of this vector creating a new vector object. If the provided vector is
     * null, an exception is thrown.
     * 
     * @param vec
     *            the vector to subtract from this vector.
     * @return the result vector.
     */
    public Vector2f subtract(Vector2f vec)
    {
        return subtract(vec, null);
    }

    /**
     * <code>subtract</code> subtracts the values of a given vector from those
     * of this vector storing the result in the given vector object. If the
     * provided vector is null, an exception is thrown.
     * 
     * @param vec
     *            the vector to subtract from this vector.
     * @param store
     *            the vector to store the result in. It is safe for this to be
     *            the same as vec. If null, a new vector is created.
     * @return the result vector.
     */
    public Vector2f subtract(Vector2f vec, Vector2f store)
    {
        if (store == null) store = new Vector2f();
        store.x = x - vec.x;
        store.y = y - vec.y;
        return store;
    }

    /**
     * <code>subtract</code> subtracts the given x,y values from those of this
     * vector creating a new vector object.
     * 
     * @param valX
     *            value to subtract from x
     * @param valY
     *            value to subtract from y
     * @return this
     */
    public Vector2f subtract(float valX, float valY)
    {
        return new Vector2f(x - valX, y - valY);
    }

    /**
     * <code>subtractLocal</code> subtracts a provided vector to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls. If the provided vector is null, null is returned.
     * 
     * @param vec
     *            the vector to subtract
     * @return this
     */
    public Vector2f subtractLocal(Vector2f vec)
    {
        if (null == vec)
        {
            return null;
        }
        x -= vec.x;
        y -= vec.y;
        return this;
    }

    /**
     * <code>subtractLocal</code> subtracts the provided values from this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls.
     * 
     * @param valX
     *            value to subtract from x
     * @param valY
     *            value to subtract from y
     * @return this
     */
    public Vector2f subtractLocal(float valX, float valY)
    {
        x -= valX;
        y -= valY;
        return this;
    }

    /**
     * <code>normalize</code> returns the unit vector of this vector.
     * 
     * @return unit vector of this vector.
     */
    public Vector2f normalize()
    {
        float length = length();
        if (length != 0)
        {
            return divide(length);
        }

        return divide(1);
    }

    /**
     * <code>normalizeLocal</code> makes this vector into a unit vector of
     * itself.
     * 
     * @return this.
     */
    public Vector2f normalizeLocal()
    {
        float length = length();
        if (length != 0)
        {
            return divideLocal(length);
        }

        return divideLocal(1);
    }

    /**
     * <code>smallestAngleBetween</code> returns (in radians) the minimum angle
     * between two vectors. It is assumed that both this vector and the given
     * vector are unit vectors (iow, normalized).
     * 
     * @param otherVector
     *            a unit vector to find the angle against
     * @return the angle in radians.
     */
    public float smallestAngleBetween(Vector2f otherVector)
    {
        float dotProduct = dot(otherVector);
        float angle = (float) Math.acos(dotProduct);
        return angle;
    }

    /**
     * <code>angleBetween</code> returns (in radians) the angle required to
     * rotate a ray represented by this vector to lie colinear to a ray
     * described by the given vector. It is assumed that both this vector and
     * the given vector are unit vectors (iow, normalized).
     * 
     * @param otherVector
     *            the "destination" unit vector
     * @return the angle in radians.
     */
    public float angleBetween(Vector2f otherVector)
    {
        float angle = (float) (Math.atan2(otherVector.y, otherVector.x) -
                      Math.atan2(y, x));
        return angle;
    }

    public float getX()
    {
        return x;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public float getY()
    {
        return y;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    /**
     * <code>getAngle</code> returns (in radians) the angle represented by this
     * Vector2f as expressed by a conversion from rectangular coordinates (
     * <code>x</code>,&nbsp;<code>y</code>) to polar coordinates
     * (r,&nbsp;<i>theta</i>).
     * 
     * @return the angle in radians. [-pi, pi)
     */
    public float getAngle()
    {
        return (float) -Math.atan2(y, x);
    }

    /**
     * <code>zero</code> resets this vector's data to zero internally.
     */
    public void zero()
    {
        x = y = 0;
    }

    /**
     * <code>hashCode</code> returns a unique code for this vector object based
     * on it's values. If two vectors are logically equivalent, they will return
     * the same hash code value.
     * 
     * @return the hash code value of this vector.
     */
    public int hashCode()
    {
        int hash = 37;
        hash += 37 * hash + Float.floatToIntBits(x);
        hash += 37 * hash + Float.floatToIntBits(y);
        return hash;
    }

    /**
     * <code>clone</code> creates a new Vector2f object containing the same data
     * as this one.
     * 
     * @return the new Vector2f
     */
    public Vector2f clone()
    {
        return new Vector2f(x, y);
    }

    /**
     * are these two vectors the same? they are is they both have the same x and
     * y values.
     * 
     * @param o
     *            the object to compare for equality
     * @return true if they are equal
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof Vector2f))
        {
            return false;
        }

        if (this == o)
        {
            return true;
        }

        Vector2f comp = (Vector2f) o;
        if (Float.compare(x, comp.x) != 0) return false;
        if (Float.compare(y, comp.y) != 0) return false;
        return true;
    }
    
    public String toString()
    {
        return String.format("[%2f, %2f]", x,y);
    }
}
