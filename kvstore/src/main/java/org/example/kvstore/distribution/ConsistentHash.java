package org.example.kvstore.distribution;

import org.jgroups.Address;
import org.jgroups.View;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class ConsistentHash implements Strategy{

    private TreeSet<Integer> ring;
    private Map<Integer,Address> addresses;

    public ConsistentHash(View view){
      List<Address> listeAddresses= view.getMembers();
      for(int i = 0; i<listeAddresses.size();i++){
        ring.add(listeAddresses.get(i).hashCode());
        addresses.put(listeAddresses.get(i).hashCode(), listeAddresses.get(i));

      }
    }

    @Override
    public Address lookup(Object key){
      Iterator<Integer> ite = ring.iterator();
      try{
        Integer last = 0;
        while(ite.hasNext()){
          Integer curr = ite.next();
          if(( curr > (Integer) key ) && (last < (Integer) key)){
            return addresses.get(ite.next());
          }
          last = curr;
        }
      } catch (Exception e){
        System.err.println(e.toString());
        return null;
      }
      return null;
    }

}
