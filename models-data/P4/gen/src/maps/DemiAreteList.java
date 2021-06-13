package maps;

import java.util.ArrayList;

/**
 *
 * @author fusy
 */
public class DemiAreteList extends ArrayList{
    
    /**
     * Creates a new instance of DemiAreteList 
     */
    public DemiAreteList() {
    }
    
    public DemiArete getDemiArete(int i){
        return (DemiArete)(this.get(i));
    }
    
    public void toScreen(){
        for (int i=0;i<this.size();++i) {
            DemiArete demiArete=(DemiArete)this.get(i);
            System.out.print(demiArete.index+" ");
        }
    }
    
}
