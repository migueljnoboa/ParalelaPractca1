import mpi.MPI;
import mpi.MPIException;

import java.util.Objects;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws MPIException {

        // Inicializando MPI
        MPI.Init(args);

        // Obtener el rango del proceso
        int rank = MPI.COMM_WORLD.Rank();

        // Obtener el tamaño del grupo
        int size = MPI.COMM_WORLD.Size();

        // Tamano de las matrices
        final int tam = 20;

        // Creando Matrices A y B, el producto estara en el C.
        int[][] a = new int[tam][tam];
        int[][] b = new int[tam][tam];
        int[][] c = new int[tam][tam];
        int[][] cPar = new int[tam][tam];

        // Inicializando las matrices
        if (rank == 0){
            Random rnd = new Random();
            rnd.setSeed(5);
            for (int i = 0; i < tam; i++){
                for (int j = 0; j < tam; j++){
                    a[i][j] = rnd.nextInt();
                    b[i][j] = rnd.nextInt();
                }
            }
        }

        MPI.COMM_WORLD.Bcast(a, 0, tam, MPI.OBJECT, 0);
        MPI.COMM_WORLD.Bcast(b, 0, tam, MPI.OBJECT, 0);

        MPI.COMM_WORLD.Barrier();

        // Probando multiplicacion de  matrices serial
        if (rank == 0){
            System.out.println("Probando Multiplicacion de Matrices serial");
            long start = System.nanoTime();
            for (int i = 0; i < tam; i++){
                for (int j = 0; j < tam; j++){
                    for (int k = 0; k < tam; k++){
                        c[i][j] += a[i][k] * b[k][j];
                    }
                }
            }
            long end = System.nanoTime();
            System.out.println("Tiempo de corrida  " + (end - start) + " nano segundos");
            System.out.println();
        }

        // Barrier
        MPI.COMM_WORLD.Barrier();


        // Imprimir matrix resultante
        if (rank == 0){
            System.out.println();
            printMatrix(c);
            System.out.println();
        }

        // Multiplicacion Parallela

        if (rank == 0){
            System.out.println("Probando Multiplicacion de Matrices Paralella");
        }

        MPI.COMM_WORLD.Barrier();

        int cantElementos = tam / size;

        int[][] localc = new int[cantElementos][tam];

        long start = System.nanoTime();
        for (int i = 0; i < cantElementos; i++) {
            for (int j = 0; j < tam; j++) {
                for (int k = 0; k < tam; k++) {
                    localc[i][j] += a[i + rank * cantElementos][k] * b[k][j];
                }
            }
        }

        // Gather the results
        MPI.COMM_WORLD.Gather(localc, 0, cantElementos, MPI.OBJECT, cPar, 0, cantElementos, MPI.OBJECT, 0);

        long end = System.nanoTime();

        if (rank == 0){
            System.out.println();
            printMatrix(cPar);
            System.out.println();
        }

        MPI.COMM_WORLD.Barrier();
        if (rank == 0){
            System.out.println("Tiempo de corrida rank " + " " + rank + " " + (end - start) + " nanos");
        }


        // Finalizar MPI
        MPI.Finalize();
    }
    private static void printMatrix(int[][] matrix){
        /*for (int[] ints : matrix) {
            for (int anInt : ints) {
                System.out.print(anInt + " ");
            }
            System.out.println();
        }*/
    }
}