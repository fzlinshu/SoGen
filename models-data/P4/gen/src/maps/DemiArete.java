package maps;

public class DemiArete {
    DemiArete next=null;
    DemiArete prec=null;
    DemiArete opp=null;
    
    int index=-1;
    int indexOfVertex=-1;
    
    /**
     * Creates a new instance of DemiArete 
     */
    public DemiArete() {
    }
    
    public static boolean belongToSameVertex(DemiArete e1, DemiArete e2){
          DemiArete e=e1;
          do{
              if (e==e2) return true;
              e=e.next;
          }
          while(e!=e1);
          return false;    
    }
    
}
