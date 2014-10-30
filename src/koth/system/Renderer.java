
package koth.system;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * Manage a collection of cubes and a camera.
 */
public abstract class Renderer implements Comparator<Renderer.Cube> {

    /**
     * Represent a axis-aligned cube.
     */
    public static final class Cube {
        public final float x, y, z, sx, sy, sz, r, g, b;
        public Cube(float x, float y, float z, float sx, float sy, float sz, float r, float g, float b) {
            this.x = x; this.y = y; this.z = z;
            this.sx = sx; this.sy = sy; this.sz = sz;
            this.r = r; this.g = g; this.b = b;
        }
    }

    protected ArrayList<Cube> cubes;
    protected float cx, cy, cz, cr;
    protected int w, h;

    public Renderer() {
        cubes = new ArrayList<Cube>();
        cx = cy = cz = 0;
        cr = 1;
        w = h = 1;
    }

    /**
     * Get underlying cubes.
     */
    public Set<Cube> getCubes() {
        return new HashSet<Cube>(cubes);
    }

    /**
     * Set cube set.
     */
    public void setCubes(Set<Cube> cubes) {
        if (cubes == null)
            this.cubes.clear();
        else {
            this.cubes = new ArrayList<Cube>(cubes);
            Collections.sort(this.cubes, this);
        }
    }

    /**
     * Set camera location and zoom.
     */
    public void setCamera(float x, float y, float z, float radius) {
        cx = x; cy = y; cz = z;
        cr = radius;
    }

    /**
     * Set viewport size.
     */
    public void setViewport(int width, int height) {
        w = width;
        h = height;
    }

    protected abstract void paint(Graphics2D g, Cube c);

    /**
     * Render cubes on specified graphic context.
     */
    public void paint(Graphics2D g) {
        for (Cube c : cubes)
            paint(g, c);
    }

    /**
     * Project a 3D point on screen.
     */
    public abstract Point.Float project(float x, float y, float z);

    /**
     * Unproject a screen point to 3D coordinates.
     * The z-coordinate is required to properly predict the location.
     */
    public abstract Point.Float unproject(float screenx, float screeny, float z);

    /**
     * Project world with an orthogonal projection.
     */
    public static class Orthogonal extends Renderer {

        @Override
        public int compare(Cube a, Cube b) {
            return Float.compare(
                a.z + a.sz * 0.5f,
                b.z + b.sz * 0.5f
            );
        }

        @Override
        public void paint(Graphics2D g, Cube c) {
            final float s = w < h ? w / cr : h / cr;
            g.setColor(new Color(c.r, c.g, c.b));
            g.fill(new Rectangle2D.Float(
                (cx - c.x - c.sx * 0.5f) * s + w * 0.5f,
                (cy - c.y - c.sy * 0.5f) * s + h * 0.5f,
                c.sx * s, c.sy * s
            ));
        }

        @Override
        public Point.Float project(float x, float y, float z) {
            float s = w < h ? w / cr : h / cr;
            return new Point.Float(
                (cx - x) * s + w * 0.5f,
                (cy - y) * s + h * 0.5f
            );
        }

        @Override
        public Point.Float unproject(float screenx, float screeny, float z) {
            float s = w < h ? w / cr : h / cr;
            return new Point.Float(
                cx - (screenx - w * 0.5f) / s,
                cy - (screeny - h * 0.5f) / s
            );
        }

    }

    /**
     * Project world with an isometric projection.
     */
    public static class Isometric extends Renderer {

        // TODO optimize isometric rendering

        private static final float EX = 0.8f, EY = 0.6f;

        @Override
        public int compare(Cube a, Cube b) {
            return Float.compare(
                a.z - (a.x + 0.5f * a.sx) * EY - (a.y + 0.5f * a.sy) * EY,
                b.z - (b.x + 0.5f * b.sx) * EY - (b.y + 0.5f * b.sy) * EY
            );
        }

        private static Shape quad(float llx, float lly, float lrx, float lry, float urx, float ury, float ulx, float uly) {
            // Java does not have a float version of Polygon... Seriously...
            Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO, 5);
            path.moveTo(llx, lly);
            path.lineTo(lrx, lry);
            path.lineTo(urx, ury);
            path.lineTo(ulx, uly);
            path.closePath();
            return path;
        }

        private static void fillQuad(Graphics2D g, float llx, float lly, float lrx, float lry, float urx, float ury, float ulx, float uly) {
            g.fill(quad(llx, lly, lrx, lry, urx, ury, ulx, uly));
            // int[] xs = new int[] {(int)llx, (int)lrx, (int)urx, (int)ulx};
            // int[] ys = new int[] {(int)lly, (int)lry, (int)ury, (int)uly};
            // g.fillPolygon(xs, ys, 4);
        }

        @Override
        public void paint(Graphics2D g, Cube c) {
            final float s = w < h ? w / cr : h / cr;
            final float SX = EX * s;
            final float SY = EY * s;
            final float ox = (cy - c.y - cx + c.x) * SX + w * 0.5f;
            final float oy = (cx - c.x + cy - c.y) * SY - (c.z - cz) * s + h * 0.5f;
            final float exx = -SX * 0.5f * c.sx, exy = SY * 0.5f * c.sx;
            final float eyx = SX * 0.5f * c.sy, eyy = SY * 0.5f * c.sy;
            final float ezx = 0, ezy = -0.5f * c.sz * s;
            Color color = new Color(c.r, c.g, c.b);
            // Left face
            g.setColor(color.darker());
            fillQuad(g,
                ox + exx - eyx - ezx, oy + exy - eyy - ezy,
                ox + exx + eyx - ezx, oy + exy + eyy - ezy,
                ox + exx + eyx + ezx, oy + exy + eyy + ezy,
                ox + exx - eyx + ezx, oy + exy - eyy + ezy
            );
            // Right face
            g.setColor(color);
            fillQuad(g,
                ox + exx + eyx - ezx, oy + exy + eyy - ezy,
                ox - exx + eyx - ezx, oy - exy + eyy - ezy,
                ox - exx + eyx + ezx, oy - exy + eyy + ezy,
                ox + exx + eyx + ezx, oy + exy + eyy + ezy
            );
            // Upper face
            g.setColor(color.brighter());
            fillQuad(g,
                ox + exx - eyx + ezx, oy + exy - eyy + ezy,
                ox + exx + eyx + ezx, oy + exy + eyy + ezy,
                ox - exx + eyx + ezx, oy - exy + eyy + ezy,
                ox - exx - eyx + ezx, oy - exy - eyy + ezy
            );
        }

        @Override
        public Point.Float project(float x, float y, float z) {
            final float s = w < h ? w / cr : h / cr;
            return new Point.Float(
                (cy - y - cx + x) * EX * s,
                (cx - x + cy - y) * EY * s - (z - cz) * s
            );
        }

        @Override
        public Point.Float unproject(float screenx, float screeny, float z) {
            float s = w < h ? w / cr : h / cr;
            float c1 = (screenx - w * 0.5f) / (EX * s);
            float c2 = (screeny - h * 0.5f + (cz - z) * s) / (EY * s);
            return new Point.Float(cx - (c2 - c1) * 0.5f, cy - (c2 + c1) * 0.5f);
        }

    }

}
