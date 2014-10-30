
package koth.game;

import java.util.Random;

/**
 * Immutable 2D integral vector
 */
public final class Vector {

    private final int x, y;

    private static final Random random = new Random();

    public Vector() {
        x = y = 0;
    }

    public Vector(int xy) {
        this.x = this.y = xy;
    }

    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isZero() {
        return x == 0 && y == 0;
    }

    public Vector neg() {
        return new Vector(-x, -y);
    }

    public Vector abs() {
        return new Vector(Math.abs(x), Math.abs(y));
    }

    public Vector add(Vector o) {
        return new Vector(x + o.x, y + o.y);
    }

    public Vector add(int o) {
        return new Vector(x + o, y + o);
    }

    public Vector add(Move o) {
        return add(o.getDelta());
    }

    public Vector sub(Vector o) {
        return new Vector(x - o.x, y - o.y);
    }

    public Vector sub(int o) {
        return new Vector(x - o, y - o);
    }

    public Vector sub(Move o) {
        return sub(o.getDelta());
    }

    public Vector mul(Vector o) {
        return new Vector(x * o.x, y * o.y);
    }

    public Vector mul(int o) {
        return new Vector(x * o, y * o);
    }

    public Vector mul(Move o) {
        return mul(o.getDelta());
    }

    public Vector div(Vector o) {
        return new Vector(x / o.x, y / o.y);
    }

    public Vector div(int o) {
        return new Vector(x / o, y / o);
    }

    public Vector mod(Vector o) {
        return new Vector(x % o.x, y % o.y);
    }

    public Vector mod(int o) {
        return new Vector(x % o, y % o);
    }

    public int dot(Vector o) {
        return x * o.x + y * o.y;
    }

    public int dot(Move o) {
        return dot(o.getDelta());
    }

    public Vector clockwise() {
        return new Vector(y, -x);
    }

    public Vector counterclockwise() {
        return new Vector(-y, x);
    }

    public int min() {
        return Math.min(x, y);
    }

    public int max() {
        return Math.max(x, y);
    }

    public Vector min(Vector o) {
        return new Vector(Math.min(x, o.x), Math.min(y, o.y));
    }

    public Vector min(int o) {
        return new Vector(Math.min(x, o), Math.min(y, o));
    }

    public Vector max(Vector o) {
        return new Vector(Math.max(x, o.x), Math.max(y, o.y));
    }

    public Vector max(int o) {
        return new Vector(Math.max(x, o), Math.max(y, o));
    }

    public Vector clamp(Vector min, Vector max) {
        return max(min).min(max);
    }

    public Vector clamp(int min, int max) {
        return max(min).min(max);
    }

    public static Vector random(int max) {
        return random(max, max);
    }

    public static Vector random(int xmax, int ymax) {
        return new Vector(random.nextInt(xmax), random.nextInt(ymax));
    }

    public int sum() {
        return x + y;
    }

    public int manhattan() {
        return Math.abs(x) + Math.abs(y);
    }

    public int manhattan(Vector o) {
        return Math.abs(o.x - x) + Math.abs(o.y - y);
    }

    @Override
    public boolean equals(Object o) {
        return !(o == null || o.getClass() != Vector.class) && equals((Vector)o);
    }

    public boolean equals(Vector o) {
        return o != null && x == o.x && y == o.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
