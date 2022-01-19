package org.example.abd.quorum;

import org.jgroups.Address;
import org.jgroups.View;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Majority {
    private View view;
    public Majority(View view){
        this.view = view;
    }



    public int quorumSize(){
        try{
            return (Math.round(view.getMembers().size()/2)) + 1;
        } catch(Exception e){
            System.err.println(e.getStackTrace());
            return 0;
        }
        
    }

    public List<Address> pickQuorum(){
        int size = quorumSize();
        List<Address> liste = new ArrayList<>();
        for (int i = 0; i < size; i++){
           while(true){
            int intRand = new Random().nextInt(view.getMembers().size());
            if(liste.contains(view.getMembers().get(intRand))){
                continue;
            }else {
                liste.add(view.getMembers().get(intRand));
                break;
            }
           }

        }
        System.out.println(liste);   
        return liste;
    }

}
