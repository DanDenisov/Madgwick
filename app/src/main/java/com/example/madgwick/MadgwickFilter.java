package com.example.madgwick;

public class MadgwickFilter
{
    static protected int call_count;
    static protected double[] a, w, m, b = {0, 1, 0, 1};
    static protected double dt;
    static protected double[] q_est;
    static protected double[] dq_est, dq_w, dq_error;
    static private double betta = 1, sigma = 1;
    static private double norm = 0;

    static protected double[] eulers;

    static void Filtrate()
    {
        call_count++;
        if (call_count >= 2)
        {
            //initial assumption
            if (call_count == 2)
                q_est = Product(dt, w);

            //normalizing accelerometer measurements
            norm = Norm(a);
            if (norm != 0)
                a = Product(norm, a);

            //normalizing magnetometer measurements
            norm = Norm(m);
            if (norm != 0)
                m = Product(norm, m);

            //main algorithm
            GetOrientationErrorRate();
            GetGyroscopeOrientation();
            Fuse();

            //normalizing resulting quaternion
            norm = Norm(q_est);
            if (norm != 0)
                q_est = Product(norm, q_est);

            //converting quaternion to Euler angles
            eulers = new double[3];

            eulers[0] = Math.atan2(2*q_est[2]*q_est[3] - 2*q_est[0]*q_est[1], 2*Math.pow(q_est[0], 2) + 2*Math.pow(q_est[3], 2) - 1);
            double expr = 2*q_est[1]*q_est[3] + 2*q_est[0]*q_est[2];
            if (Math.abs(expr) > 1)
                eulers[1] = Math.signum(expr) * Math.PI / 2;
            else
                eulers[1] = -Math.asin(expr);
            eulers[2] = Math.atan2(2*q_est[1]*q_est[2] - 2*q_est[0]*q_est[3], 2*Math.pow(q_est[0], 2) + 2*Math.pow(q_est[1], 2) - 1);

            /*eulers[0] = Math.atan2(2*q_est[2]*q_est[3] + 2*q_est[0]*q_est[1], 1 - 2*Math.pow(q_est[1], 2) - 2*Math.pow(q_est[2], 2));

            double expr = 2*q_est[0]*q_est[2] - 2*q_est[1]*q_est[3];
            if (Math.abs(expr) > 1)
                eulers[1] = Math.signum(expr) * Math.PI / 2;
            else
                eulers[1] = Math.asin(expr);

            eulers[2] = Math.atan2(2*q_est[1]*q_est[2] + 2*q_est[0]*q_est[3], 1 - 2*Math.pow(q_est[2], 2) - 2*Math.pow(q_est[3], 2));*/

            //outputting results on the screen
            MainActivity.X.setText("X: " + eulers[0]);
            MainActivity.Y.setText("Y: " + eulers[1]);
            MainActivity.Z.setText("Z: " + eulers[2]);
        }
    }

    static private void GetOrientationErrorRate()
    {
        //objective function (accelerometer)
        double[][] f_g =
                {
                        { 2*(q_est[1]*q_est[3] - q_est[0]*q_est[2]) - a[1] },
                        { 2*(q_est[0]*q_est[1] + q_est[2]*q_est[3]) - a[2] },
                        { 2*(0.5 - Math.pow(q_est[1], 2) - Math.pow(q_est[2], 2)) - a[3] }
                };

        //transposed Jacobian
        double[][] JT_g =
                {
                        { -2*q_est[2], 2*q_est[1], 0 },
                        { 2*q_est[3], 2*q_est[0], -4*q_est[1] },
                        { -2*q_est[0], 2*q_est[3], -4*q_est[2] },
                        { 2*q_est[1], 2*q_est[2], 0 }
                };

        double[] grad_f;
        if (Sensors.mag != null)
        {
            //compensating magnetic measurement error
            double[] h = QuatProduct(QuatProduct(q_est, m), Conjugated(q_est));
            b = new double[] { 0, 2 * Math.sqrt(Math.pow(h[1], 2) + Math.pow(h[2], 2)), 0, 2 * h[3] };

            //objective function (magnetometer)
            double[][] f_b =
                    {
                            { 2*b[1]*(0.5 - Math.pow(q_est[2], 2) - Math.pow(q_est[3], 2)) + 2*b[3]*(q_est[1]*q_est[3] - q_est[0]*q_est[2]) - m[1] },
                            { 2*b[1]*(q_est[1]*q_est[2] - q_est[0]*q_est[3]) + 2*b[3]*(q_est[0]*q_est[1] + q_est[2]*q_est[3]) - m[2] },
                            { 2*b[1]*(q_est[0]*q_est[2] + q_est[1]*q_est[3]) + 2*b[3]*(0.5 - Math.pow(q_est[1], 2) - Math.pow(q_est[2], 2)) - m[3] }
                    };

            //transposed Jacobian
            double[][] JT_b =
                    {
                            { -2*b[3]*q_est[2], -2*b[1]*q_est[3] + 2*b[3]*q_est[1], 2*b[1]*q_est[2] },
                            { 2*b[3]*q_est[3], 2*b[1]*q_est[2] + 2*b[3]*q_est[0], 2*b[1]*q_est[3] - 4*b[3]*q_est[1] },
                            { -4*b[1]*q_est[2] - 2*b[3]*q_est[0], 2*b[1]*q_est[1] + 2*b[3]*q_est[3], 2*b[1]*q_est[0] - 4*b[3]*q_est[2] },
                            { -4*b[1]*q_est[3] + 2*b[3]*q_est[1], -2*b[1]*q_est[0] + 2*b[3]*q_est[2], 2*b[1]*q_est[1] }
                    };

            grad_f = Sum(Product(JT_g, f_g), Product(JT_b, f_b));
        }
        else
            grad_f = Product(JT_g, f_g);

        //normalizing gradient => orientation error rate
        norm = Norm(grad_f);
        if (norm != 0)
            dq_error = Product(norm, grad_f);
        else
            dq_error = new double[] { 0, 0, 0, 0 };
    }

