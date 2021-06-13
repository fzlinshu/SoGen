package randomChoose;

import java.util.Vector;
import java.util.Random;

public class ChooseVector{
    // each ChooseVector has an array of values summing to 1, which corresponds to a Bernoulli-choice
    double[] values;
    int nr_values=0;
    
    /** Creates a new instance of ChooseVector */
    public ChooseVector(int n) {
        values=new double[n-1];
        this.nr_values=n-1;
    }
    
    public void addDoubleAt(double x, int index){
        this.values[index]=x;
    }
    
    public double getDoubleAt(int i){
        return this.values[i];
    }
    
    public void setDoubleAt(int i, double x){
        if(i>=this.nr_values) System.out.println(x);
        this.values[i]=x;
    }
    
    // if the array values is [p0,p1,...,pk], the procedure choose returns a random index i in [0,i-1]
    // such that Pr(i)=pi
    public int choose(Random random){
        int i=0; 
        double u=random.nextDouble();
        while (true) {
            if (u<=this.values[i]) return i;
            i++; 
            if(i>=this.nr_values) return this.nr_values;
        }
    }
    
    public void toScreen(){
        for (int i=0;i<nr_values;i++){
            System.out.println("value at index "+i+": "+this.getDoubleAt(i));
        }
        System.out.println("");
    }
    
}
