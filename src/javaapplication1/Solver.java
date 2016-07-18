/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

/**
 *
 * @author fernando
 */

import com.sun.xml.internal.ws.resources.SoapMessages;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class Solver {
    public LpSolve solver;
    int meses;
    int [] temperaturas;
    int [] precipitacion;
    int [] d_min;
    int [] d_max;
    int produccion;
    int precio;
    
//metodo que inicaliza el solver 
    public void inicializar_solver ()
    {
        try { 
            
            // 0: numero restricciones inicialmente, meses: cantidad de varibles del modelo
            this.solver= LpSolve.makeLp(0, this.meses);
            //vuelve binarias las variables
            for (int s =1 ; s<=this.meses;s++)
        {
            this.solver.setBinary(s, true);
        }
            System.out.println("inicialized");
        } catch (LpSolveException ex) {
            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    //metodo que lle el archivo de texto y almacena las variables al programa
    public void leer_archivo ()
    {
        FileReader filer = null;
        BufferedReader buffer = null;
        String linea ;
          try {
                filer = new FileReader("D:/DatosPruebas/entrada4.txt");
                buffer = new BufferedReader(filer);
                //lectura de meses 
                this.meses = Integer.parseInt(buffer.readLine());
                
                //asignacion del tamaño de los arreglos al numero de meses
                this.temperaturas = new int [meses];
                precipitacion = new int [meses];
                this.d_min = new int [meses];
                this.d_max = new int [meses];
                
                //lectura de las temperaturas
                linea = buffer.readLine();
                String palabra[] =  linea.split(" ");
                for (int i =0; i<palabra.length; i++)
                {
                    this.temperaturas[i] = Integer.parseInt(palabra[i]);
                    //System.out.println(this.temperaturas[i]);
                }
                
                //lectura de las precipitaciones
                linea = buffer.readLine();
                palabra =  linea.split(" ");
                for (int i =0; i<palabra.length; i++)
                {
                    this.precipitacion[i] = Integer.parseInt(palabra[i]);
                    //System.out.println(this.precipitacion[i]);
                }
                
                 //lectura de las demandas minimas
                linea = buffer.readLine();
                palabra =  linea.split(" ");
                for (int i =0; i<palabra.length; i++)
                {
                    this.d_min[i] = Integer.parseInt(palabra[i]);
                    //System.out.println(this.d_min[i]);
                }
                
                
                  //lectura de las demandas maximas
                linea = buffer.readLine();
                palabra =  linea.split(" ");
                for (int i =0; i<palabra.length; i++)
                {
                    this.d_max[i] = Integer.parseInt(palabra[i]);
                    //System.out.println(this.d_max[i]);
                }
                
                // lectura de la produccion
                this.produccion = Integer.parseInt(buffer.readLine());
                
                // lectura del precio por bulto
                this.precio = Integer.parseInt(buffer.readLine());
                
                //System.out.println(this.produccion);
                // System.out.println(this.precio); 
                System.out.println("archivo leido"); 
         }
          catch (Exception e) {
                System.err.println(e.toString());
            }
    }
    
    
    //calcula los coeficientes de la funcion objteivo y la pasa al solver
    public void funcion_obj () 
    {
        
        int prod = this.produccion;
        int valor = this.precio;
        double  []  coeficientes = new double [this.meses];
        // en los 3 primeros meses no se puede cosechas
        coeficientes [0] = 0;
        coeficientes [1] = 0;
        coeficientes [2] = 0;
        double coeficiente;
        for (int i = 3; i< this.meses;i++)
        {
            //si la produccion esta dentro del rango de la demanda
            if ((prod >= this.d_min[i]) && (prod  <= this.d_max[i]))
            {
                coeficiente = valor*prod;
            }
            else
            {
                //si la produccion es menor que la demanda minina
                if (prod < this.d_min[i])
                {
                    coeficiente = (valor /2) * prod;
                }
                //si la produccion es mayor que la demanda maxima   
                else
                {
                    coeficiente = (valor * this.d_max[i]) + (produccion - this.d_max[i]) * (valor/2); 
                }
            }
            coeficientes [i] = coeficiente; 
        }
        
        String f_obj = "";
        for (int a = 0; a<this.meses;a++)
            {
                //System.out.println(coeficientes[a]);
                f_obj = f_obj + "-" + coeficientes[a] + " ";
            }
        try {
            // se le pasa la funcion objetivo al solver
            solver.strSetObjFn(f_obj);
            System.out.println("fun_obj añadida");
        } catch (LpSolveException ex) {
            System.err.println("error en set function");
        }
        
    }
    
    //todas las restricciones del modelo
    public void restricciones ()
    {
        //añade restricciones para poder cultivar
        cond_cultivar ();
        //restriccion para que en los primeros 3 meses no se coseche
        no_cosechar();
        //restringir en 4 meses a lo maximo 1 sola cosecha
        restr_cosecha ();
        
    }
    
    //restriccion de que en los primeros 3 meses no se coseche
    public void no_cosechar ()
    {
        //x1 <=0
        //x2 <=0
        //x3 <=0
        try {
            String r1;
            r1 = "1 1 1";
            for (int i=0 ; i<this.meses - 3;i++)
            {
                r1 = r1 + " 0";
            }
            //System.out.println(r1);
            solver.strAddConstraint(r1, LpSolve.EQ, 0);
            System.out.println("R no cosechar");
            
        } catch (LpSolveException ex) {
            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //restriccion de que un periodo de 4 meses solo exista una cosecha
    public void restr_cosecha ()
    {
        int [] r = new int [meses];
        for (int a =0; a<meses;a++)
        {
           r[a] = 0;
        }
        for (int i =0; i<meses-3;i++)
        {
            try {
                //añadir restriccion xi + x(i+1) + x(i+2) + x(i+3) <=1
                r[i] = 1;
                r[i+1] = 1;
                r[i+2] = 1;
                r[i+3] = 1;
                String constr = "";
                for (int a = 0; a<this.meses;a++)
                {
                    //System.out.println(coeficientes[a]);
                    constr = constr + r[a] + " ";
                }
                solver.strAddConstraint(constr, LpSolve.LE, 1);
                
                
                r[i] = 0;
                r[i+1] = 0;
                r[i+2] = 0;
                r[i+3] = 0;
            } catch (LpSolveException ex) {
                Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        System.out.println("rest_cosecha");
    }
    
    //verifica si en el mes i se puede cosechar
    public void cond_cultivar () 
    {
        int meses = this.meses;
        int [] temperaturas = this.temperaturas;
        int [] precipitacion = this.precipitacion;
        int [] r = new int [meses];
        for (int a =0; a<meses;a++)
        {
           r[a] = 0;
        }
        for (int i =0; i<meses-3;i++)
        {
            if (!condiciones (temperaturas[i],precipitacion[i]))
            {
                //añadir restriccion que x(i+3) =0
                r[i+3]=1;
            }
            String restriccion = "";
            for (int a = 0; a<this.meses;a++)
            {
                //System.out.println(coeficientes[a]);
                restriccion = restriccion + r[a] + " ";
                r[a] = 0;
            }
            try {
                //System.out.println(restriccion);
                this.solver.strAddConstraint(restriccion, LpSolve.EQ, 0);
            } catch (LpSolveException ex) {
                Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("rest_cond cultivar");
    }
    
    //verifica si en el mes i se puede cosechar
    public boolean condiciones (int temperatura, int precip)
    {
        boolean flag = false;
        if ((temperatura >= 18 ) && (temperatura <=20) && (precip >=63))
        {
            flag = true;
        }
        return flag;
    }
    
    /**
     * no es de este ejemplo
     */
    public void example ()
    {
        try {
        // Create a problem with 4 variables and 0 constraints
        LpSolve solver = LpSolve.makeLp(0, 8);

        // add constraints
        solver.strAddConstraint("0 0 0 0 1 0 0 0", LpSolve.LE, 0);
        solver.strAddConstraint("0 0 0 0 0 0 1 0", LpSolve.LE, 0);
        
        solver.strAddConstraint("1 1 1 0 0 0 1 0", LpSolve.LE, 0);
        for (int s =1 ; s<=8;s++)
        {
            solver.setBinary(s, true);
        }

        // set objective function
        //0.0 0.0 0.0 5850000.0 2925000.0 5850000.0 2925000.0 5850000.0
        solver.strSetObjFn("0.0 0.0 0.0 -5850000.0 -2925000.0 -5850000.0 -2925000.0 -5850000.0");

        // solve the problem
        solver.solve();

        // print solution
        System.out.println("Value of objective function: " + solver.getObjective());
        double[] var = solver.getPtrVariables();
        for (int i = 0; i < var.length; i++) {
          System.out.println("Value of var[" + i + "] = " + var[i]);
        }

        // delete the problem and free memory
        solver.deleteLp();
        }
        catch (LpSolveException e) {
           e.printStackTrace();
        }
    }
    
    //ejecuta el solver
    public void ejecutar ()
    {
        try {
            this.solver.solve();
        } catch (LpSolveException ex) {
            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
//             TODO code application logic here
            Solver solv = new Solver ();
            solv.leer_archivo();
            solv.inicializar_solver();
                solv.funcion_obj();
         
            solv.restricciones ();
            solv.ejecutar();
            System.out.println("Value of objective function: " + solv.solver.getObjective());
            double[] var = solv.solver.getPtrVariables();
            for (int i = 0; i < var.length; i++) {
                System.out.println("Value of var[" + i + "] = " + var[i]);
            }
            //solv.example();
            
            
        } catch (LpSolveException ex) {
            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}
