import java.io.*;
import java.util.*;
import java.awt.image.*;

/**
 * Description: Base Utility class
 * Methods:
 *      zigzag
 *      DWT
 *      decomposition
 *      decompositionStep
 *      composition
 *      compositionStep
 *      inverseDWT
 *      animate: generates frames from level 0 - 9 when -1 is entered in command line following filename
 *      transpose: Matrix transpose operation
 */
public class Util {

    public double[][] DWT(int[][] imgChannel, int height, int width, double coeff) {
        coeff = Math.pow(2.0, coeff);
        coeff = coeff * coeff;

        double[][] dwtMatrix = new double[height][width];

        //Make shallow copy of original array
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                dwtMatrix[i][j] = imgChannel[i][j];
            }
        }

        //Decompose Row
        for (int i = 0; i < width; i++) {
            dwtMatrix[i] = decomposition(dwtMatrix[i]);
        }
        dwtMatrix = transpose(dwtMatrix);

        for (int j = 0; j < height; j++) {
            dwtMatrix[j] = decomposition(dwtMatrix[j]);
        }
        dwtMatrix = transpose(dwtMatrix);
        dwtMatrix = zigzag(dwtMatrix, (int) coeff);

        return dwtMatrix;
    }

    public int[][] inverseDWT(double[][] mat) {
        int row = mat.length;
        int col = mat[0].length;

        int[][] inverseMat = new int[col][row];

        //Perform inverse operations in DWT
        mat = transpose(mat);

        for (int i = 0; i < row; i++) {
            mat[i] = composition(mat[i]);
        }
        mat = transpose(mat);

        for (int j = 0; j < col; j++) {
            mat[j] = composition(mat[j]);
        }

        for (int j = 0; j < col; j++) {
            for (int i = 0; i < row; i++) {
                inverseMat[j][i] = (int) Math.round(mat[j][i]);
                inverseMat[j][i] = inverseMat[j][i] < 0 ? 0 : (Math.min(inverseMat[j][i], 255));
            }
        }
        return inverseMat;
    }

    public double[] decomposition(double[] vector) {
        int h = vector.length;
        //normalize
        while (h > 0) {
            vector = decompositionStep(vector, h);
            h = h/2;
        }
        return vector;
    }

    public double[] decompositionStep(double[] vector, int h) {
        double[] vectorPrime = Arrays.copyOf(vector, vector.length);
        for (int i = 0; i < h/2; i++) {
            vectorPrime[i] = (vector[2 * i] + vector[2 * i + 1])/2;
            vectorPrime[h/2 + i] = (vector[2 * i] - vector[2 * i + 1])/2;
        }
        return vectorPrime;
    }

    public double[][] transpose(double[][] mat) {
        int row = mat.length;
        int col = mat[0].length;
        double[][] trans = new double[col][row];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                trans[j][i] = mat[i][j];
            }
        }
        return trans;
    }

    public double[] composition(double[] vector) {
        int h = 1;
        while (h <= vector.length) {
            vector = compositionStep(vector, h);
            h *= 2;
        }
        return vector;
    }

    public double[] compositionStep(double[] vector, int h) {
        double[] vectorPrime = Arrays.copyOf(vector, vector.length);
        for (int i = 0; i < h/2; i++) {
            vectorPrime[2 * i] = vector[i] + vector[h/2 + i];
            vectorPrime[2 * i + 1] = vector[i] - vector[h/2 + i];
        }
        return vectorPrime;
    }

    public double[][] zigzag(double[][] mat, int coeff) {
        int row = 0, col = 0;
        int count = 1;
        int len = mat.length - 1;

        mat[row][col] = count > coeff ? 0 : mat[row][col];
        count++;

        while (true) {

            col++;
            mat[row][col] = count > coeff ? 0 : mat[row][col];
            count++;

            while (col != 0) {
                row++;
                col--;
                mat[row][col] = count > coeff ? 0 : mat[row][col];
                count++;
            }

            row++;
            if (row > len) {
                row--;
                break;
            }

            mat[row][col] = count > coeff ? 0 : mat[row][col];
            count++;

            while (row != 0) {
                row--;
                col++;
                mat[row][col] = count > coeff ? 0 : mat[row][col];
                count++;
            }
        }

        while (true) {
            col++;
            count++;

            if (count > coeff) {
                mat[row][col] = 0;
            }

            while (col != len) {
                col++;
                row--;
                mat[row][col] = count > coeff ? 0 : mat[row][col];
                count++;
            }

            row++;
            if (row > len) {
                row--;
                break;
            }
            mat[row][col] = count > coeff ? 0 : mat[row][col];
            count++;

            while (row < len) {
                row++;
                col--;
                mat[row][col] = count > coeff ? 0 : mat[row][col];
                count++;
            }
        }
        return mat;
    }

    public BufferedImage animate(int[][] redChannel, int[][] greenChannel,
                                 int[][] blueChannel, int height, int width, int coeff) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        //Create copies
        int[][] tempRedChannel = new int[width][height];
        int[][] tempGreenChannel = new int[width][height];
        int[][] tempBlueChannel = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tempRedChannel[i][j] = redChannel[i][j];
                tempGreenChannel[i][j] = greenChannel[i][j];
                tempBlueChannel[i][j] = blueChannel[i][j];
            }
        }

        if (coeff < 9) {
            double[][] tempRedDWT = DWT(tempRedChannel, height, width, coeff);
            double[][] tempGreenDWT = DWT(tempGreenChannel, height, width, coeff);
            double[][] tempBlueDWT = DWT(tempBlueChannel, height, width, coeff);

            tempRedChannel = inverseDWT(tempRedDWT);
            tempGreenChannel = inverseDWT(tempGreenDWT);
            tempBlueChannel = inverseDWT(tempBlueDWT);
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pix = 0xff000000 | ((tempRedChannel[i][j] & 0xff) << 16) | ((tempGreenChannel[i][j] & 0xff) << 8) |
                        (tempBlueChannel[i][j] & 0xff);
                img.setRGB(j, i, pix);
            }
        }
        return img;
    }
}
