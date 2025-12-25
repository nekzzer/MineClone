package org.example;

import java.util.Random;

public class PerlinNoise {
    private int[] p;
    private double[] gx, gy, gz;

    public PerlinNoise() {
        p = new int[512];
        gx = new double[512];
        gy = new double[512];
        gz = new double[512];
        Random r = new Random();

        for (int i = 0; i < 256; i++) p[i] = i;
        for (int i = 0; i < 256; i++) {
            int j = r.nextInt(256);
            int temp = p[i]; p[i] = p[j]; p[j] = temp;
        }
        for (int i = 0; i < 256; i++) {
            p[i + 256] = p[i];
            double theta = r.nextDouble() * 2 * Math.PI;
            double phi = r.nextDouble() * 2 * Math.PI;
            gx[i] = Math.sin(theta) * Math.cos(phi);
            gy[i] = Math.sin(theta) * Math.sin(phi);
            gz[i] = Math.cos(theta);
            gx[i + 256] = gx[i];
            gy[i + 256] = gy[i];
            gz[i + 256] = gz[i];
        }
    }

    public double noise(double x, double y, double z) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        double u = fade(x), v = fade(y), w = fade(z);
        int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z;
        int B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;

        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z), grad(p[BA], x - 1, y, z)),
                        lerp(u, grad(p[AB], x, y - 1, z), grad(p[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1), grad(p[BA + 1], x - 1, y, z - 1)),
                        lerp(u, grad(p[AB + 1], x, y - 1, z - 1), grad(p[BB + 1], x - 1, y - 1, z - 1))));
    }

    private double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private double lerp(double t, double a, double b) { return a + t * (b - a); }
    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y, v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}