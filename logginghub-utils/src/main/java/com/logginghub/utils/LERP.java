package com.logginghub.utils;

public class LERP {

    private double[] actualValues;

    public static LERP gausian() {

        /*
         * To generate these values: for (int i = 0; i < 11; i++) {
         * System.out.println(lerp.gaussian(i, 10, 5, 2, 0)); }
         */

        double[] values = new double[] { 0.04393693362340742,
                                        0.1353352832366127,
                                        0.32465246735834974,
                                        0.6065306597126334,
                                        0.8824969025845954,
                                        1.0,
                                        0.8824969025845954,
                                        0.6065306597126334,
                                        0.32465246735834974,
                                        0.1353352832366127,
                                        0.04393693362340742, };

        return new LERP(values);
    }

    public LERP(double... values) {
        this.actualValues = values;
    }

    public LERP(int[] randomisationDistribution) {
        actualValues = new double[randomisationDistribution.length];
        for (int i = 0; i < actualValues.length; i++) {
            actualValues[i] = randomisationDistribution[i];
        }
    }

    /**
     * Give it a factor between 0 and 1, and it'll give you value using linear interpolation through
     * the array
     * 
     * @param value
     * @return
     */

    public double lerp(double value) {

        // Figure out the indicies of the adjacent array points
        double exactIndex = value * (actualValues.length - 1);
        int first = (int) exactIndex;
        int next = first + 1;

        double howFarBetweenAreWe = exactIndex - first;

        double result;
        if (exactIndex >= (actualValues.length - 1)) {

            result = actualValues[actualValues.length - 1];
        }
        else {
            double valueAtFirst = actualValues[first];
            double valueAtNext = actualValues[next];

            result = valueAtFirst + ((valueAtNext - valueAtFirst) * howFarBetweenAreWe);
        }
        return result;
    }

    public double gaussian(double x, double a_heightAtPeak, double b_positionOfCentre, double c_stddev, double d_yoffset) {

        double xMinusB = x - b_positionOfCentre;
        double xMinusBSquared = xMinusB * xMinusB;
        double twoCSquared = c_stddev * c_stddev * 2;
        double power = -1 * (xMinusBSquared / twoCSquared);

        double f = a_heightAtPeak * Math.exp(power) + d_yoffset;
        return f;

    }

}