    static private void GetGyroscopeOrientation()
    {
        //compensating angular rate measurement error
        double[] w_error = QuatProduct(Product(2, Conjugated(q_est)), dq_error);
        double[] w_bias = Product(sigma * dt, w_error);
        w = Sum(w, Product(-1, w_bias));

        //retrieving orientation change rate based on gyroscope measurement
        if (call_count <= 2)
            dq_w = w;
        else
            dq_w = QuatProduct(Product(0.5, q_est), w);
    }

    static private void Fuse()
    {
        //applying feedback from gradient descent
        dq_est = Sum(dq_w, Product(-betta, dq_error));

        //adding estimated orientation change to current (estimated) orientation
        q_est = Sum(q_est, Product(dt, dq_est));
    }

    static private double[] Conjugated(double[] q)  //conjugated quaternion q* of q
    {
        double[] res = new double[4];
        res[0] = q[0];
        res[1] = -q[1];
        res[2] = -q[2];
        res[3] = -q[3];
        return res;
    }

    static private double[] Sum(double[] q1, double[] q2)  //res = q1 + q2
    {
        double[] res = new double[4];
        for (int i = 0; i < q1.length; i++)
            res[i] = q1[i] + q2[i];
        return res;
    }

    static private double[] QuatProduct(double[] q1, double[] q2)  //res = q1 ⊗ q2
    {
        double[] res = new double[4];
        res[0] = q1[0]*q2[0] - q1[1]*q2[1] - q1[2]*q2[2] - q1[3]*q2[3];
        res[1] = q1[0]*q2[1] + q1[1]*q2[0] + q1[2]*q2[3] - q1[3]*q2[2];
        res[2] = q1[0]*q2[2] - q1[1]*q2[3] + q1[2]*q2[0] + q1[3]*q2[1];
        res[3] = q1[0]*q2[3] + q1[1]*q2[2] - q1[2]*q2[1] + q1[3]*q2[0];
        return res;
    }

    static private double[] Product(double c, double[] q)  //res = c * q
    {
        double[] res = new double[4];
        for (int i = 0; i < q.length; i++)
            res[i] = c * q[i];
        return res;
    }

    static private double[] Product(double[][] m1, double[][] m2)  //res = m1 * m2 (matrices)
    {
        double[][] res = new double[m1.length][m2[0].length];
        double sum;
        for (int i = 0; i < m1.length; i++)
        {
            for (int j = 0; j < m2[0].length; j++)
            {
                sum = 0;
                for (int k = 0; k < m1[0].length; k++)
                {
                    sum += m1[i][k] * m2[k][j];
                }
                res[i][j] = sum;
            }
        }

        double[] res_onedim = new double[4];
        for (int i = 0; i < res_onedim.length; i++)
            res_onedim[i] = res[i][0];

        return res_onedim;
    }

    static private double Norm(double[] q)  //normalization method
    {
        double sum = 0;
        for (int i = 0; i < q.length; i++)
            sum += Math.pow(q[i], 2);
        return Math.sqrt(sum) == 0 ? 0 : 1 / Math.sqrt(sum);
    }
}
